package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.vo.questionConcern.ListQuestionConcernVo;
import com.zkm.forum.service.QuestionCocernService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class QuestionConcernController {
    @Resource
    private QuestionCocernService questionCocernService;

    @GetMapping("/doQuestionCocern/{questionConcernId}")
    public BaseResponse<Integer> doQuestionCocern(@PathVariable("questionConcernId") Long questionConcernId, HttpServletRequest request) {
        return ResultUtils.success(questionCocernService.doQuestionConcern(questionConcernId, request));
    }

    @GetMapping("/listQuestionConcernVo")
    public BaseResponse<List<ListQuestionConcernVo>> listQuestionConcernVo(HttpServletRequest request) {
        return ResultUtils.success(questionCocernService.listQuestionConcernVo(request));
    }
}
