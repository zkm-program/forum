package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.entity.Fitness;
import com.zkm.forum.model.vo.fitness.AnalysePictureVo;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.model.vo.fitness.GetUserInfoVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 张凯铭
 * @description 针对表【fitness(健身表)】的数据库操作Service
 * @createDate 2025-04-07 17:25:30
 */
public interface FitnessService extends IService<Fitness> {
    Long saveOrUpdateMessage(SaveOrUpdateMessageRequest saveOrUpdateMessageRequest, HttpServletRequest request);

    AnalyseUserVo analyseUser(HttpServletRequest request);

    AnalysePictureVo analysePicture(AiXinghuoPictureRequest request, HttpServletRequest userRequest);
    GetUserInfoVo getUserInfo(HttpServletRequest request);
}
