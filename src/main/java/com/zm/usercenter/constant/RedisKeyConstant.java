package com.zm.usercenter.constant;

public interface RedisKeyConstant {

    //IP和sessionId的key前缀
    String USER_EXTRA_INFO = "user:extra";
    //Spring Session 中Session信息的后缀（sessionId前面）
    String SESSION_KEY_POSTFIX = "sessions";
    //session中保存的属性的前缀
    String SESSION_ATTRIBUTE_PREFIX = "sessionAttr";

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    String IP="ip";

    String SESSION_ID = "sessionId";
}
