package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.entity.Question;
import com.zkm.forum.model.entity.QuestionCocern;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.service.QuestionCocernService;
import com.zkm.forum.mapper.QuestionCocernMapper;
import com.zkm.forum.service.QuestionService;
import com.zkm.forum.service.UserService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @author 张凯铭
 * @description 针对表【question_cocern(问题关注)】的数据库操作Service实现
 * @createDate 2025-04-11 20:07:48
 */
@Service
public class QuestionCocernServiceImpl extends ServiceImpl<QuestionCocernMapper, QuestionCocern>
        implements QuestionCocernService {
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;
    @Resource
    private QuestionCocernService questionCocernService;

    @Override
    public Integer doQuestionCocern(Long questionId, HttpServletRequest request) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        User loginUser = userService.getLoginUser(request);
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "关注的问题不存在");
        }
        questionCocernService = (QuestionCocernService) AopContext.currentProxy();
        synchronized (String.valueOf(loginUser.getId())) {
            return doQuestionCocernInner(questionId, loginUser.getId());
        }
    }

    @Override
    public Integer doQuestionCocernInner(Long questionId, Long userId) {
        QuestionCocern questionCocern = new QuestionCocern();
        questionCocern.setQuestionId(questionId);
        questionCocern.setUserId(userId);
        QueryWrapper<QuestionCocern> questionCocernQueryWrapper = new QueryWrapper<>();
        questionCocernQueryWrapper.eq("questionId", questionId);
        questionCocernQueryWrapper.eq("userId", userId);
        QuestionCocern oldQuestionCocern = this.getOne(questionCocernQueryWrapper);
        if (oldQuestionCocern == null) {
            boolean result = this.save(questionCocern);
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
}




