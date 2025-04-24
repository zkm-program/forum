package com.zkm.forum.controller;

import com.zkm.forum.annotation.AuthCheck;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.tag.AddTagForAdminRequest;
import com.zkm.forum.model.dto.tag.UpdateTagsDeleteRequest;
import com.zkm.forum.model.vo.tag.ListTagsForAdminVo;
import com.zkm.forum.model.vo.tag.ListTagsForUserVo;
import com.zkm.forum.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
@Api(tags = "标签管理模块")
@RestController
@RequestMapping("/tag")
public class TagController {
    @Resource
    TagService tagService;

    @ApiOperation("新增或修改标签")
    @PostMapping("/addorupdate")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> saveOrUpdateTag(@RequestBody AddTagForAdminRequest addTagForAdmin) {
        return ResultUtils.success(tagService.addOrUpdateTagForAdmin(addTagForAdmin));
    }

    @ApiOperation("逻辑删除标签")
    @PostMapping("/updateTagsDelete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateTagsDelete(@RequestBody UpdateTagsDeleteRequest updateTagsDeleteRequest){
        if(ObjectUtils.isEmpty(updateTagsDeleteRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"选择要修改的标签");
        }
        return ResultUtils.success(tagService.updateTagsDelete(updateTagsDeleteRequest));
    }
    // todo 下面这个接口可以加个缓存
    @ApiOperation("查看已有的标签")
    @GetMapping("/listTagsForAdminVo")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<List<ListTagsForAdminVo>> listTagsForAdminVo(){
        return ResultUtils.success(tagService.listTagsForAdminVo());
   }
    @ApiOperation("用户查看自己设置的标签")
   @GetMapping("/listTagsForUserVo")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<ListTagsForUserVo>> listTagsForUserVo(){
        return ResultUtils.success(tagService.listTagsForUserVo());
    }
}
