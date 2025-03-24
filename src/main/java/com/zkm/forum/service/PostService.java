package com.zkm.forum.service;

import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 张凯铭
* @description 针对表【post(帖子)】的数据库操作Service
* @createDate 2025-03-24 21:31:47
*/
public interface PostService extends IService<Post> {
    Boolean addPost(AddPostRequest addPostRequest);

}
