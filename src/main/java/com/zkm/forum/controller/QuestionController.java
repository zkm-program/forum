package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.question.SaveQuestionRequest;
import com.zkm.forum.service.QuestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/question")
public class QuestionController {
    @Resource
    QuestionService questionService;

    @PostMapping("/save")
    public BaseResponse<Boolean> saveQuestion(SaveQuestionRequest saveQuestionRequest, HttpServletRequest request) {
        return ResultUtils.success(questionService.saveQuestion(saveQuestionRequest, request));
    }

    @PostMapping("/answer")
    BaseResponse<Boolean> answerQuestion(AddPostRequest addPostRequest, HttpServletRequest request){
        return ResultUtils.success(questionService.answerQuestion(addPostRequest,request));
    }

}
