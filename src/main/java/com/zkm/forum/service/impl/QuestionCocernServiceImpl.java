package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.QuestionConcernMapper;
import com.zkm.forum.model.entity.Question;
import com.zkm.forum.model.entity.QuestionConcern;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.questionConcern.ListQuestionConcernVo;
import com.zkm.forum.service.QuestionCocernService;
import com.zkm.forum.service.QuestionService;
import com.zkm.forum.service.UserService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author 张凯铭
 * @description 针对表【question_cocern(问题关注)】的数据库操作Service实现
 * @createDate 2025-04-11 20:07:48
 */
@Service
public class QuestionCocernServiceImpl extends ServiceImpl<QuestionConcernMapper, QuestionConcern>
        implements QuestionCocernService {
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;
//    @Resource
//    private QuestionCocernService questionCocernService;

    @Override
    public Integer doQuestionConcern(Long questionId, HttpServletRequest request) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        User loginUser = userService.getLoginUser(request);
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关注的问题不存在");
        }
       QuestionCocernService questionCocernService = (QuestionCocernService) AopContext.currentProxy();
        synchronized (String.valueOf(loginUser.getId())) {
            return questionCocernService.doQuestionConcernInner(questionId, loginUser.getId());
        }
    }

    @Override
    public Integer doQuestionConcernInner(Long questionId, Long userId) {
        QuestionConcern questionConcern = new QuestionConcern();
        questionConcern.setQuestionId(questionId);
        questionConcern.setUserId(userId);
        QueryWrapper<QuestionConcern> questionCocernQueryWrapper = new QueryWrapper<>();
        questionCocernQueryWrapper.eq("questionId", questionId);
        questionCocernQueryWrapper.eq("userId", userId);
        QuestionConcern oldQuestionConcern = this.getOne(questionCocernQueryWrapper);
        if (oldQuestionConcern == null) {
            boolean result = this.save(questionConcern);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "关注失败");
            } else {
                UpdateWrapper<Question> questionUpdateWrapper = new UpdateWrapper<>();
                questionUpdateWrapper.eq("id", questionId)
                        .setSql("concernNum=concernNum+1");
                boolean flag = questionService.update(questionUpdateWrapper);
                return flag ? -1 : 0;
            }
        } else {
            boolean falg = this.remove(questionCocernQueryWrapper);
            if (!falg) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消收藏失败，稍后再试");
            } else {
                boolean result = questionService.update().eq("id", questionId)
                        .setSql("concernNum=concernNum-1")
                        .update();
                return result ? -1 : 0;
            }
        }
     }

    @Override
    public List<ListQuestionConcernVo> listQuestionConcernVo(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<QuestionConcern> questionCocernQueryWrapper = new QueryWrapper<>();
        questionCocernQueryWrapper.select("questionId");
        questionCocernQueryWrapper.eq("userId",loginUser.getId());
        List<Long> questionIdlist = this.list(questionCocernQueryWrapper).stream().map(QuestionConcern::getQuestionId).toList();
        List<Question> questionList = questionService.listByIds(questionIdlist);
        List<ListQuestionConcernVo> listQuestionConcernVoList = questionList.stream().map(this::transformTolistQuestionConcernVo).toList();
        return listQuestionConcernVoList;
    }
    private ListQuestionConcernVo transformTolistQuestionConcernVo(Question question){
        ListQuestionConcernVo listQuestionConcernVo = new ListQuestionConcernVo();
        listQuestionConcernVo.setQuestionId(question.getId());
        listQuestionConcernVo.setQuestion(question.getQuestion());
        listQuestionConcernVo.setTags(JSONUtil.toList(question.getTags(),String.class));
        listQuestionConcernVo.setConcernNum(question.getConcernNum());
        listQuestionConcernVo.setViewCount(question.getViewCount());
        listQuestionConcernVo.setQuestionCount(question.getQuestionCount());
        return listQuestionConcernVo;

    }
}




