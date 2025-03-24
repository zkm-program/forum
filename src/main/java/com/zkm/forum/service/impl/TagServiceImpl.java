package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.TagMapper;
import com.zkm.forum.model.dto.tag.AddTagForAdminRequest;
import com.zkm.forum.model.dto.tag.UpdateTagsDeleteRequest;
import com.zkm.forum.model.entity.Tag;
import com.zkm.forum.model.vo.tag.ListTagsForAdminVo;
import com.zkm.forum.model.vo.tag.ListTagsForUserVo;
import com.zkm.forum.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 张凯铭
 * @description 针对表【tag(标签)】的数据库操作Service实现
 * @createDate 2025-03-21 17:05:10
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {
    @Resource
    TagMapper tagMapper;
    @Override
    public Boolean addOrUpdateTagForAdmin(AddTagForAdminRequest addTagForAdminRequest) {
        Tag tag = new Tag();
        BeanUtils.copyProperties(addTagForAdminRequest, tag);
        return this.saveOrUpdate(tag);


    }

    @Override
    public Boolean updateTagsDelete(UpdateTagsDeleteRequest updateTagsDeleteRequest) {
        List<Long> idList = updateTagsDeleteRequest.getIdList();
        List<Tag> tags = this.listByIds(idList);
        List<Integer> isParentList = tags.stream().map(Tag::getIsParent).collect(Collectors.toList());
        if (isParentList.contains(1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "批量修改存在父标签");
        }
        List<Tag> tagList = idList.stream().map(id -> Tag.builder()
                .id(id)
                .isDelete(updateTagsDeleteRequest.getIsDelete())
                .build()).collect(Collectors.toList());
        return tagMapper.updateBatchById(tagList);
    }

    @Override
    public List<ListTagsForAdminVo> listTagsForAdminVo() {
        QueryWrapper<Tag> parentQueryWrapper = new QueryWrapper<>();
        parentQueryWrapper.eq("isParent",1);
        List<Tag> parentTagList = this.list(parentQueryWrapper);
        List<ListTagsForAdminVo> listParentTagsForAdminVos = parentTagList.stream().map(parentTag -> ListTagsForAdminVo.builder()
                .tageName(parentTag.getTageName())
                .parentTagName(parentTag.getParentTagName())
                .createTime(parentTag.getCreateTime())
                .isParent(parentTag.getIsParent())
                .updateTime(parentTag.getUpdateTime()).build()).toList();
        QueryWrapper<Tag> childrenQueryWrapper = new QueryWrapper<>();
        childrenQueryWrapper.eq("isParent",0);
        List<Tag> childrenTagList = this.list(childrenQueryWrapper);
        List<ListTagsForAdminVo> listChildrenTagsForAdminVos = childrenTagList.stream().map(childrenTag -> ListTagsForAdminVo.builder()
                .tageName(childrenTag.getTageName())
                .parentTagName(childrenTag.getParentTagName())
                .createTime(childrenTag.getCreateTime())
                .isParent(childrenTag.getIsParent())
                .updateTime(childrenTag.getUpdateTime()).build()).toList();
        for(ListTagsForAdminVo listParentTagsForAdminVo:listParentTagsForAdminVos){
            listParentTagsForAdminVo.setList(listChildrenTagsForAdminVos.stream().filter(listChildrenTagsForAdminVo->listChildrenTagsForAdminVo.getParentTagName().equals(listParentTagsForAdminVo.getTageName())).toList());
        }

        return listParentTagsForAdminVos;
    }

    public List<ListTagsForUserVo> listTagsForUserVo(){
        QueryWrapper<Tag> parentQueryWrapper = new QueryWrapper<>();
        parentQueryWrapper.eq("isParent",1);
        List<Tag> parentTagList = this.list(parentQueryWrapper);
        List<ListTagsForUserVo> listParentTagsForUserVos = parentTagList.stream().map(parentTag -> ListTagsForUserVo.builder()
                .tageName(parentTag.getTageName())
                .parentTagName(parentTag.getParentTagName())
                .isParent(parentTag.getIsParent())
                .build()).toList();
        QueryWrapper<Tag> childrenQueryWrapper = new QueryWrapper<>();
        childrenQueryWrapper.eq("isParent",0);
        List<Tag> childrenTagList = this.list(childrenQueryWrapper);
        List<ListTagsForUserVo> listChildrenTagsForAdminVos = childrenTagList.stream().map(childrenTag -> ListTagsForUserVo.builder()
                .tageName(childrenTag.getTageName())
                .parentTagName(childrenTag.getParentTagName())
                .isParent(childrenTag.getIsParent())
                .build()).toList();
        for(ListTagsForUserVo listParentTagsForUserVo:listParentTagsForUserVos){
            listParentTagsForUserVo.setList(listChildrenTagsForAdminVos.stream().filter(listChildrenTagsForAdminVo->listChildrenTagsForAdminVo.getParentTagName().equals(listParentTagsForUserVo.getTageName())).toList());
        }
        return listParentTagsForUserVos;
    }

}




