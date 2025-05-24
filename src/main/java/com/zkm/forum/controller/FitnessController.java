package com.zkm.forum.controller;

import cn.hutool.core.io.file.FileNameUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.aixinghuo.AiXinghuoPictureRequest;
import com.zkm.forum.model.dto.fitness.SaveOrUpdateMessageRequest;
import com.zkm.forum.model.dto.user.UploadCosWanXiangRequest;
import com.zkm.forum.model.entity.User;

import com.zkm.forum.model.vo.fitness.GetUserInfoVo;
import com.zkm.forum.model.vo.fitnessImage.AnalysePictureVo;
import com.zkm.forum.service.FitnessService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.strategy.context.UploadStrategyContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

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
        return ResultUtils.success(fitnessService.saveOrUpdateMessage(saveOrUpdateMessageRequest, request));
    }

    // todo 熔断期间，处理措施【可以返回空或者？】
    @SentinelResource(value = "analyseUser")
    @ApiOperation("根据目标分析自己每日摄入多少卡路里")
    @GetMapping("/ai/analyseUser")
    public BaseResponse<Long> analyseUser(HttpServletRequest request) {
        return ResultUtils.success(fitnessService.analyseUser(request));
    }

    @SentinelResource(value = "analysePicture")
    @ApiOperation("根据图片分析卡路里脂肪蛋白质碳水含量")
    @PostMapping("/analysePicture")
    public BaseResponse<Long> analysePicture(@RequestBody AiXinghuoPictureRequest request, HttpServletRequest userRequest) {
        return ResultUtils.success(fitnessService.analysePicture(request,userRequest));
    }
//    public BaseResponse<AnalysePictureVo> analysePicture(@RequestBody AiXinghuoPictureRequest request, HttpServletRequest userRequest) {
//        return ResultUtils.success(fitnessService.analysePicture(request,userRequest));
//    }
//    public BaseResponse<AnalysePictureVo> analysePicture(@RequestBody AiXinghuoPictureRequest request, HttpServletRequest userRequest) {
//        return ResultUtils.success(fitnessService.analysePicture(request,userRequest));
//    }

    @ApiOperation("获得用户基本信息")
    @GetMapping("/getUserInfo")
    public BaseResponse<GetUserInfoVo> getUserInfo(HttpServletRequest request) {
        return ResultUtils.success(fitnessService.getUserInfo(request));
    }


    @ApiOperation("上传食物图片进行分析")
    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile multipartFile, HttpServletRequest request) {
        final long ONE_M = 1024 * 1024L;
        long multipartFileSize = multipartFile.getSize();
        if (multipartFileSize > ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不能大于1M");
        }
        String suffix = FileNameUtil.getSuffix(multipartFile.getOriginalFilename());
        if (!Arrays.asList("jpg", "png", "jpeg").contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片格式错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 修改：在路径末尾添加随机数，使用Math.random生成6位随机数
        String path = "fitnessImage/" + loginUser.getId() + "/" + (int)(Math.random() * 1000000) + "/";
        UploadCosWanXiangRequest uploadCosWanXiangRequest = new UploadCosWanXiangRequest();
        uploadCosWanXiangRequest.setMultipartFile(multipartFile);
//        uploadCosWanXiangRequest.setLoginUser(loginUser);
        uploadCosWanXiangRequest.setPath(path);
        // 拼接路径：fitness/loginUserId/
        String folderPath = "fitness/" + loginUser.getId() + "/";
        return ResultUtils.success(fitnessService.uploadCosWanXiang(uploadCosWanXiangRequest));
    }
    // todo 修改成缩略压缩模式，且只能上传一张
//    @ApiOperation("上传食物图片进行分析")
//    @PostMapping("/upload")
//    public BaseResponse<String> upload(MultipartFile multipartFile, HttpServletRequest request) {
//        final long ONE_M = 1024 * 1024L;
//        long multipartFileSize = multipartFile.getSize();
//        if(multipartFileSize>ONE_M){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"图片不能大于1M");
//        }
//        String suffix = FileNameUtil.getSuffix(multipartFile.getOriginalFilename());
//        if(!Arrays.asList("jpg","png","jpeg").contains(suffix)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"图片格式错误");
//        }
//        // 获取当前登录用户的 ID
//        User loginUser = userService.getLoginUser(request);
//        Long loginUserId= loginUser.getId();
//        // 拼接路径：fitness/loginUserId/
//        String folderPath = "fitness/" + loginUserId + "/";
//        return ResultUtils.success(uploadStrategyContext.executeUploadStrategy(multipartFile, folderPath));
//    }
}
