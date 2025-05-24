package com.zkm.forum.strategy.context;

import com.zkm.forum.model.dto.post.PostSearchRequest;
import com.zkm.forum.model.enums.PostSearchModeEnum;
import com.zkm.forum.model.enums.QuestionSearchModeEnum;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.question.QuestionSearchVo;
import com.zkm.forum.strategy.PostSearchStrategy;
import com.zkm.forum.strategy.QuestionSearchStrategy;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Service
public class SearchStrategyContext {
    @Value("${search.mode}")
    String mode;
    @Resource
    private Map<String, PostSearchStrategy> postSearchStrategyMap;
    private Map<String, QuestionSearchStrategy> questionSearchStrategyMap;
    public List<PostSearchVo> postExcuteSearchStrategy(PostSearchRequest postSearchRequest){
       return postSearchStrategyMap.get(PostSearchModeEnum.getMessageByMode(mode)).searchPost(postSearchRequest);
    }
    public List<QuestionSearchVo> questionExcuteSearchStrategy(String keyWords){
        return questionSearchStrategyMap.get(QuestionSearchModeEnum.getMessageByMode(mode)).searchQuestion(keyWords);
    }

}
