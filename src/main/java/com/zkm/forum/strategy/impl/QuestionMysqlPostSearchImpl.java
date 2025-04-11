package com.zkm.forum.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.zkm.forum.constant.CommonConstant;
import com.zkm.forum.mapper.QuestionMapper;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.Question;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.question.QuestionSearchVo;
import com.zkm.forum.service.UserService;
import com.zkm.forum.strategy.PostSearchStrategy;
import com.zkm.forum.strategy.QuestionSearchStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.zkm.forum.constant.LocalCacheConstant.USERID_USERNAME;

@Service("questionMysqlSearchImpl")
public class QuestionMysqlPostSearchImpl implements QuestionSearchStrategy {
    @Resource
    QuestionMapper questionMapper;
    @Resource
    private Cache<String, String> LOCAL_CACHE;
    @Resource
    private UserService userService;

    @Override
    public List<QuestionSearchVo> searchQuestion(String keyWords) {
        if (keyWords.isBlank()) {
            return new ArrayList<>();
        }
        QueryWrapper<Question> questionSearchVoQueryWrapper = new QueryWrapper<>();
        questionSearchVoQueryWrapper.like("question", keyWords).or()
                //只要标签【Json字符串】中包含搜索词，就会被返回
                .apply("JSON_CONTAINS(tags, {0})", "\"" + keyWords.replace("\"", "\\\"") + "\"");
        List<Question> questions = questionMapper.selectList(questionSearchVoQueryWrapper);
        return questions.stream().map(question -> {
                    QuestionSearchVo questionSearchVo = new QuestionSearchVo();
                    boolean lowerCase = true;
                    int key = question.getQuestion().indexOf(keyWords.toLowerCase());
                    if (key == -1) {
                        key = question.getQuestion().indexOf(keyWords.toUpperCase());
                        if (key != -1) {
                            lowerCase = false;
                        }
                    }
                    if (key != -1) {
                        if (lowerCase) {
                            questionSearchVo.setQuestion(question.getQuestion().replaceAll(keyWords.toLowerCase(), CommonConstant.PRE_TAG + keyWords.toLowerCase() + CommonConstant.QUESTION_TAG));
                        } else {
                            questionSearchVo.setQuestion(question.getQuestion().replaceAll(keyWords.toUpperCase(), CommonConstant.PRE_TAG + keyWords.toUpperCase() + CommonConstant.QUESTION_TAG));
                        }
                    } else {
                        questionSearchVo.setQuestion(question.getQuestion());
                    }
                    List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
                    List<String> list = tagList.stream().filter(tag -> tag.contains(keyWords.toUpperCase())).filter(tag -> tag.contains(keyWords.toLowerCase())).toList();
                    questionSearchVo.setConcernNum(question.getConcernNum());
                    questionSearchVo.setQuestionId(question.getId());
                    questionSearchVo.setTag(list);

                    String ifPresent = LOCAL_CACHE.getIfPresent(USERID_USERNAME + question.getUserId());
                    if (ifPresent != null) {
                        questionSearchVo.setAuthorName(ifPresent);
                    } else {
                        CompletableFuture.runAsync(() -> {
                            User author = userService.getById(question.getUserId());
                            LOCAL_CACHE.put(USERID_USERNAME + author.getId(), author.getUserName());
                            questionSearchVo.setAuthorName(author.getUserName());
                        });
                    }
                    return questionSearchVo;
                }
        ).toList();
    }
}
