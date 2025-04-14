package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 问题
 *
 * @TableName question
 */
@TableName(value = "question")
@Data
public class Question implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 问题
     */
    private String question;

    /**
     * 标签列表（json 数组）
     */
    private String tags;

    /**
     * 关注数
     */
    private int concernNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否置顶 0否 1是
     */
    private Integer is_top;

    /**
     * 是否推荐 0否 1是
     */
    private Integer is_featured;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 0是没被举报，1是被举报了
     */
    private Integer isReported;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 被举报原因
     */
    private String reportResults;

    /**
     * 举报用户id
     */
    private Long reportUserId;

    /**
     * 回答个数
     */
    private int questionCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}