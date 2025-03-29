package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.postfavour.PostFavourRequest;
import com.zkm.forum.service.PostFavourService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/postfavour")
public class PostFavourController {
    @Resource
    PostFavourService postFavourService;
    @PostMapping("/")
    public BaseResponse<Integer> doPostFavour(@RequestBody PostFavourRequest postFavourRequest, HttpServletRequest request){
        return ResultUtils.success(postFavourService.doPostFavour(postFavourRequest,request));
    }
}
