package com.zm.usercenter.controller;

import com.zm.usercenter.common.BaseResponse;
import com.zm.usercenter.common.ResultUtils;
import com.zm.usercenter.manager.SessionManager;
import com.zm.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/mock")
@Profile({"local","dev"})
@Slf4j
public class UserMockController {

    @Resource
    private SessionManager sessionManager;

    @PostMapping("/login")
    public BaseResponse<User> userLoginMock(HttpServletRequest request){
        User user=new User();
        user.setId(1L);
        sessionManager.login(user,request);
        log.info("user login succeed, id={}",user.getId());;

        return ResultUtils.success(user);
    }
}
