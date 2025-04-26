package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.fitnessImage.SaveFitnessImageRequest;
import com.zkm.forum.model.entity.FitnessImage;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.fitnessImage.AnalysePictureVo;
import com.zkm.forum.service.FitnessImageService;
import com.zkm.forum.mapper.FitnessImageMapper;
import com.zkm.forum.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 张凯铭
 * @description 针对表【fitness_image(食品图片表)】的数据库操作Service实现
 * @createDate 2025-04-24 10:31:23
 */
@Service
public class FitnessImageServiceImpl extends ServiceImpl<FitnessImageMapper, FitnessImage>
        implements FitnessImageService {

    @Resource
    private UserService userService;
    @Override
    public Long saveFitnessImage(SaveFitnessImageRequest fitnessImageRequest) {
        FitnessImage fitnessImage = new FitnessImage();
        fitnessImage.setCount(fitnessImageRequest.getCount());
        fitnessImage.setFitnessId(fitnessImageRequest.getFitnessId());
        fitnessImage.setPictureUrl(fitnessImageRequest.getPictureUrl());
        fitnessImage.setFoodName(fitnessImageRequest.getFoodName());
        fitnessImage.setCalorie(fitnessImageRequest.getCalorie());
        fitnessImage.setProtein(fitnessImageRequest.getProtein());
        fitnessImage.setCarbohydrate(fitnessImageRequest.getCarbohydrate());
        fitnessImage.setFat(fitnessImageRequest.getFat());
        fitnessImage.setStatus(fitnessImageRequest.getStatus());
        boolean result = this.save(fitnessImage);
        if(!result){
            // todo 如果插入失败了可以使用mq 过段时间再重新插入，专门设置个存储操作失败信息的队列
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"操作失败请稍后再试");
        }
        return fitnessImage.getId();
    }

    @Override
    public List<AnalysePictureVo> getAnalysePictureVo(HttpServletRequest request,int time) {

        User loginUser = userService.getLoginUser(request);
        QueryWrapper<FitnessImage> fitnessImageQueryWrapper = new QueryWrapper<>();
        //time 0代表今天
        if(time==0){
            // 今天开始时间（00:00:00）
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            // 今天结束时间（23:59:59）
            LocalDateTime todayEnd = todayStart.plusDays(1).minusSeconds(1);
            fitnessImageQueryWrapper.between("createTime", todayStart, todayEnd);
        }else{
            LocalDateTime yesterdayStart = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1);
            fitnessImageQueryWrapper.between("createTime", yesterdayStart, yesterdayEnd);
        }
        fitnessImageQueryWrapper.eq("fitnessId",loginUser.getFitnessId());
        List<FitnessImage> fitnessImageList = this.list(fitnessImageQueryWrapper);
        List<AnalysePictureVo> analysePictureVoList = fitnessImageList.stream().map(this::converToAnalysePictureVo).toList();
        return analysePictureVoList;

    }
    private AnalysePictureVo converToAnalysePictureVo(FitnessImage fitnessImage){
        AnalysePictureVo analysePictureVo = new AnalysePictureVo();
        analysePictureVo.setId(fitnessImage.getId());
        analysePictureVo.setFitnessId(fitnessImage.getFitnessId());
        analysePictureVo.setPictureUrl(fitnessImage.getPictureUrl());
        analysePictureVo.setFoodName(fitnessImage.getFoodName());
        analysePictureVo.setCalorie(fitnessImage.getCalorie());
        analysePictureVo.setProtein(fitnessImage.getProtein());
        analysePictureVo.setCarbohydrate(fitnessImage.getCarbohydrate());
        analysePictureVo.setFat(fitnessImage.getFat());
        analysePictureVo.setCreateTime(fitnessImage.getCreateTime());
        analysePictureVo.setCount(fitnessImage.getCount());
        analysePictureVo.setStatus(fitnessImage.getStatus());
        analysePictureVo.setDescription(fitnessImage.getDescription());
        analysePictureVo.setType(fitnessImage.getType());
        return analysePictureVo;
    }


}




