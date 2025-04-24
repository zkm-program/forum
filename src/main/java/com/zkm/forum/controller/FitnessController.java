package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.fitness.AnalysePictureVo;
import com.zkm.forum.model.vo.fitness.AnalyseUserVo;
import com.zkm.forum.model.vo.fitness.GetUserInfoVo;
import com.zkm.forum.service.FitnessService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.strategy.context.UploadStrategyContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Api(tags = "ai+健身模块")
@RestController
@RequestMapping("/fitness")
public class FitnessController {
    @Resource
    private FitnessService fitnessService;
    @Resource
    private UploadStrategyContext uploadStrategyContext;
    @Resource
    private UserService userService;

    @ApiOperation("新增或修改基本信息")
    @PostMapping("/saveOrUpdate")
    public BaseResponse<Long> saveOrUpdateMessage(@RequestBody SaveOrUpdateMessageRequest saveOrUpdateMessageRequest, HttpServletRequest request) {
        System.out.println(fitnessService.saveOrUpdateMessage(saveOrUpdateMessageRequest, request));
        return ResultUtils.success(fitnessService.saveOrUpdateMessage(saveOrUpdateMessageRequest, request));
    }

    @ApiOperation("根据目标分析自己每日摄入多少卡路里")
    @GetMapping("/ai/analyseUser")
    public BaseResponse<AnalyseUserVo> analyseUser(HttpServletRequest request) {
        return ResultUtils.success(fitnessService.analyseUser(request));
    }

    @ApiOperation("根据图片分析卡路里脂肪蛋白质碳水含量")
    @PostMapping("/analysePicture")
    public BaseResponse<AnalysePictureVo> analysePicture(@RequestBody AiXinghuoPictureRequest request, HttpServletRequest userRequest) {
        return ResultUtils.success(fitnessService.analysePicture(request,userRequest));
    }

    @ApiOperation("获得用户基本信息")
    @GetMapping("/getUserInfo")
    public BaseResponse<GetUserInfoVo> getUserInfo(HttpServletRequest request) {
        return ResultUtils.success(fitnessService.getUserInfo(request));
    }

    @ApiOperation("上传食物图片进行分析")
    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile multipartFile, HttpServletRequest request) {
        // 获取当前登录用户的 ID
        User loginUser = userService.getLoginUser(request);
        Long loginUserId= loginUser.getId();
        // 拼接路径：fitness/loginUserId/
        String folderPath = "fitness/" + loginUserId + "/";
        return ResultUtils.success(uploadStrategyContext.executeUploadStrategy(multipartFile, folderPath));
    }
}
