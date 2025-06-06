package com.zkm.forum.model.vo.comment;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVo {
    /**
     * 评论主键
     */
    private Long id;
    /**
     * 发表评论用户Id
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
     * 用户性别
     */
    private String gender;
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
     * 回复的是谁，那个谁的用户名字
     */
    private String replyUserName;
    /**
     * 回复的是谁，那个谁的用户头像
     */
    private String replyUserAvatar;

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
     * 是否有子评论，0-没有，1-有（针对顶层评论）
     */
    private int hasChildComment;
    /**
     * 最顶层的评论id
     */
    private Long topCommentId;
}
