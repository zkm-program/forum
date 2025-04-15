package com.zkm.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.comment.ListCommentRequest;
import com.zkm.forum.model.dto.comment.SaveCommentRequest;
import com.zkm.forum.model.vo.comment.CommentVo;
import com.zkm.forum.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "评论模块")
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @ApiOperation("发布评论")
    @PostMapping("/save")
    public BaseResponse<Boolean> saveComment(@RequestBody SaveCommentRequest saveCommentRequest, HttpServletRequest request) {
        return ResultUtils.success(commentService.saveComment(saveCommentRequest, request));
    }

    @ApiOperation("删除自己发布的评论")
    @GetMapping("/delete/{commentId}")
    public BaseResponse<Boolean> deleteComment(@PathVariable("commentId") Long commentId, HttpServletRequest request) {
        return ResultUtils.success(commentService.deleteComment(commentId, request));
    }

    @ApiOperation("查看某篇文章的父评论")
    @PostMapping("/listParent")
    public BaseResponse<Page<CommentVo>> listParentComment(@RequestBody ListCommentRequest request) {
        return ResultUtils.success(commentService.listParentComment(request));
    }
    @ApiOperation("查看某篇文章的某个父评论的子评论")
    @PostMapping("/listChildren")
    public BaseResponse<Page<CommentVo>> listChildrenComment(@RequestBody ListCommentRequest request) {
        return ResultUtils.success(commentService.listChildrenComment(request));
    }
}
