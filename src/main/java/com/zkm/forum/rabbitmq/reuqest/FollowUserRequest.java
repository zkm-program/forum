package com.zkm.forum.rabbitmq.reuqest;

import lombok.Data;

import java.util.List;
@Data
public class FollowUserRequest {
   private String articleName;
    /**
     * 被关注者id
     */
   private Long userId;
    /**
     * 被关注者姓名
     */
    private String userName;

}
