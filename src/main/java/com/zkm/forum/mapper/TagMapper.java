package com.zkm.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zkm.forum.model.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 张凯铭
* @description 针对表【tag(标签)】的数据库操作Mapper
* @createDate 2025-03-21 17:05:10
* @Entity com.zkm.com.zkm.forum.model.entity.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {

    Boolean updateBatchById(@Param("list") List<Tag> tagList);
}




