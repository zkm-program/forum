package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.service.FitnessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/fitness")
public class FitnessController {
    @Resource
    private FitnessService fitnessService;

    @PostMapping("/saveOrUpdate")
    public BaseResponse<Boolean> saveOrUpdateMessage(@RequestBody SaveOrUpdateMessageRequest saveOrUpdateMessageRequest, HttpServletRequest request) {
        return ResultUtils.success(fitnessService.saveOrUpdateMessage(saveOrUpdateMessageRequest, request));
    }

    @PostMapping("/ai/analyseUser")
    public BaseResponse<AnalyseUserVo> analyseUser(HttpServletRequest request) {
        return ResultUtils.success(fitnessService.analyseUser(request));
    }

    @PostMapping("/analysePicture")
    public BaseResponse<String> analysePicture(AiXinghuoPictureRequest request) {
        return ResultUtils.success(fitnessService.analysePicture(request));
    }
}
