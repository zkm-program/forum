package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.postfavour.PostFavourRequest;

import com.zkm.forum.model.vo.postFavour.ListMyPostFavourVo;
import com.zkm.forum.service.PostFavourService;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Lazy
@RestController
@RequestMapping("/postfavour")
public class PostFavourController {
    @Resource
    PostFavourService postFavourService;

    @PostMapping("/")
    public BaseResponse<Integer> doPostFavour(@RequestBody PostFavourRequest postFavourRequest, HttpServletRequest request) {
        return ResultUtils.success(postFavourService.doPostFavour(postFavourRequest, request));
    }

    /**
     * 返回的post简略版，想看完整内容需点击对应post简略版
     * @param request
     * @return
     */
    @PostMapping("/listMyPostFavour")
    BaseResponse<List<ListMyPostFavourVo>> listMyPostFavourVo(HttpServletRequest request){
        return ResultUtils.success(postFavourService.listMyPostFavourVo(request));
    }
}
