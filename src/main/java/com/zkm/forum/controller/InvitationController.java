package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.service.InvitationService;
import com.zkm.forum.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
@Api(tags = "邀请用户注册模块")
@RestController
@RequestMapping("/invite")
public class InvitationController {
    @Resource
    private InvitationService invitationService;
    @Resource
    private UserService userService;

    @ApiOperation("生成邀请用户链接")
    @GetMapping("/getInviteLink")
    public BaseResponse<String> getInviteLink(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(invitationService.getInviteLink(loginUser));
    }
    @ApiOperation("处理被邀请注册的用户")
    @GetMapping("/processInvitation/{inviteeId}/{inviterId}")
    public BaseResponse<Boolean> processInvitation(@PathVariable("inviteeId") Long inviteeId, @PathVariable("inviterId") Long inviterId) {
        return ResultUtils.success(invitationService.processInvitation(inviteeId, inviterId));
    }
}
