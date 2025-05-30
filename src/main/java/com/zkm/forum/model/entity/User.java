package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户头像缩略图url
     */
    private String thumbnailAvatarUrl;
    /**
     * 是否被举报
     */
    private int isReported;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 健身模块主键
     */
    private Long fitnessId;
    /**
     * 用户标签,json数组里面都是字符串形式
     */
    private String tags;

    private String userQqEmail;
    /**
     * 用户密码
     */
    private String userPassword;


    /**
     * 用户匹配次数
     */
    private int matchCount;

    private int superMatchCount;


    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 用户创建时间
     */
    private Date createTime;

    /**
     * 用户更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户性别
     */
    private String gender;

    private String reportResults;
    private Long reportUserId;

    /**
     * 经度
     */
    private double longitude;

    /**
     * 维度
     */
    private double dimension;
    /**
     * 被关注数
     */
    private int followerCount;
    /**
     * 用户介绍
     */
    private String introduction;
    /**
     * ai分析用户次数
     */
    private int analyseUserCount;
    /**
     * ai分析图片次数
     */
    private int analysePictureCount;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}