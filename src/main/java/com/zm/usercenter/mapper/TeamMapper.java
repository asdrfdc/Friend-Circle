package com.zm.usercenter.mapper;

import com.zm.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author 29524
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2024-05-11 23:11:45
* @Entity com.zm.usercenter.model.domain.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




