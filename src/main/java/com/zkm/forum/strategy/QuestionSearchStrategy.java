package com.zkm.forum.strategy;

import com.zkm.forum.model.vo.question.QuestionSearchVo;

import java.util.List;

public interface QuestionSearchStrategy {
    List<QuestionSearchVo> searchQuestion(String keyWords);
}
