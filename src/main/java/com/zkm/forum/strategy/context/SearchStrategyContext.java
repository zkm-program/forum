package com.zkm.forum.strategy.context;

import com.zkm.forum.model.enums.SearchModeEnum;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.strategy.SearchStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
@Service
public class SearchStrategyContext {
    @Value("${search.mode}")
    String mode;
    @Resource
    private Map<String, SearchStrategy> searchStrategyMap;
    public List<PostSearchVo> excuteSearchStrategy(String keyWords){
       return searchStrategyMap.get(SearchModeEnum.getMessageByMode(mode)).searchPost(keyWords);
    }
}
