package com.zkm.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.annotation.AuthCheck;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.question.QuestionListRequest;
import com.zkm.forum.model.dto.question.SaveQuestionRequest;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.model.vo.question.QuestionListVo;
import com.zkm.forum.model.vo.question.QuestionSearchVo;
import com.zkm.forum.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "问题模块")
@RestController
@RequestMapping("/question")
public class QuestionController {
    @Resource
    QuestionService questionService;

    @ApiOperation("发布问题")
    @PostMapping("/save")
    public BaseResponse<Boolean> saveQuestion(SaveQuestionRequest saveQuestionRequest, HttpServletRequest request) {
        return ResultUtils.success(questionService.saveQuestion(saveQuestionRequest, request));
    }

    @ApiOperation("回答问题")
    @PostMapping("/answer")
    BaseResponse<Boolean> answerQuestion(AddPostRequest addPostRequest, HttpServletRequest request) {
        return ResultUtils.success(questionService.answerQuestion(addPostRequest, request));
    }

    @ApiOperation("搜索问题sql")
    @GetMapping("/search/{keyWords}")
    public BaseResponse<QuestionSearchVo> searchQustion(@PathVariable("keyWords") String keyWords) {
        return ResultUtils.success(questionService.searchQustion(keyWords));
    }

    @ApiOperation("用户查讯问题")
    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Page<QuestionListVo>> listQuestion(QuestionListRequest questionListRequest) {
        return ResultUtils.success(questionService.listQuestion(questionListRequest));
    }

    /**
     * 返回完整版类似知乎
     * @param questionId
     * @return
     */
    @ApiOperation("用户查看问题答案列表")
    @GetMapping("/getQuestionAnswer/{questionId}")
    public BaseResponse<List<PostVo>> getQuestionAnswer(Long questionId) {
        return ResultUtils.success(questionService.getQuestionAnswer(questionId));
    }
}
