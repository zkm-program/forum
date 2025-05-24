package com.zkm.forum.model.dto.comment;

import lombok.Data;

@Data
public class SaveCommentRequest {

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
     * 发表评论用户id
     */
    private Long userId;
    /**
     * 最顶层的评论id
     */
    private Long topCommentId;

}
