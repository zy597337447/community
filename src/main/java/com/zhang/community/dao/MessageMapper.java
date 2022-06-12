package com.zhang.community.dao;

import com.zhang.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，针对每个会话只返回最新的一条私信
    List<Message> selectConversations(@Param(value = "userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(@Param(value = "conversationId") String conversationId,@Param("offset") int offset,@Param("limit") int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    int selectLetterUnreadCount(@Param(value = "userId") int userId,@Param("conversationId") String conversationId);

    //新增消息
    int insertMessage(Message message);

    //更改消息状态
    int updateMessageStatus( @Param( "ids") List<Integer> ids, @Param("status") int status);
}
