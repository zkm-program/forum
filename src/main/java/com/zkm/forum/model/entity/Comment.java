package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发表评论用户Id
     */
    private Long userId;

    /**
     * 评论帖子id
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 回复的是谁，那个谁的用户id
     */
    private Long replyUserId;

    /**
     * 父评论的评论id
     */
    private Long parentId;




    /**
     * 是否审核
     */
    private Integer isReview;

    /**
     * 评论时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 是否有子评论，0-没有，1-有
     */
    private int hasChildComment;
    /**
     * 最顶层的评论id
     */
    private Long topCommentId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}