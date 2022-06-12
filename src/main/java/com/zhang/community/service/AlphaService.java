package com.zhang.community.service;

import com.sun.org.apache.regexp.internal.RE;
import com.zhang.community.dao.DiscussPostMapper;
import com.zhang.community.dao.UserMapper;
import com.zhang.community.entity.DiscussPost;
import com.zhang.community.entity.User;
import com.zhang.community.util.CommunityUtil;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Date;

@Service
public class AlphaService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    //事务传播机制
    //REQUIRED：支持当前事务（外部事物），如果不存在创建新事物
    //REQUIRED_NEW：创建一个新事物，并暂停当前外部事物
    //NESTED：如果存在当前外部事物，则嵌套在该事物中执行（独立的提交和回滚），如果不存在外部事务，则和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
        //先增加用户
        User user = new User();
        user.setUsername("Alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("image/nowcoder.com");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("新人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
        Integer.valueOf("abc");

        return "ok";
    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                User user = new User();
                user.setUsername("beita");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.MD5("123" + user.getSalt()));
                user.setEmail("beita@qq.com");
                user.setHeaderUrl("image/nowcoder.com");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("新人报道");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);
                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

}
