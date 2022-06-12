package com.zhang.community.dao;

import com.zhang.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(  @Param(value ="userId") int userId,@Param("offset") int offset,@Param("limit") int limit);
    //如果需要动态sql语句，变量需要加@Param
    int selectDiscussPostRows( @Param("userId") int userId);

    List<DiscussPost> selectNewDiscussPosts(@Param(value = "userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(@Param(value = "id") int id,@Param("commentCount") int commentCount);


}
