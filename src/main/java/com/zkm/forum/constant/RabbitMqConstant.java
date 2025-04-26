package com.zkm.forum.constant;

public interface RabbitMqConstant {
    String EMAIL_QUEUE = "email:queue";
    String EMAIL_EXCHANGE = "email:exchange";
    String FOLLOW_EXCHANGE = "follow:exchange";
    String Follow_QUEUE = "follow:queue";
    String FOLLOW_USER_ROUTINGKEY = "follow:user:routingkey";
    String AI_PICTURE_EXCHANGE = "ai:picture:exchange";
    String AI_PICTURE_QUEUE = "ai:picture:queue";
    String AI_PICTURE_ROUTINGKEY = "ai:picture:routingkey";
    String ANALYSE_USER_QUEUE = "analyse:user:queue";
    String ANALYSE_USER_EXCHANGE = "analyse:user:exchange";
    String ANALYSE_USER_ROUTINGKEY = "analyse:user:routingkey";
}
