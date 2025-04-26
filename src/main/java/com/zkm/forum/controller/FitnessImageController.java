package com.zkm.forum.controller;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.model.dto.fitnessImage.SaveFitnessImageRequest;
import com.zkm.forum.model.vo.fitnessImage.AnalysePictureVo;
import com.zkm.forum.model.vo.fitnessImage.SelectIfEatRequest;
import com.zkm.forum.service.FitnessImageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
@Api(tags = "食品图片模块")
@RestController
@RequestMapping("/fitness/image")
public class FitnessImageController {
    @Resource
    private FitnessImageService fitnessImageService;

    @ApiOperation("新增食品图片")
    @PostMapping("/save")
    public BaseResponse<Long> saveFitnessImage(@RequestBody SaveFitnessImageRequest fitnessImageRequest) {
        return ResultUtils.success(fitnessImageService.saveFitnessImage(fitnessImageRequest));
    }

    @ApiOperation("获得食品图片信息")
    @GetMapping("/getInfo/{time}")
    public BaseResponse<List<AnalysePictureVo>> getAnalysePictureVo(HttpServletRequest request,@PathVariable("time") int time){
        return ResultUtils.success(fitnessImageService.getAnalysePictureVo(request,time));
    }
    @ApiOperation("更新吃还是不吃状态")
    @PostMapping("/selectIfEat")
    public BaseResponse<Boolean> selectIfEat(@RequestBody SelectIfEatRequest selectIfEatRequest){
        boolean result = fitnessImageService.update().eq("id", selectIfEatRequest.getId()).setSql("status=" + selectIfEatRequest.getFlag()).update();
        if(!result){
            return ResultUtils.error(ErrorCode.OPERATION_ERROR,"操作失败");
        }
        return ResultUtils.success(result);
    }
}
