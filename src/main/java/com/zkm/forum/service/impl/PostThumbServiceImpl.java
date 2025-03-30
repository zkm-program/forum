package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.postthumb.PostThumbRequest;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.PostThumb;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.service.PostService;
import com.zkm.forum.service.PostThumbService;
import com.zkm.forum.mapper.PostThumbMapper;
import com.zkm.forum.service.UserService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 张凯铭
 * @description 针对表【post_thumb(帖子点赞)】的数据库操作Service实现
 * @createDate 2025-03-29 20:24:59
 */
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
        implements PostThumbService {
    @Resource
    UserService userService ;

    @Resource
    PostService postService;

    @Override
    public Integer doPostThumb(PostThumbRequest postThumbRequest, HttpServletRequest request) {
        User loginuser = userService .getLoginUser(request);
        Long userId = postThumbRequest.getUserId();
        Long postId = postThumbRequest.getPostId();

        if (!loginuser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            if (!loginuser.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "没有权限");
            }
        }
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前文章不存在");
        }
        PostThumbService postThumbService = (PostThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postThumbService.doInnerPostThumb(postId, userId);
        }
    }

    @Override
    public Integer doInnerPostThumb(Long postId, Long userId) {
        PostThumb postThumb = new PostThumb();
        postThumb.setPostId(postId);
        postThumb.setUserId(userId);
        QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostThumb one = this.getOne(postThumbQueryWrapper);
        boolean flag;
        boolean result;
        if (one == null) {
            flag = this.save(postThumb);
            if (flag) {
                result = postService
                        .update()
                        .eq("postId", postId)
                        .setSql("thumbNum=thumbNum+1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统异常请稍后再试");
            }

        } else {
            flag = this.remove(postThumbQueryWrapper);
            if (flag) {
                result = postService
                        .update()
                        .eq("postId", postId)
                        .ge("thumbNum", 0)
                        .setSql("thumbNum=thumbNum-1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统异常请稍后再试");
            }
        }
    }
}




