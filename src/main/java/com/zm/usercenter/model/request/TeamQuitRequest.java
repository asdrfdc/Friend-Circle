package com.zm.usercenter.model.request;

import lombok.Data;

/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest {

    /**
     * id
     */
    private Long teamId;


    private static final long serialVersionUID = 1L;
}
