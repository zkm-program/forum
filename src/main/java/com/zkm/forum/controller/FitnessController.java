package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.service.FitnessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "ai+健身模块")
@RestController
@RequestMapping("/fitness")
public class FitnessController {
    @Resource
    private FitnessService fitnessService;

    @ApiOperation("新增或修改基本信息")
    @PostMapping("/saveOrUpdate")
    public BaseResponse<Boolean> saveOrUpdateMessage(@RequestBody SaveOrUpdateMessageRequest saveOrUpdateMessageRequest, HttpServletRequest request) {
        return ResultUtils.success(fitnessService.saveOrUpdateMessage(saveOrUpdateMessageRequest, request));
    }

    @ApiOperation("根据目标分析自己每日摄入多少卡路里")
    @PostMapping("/ai/analyseUser")
    public BaseResponse<AnalyseUserVo> analyseUser(HttpServletRequest request) {
        return ResultUtils.success(fitnessService.analyseUser(request));
    }
    @ApiOperation("根据图片分析卡路里脂肪蛋白质碳水含量")
    @PostMapping("/analysePicture")
    public BaseResponse<String> analysePicture(AiXinghuoPictureRequest request) {
        return ResultUtils.success(fitnessService.analysePicture(request));
    }
}
