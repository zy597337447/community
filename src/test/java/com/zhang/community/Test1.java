package com.zhang.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class Test1 {
    int a = 5;
    public boolean isTrue(int number){
        return !(number >6 ) && number>0;
    }
    @Test
    public void testTrue(){
        System.out.println(isTrue(a));
    }
}
