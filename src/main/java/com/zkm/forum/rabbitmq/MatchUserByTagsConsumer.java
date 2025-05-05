package com.zkm.forum.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.user.MatchUserVo;
import com.zkm.forum.rabbitmq.reuqest.MatchUserByTagsRequest;
import com.zkm.forum.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.zkm.forum.constant.RabbitMqConstant.EMAIL_QUEUE;
import static com.zkm.forum.constant.RabbitMqConstant.Match_USER_BYTAGS_QUEUE;

@Component
@RabbitListener(queues = Match_USER_BYTAGS_QUEUE)
public class MatchUserByTagsConsumer {
    @Resource
    private UserService userService;

    @RabbitHandler
    public void process(byte[] data) {
        MatchUserByTagsRequest matchUserByTagsRequest = JSON.parseObject(new String(data), MatchUserByTagsRequest.class);
        List<String> tags = matchUserByTagsRequest.getTags();
        User loginUser = matchUserByTagsRequest.getLoginUser();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        userQueryWrapper.last("LIMIT 500");
        List<User> userList = userService.list(userQueryWrapper);
        List<MatchUserVo> matchUserVos = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            // todo  可以使用set类型进行存储，这样可以过滤重复的标签，但是要注意，使用不同的序列化工具可能会报错
            List<String> tagList = JSONUtil.toList(userList.get(i).getTags(), String.class);
            for (String tag : tags) {
                if (!tagList.contains(tag)) {
                    break;
                } else {
                    if (matchUserVos.size() <= 1) {
                        matchUserVos.add(getMatchUserVo(userList.get(i)));
                    } else {
                        // todo 可以换成异步避免等太久(先返回再减？)，弹到其他页面先减再返回，避免并发错误
                        synchronized (String.valueOf(loginUser.getId()).intern()) {
                            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                            userUpdateWrapper.eq("id", loginUser.getId());
                            userUpdateWrapper.setSql(true, "matchCount=matchCount-1");
                            boolean result = userService.update(userUpdateWrapper);
                            // todo 如果没减成功可通过mq进行再减，添加用户匹配到的人
                        }
                        List<Long> matchUserIdList = matchUserVos.stream().map(MatchUserVo::getId).toList();
                        List<MatchUserVo> matchUserVoList = userService.listByIds(matchUserIdList).stream().map(this::getMatchUserVo).toList();
                        for (MatchUserVo matchUserVo : matchUserVoList) {
                            matchUserVo.setTags(tags);
                        }
                        return matchUserVoList;
                    }

                }
            }

        }
    }

    private MatchUserVo getMatchUserVo(User user) {
        MatchUserVo matchUserVo = new MatchUserVo();
        BeanUtils.copyProperties(user, matchUserVo);
        matchUserVo.setTags(JSONUtil.toList(user.getTags(), String.class));
        return matchUserVo;
    }

}
