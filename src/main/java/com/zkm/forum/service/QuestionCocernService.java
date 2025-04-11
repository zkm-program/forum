package com.zkm.forum.service;

import com.zkm.forum.model.dto.postfavour.PostFavourRequest;
import com.zkm.forum.model.entity.QuestionCocern;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 张凯铭
* @description 针对表【question_cocern(问题关注)】的数据库操作Service
* @createDate 2025-04-11 20:07:48
*/
public interface QuestionCocernService extends IService<QuestionCocern> {
    Integer doQuestionCocern(Long questionCocernId, HttpServletRequest request);
    Integer doQuestionCocernInner(Long questionCocernId,Long userId);
}
