package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.constant.CommonConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.question.QuestionListRequest;
import com.zkm.forum.model.dto.question.SaveQuestionRequest;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.Question;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.model.vo.question.QuestionListVo;
import com.zkm.forum.model.vo.question.QuestionSearchVo;
import com.zkm.forum.service.PostService;
import com.zkm.forum.service.QuestionService;
import com.zkm.forum.mapper.QuestionMapper;
import com.zkm.forum.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 张凯铭
 * @description 针对表【question(问题)】的数据库操作Service实现
 * @createDate 2025-04-11 10:07:11
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    UserService userService;
    @Resource
    PostService postService;
    @Resource
    private Cache<String,String> LOCAL_CACHE;

    @Override
    public Boolean saveQuestion(SaveQuestionRequest saveQuestionRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Question question = new Question();
        question.setQuestion(saveQuestionRequest.getQuestion());
        question.setTags(JSONUtil.toJsonStr(saveQuestionRequest.getTags()));
        question.setUserId(loginUser.getId());
        boolean result = this.save(question);
        if (result) {
            return result;
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发布失败,请稍后再试");
        }

    }

    @Override
    public Boolean answerQuestion(AddPostRequest addPostRequest, HttpServletRequest request) {
        Boolean result = postService.addPost(addPostRequest, request);
        if (result) {
            UpdateWrapper<Question> questionUpdateWrapper = new UpdateWrapper<>();
            questionUpdateWrapper.setSql("answerCount=answerCount+1");
            return result;
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发布失败，请稍后再试");
        }
    }

    @Override
    public QuestionSearchVo searchQustion(String keyWords) {
        return null;
    }

    @Override
    public Page<QuestionListVo> listQuestion(QuestionListRequest questionListRequest) {
        LocalDateTime start = questionListRequest.getStart();
        LocalDateTime end = questionListRequest.getEnd();
        int current = questionListRequest.getCurrent();
        int pageSize = questionListRequest.getPageSize();
        String sortField = questionListRequest.getSortField();
        String sortOrder = questionListRequest.getSortOrder();

        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        questionQueryWrapper.between(start != null && end != null, "createTime", start, end);
        Page<Question> questionPage = new Page<>(current, pageSize);
        Page<Question> page = this.page(questionPage, questionQueryWrapper);
        List<Question> records = page.getRecords();
        Page<QuestionListVo> questionListVoPage = new Page<>();
        List<QuestionListVo> questionListVos = new ArrayList<>();
        for (QuestionListVo questionListVo : questionListVos) {
            List<QuestionListVo> QuestionListVoRecords = records.stream().map(record -> {
                BeanUtils.copyProperties(record, questionListVo);
                questionListVo.setTags(JSONUtil.toList(record.getTags(), String.class));
                return questionListVo;
            }).toList();
            BeanUtils.copyProperties(page, questionListVoPage);
            questionListVoPage.setRecords(QuestionListVoRecords);
//            questionListVoPage.setTotal(page.getTotal());
//            questionListVoPage.setCurrent(page.getCurrent());
//            questionListVoPage.setPages(page.getPages());
        }


        return questionListVoPage;
    }

    @Override
    public List<PostVo> getQuestionAnswer(Long questionId) {
        if (questionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择有效的问题");
        }
        Question question = this.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题不存在");
        }
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("questionId", questionId);
        List<Post> postList = postService.list(postQueryWrapper);
        List<PostVo> postVos = new ArrayList<>();
        for (PostVo postVo : postVos) {
            postVos = postList.stream().map(post -> {
                post.setContent(StringUtils.substring(post.getContent(),0,15));
                BeanUtils.copyProperties(post, postVo);
                postVo.setTags(JSONUtil.toList(post.getTags(), String.class));
                return postVo;
            }).toList();
        }
        return postVos;
    }
}




