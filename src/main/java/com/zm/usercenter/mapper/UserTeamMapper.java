package com.zm.usercenter.mapper;

import com.zm.usercenter.model.domain.UserTeam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author 29524
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2024-05-11 23:12:35
* @Entity com.zm.usercenter.model.domain.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




