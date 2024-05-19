package com.zm.usercenter.service.impl;

import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zm.usercenter.common.ErrorCode;
import com.zm.usercenter.exception.BusinessException;
import com.zm.usercenter.manager.SessionManager;
import com.zm.usercenter.model.domain.User;
import com.zm.usercenter.service.UserService;
import com.zm.usercenter.mapper.UserMapper;
import com.zm.usercenter.utils.NetUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import com.zm.usercenter.utils.AlgorithmUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

import static com.zm.usercenter.constant.RedisKeyConstant.USER_LOGIN_STATE;
import static com.zm.usercenter.constant.UserConstant.ADMIN_ROLE;

/**
* @author 29524
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-05-08 10:40:45
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "zm";

    @Resource
    private UserMapper userMapper;

    @Resource
    private SessionManager sessionManager;


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");

        }
        if(userPassword.length()<8|| checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(planetCode.length()<5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号过长");
        }
        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        boolean matches = userAccount.matches(validPattern);
        if(matches){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        //密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        long count= this.count(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能重复");
        }
        //编号不能重复
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        count= this.count(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号不能重复");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        boolean save = this.save(user);
        if(!save){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"数据库操作失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能小于四个字符");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        boolean matches = userAccount.matches(validPattern);
        if(matches){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        queryWrapper.eq("password",encryptPassword);
        User user=this.getOne(queryWrapper);
        //用户不存在
        if(user == null){
            log.info("user login faied, userAccount cannot match userPassword");
        }
        //3.用户脱敏
        User safetyUser=getSafetyUser(user);
        //4.记录用户的登录态
        //request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        String message=sessionManager.login(user,request);
        log.info(message);
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        if(originUser==null){
            return null;
        }
        User safetyUser=new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setStatus(originUser.getStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }


    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        //移除登录态
        sessionManager.logout(request);
    }

    /**
     * 根据标签搜索用户（内存过滤）
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson=new Gson();
        //2.在内存中判断是否包含要求的标签
        //把stream（）改成parallelStream就可以变成并行流
        // 不过parallelStream有缺点，使用公共线程池，有很多不确定的风险，比如线程池慢了会阻塞
        return userList.stream().filter(user-> {
            String tagStr=user.getTags();
//            if(StringUtils.isBlank(tagStr)){
//                return false;
//            }
            Set<String> tempTagNameSet=gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
            //判空
            tempTagNameSet= Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName : tagNameList){
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }


    @Override
    public int updateUser(User user, User loginUser) {
        long userId=user.getId();
        if(userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 补充校验，如果用户没有传任何要更新的值，直接报错，不用执行update语句
        //如果既不是管理员也不是自己，则不能修改
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //检查一下有没有这个用户
        User oldUser=userMapper.selectById(userId);
        if(oldUser==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //修改
        return userMapper.updateById(user);
    }

//    @Override
//    public User getLoginUser(HttpServletRequest request) {
//        if(request==null){
//            return null;
//        };
//        Object userObj=request.getSession().getAttribute(USER_LOGIN_STATE);
//        if(userObj==null){
//            throw new BusinessException(ErrorCode.NO_AUTH);
//        }
//        return (User) userObj;
//    }

    @Override
    public User getLoginUser(HttpServletRequest request){
        //先判断是否已登录
        Object userObj=request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser=(User) userObj;
        if(currentUser == null || currentUser.getId() == null){
            throw new RuntimeException("未登录");
        }
        //校验是否已在其他客户端登录
        String ipAddress= NetUtils.getIpAddress(request);
        String oldSessionId = sessionManager.checkOtherLogin(currentUser.getId(),ipAddress,request);
        if(StringUtils.isNotBlank(oldSessionId)){
            request.getSession().removeAttribute(USER_LOGIN_STATE);
            throw new RuntimeException("已在其他设备登录，请重新登录");
        }
        //从数据库查询（追求性能的话可以注释，直接走缓存
        long userId=currentUser.getId();
        currentUser=this.getById(userId);
        if(currentUser==null){
            throw new RuntimeException("当前未登录");
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User)userObj;
        return user!=null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser!=null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        //减少查询的列优化性能
        queryWrapper.select("id", "tags");

        //排除标签为空
        queryWrapper.isNotNull("tags");

        //用户列表
        List<User> userList = this.list(queryWrapper);

        //拿出当前登录用户的标签用以匹配和推荐
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());

        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        //SortedMap<Integer,Long> indexDistanceMap=new TreeMap<>();

        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            //获取第i个用户的标签
            User user = userList.get(i);
            String userTags = user.getTags();

            // 无标签或者为当前用户自己跳过
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }

            //Strng转list
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());

            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }

        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                //取出需要的用户数
                .limit(num)
                .collect(Collectors.toList());

        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());

        //前面由于查询所有数据，为了优化性能只查两个字段，所有下面要把完整用户信息查出来
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //in查询没有顺序，比如前面排好序（1,3，2），查出来可能是（1，2，3）
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        //这段逻辑很重要，通过map映射把查出来的数据排好序，解决sql查询打乱顺序的问题
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));

        List<User> finalUserList = new ArrayList<>();
        //遍历原本顺序的id列表，从乱序的map当中通过key拿出user逐一放到finalUserList排好序
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }


    /**
     * 根据标签搜索用户(SQL查询版）
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUserByTagsBySQL(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //拼接and查询（用数据库查询）
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper.like("tags",tagName);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




