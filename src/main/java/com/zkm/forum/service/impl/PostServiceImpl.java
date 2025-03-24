package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.Tag;
import com.zkm.forum.service.PostService;
import com.zkm.forum.mapper.PostMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 张凯铭
* @description 针对表【post(帖子)】的数据库操作Service实现
* @createDate 2025-03-24 21:31:47
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

    @Override
    public Boolean addPost(AddPostRequest addPostRequest) {
        String title = addPostRequest.getTitle();
        String content = addPostRequest.getContent();
        String password = addPostRequest.getPassword();
        if(title.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题禁止超过20字");
        }
        if(content.length()>2000){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"内容不能超过2000字");
        }
        if(password.length()<6|| password.length()>12){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码必须大于6小于12");
        }
        Post post = new Post();
        BeanUtils.copyProperties(addPostRequest,post);
        return this.saveOrUpdate(post);
    }
}




