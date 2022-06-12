package com.zhang.community.controller;

import com.zhang.community.entity.Message;
import com.zhang.community.entity.Page;
import com.zhang.community.entity.User;
import com.zhang.community.service.MessageService;
import com.zhang.community.service.UserService;
import com.zhang.community.util.CommunityUtil;
import com.zhang.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.dc.pr.PRError;
import sun.misc.Contended;

import javax.jws.WebParam;
import java.net.PortUnreachableException;
import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;



    //私信列表
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){

        User user = hostHolder.getUser();

        page.setLimit(10);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());

        //封装会话
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList!=null){
            for (Message conversation:conversationList){
                Map<String ,Object> map = new HashMap<>();
                map.put("conversation",conversation);
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
                map.put("letterCount",messageService.findLetterCount(conversation.getConversationId()));
                //获得对话用户信息
                int targetId = user.getId() == conversation.getFromId()?conversation.getToId():conversation.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //总未读消息数
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page,Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList!=null){
            for (Message message:letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("letterFromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }

            model.addAttribute("letters",letters);

            //获取私信目标
            model.addAttribute("target",getLetterTarget(conversationId));
        }

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }



        return "/site/letter-detail";

    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> Ids = new ArrayList<>();
        if (letterList != null){
            for (Message letter:letterList){
                if (hostHolder.getUser().getId() == letter.getToId()  && letter.getStatus()==0){
                    Ids.add(letter.getId());
                }
            }
        }
        return Ids;
    }

    private User getLetterTarget(String conversationId){
        String [] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id1);
        }
    }


    @RequestMapping(path = "letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){

        User target = userService.findUserByName(toName);
        if (target == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);

    }
}
