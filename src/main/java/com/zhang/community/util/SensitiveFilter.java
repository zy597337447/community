package com.zhang.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换字符
    private static final String REPLACEMENT = "***";

    //根节点
    private TreeNode rootNode = new TreeNode();

    /**
     *     标记该方法为初始化方法，容器实例化这个bean之后，在调用这个方法的构造器之后这个方法就会自动被调用
     *     服务启动的时候bean被初始化，改方法被调用
     */
    @PostConstruct
    public void init(){
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ) {
            String keyword;
            while((keyword = reader.readLine()) != null){
                //添加到前缀树
                this.addKeyWord(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败"+ e.getMessage());
        }

    }


    //初始化前缀树
    // 将一个敏感词添加到前缀树中
    public void addKeyWord(String keyword){
        TreeNode tempNode = rootNode;
        for(int i = 0;i < keyword.length(); i++){
            char c = keyword.charAt(i);
            TreeNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                subNode = new TreeNode();
                tempNode.addSubNode(c,subNode);
            }

            //指针指向子节点，进入下一个循环
            tempNode = subNode;

            if (i == keyword.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }


    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     *
     * @return  过滤后的文本
     */
    public String filter(String text){
        //判空
        if (StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TreeNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder res = new StringBuilder();

        while(position<text.length()){
            char c = text.charAt(position);
            //跳过符号
            if (isSymbol(c)){
                //若指针1处于根节点
                if (tempNode == rootNode){
                    res.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            //以begin开头的字符不是敏感词
            if (tempNode == null){
                res.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }else if (tempNode.isIdKeyWordEnd()){
                res.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            }else {
                position++;
            }
        }
        res.append(text.substring(begin));
        return res.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c > 0x9FFF);
    }
















    //前缀树
    private class  TreeNode{
        //关键词结束的标识
        private boolean idKeyWordEnd = false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character,TreeNode> subNodes = new HashMap<>();

        public boolean isIdKeyWordEnd() {
            return idKeyWordEnd;
        }

        public void setKeyWordEnd(boolean idKeyWordEnd) {
            this.idKeyWordEnd = idKeyWordEnd;
        }

        public void addSubNode(Character c ,TreeNode node){
            subNodes.put(c,node);
        }

        public TreeNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
