package com.zkm.forum.service;

import com.zkm.forum.model.dto.postthumb.PostThumbRequest;
import com.zkm.forum.model.entity.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 张凯铭
* @description 针对表【post_thumb(帖子点赞)】的数据库操作Service
* @createDate 2025-03-29 20:24:59
*/
public interface PostThumbService extends IService<PostThumb> {
    Integer doPostThumb(PostThumbRequest postThumbRequest, HttpServletRequest request);
    Integer doInnerPostThumb(Long postId,Long userId);
}
