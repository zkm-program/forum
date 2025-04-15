package com.zkm.forum.model.vo.user;

import lombok.Data;

import java.util.Date;

@Data
public class KnowUserVo {
    private String userName;
    /**
     * 用户角色
     */
    private String userRole;
    /**
     * 用户创建时间
     */
    private Date createTime;


    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 用户性别
     */
    private String gender;
    /**
     * 被关注数
     */
    private int followerCount;





}
