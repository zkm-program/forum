package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.constant.JdHotKeyConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.FitnessMapper;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.entity.Fitness;
import com.zkm.forum.model.entity.FitnessImage;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.fitnessImage.AnalysePictureVo;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.model.vo.fitness.GetUserInfoVo;
import com.zkm.forum.service.FitnessImageService;
import com.zkm.forum.service.FitnessService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.AIUtils;
import com.zkm.forum.utils.AiPictureUtils;
import com.zkm.forum.utils.RedisLimiterUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import static com.zkm.forum.constant.RabbitMqConstant.*;
import static com.zkm.forum.constant.JdHotKeyConstant.*;

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
    private FitnessImageService fitnessImageService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RedisLimiterUtils redisLimiterUtils;
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
        // todo 一次请求却插入两次
        boolean result = this.save(fitness);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败，稍后再试");
        }
        loginUser.setFitnessId(fitness.getId());
        userService.updateById(loginUser);
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

//    @Override
//    public AnalyseUserVo analyseUser(HttpServletRequest request) {
//        User loginUser = userService.getLoginUser(request);
//        Fitness fitness = this.getById(loginUser.getFitnessId());
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("用户基本信息").append("\n");
//        stringBuilder.append("性别:" + fitness.getGender()).append("\n");
//        stringBuilder.append("年龄:" + fitness.getAge() + "岁").append("\n");
//        stringBuilder.append("身高:" + fitness.getHeight() + "cm").append("\n");
//        stringBuilder.append("体重:" + fitness.getWeight() + "kg").append("\n");
//        stringBuilder.append("目标:" + fitness.getTarget()).append("\n");
//        String s = aiUtils.sendMsgToXingHuo(true, stringBuilder.toString());
//        String[] split = s.split("【【【【");
//
//        // 提取数字部分并存入 fitness 对象
//        fitness.setCalorieTarget(extractNumber(split[1]));
//        fitness.setProteinTarget(extractNumber(split[2]));
//        fitness.setCarbohydrateTarget(extractNumber(split[3]));
//        fitness.setFatTarget(extractNumber(split[4]));
//
//        AnalyseUserVo analyseUserVo = new AnalyseUserVo();
//        analyseUserVo.setCalorieTarget(extractNumber(split[1]));
//        analyseUserVo.setProteinTarget(extractNumber(split[2]));
//        analyseUserVo.setCarbohydrateTarget(extractNumber(split[3]));
//        analyseUserVo.setFatTarget(extractNumber(split[4]));
//        this.update().eq("id", loginUser.getFitnessId()).update(fitness);
//        return analyseUserVo;
//    }


    @Override
    public Long analyseUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Fitness fitness = this.getById(loginUser.getFitnessId());
        fitness.setStatus(0);
        redisLimiterUtils.doRateLimit("analseUser"+loginUser.getId());
        boolean result = this.updateById(fitness);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改fitness状态失败");
        }
        rabbitTemplate.convertAndSend(ANALYSE_USER_EXCHANGE, ANALYSE_USER_ROUTINGKEY, String.valueOf(loginUser.getFitnessId()));
        return fitness.getId();
    }


//    @Override
//    public AnalysePictureVo analysePicture(AiXinghuoPictureRequest request,HttpServletRequest userRequest) {
//        try {
//            System.out.println(request.getPictureUrl());
//            String s = aiPictureUtils.analyzeImage(request.getDescription(), request.getPictureUrl());
//            String[] split = s.split("【【【【");
//            AnalysePictureVo analysePictureVo = new AnalysePictureVo();
//            User loginUser = userService.getLoginUser(userRequest);
//            analysePictureVo.setFitnessId(loginUser.getFitnessId());
//            analysePictureVo.setPictureUrl(request.getPictureUrl());
//            analysePictureVo.setFoodName(request.getFoodName());
//            analysePictureVo.setCalorie(extractNumber(split[1]));
//            analysePictureVo.setProtein(extractNumber(split[2]));
//            analysePictureVo.setCarbohydrate(extractNumber(split[3]));
//            analysePictureVo.setFat(extractNumber(split[4]));
//            return  analysePictureVo;
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.getMessage());
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
//        }
//
//    }

    @Override
    public Long analysePicture(AiXinghuoPictureRequest request, HttpServletRequest userRequest) {
        FitnessImage fitnessImage = new FitnessImage();
        fitnessImage.setDescription(request.getDescription());
        fitnessImage.setPictureUrl(request.getPictureUrl());
        fitnessImage.setType(0);
        fitnessImage.setFitnessId(request.getFitnessId());
        fitnessImage.setFoodName(request.getFoodName());
        fitnessImage.setCount(1);
        User loginUser = userService.getLoginUser(userRequest);
        redisLimiterUtils.doRateLimit("analysePicture"+loginUser.getId());
        boolean save = fitnessImageService.save(fitnessImage);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败请稍后再试");
        }
        rabbitTemplate.convertAndSend(AI_PICTURE_EXCHANGE, AI_PICTURE_ROUTINGKEY, String.valueOf(fitnessImage.getId()));
        return fitnessImage.getId();
    }

    @Override
    public GetUserInfoVo getUserInfo(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Fitness fitness = new Fitness();
        if(loginUser.getFitnessId()==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"点击右下角加号，完善个人信息");
        }
        String key= FITNESS_USER_INFO +loginUser.getId();
        if(JdHotKeyStore.isHotKey(key)){
            Object object = JdHotKeyStore.get(key);
            if(object!=null){
                return JSONUtil.toBean(JSONUtil.toJsonStr(object), GetUserInfoVo.class);
            }
        }
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
            getUserInfoVo.setStatus(fitness.getStatus());
        }
        JdHotKeyStore.smartSet(key, getUserInfoVo);
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




