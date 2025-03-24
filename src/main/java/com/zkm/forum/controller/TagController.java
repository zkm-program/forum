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
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Resource
    TagService tagService;

    @PostMapping("/addorupdate")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> saveOrUpdateTag(@RequestBody AddTagForAdminRequest addTagForAdmin) {
        return ResultUtils.success(tagService.addOrUpdateTagForAdmin(addTagForAdmin));
    }

    @PostMapping("/updateTagsDelete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateTagsDelete(@RequestBody UpdateTagsDeleteRequest updateTagsDeleteRequest){
        if(ObjectUtils.isEmpty(updateTagsDeleteRequest)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"选择要修改的标签");
        }
        return ResultUtils.success(tagService.updateTagsDelete(updateTagsDeleteRequest));
    }
    @GetMapping("/listTagsForAdminVo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<List<ListTagsForAdminVo>> listTagsForAdminVo(){
        return ResultUtils.success(tagService.listTagsForAdminVo());
   }
   @GetMapping("/listTagsForUserVo")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<ListTagsForUserVo>> listTagsForUserVo(){
        return ResultUtils.success(tagService.listTagsForUserVo());
    }
}
