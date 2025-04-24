package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.FitnessMapper;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.entity.Fitness;
import com.zkm.forum.model.entity.FitnessImage;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.fitness.AnalysePictureVo;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.model.vo.fitness.GetUserInfoVo;
import com.zkm.forum.service.FitnessService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.AIUtils;
import com.zkm.forum.utils.AiPictureUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

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
    @Resource
    FitnessMapper fitnessMapper;


    AiPictureUtils aiPictureUtils = new AiPictureUtils();

    @Override
    @Transactional
    public Long saveOrUpdateMessage(SaveOrUpdateMessageRequest saveOrUpdateMessageRequest, HttpServletRequest request) {
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
// 新增时重新查询确保ID正确
//        if (!flag) {
//            Fitness savedFitness = this.getById(fitness.getId());
//            if (savedFitness != null) {
//                fitness = savedFitness;
//            }
//        }
//        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
//        if (!flag) {
//            Long fitnessId = fitness.getId();
//            userUpdateWrapper.set("fitnessId", fitnessId);
//            userService.update(userUpdateWrapper);
//        }

        return fitness.getId();
    }

    @Override
    public AnalyseUserVo analyseUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Fitness fitness = this.getById(loginUser.getFitnessId());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("用户基本信息").append("\n");
        stringBuilder.append("性别:" + fitness.getGender()).append("\n");
        stringBuilder.append("年龄:" + fitness.getAge() + "岁").append("\n");
        stringBuilder.append("身高:" + fitness.getHeight() + "cm").append("\n");
        stringBuilder.append("体重:" + fitness.getWeight() + "kg").append("\n");
        stringBuilder.append("目标:" + fitness.getTarget()).append("\n");
        String s = aiUtils.sendMsgToXingHuo(true, stringBuilder.toString());
        String[] split = s.split("【【【【");

        // 提取数字部分并存入 fitness 对象
        fitness.setCalorieTarget(extractNumber(split[1]));
        fitness.setProteinTarget(extractNumber(split[2]));
        fitness.setCarbohydrateTarget(extractNumber(split[3]));
        fitness.setFatTarget(extractNumber(split[4]));

        AnalyseUserVo analyseUserVo = new AnalyseUserVo();
        analyseUserVo.setCalorieTarget(extractNumber(split[1]));
        analyseUserVo.setProteinTarget(extractNumber(split[2]));
        analyseUserVo.setCarbohydrateTarget(extractNumber(split[3]));
        analyseUserVo.setFatTarget(extractNumber(split[4]));
        this.update().eq("id", loginUser.getFitnessId()).update(fitness);
        return analyseUserVo;
    }



    @Override
    public AnalysePictureVo analysePicture(AiXinghuoPictureRequest request,HttpServletRequest userRequest) {
        try {
            System.out.println(request.getPictureUrl());
            String s = aiPictureUtils.analyzeImage(request.getDescription(), request.getPictureUrl());
            String[] split = s.split("【【【【");
            AnalysePictureVo analysePictureVo = new AnalysePictureVo();
            User loginUser = userService.getLoginUser(userRequest);
            analysePictureVo.setFitnessId(loginUser.getFitnessId());
            analysePictureVo.setPictureUrl(request.getPictureUrl());
            analysePictureVo.setFoodName(request.getFoodName());
            analysePictureVo.setCalorie(extractNumber(split[1]));
            analysePictureVo.setProtein(extractNumber(split[2]));
            analysePictureVo.setCarbohydrate(extractNumber(split[3]));
            analysePictureVo.setFat(extractNumber(split[4]));
            return  analysePictureVo;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
        }

    }

    @Override
    public GetUserInfoVo getUserInfo(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Fitness fitness = new Fitness();
        if (ObjectUtils.isNotEmpty(loginUser.getFitnessId())) {
            fitness = this.getById(loginUser.getFitnessId());
        }
        GetUserInfoVo getUserInfoVo = new GetUserInfoVo();
        if (ObjectUtils.isNotEmpty(fitness)) {
            getUserInfoVo.setFitnessId(fitness.getId());
            getUserInfoVo.setAge(fitness.getAge());
            getUserInfoVo.setHeight(fitness.getHeight());
            getUserInfoVo.setWeight(fitness.getWeight());
            getUserInfoVo.setCalorieTarget(fitness.getCalorieTarget());
            getUserInfoVo.setProteinTarget(fitness.getProteinTarget());
            getUserInfoVo.setCarbohydrateTarget(fitness.getCarbohydrateTarget());
            getUserInfoVo.setFatTarget(fitness.getFatTarget());
        }
        return getUserInfoVo;
    }

    // 新增方法：提取字符串中的数字部分
    private BigDecimal extractNumber(String input) {
        if (input == null || input.isEmpty()) {
            return BigDecimal.ZERO; // 默认值
        }
        String numberPart = input.replaceAll("[^0-9.]", ""); // 提取数字和小数点
        return new BigDecimal(numberPart);
    }
}




