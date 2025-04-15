package com.zkm.forum.model.dto.comment;

import com.zkm.forum.common.PageRequest;
import lombok.Data;

import java.util.List;
@Data
public class ListCommentRequest extends PageRequest {

    /**
     * 发表评论用户Id
     */
    private List<Long> userId;
    /**
     * 评论帖子id
     */
    private Long postId;

    /**
     * 回复的是谁，那个谁的用户id
     */
    private Long replyUserId;
    /**
     * 父评论的评论id
     */
    private Long parentId;


}
