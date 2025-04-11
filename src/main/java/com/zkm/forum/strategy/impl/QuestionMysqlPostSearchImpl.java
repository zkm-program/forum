package com.zkm.forum.strategy.impl;

import com.zkm.forum.model.vo.question.QuestionSearchVo;
import com.zkm.forum.strategy.PostSearchStrategy;
import com.zkm.forum.strategy.QuestionSearchStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("questionMysqlSearchImpl")
public class QuestionMysqlPostSearchImpl implements QuestionSearchStrategy {
    @Override
    public List<QuestionSearchVo> searchQuestion(String keyWords) {

        return List.of();
    }
}
