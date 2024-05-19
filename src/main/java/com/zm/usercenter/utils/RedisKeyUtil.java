package com.zm.usercenter.utils;

import com.zm.usercenter.constant.RedisKeyConstant;

import static com.zm.usercenter.constant.RedisKeyConstant.*;
import static org.springframework.session.data.redis.RedisIndexedSessionRepository.DEFAULT_NAMESPACE;

public class RedisKeyUtil {


    /**
     * 获取已登录用户的IP和sessionId对应的key
     *
     * @param userId 用户id
     * @return
     */
    public static String getUserExtraInfoKey(long userId)
    {
        return USER_EXTRA_INFO+String.valueOf(userId);
    }

    /**
     * 获取session信息对应的key
     * @param sessionId
     * @return
     */
    public static String getSessionKey(String sessionId){
        return DEFAULT_NAMESPACE + ":" + SESSION_KEY_POSTFIX + ":" +sessionId;
    }

    public static String getSessionAttrKey(String attrName){
        return SESSION_ATTRIBUTE_PREFIX + ":" + attrName;
    }
}
