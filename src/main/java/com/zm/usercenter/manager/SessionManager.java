package com.zm.usercenter.manager;

import cn.hutool.core.net.NetUtil;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.zm.usercenter.constant.RedisKeyConstant;
import com.zm.usercenter.model.domain.User;
import com.zm.usercenter.model.info.UserLoginRedisInfo;
import com.zm.usercenter.service.UserService;

import com.zm.usercenter.utils.NetUtils;
import com.zm.usercenter.utils.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.concurrent.TimeUnit;

import static com.zm.usercenter.constant.RedisKeyConstant.*;

@Component
@Slf4j
@ConfigurationProperties
public class SessionManager {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIndexedSessionRepository sessionRepository;

    @Value("${spring.session.timeout}")
    private long sessionTimeout;

    @Lazy
    @Resource
    private UserService userService;


    /**
     * 登录
     *
     * @param user     用户
     * @param request  请求信息
     * @return
     */
    public String login(User user,HttpServletRequest request){
        String message="登录成功";
        String ipAddress= NetUtils.getIpAddress(request);
        String oldSessionId=this.checkOtherLogin(user.getId(), ipAddress,request);
        //不为空，说明已在其他端登录
        if(StrUtil.isNotBlank(oldSessionId)){
            //删除 oldSessionId的登录态
            this.removeOtherSessionLoginAttribute(oldSessionId, user.getId());
            message+=",已移除其他设备的登录";
        }
        UserLoginRedisInfo userLoginRedisInfo=UserLoginRedisInfo.builder()
                .user(user)
                .ip(ipAddress)
                .build();
        this.setLoginAttribute(request,USER_LOGIN_STATE,userLoginRedisInfo);

        return message;
    }


    /**
     * 检查是否已在其他端登录
     *
     * @param userId    用户id
     * @param currentIp
     * @return          如果已在其他端登录，则返回其他端的sessionId，否则返回null
     */
    public String checkOtherLogin(Long userId,String currentIp,HttpServletRequest request){
        //校验 sessionId
        Object oldSessionIdObj=
                stringRedisTemplate.opsForHash().get(RedisKeyUtil.getUserExtraInfoKey(userId),SESSION_ID);
        String oldSessionId=null;
        if(oldSessionIdObj != null){
            oldSessionId = (String) oldSessionIdObj;
        }

        //校验ip
        Object oldIpObj=
                stringRedisTemplate.opsForHash().get(RedisKeyUtil.getUserExtraInfoKey(userId),IP);
        String oldIp=null;
        if(oldIpObj != null){
            oldIp=(String) oldIpObj;
        }

        //判断sessionId结果
        //  为空或相等 返回null
        //  不等，判断ip，如果
        //      为空或相等，返回null
        //      不等，返回oldSessionId
        if(StrUtil.isBlank(oldSessionId) || oldSessionId.equals(request.getSession().getId())){
            return null;
        }else {
            if(StrUtil.isBlank(oldIp) || oldIp.equals(currentIp)){
                return null;
            }
            return oldSessionId;
        }
    }


    /**
     * 删除其他session的登录属性
     *
     * @param sessionId
     * @param userId
     */
    public void removeOtherSessionLoginAttribute(String sessionId,Long userId){
        String sessionKey= RedisKeyUtil.getSessionKey(sessionId);
        String sessionAttrKey = RedisKeyUtil.getSessionAttrKey(USER_LOGIN_STATE);
        //删除用户的额外信息
        Boolean userExtraInfoDelete=
                stringRedisTemplate.delete(RedisKeyUtil.getUserExtraInfoKey(userId));
        Long delete=sessionRepository.getSessionRedisOperations().opsForHash().delete(sessionKey,sessionAttrKey);

        log.info("oldSessionId : {}, user extra info delete result: {}, user login state delete result: {}",sessionId,userExtraInfoDelete,delete);
    }




    /**
     * 设置登录属性
     *
     * @param request            请求信息
     * @param loginKey           登录键
     * @param userLoginRedisInfo 用户信息
     */
    public void setLoginAttribute(HttpServletRequest request,String loginKey,UserLoginRedisInfo userLoginRedisInfo){
        setAttribute(request,loginKey,userLoginRedisInfo,true);
    }


    /**
     * 设置属性
     *
     * @param request 请求信息
     * @param key     键
     * @param value   值
     * @param login   登录
     */
    public void setAttribute(HttpServletRequest request,String key,Object value,boolean login){
        HttpSession session=request.getSession();
        if(login){
            UserLoginRedisInfo userLoginRedisInfo=(UserLoginRedisInfo) value;
            User user = userLoginRedisInfo.getUser();
            //存储登录态
            session.setAttribute(key,user);

            //存储sessionId和ip信息
            String sessionId=session.getId();
            String userExtraInfoKey= RedisKeyUtil.getUserExtraInfoKey(user.getId());
            stringRedisTemplate.opsForHash().put(userExtraInfoKey, SESSION_ID,sessionId);
            stringRedisTemplate.opsForHash().put(userExtraInfoKey,IP,userLoginRedisInfo.getIp());
            stringRedisTemplate.expire(userExtraInfoKey,sessionTimeout, TimeUnit.SECONDS);
         }else {
            session.setAttribute(key,value);
        }
    }


    /**
     * 退出登录
     *
     * @param request  请求信息
     */
    public void logout(HttpServletRequest request){
        User loginUser=userService.getLoginUser(request);
        removeAttribute(request,USER_LOGIN_STATE);
        stringRedisTemplate.delete(RedisKeyUtil.getUserExtraInfoKey(loginUser.getId()));
    }


    /**
     * 删除属性
     *
     * @param request  请求信息
     * @param key      键
     */
    public void removeAttribute(HttpServletRequest request,String key){
        HttpSession session=request.getSession();
        session.removeAttribute(key);
    }
}
