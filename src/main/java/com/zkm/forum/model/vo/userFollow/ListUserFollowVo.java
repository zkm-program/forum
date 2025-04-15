package com.zkm.forum.model.vo.userFollow;

import lombok.Data;

@Data
public class ListUserFollowVo {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 用户介绍
     */
    private String introduction;
}
