package com.zkm.forum.mapper;

import com.zkm.forum.model.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【post(帖子)】的数据库操作Mapper
* @createDate 2025-03-24 21:31:47
* @Entity com.zkm.forum.model.entity.Post
*/
public interface PostMapper extends BaseMapper<Post> {

    @Update("update post set isDelete=#{isDelete} where id=#{id}")
    Boolean updateDeleteById(@Param("id") Long id,@Param("isDelete") Integer isDelete);
    @Select("select * from post where isDelete=0 and updateTime>#{date}")
    List<Post> lsitPostWithTime(@Param("date") Date date);

}




