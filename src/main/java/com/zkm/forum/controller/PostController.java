package com.zkm.forum.controller;

import com.zkm.forum.annotation.AuthCheck;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.post.GetPrivateRecommendRequest;
import com.zkm.forum.model.dto.post.PostSearchRequest;
import com.zkm.forum.model.dto.post.UpdatePostDeleteForMy;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.service.PostService;
import org.apache.commons.lang3.ObjectUtils;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.zkm.forum.constant.RedisConstant.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    PostService postService;
//    @Resource
//    RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> addPost(@RequestBody AddPostRequest addPostRequest, HttpServletRequest httpServletRequest) {
        if (ObjectUtils.isEmpty(addPostRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "信息填写完整再发布");
        }
        return ResultUtils.success(postService.addPost(addPostRequest, httpServletRequest));
    }

    @PostMapping("/updatedelete/mypost")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updatePostDeletForMy(UpdatePostDeleteForMy updatePostDeleteForMy, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(postService.updatePostDeletForMy(updatePostDeleteForMy, httpServletRequest));
    }

    @PostMapping("/searchPost")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<PostSearchVo>> searchPost(PostSearchRequest postSearchRequest) {
        return ResultUtils.success(postService.searchPost(postSearchRequest));
    }

    @GetMapping("/{postId}")
    public BaseResponse<PostVo> getPostById(@PathVariable("postId") Long postId, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(postService.getPostById(postId, httpServletRequest));
    }

    /**
     * 首页的公共推荐
     * @return
     */
    @GetMapping("/public/recommend")
    public BaseResponse<PostVo> getPublicRecommend( ) {
        return ResultUtils.success(null);
    }

    /**
     * 每个用户看文章下滑会触发私人推荐文章
     * @param userId
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/private/recommend/{userId}")
    public BaseResponse<List<PostVo>> getPrivateRecommend(@PathVariable("userId") Long userId, @RequestBody GetPrivateRecommendRequest getPrivateRecommendRequest) {

        return ResultUtils.success(postService.getPrivateRecommend(userId,getPrivateRecommendRequest));
    }

}
