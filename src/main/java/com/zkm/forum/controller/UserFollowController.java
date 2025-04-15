package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.vo.userFollow.ListUserFollowVo;
import com.zkm.forum.service.UserFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
@Api(tags = "用户关注模块")
@RestController
@RequestMapping("/follow")
public class UserFollowController {
    @Resource
    private UserFollowService userFollowService;

    @ApiOperation("关注某个用户")
    @GetMapping("/user/{followerId}")
    public BaseResponse<Integer> doFollowUser(@PathVariable("followerId") Long followerId, HttpServletRequest request) {
        return ResultUtils.success(userFollowService.doFollowUser(followerId, request));
    }

    @ApiOperation("查看关注列表")
    @GetMapping("/listUserFollowVo")
    public BaseResponse<List<ListUserFollowVo>> listUserFollowVo(HttpServletRequest request) {
        return ResultUtils.success(userFollowService.listUserFollowVo(request));
    }
}
