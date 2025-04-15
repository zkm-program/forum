package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.postthumb.PostThumbRequest;
import com.zkm.forum.service.PostThumbService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
@Api(tags="帖子点赞模块")
@RestController
@RequestMapping("/PostThumb")
public class PostThumbController {
    @Resource
    PostThumbService postThumbService;
    @ApiOperation("点赞帖子")
    @PostMapping("/")
    public BaseResponse<Integer> doPostThumb(PostThumbRequest postThumbRequest, HttpServletRequest request){
        return ResultUtils.success(postThumbService.doPostThumb(postThumbRequest,request));

    }
}
