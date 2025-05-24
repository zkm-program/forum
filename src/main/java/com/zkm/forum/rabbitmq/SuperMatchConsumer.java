package com.zkm.forum.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.entity.MatchTags;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.rabbitmq.reuqest.MatchUserByTagsRequest;
import com.zkm.forum.service.MatchTagsService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.AlgorithmUtils;
import kotlin.Pair;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zkm.forum.constant.RabbitMqConstant.SUPER_MATCH_QUEUE;

@Component
@RabbitListener(queues = SUPER_MATCH_QUEUE, ackMode = "MANUAL")
public class SuperMatchConsumer {
    @Resource
    private UserService userService;
    @Resource
    private MatchTagsService matchTagsService;

    @SneakyThrows
    @RabbitHandler
    public void process(byte[] data, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        MatchUserByTagsRequest matchUserByTagsRequest = JSON.parseObject(new String(data), MatchUserByTagsRequest.class);
        User loginUser = matchUserByTagsRequest.getLoginUser();
        Long matchTagsId = matchUserByTagsRequest.getMatchTagsId();
        MatchTags matchTags = matchTagsService.getById(matchTagsId);
        if(matchTagsId == null){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请稍后再试");
        }
        if (matchTags == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (loginUser == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请登录");
        }

        QueryWrapper<MatchTags> matchTagsQueryWrapper = new QueryWrapper<>();
        matchTagsQueryWrapper.select("matchUserId");
        matchTagsQueryWrapper.eq("userId", loginUser.getId());
        matchTagsQueryWrapper.ne("id", matchTagsId);
        matchTagsQueryWrapper.eq("kind",1);
        List<MatchTags> matchTagsList = matchTagsService.list(matchTagsQueryWrapper);
        List<Long> matchUserIds = new ArrayList<>();
        if (ObjectUtils.isEmpty(matchTagsList)) {
            matchUserIds = matchTagsList.stream().map(MatchTags::getMatchUserId).collect(Collectors.toList());
        }
        matchUserIds.add(loginUser.getId());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        userQueryWrapper.notIn(ObjectUtils.isNotEmpty(matchUserIds), "id", matchUserIds);
        // 排除当前登录用户
        List<User> userList = userService.list(userQueryWrapper);
        if(ObjectUtils.isEmpty(userList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有相匹配的用户");
        }
        Map<Long, List<User>> map = userList.stream().collect(Collectors.groupingBy(User::getId));
        List<Pair<User, Long>> pairs = new ArrayList<>();
        for (User user : userList) {
            int i = AlgorithmUtils.minDistance(user.getTags(), loginUser.getTags());
            pairs.add(new Pair<User, Long>(user, (long) i));
        }
        // todo 如果 list 很大，排序操作可能会消耗较多时间。在这种情况下，可以考虑优化数据结构或使用并行流（list.parallelStream()）来提高性能。
//        Pair<User, Long> pair = pairs.stream().sorted((a, b) -> (int) (a.getSecond() - b.getSecond())).limit(1).toList().get(0);
        Pair<User, Long> pair = pairs.stream().sorted((a, b) -> (int) (a.getSecond() - b.getSecond())).limit(1).toList().get(0);
        User userByMatch = userService.getById(pair.getFirst().getId());
        if (userByMatch == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "没有匹配的用户");
        }
        synchronized (String.valueOf(loginUser.getId()).intern()) {
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("id", loginUser.getId());
            userUpdateWrapper.setSql(true, "superMatchCount=superMatchCount-1");
            boolean result = userService.update(userUpdateWrapper);
            // todo 如果没减成功可通过mq进行再减，添加用户匹配到的人
        }


        matchTags.setMatchUserId(userByMatch.getId());
        matchTags.setUserName(userByMatch.getUserName());
        matchTags.setTags(userByMatch.getTags());
        matchTags.setUserAvatar(userByMatch.getUserAvatar());
        matchTags.setGender(userByMatch.getGender());
        matchTags.setUserQqEmail(userByMatch.getUserQqEmail());
        matchTags.setIntroduction(userByMatch.getIntroduction());
        matchTags.setStatus(1);
        boolean result = matchTagsService.updateById(matchTags);
        if (!result) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新标签匹配表失败");
        }
        channel.basicAck(deliveryTag, false);
    }

}
