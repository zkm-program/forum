package com.zkm.forum.constant;

public interface RabbitMqConstant {
    String EMAIL_QUEUE = "email:queue";

    String EMAIL_EXCHANGE = "email:exchange";
    String FOLLOW_EXCHANGE = "follow:exchange";
    String Follow_QUEUE = "follow:queue";
    String FOLLOW_USER_ROUTINGKEY="follow:user:routingkey";
}
