package com.zm.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -4963013022444033090L;

    private String userAccount;

    private String userPassword;
}
