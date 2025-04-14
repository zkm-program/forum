package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.service.InvitationService;
import com.zkm.forum.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/invite")
public class InvitationController {
    @Resource
    private InvitationService invitationService;
    @Resource
    private UserService userService;

    @GetMapping("/getInviteLink")
    public BaseResponse<String> getInviteLink(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(invitationService.getInviteLink(loginUser));
    }

    @GetMapping("/processInvitation/{inviteeId}/{inviterId}")
    public BaseResponse<Boolean> processInvitation(@PathVariable("inviteeId") Long inviteeId, @PathVariable("inviterId") Long inviterId) {
        return ResultUtils.success(invitationService.processInvitation(inviteeId, inviterId));
    }
}
