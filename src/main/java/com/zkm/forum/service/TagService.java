package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.tag.AddTagForAdminRequest;
import com.zkm.forum.model.dto.tag.UpdateTagsDeleteRequest;
import com.zkm.forum.model.entity.Tag;
import com.zkm.forum.model.vo.tag.ListTagsForAdminVo;
import com.zkm.forum.model.vo.tag.ListTagsForUserVo;

import java.util.List;

/**
* @author 张凯铭
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2025-03-21 17:05:10
*/
public interface TagService extends IService<Tag> {
    Boolean addOrUpdateTagForAdmin(AddTagForAdminRequest addTagForAdminRequest);
    Boolean updateTagsDelete(UpdateTagsDeleteRequest updateTagsDeleteRequest);
    List<ListTagsForUserVo> listTagsForUserVo();
    List<ListTagsForAdminVo> listTagsForAdminVo();
}
