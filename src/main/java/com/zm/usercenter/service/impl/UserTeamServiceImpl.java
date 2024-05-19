package com.zm.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zm.usercenter.model.domain.UserTeam;
import com.zm.usercenter.service.UserTeamService;
import com.zm.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 29524
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-05-11 23:12:35
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




