package com.zm.usercenter.mapper;

import com.zm.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 29524
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-05-08 10:40:45
* @Entity com.zm.usercenter.model.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




