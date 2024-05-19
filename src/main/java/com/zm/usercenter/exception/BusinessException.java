package com.zm.usercenter.exception;

import com.zm.usercenter.common.ErrorCode;

/**
 * 自定义异常类
 */
public class BusinessException extends RuntimeException{

    private final int code;

    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode code){
        super(code.getMessage());
        this.code = code.getCode();
        this.description = code.getDescription();
    }

    public BusinessException(ErrorCode code, String description){
        super(code.getMessage());
        this.code = code.getCode();
        this.description = description;
    }


    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
