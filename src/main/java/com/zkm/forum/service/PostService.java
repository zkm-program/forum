package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.model.dto.post.*;
import com.zkm.forum.model.entity.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.post.PostVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 张凯铭
 * @description 针对表【post(帖子)】的数据库操作Service
 * @createDate 2025-03-24 21:31:47
 */
public interface PostService extends IService<Post> {
    Boolean addPost(AddPostRequest addPostRequest, HttpServletRequest httpServletRequest);

    Boolean updatePostDeletForMy(UpdatePostDeleteForMy updatePostDeleteForMy, HttpServletRequest httpServletRequest);

    List<PostSearchVo> searchPost(PostSearchRequest postSearchRequest);

    PostVo getPostById(Long id,HttpServletRequest httpServletRequest);
    List<PostVo> getPrivateRecommend(Long userId, GetPrivateRecommendRequest getPrivateRecommendRequest);

    Page<Post> listPostForAdmin(PostQueryRequest postQueryRequest);
    Boolean reportPost(ReportPostRequest reportPostRequest, HttpServletRequest request);

}
