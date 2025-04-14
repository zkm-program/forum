package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.entity.QuestionConcern;
import com.zkm.forum.model.vo.questionConcern.ListQuestionConcernVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【question_cocern(问题关注)】的数据库操作Service
* @createDate 2025-04-11 20:07:48
*/
public interface QuestionCocernService extends IService<QuestionConcern> {
    Integer doQuestionConcern(Long questionConcernId, HttpServletRequest request);
    Integer doQuestionConcernInner(Long questionConcernId,Long userId);
    List<ListQuestionConcernVo> listQuestionConcernVo(HttpServletRequest request);

}
