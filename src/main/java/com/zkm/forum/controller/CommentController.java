package com.zkm.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.comment.ListCommentRequest;
import com.zkm.forum.model.dto.comment.SaveCommentRequest;
import com.zkm.forum.model.vo.comment.CommentVo;
import com.zkm.forum.service.CommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @PostMapping("/save")
    public BaseResponse<Boolean> saveComment(@RequestBody SaveCommentRequest saveCommentRequest, HttpServletRequest request) {
        return ResultUtils.success(commentService.saveComment(saveCommentRequest, request));
    }

    @GetMapping("/delete/{commentId}")
    public BaseResponse<Boolean> deleteComment(@PathVariable("commentId") Long commentId, HttpServletRequest request) {
        return ResultUtils.success(commentService.deleteComment(commentId, request));
    }

    @PostMapping("/listParent")
    public BaseResponse<Page<CommentVo>> listParentComment(@RequestBody ListCommentRequest request) {
        return ResultUtils.success(commentService.listParentComment(request));
    }

    @PostMapping("/listChildren")
    public BaseResponse<Page<CommentVo>> listChildrenComment(@RequestBody ListCommentRequest request) {
        return ResultUtils.success(commentService.listChildrenComment(request));
    }
}
