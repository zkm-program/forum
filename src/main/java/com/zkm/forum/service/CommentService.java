package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.comment.ListCommentRequest;
import com.zkm.forum.model.dto.comment.SaveCommentRequest;
import com.zkm.forum.model.entity.Comment;
import com.zkm.forum.model.vo.comment.CommentVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author 张凯铭
* @description 针对表【comment(评论表)】的数据库操作Service
* @createDate 2025-04-12 17:31:09
*/
public interface CommentService extends IService<Comment> {
    Boolean saveComment(SaveCommentRequest saveCommentRequest, HttpServletRequest request);
    Boolean deleteComment(Long commentId,HttpServletRequest request);
    Page<CommentVo> listParentComment(ListCommentRequest request);
    Page<CommentVo> listChildrenComment(ListCommentRequest request);

}
