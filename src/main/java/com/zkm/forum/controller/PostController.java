package com.zkm.forum.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.annotation.AuthCheck;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.*;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.service.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.zkm.forum.constant.RedisConstant.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
@Api(tags = "帖子模块")
@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    PostService postService;
    @Resource
    StringRedisTemplate stringredisTemplate;
    @ApiOperation("发布或修改帖子")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> addPost(@RequestBody AddPostRequest addPostRequest, HttpServletRequest httpServletRequest) {
        if (ObjectUtils.isEmpty(addPostRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "信息填写完整再发布");
        }
        return ResultUtils.success(postService.addPost(addPostRequest, httpServletRequest));
    }

    @ApiOperation("逻辑删除帖子")
    @PostMapping("/updatedelete/mypost")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updatePostDeletForMy(UpdatePostDeleteForMy updatePostDeleteForMy, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(postService.updatePostDeletForMy(updatePostDeleteForMy, httpServletRequest));
    }
    @ApiOperation("搜索帖子")
    @PostMapping("/searchPost")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<PostSearchVo>> searchPost(PostSearchRequest postSearchRequest) {
        return ResultUtils.success(postService.searchPost(postSearchRequest));
    }

    @ApiOperation("查看帖子详情")
    @GetMapping("/{postId}")
    public BaseResponse<PostVo> getPostById(@PathVariable("postId") Long postId, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(postService.getPostById(postId, httpServletRequest));
    }


    /**
     * 首页的公共推荐(登录看到的)
     *
     * @return
     */
    @ApiOperation("热缓存推荐登录")
    @GetMapping("/public/login/recommend")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<PostVo>> getLoginPublicRecommend() {
        return ResultUtils.success(Objects.requireNonNull(stringredisTemplate
                        .opsForList().range(PRE_CACHE_POST_LOGIN, 0, 14))
                .stream().map(str -> JSONUtil.toBean(str, PostVo.class)).toList());
    }

    @ApiOperation("热缓存推荐未登录")
    @GetMapping("/logout/recommend")
    public BaseResponse<List<PostVo>> getLogoutRecommend() {
        return ResultUtils.success(Objects.requireNonNull(stringredisTemplate
                        .opsForList().range(PRE_CACHE_POST_LOGOUT, 0, 14))
                .stream().map(str -> JSONUtil.toBean(str, PostVo.class)).toList());
    }

    /**
     * 每个用户看文章下滑会触发私人推荐文章
     *
     * @param userId
     * @param getPrivateRecommendRequest
     * @return
     */
    @ApiOperation("热缓存私人推荐文章")
    @PostMapping("/private/recommend/{userId}")
    public BaseResponse<List<PostVo>> getPrivateRecommend(@PathVariable("userId") Long userId, @RequestBody GetPrivateRecommendRequest getPrivateRecommendRequest) {

        return ResultUtils.success(postService.getPrivateRecommend(userId, getPrivateRecommendRequest));
    }

    @ApiOperation("管理员:发布文章列表")
    @PostMapping("/list/post")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    BaseResponse<Page<Post>> listPostForAdmin(PostQueryRequest postQueryRequest) {
        return ResultUtils.success(postService.listPostForAdmin(postQueryRequest));
    }

    @ApiOperation("举报帖子")
    @PostMapping("/report/post")
    public BaseResponse<Boolean> reportPost(@RequestBody ReportPostRequest reportPostRequest, HttpServletRequest request) {
        return ResultUtils.success(postService.reportPost(reportPostRequest, request));
    }

    @ApiOperation("ES搜索帖子")
    @PostMapping("/searchEsPost")
    public BaseResponse<Page<PostSearchVo>> searchEsPost(@RequestBody PostQueryRequest postQueryRequest){
       return ResultUtils.success(postService.searchEsPost(postQueryRequest));
    }

}
