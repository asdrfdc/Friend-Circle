package com.zm.usercenter.model.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * 用户加入队伍请求体
 */
@Data
public class TeamJoinRequest {

    /**
     * id
     */
    private Long teamId;


    /**
     * 密码
     */
    private String password;


    private static final long serialVersionUID = 1L;
}
