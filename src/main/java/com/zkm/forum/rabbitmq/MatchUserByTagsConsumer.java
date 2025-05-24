package com.zkm.forum.rabbitmq;

import cn.hutool.json.JSONUtil;
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
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zkm.forum.constant.RabbitMqConstant.EMAIL_QUEUE;
import static com.zkm.forum.constant.RabbitMqConstant.Match_USER_BYTAGS_QUEUE;

@Component
@RabbitListener(queues = Match_USER_BYTAGS_QUEUE, ackMode = "MANUAL")
public class MatchUserByTagsConsumer {
    @Resource
    private UserService userService;
    @Resource
    private MatchTagsService matchTagsService;

    @SneakyThrows
    @RabbitHandler
    public void process(byte[] data, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        MatchUserByTagsRequest matchUserByTagsRequest = JSON.parseObject(new String(data), MatchUserByTagsRequest.class);
        List<String> tags = matchUserByTagsRequest.getTags();
        User loginUser = matchUserByTagsRequest.getLoginUser();
        Long matchTagsId = matchUserByTagsRequest.getMatchTagsId();
        if(tags.size() == 0){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择标签");

        }
        if(loginUser == null){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请登录");
        }
        if(matchTagsId == null){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请稍后再试");
        }
        MatchTags matchTags = matchTagsService.getById(matchTagsId);
        if(matchTags == null){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"请稍后再试");
        }
        QueryWrapper<MatchTags> matchTagsQueryWrapper = new QueryWrapper<>();
        matchTagsQueryWrapper.select("matchUserId");
        matchTagsQueryWrapper.eq("userId", loginUser.getId());
        matchTagsQueryWrapper.ne("id", matchTagsId);
        matchTagsQueryWrapper.eq("kind",0);
        List<MatchTags> matchTagsList = matchTagsService.list(matchTagsQueryWrapper);
        List<Long> matchUserIds = new ArrayList<>();
        if(ObjectUtils.isNotEmpty(matchTagsList)){
              matchUserIds = matchTagsList.stream().map(MatchTags::getMatchUserId).collect(Collectors.toList());;
        }
        matchUserIds.add(loginUser.getId());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        userQueryWrapper.last("LIMIT 500");
        userQueryWrapper.notIn(ObjectUtils.isNotEmpty(matchUserIds),"id", matchUserIds);
        List<User> userList = userService.list(userQueryWrapper);
        if(ObjectUtils.isEmpty(userList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有相匹配的用户");
        }
        List<MatchTags> matchTagList = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            if(!matchTagList.isEmpty()){
                break;
            }
            // todo  可以使用set类型进行存储，这样可以过滤重复的标签，但是要注意，使用不同的序列化工具可能会报错
            List<String> tagList = JSONUtil.toList(userList.get(i).getTags(), String.class);
//            for (String tag : tags) {
                if (!tagList.containsAll(tags)) {
                    break;
                } else {
                    // todo 修改成普通匹配只返回一个了，后续修改成不使用列表，不过前端也要改
                    if (matchTagList.isEmpty()) {
                        matchTagList.add(getMatchTags(userList.get(i)));
                    } else {
                        // todo 可以换成异步避免等太久(先返回再减？)，弹到其他页面先减再返回，避免并发错误
                        synchronized (String.valueOf(loginUser.getId()).intern()) {
                            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                            userUpdateWrapper.eq("id", loginUser.getId());
                            userUpdateWrapper.setSql(true, "matchCount=matchCount-1");
                            boolean result = userService.update(userUpdateWrapper);
                            // todo 如果没减成功可通过mq进行再减，添加用户匹配到的人
                        }
                        List<Long> matchUserIdList = matchTagList.stream().map(MatchTags::getMatchUserId).toList();
                        List<MatchTags> matchUserVoList = userService.listByIds(matchUserIdList).stream().map(this::getMatchTags).toList();
                        for (MatchTags matchTag : matchUserVoList) {
                            matchTag.setTags(JSONUtil.toJsonStr(tags));
                            matchTag.setId(matchTagsId);
                        }
                        boolean result = matchTagsService.updateBatchById(matchUserVoList);
                        if(!result){
                            channel.basicNack(deliveryTag, false, false);
                            throw new BusinessException(ErrorCode.OPERATION_ERROR,"更新标签匹配表失败");
                        }
                    }


                }

//            }
        }
        channel.basicAck(deliveryTag, false);
        if(matchTagList.isEmpty()){
            matchTagsService.removeById(matchTagsId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有相匹配的用户");
        }
    }

    private MatchTags getMatchTags(User user) {
        MatchTags matchTags = new MatchTags();
        matchTags.setUserName(user.getUserName());
        matchTags.setUserAvatar(user.getUserAvatar());
        matchTags.setGender(user.getGender());
        matchTags.setUserQqEmail(user.getUserQqEmail());
        matchTags.setIntroduction(user.getIntroduction());
//        BeanUtils.copyProperties(user, matchTags);
        matchTags.setMatchUserId(user.getId());
        matchTags.setStatus(1);
        matchTags.setTags(user.getTags());
        return matchTags;
    }

}
