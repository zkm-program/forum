package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.question.SaveQuestionRequest;
import com.zkm.forum.model.entity.Question;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.question.QuestionSearchVo;
import com.zkm.forum.service.PostService;
import com.zkm.forum.service.QuestionService;
import com.zkm.forum.mapper.QuestionMapper;
import com.zkm.forum.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 张凯铭
* @description 针对表【question(问题)】的数据库操作Service实现
* @createDate 2025-04-11 10:07:11
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

    @Resource
    UserService userService;
    @Resource
    PostService postService;
    @Override
    public Boolean saveQuestion(SaveQuestionRequest saveQuestionRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Question question = new Question();
        question.setQuestion(saveQuestionRequest.getQuestion());
        question.setTags(JSONUtil.toJsonStr(saveQuestionRequest.getTags()));
        question.setUserId(loginUser.getId());
        boolean result = this.save(question);
        if(result){
            return result;
        }else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"发布失败,请稍后再试");
        }

    }

    @Override
    public Boolean answerQuestion(AddPostRequest addPostRequest, HttpServletRequest request) {
        Boolean result = postService.addPost(addPostRequest, request);
        if(result){
            return result;
        }else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"发布失败，请稍后再试");
        }
    }

    @Override
    public QuestionSearchVo searchQustion(String keyWords) {
        return null;
    }
}




