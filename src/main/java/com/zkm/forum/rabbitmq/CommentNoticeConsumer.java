package com.zkm.forum.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.zkm.forum.model.dto.email.EmailRequest;
import com.zkm.forum.utils.MailUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.zkm.forum.constant.RabbitMqConstant.EMAIL_QUEUE;


//@Component
@RabbitListener(queues = EMAIL_QUEUE)
public class CommentNoticeConsumer {


    @RabbitHandler
    public void process(byte[] data) {
        EmailRequest emailDTO = JSON.parseObject(new String(data), EmailRequest.class);
        Set<String> email = new HashSet<>();
        email.add(emailDTO.getEmail());
        MailUtils.sendEmail(email, emailDTO.getSubject(), emailDTO.getCommentMap().get("content").toString());
    }

}
