package com.zhang.community.dao;

import com.zhang.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Mapper
public interface UserMapper {

    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
    int insertUser(User user);
//    int update(int id,int status);
    int updateHeader(@Param("id")int id,@Param("headerUrl")String headerUrl);
    int updateStatus(@Param("id") int id ,@Param("status") int status);
    int updatePassword(@Param("id")int id,@Param("password")String password);
}
