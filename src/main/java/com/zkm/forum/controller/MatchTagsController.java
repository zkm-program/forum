package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.matchTags.MatchTagsVo;
import com.zkm.forum.service.MatchTagsService;
import com.zkm.forum.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/matchTags")
public class MatchTagsController {
    @Resource
    private UserService userService;
    @Resource
    private MatchTagsService matchTagsService;

    @GetMapping("/get")
    public BaseResponse<List<MatchTagsVo>> getBasicMatchTags(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(matchTagsService.getBasicMatchTags(loginUser));
    }
}
