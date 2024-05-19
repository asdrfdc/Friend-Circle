package com.zm.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zm.usercenter.common.BaseResponse;
import com.zm.usercenter.common.ErrorCode;
import com.zm.usercenter.common.ResultUtils;
import com.zm.usercenter.exception.BusinessException;
import com.zm.usercenter.manager.SessionManager;
import com.zm.usercenter.model.domain.User;
import com.zm.usercenter.model.request.UserLoginRequest;
import com.zm.usercenter.model.request.UserRegisterRequest;
import com.zm.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zm.usercenter.constant.RedisKeyConstant.USER_LOGIN_STATE;
import static com.zm.usercenter.constant.UserConstant.ADMIN_ROLE;


/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000"})
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private SessionManager sessionManager;


    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result=userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user= userService.userLogin(userAccount, userPassword,request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<String> userLogout(HttpServletRequest request){
        if(request==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.userLogout(request);
        return ResultUtils.success("退出成功");
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        User currentUser=userService.getLoginUser(request);
        return ResultUtils.success(currentUser);
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User currentUser=(User)userObj;
//        if(currentUser==null){
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }
//        long userId=currentUser.getId();
//        //todo 校验用户是否合法
//        User user=userService.getById(userId);
//        User safetyUser=userService.getSafetyUser(user);
//        return ResultUtils.success(safetyUser);
    }


    /**
     * 搜索用户
     * @param username
     * @param request
     * @return
     */
    @PostMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request){
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list=userList.stream().map(user ->
            userService.getSafetyUser(user)
        ).collect(Collectors.toList());
        return ResultUtils.success(list);
    }


    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.searchUserByTags(tagNameList));
    }

    /**
     * 推荐用户
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    //todo 推荐多个
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey=String.format("user-center:user:recommend:%s",loginUser.getId());
        ValueOperations<String,Object> valueOperations=redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage=(Page<User>) valueOperations.get(redisKey);
        if(userPage!=null){
            return ResultUtils.success(userPage);
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage=userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        //写缓存
        try {
            valueOperations.set(redisKey,userPage,60L, TimeUnit.MINUTES);
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 更新用户
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b=userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        if(num <=0 || num >20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user=userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num,user));
    }




}
