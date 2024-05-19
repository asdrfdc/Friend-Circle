package com.zm.usercenter.model.info;

import com.zm.usercenter.model.domain.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginRedisInfo {

    private User user;

    private String ip;
}
