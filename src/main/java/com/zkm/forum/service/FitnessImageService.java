package com.zkm.forum.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.fitnessImage.SaveFitnessImageRequest;
import com.zkm.forum.model.entity.FitnessImage;
import com.zkm.forum.model.vo.fitnessImage.AnalysePictureVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【fitness_image(食品图片表)】的数据库操作Service
* @createDate 2025-04-24 10:31:23
*/
public interface FitnessImageService extends IService<FitnessImage> {
    Long saveFitnessImage(SaveFitnessImageRequest fitnessImageRequest);
    List<AnalysePictureVo> getAnalysePictureVo(HttpServletRequest request,int time);
}
