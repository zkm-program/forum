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
     * 是否被举报
     */
    private int isReported;
    /**
     * 用户姓名
     */
    private String userName;

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
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}