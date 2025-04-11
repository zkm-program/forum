package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.question.QuestionListRequest;
import com.zkm.forum.model.dto.question.SaveQuestionRequest;
import com.zkm.forum.model.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.model.vo.question.QuestionListVo;
import com.zkm.forum.model.vo.question.QuestionSearchVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【question(问题)】的数据库操作Service
* @createDate 2025-04-11 10:07:11
*/
public interface QuestionService extends IService<Question> {
    Boolean saveQuestion(SaveQuestionRequest saveQuestionRequest, HttpServletRequest request);
    Boolean answerQuestion(AddPostRequest addPostRequest, HttpServletRequest request);
    QuestionSearchVo searchQustion(String keyWords);
    Page<QuestionListVo> listQuestion(QuestionListRequest questionListRequest);
    List<PostVo> getQuestionAnswer(Long questionId);

}
