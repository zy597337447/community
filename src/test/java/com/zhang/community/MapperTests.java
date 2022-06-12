package com.zhang.community;

import com.zhang.community.dao.*;
import com.zhang.community.entity.DiscussPost;
import com.zhang.community.entity.LoginTicket;
import com.zhang.community.entity.Message;
import com.zhang.community.entity.User;
import org.apache.ibatis.annotations.Param;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);
        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }
    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("12345666");
        user.setSalt("abc");
        user.setEmail("sou007@qq.com");
        user.setHeaderUrl("http://www.baidu.com");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }
    @Test
    public void testUpdate(){
        int rows = userMapper.updateStatus(150,1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150,"http://www.google.com");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0,0,10);
        for (DiscussPost post : discussPosts){
            System.out.println(post);
        }
        int discussPostsCount = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(discussPostsCount);

    }

    @Test
    public void testInsertTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("111");
        loginTicket.setId(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 *60 *10));
        loginTicket.setStatus(0);
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("111");
        loginTicketMapper.updateStatus(loginTicket.getTicket(),1);
    }
    @Test
    public void testSelectConversations(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message:messages){
            System.out.println(message);
        }
    }
    @Test
    public void testSelectConversationCount(){
        int sum = messageMapper.selectConversationCount(111);
        System.out.println(sum);
    }

    @Test
    public void testSelectLetters(){
        List<Message> letters = messageMapper.selectLetters("111_160", 0, 20);
        for (Message letter:letters){
            System.out.println(letter);
        }
    }
    @Test
    public void testSelectLetterCount(){
        System.out.println(messageMapper.selectLetterCount("111_112"));
    }
    @Test
    public void TestSelectLetterUnreadCount(){
        System.out.println(messageMapper.selectLetterUnreadCount(111,"111_112"));
    }

    public void testMessageStatus(){
        String conversationId = "111_160";
        int offset = 0;
        int limit = 5;
        List<Message> messages = messageMapper.selectLetters(conversationId, offset, limit);
    }

}
