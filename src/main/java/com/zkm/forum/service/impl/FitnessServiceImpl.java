package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.FitnessMapper;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.entity.Fitness;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.service.FitnessService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.AIUtils;
import com.zkm.forum.utils.AiPictureUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 张凯铭
 * @description 针对表【fitness(健身表)】的数据库操作Service实现
 * @createDate 2025-04-07 17:25:30
 */
@Service
public class FitnessServiceImpl extends ServiceImpl<FitnessMapper, Fitness>
        implements FitnessService {
    @Resource
    private UserService userService;
    @Resource
    private AIUtils aiUtils;


    AiPictureUtils aiPictureUtils = new AiPictureUtils();

    @Override
    @Transactional
    public Boolean saveOrUpdateMessage(SaveOrUpdateMessageRequest saveOrUpdateMessageRequest, HttpServletRequest request) {
        boolean flag = true;
        if (ObjectUtils.isEmpty(saveOrUpdateMessageRequest.getId())) {
            flag = false;
        }
        Fitness fitness = new Fitness();
        User loginUser = userService.getLoginUser(request);
        fitness.setId(saveOrUpdateMessageRequest.getId());
        fitness.setUserId(loginUser.getId());
        fitness.setAge(saveOrUpdateMessageRequest.getAge());
        fitness.setGender(loginUser.getGender());
        fitness.setHeight(saveOrUpdateMessageRequest.getHeight());
        fitness.setWeight(saveOrUpdateMessageRequest.getWeight());
        // todo 前端传过来的最好是固定样式的如下类型【每周减脂0.5g】
        fitness.setTarget(saveOrUpdateMessageRequest.getTarget());
        fitness.setUserAvatar(loginUser.getUserAvatar());
        boolean result = this.saveOrUpdate(fitness);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败，稍后再试");
        }
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        if (!flag) {
            Long fitnessId = fitness.getId();
            userUpdateWrapper.set("fitnessId", fitnessId);
            userService.update(userUpdateWrapper);
        }

        return result;
    }

    @Override
    public AnalyseUserVo analyseUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Fitness fitness = this.getById(loginUser.getFitnessId());
        /*
                "分析需求：\n" +
                "{根据用户的基本信息和目标生成用户的BMR，假设用户每天不运动用户每天摄入多少卡路里才能达到用户的目标}\n" +
                "用户基本信息：\n" +
                "{原始数据格式如下
                   性别：女
                   年龄：30岁
                   身高：165cm
                   体重：68kg
                   目标：减脂（每周减0.5kg）}\n"*/
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("分析需求").append("\n");
        stringBuilder.append("根据用户的基本信息和目标生成用户的BMR，假设用户每天不运动，用户每天摄入多少卡路里才能达到用户的目标").append("\n");
        stringBuilder.append("用户基本信息").append("\n");
        stringBuilder.append("性别:" + fitness.getGender()).append("\n");
        stringBuilder.append("年龄:" + fitness.getAge() + "岁").append("\n");
        stringBuilder.append("身高:" + fitness.getHeight() + "cm").append("\n");
        stringBuilder.append("体重:" + fitness.getWeight() + "kg").append("\n");
        stringBuilder.append("目标:" + fitness.getTarget()).append("\n");
        String s = aiUtils.sendMsgToXingHuo(true, stringBuilder.toString());
        String[] split = s.split("【【【【");
        AnalyseUserVo analyseUserVo = new AnalyseUserVo();
        analyseUserVo.setBMR(split[1]);
        analyseUserVo.setGetKcal(split[2]);
        return analyseUserVo;

    }

    @Override
    public String analysePicture(AiXinghuoPictureRequest request) {
        try {
            return aiPictureUtils.analyzeImage(request.getDescription(), request.getPictureUrl());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
        }

    }
}




