package com.zkm.forum.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.entity.UserFollow;
import com.zkm.forum.rabbitmq.reuqest.FollowUserRequest;
import com.zkm.forum.service.UserFollowService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.MailUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.*;

import static com.zkm.forum.constant.RabbitMqConstant.Follow_QUEUE;

@Component
@RabbitListener(queues = Follow_QUEUE)
public class FollowerUserNoticeConsumer {
    @Resource
    private UserFollowService userFollowService;
    @Resource
    private UserService userService;

    @RabbitHandler
    public void process(byte[] data) {
        FollowUserRequest followUserRequest = JSON.parseObject(new String(data), FollowUserRequest.class);
        String articleName = followUserRequest.getArticleName();
        Long userId = followUserRequest.getUserId();
        String userName = followUserRequest.getUserName();
        QueryWrapper<UserFollow> userFollowQueryWrapper = new QueryWrapper<>();
        userFollowQueryWrapper.select("followerId");
        userFollowQueryWrapper.eq("userId",userId);
        List<UserFollow> userFollowList = userFollowService.list(userFollowQueryWrapper);
        List<Long> followerIdList = userFollowList.stream().map(UserFollow::getFollowerId).toList();
        List<User> followerList = userService.listByIds(followerIdList);
        List<String> followerQqEmaillist = followerList.stream().map(User::getUserQqEmail).toList();
        Set<String> followerQqEmailset = new HashSet<>(followerQqEmaillist);
        MailUtils.sendEmail(followerQqEmailset,"用户关注","您关注的用户"+"【"+userName+"】"+"发表了文章题目："+articleName);
    }
}
