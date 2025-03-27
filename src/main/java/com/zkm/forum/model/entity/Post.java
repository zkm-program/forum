package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子
 * @TableName post
 */
@TableName(value ="post")
@Data
public class Post implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private String tags;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;
    private Integer viewCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0-公开，1-私有，2-草稿
     */
    private Integer status;

    /**
     * 文章摘要，如果该字段为空，默认取文章的前500个字符作为摘要
     */
    private String article_abstract;

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
     * 文章类型 1原创 2转载 3翻译
     */
    private Integer type;

    /**
     * 访问密码
     */
    private String password;

    /**
     * 原文链接
     */
    private String original_url;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}