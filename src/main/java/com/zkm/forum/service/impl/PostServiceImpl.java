package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.enums.UserRoleEnum;
import com.zkm.forum.service.PostService;
import com.zkm.forum.mapper.PostMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 张凯铭
 * @description 针对表【post(帖子)】的数据库操作Service实现
 * @createDate 2025-03-24 21:31:47
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {
    @Resource
    UserServiceImpl userServiceImpl;

    @Override
    public Boolean addPost(AddPostRequest addPostRequest, HttpServletRequest httpServletRequest) {
        Long id = addPostRequest.getId();
        String title = addPostRequest.getTitle();
        String content = addPostRequest.getContent();
        String password = addPostRequest.getPassword();
        Integer status = addPostRequest.getStatus();
        List<String> tags = addPostRequest.getTags();
        if (title.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题禁止超过20字");
        }
        if (content.length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能超过2000字");
        }
        if (status == 1 && (password.length() < 6 || password.length() > 12)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码必须大于6小于12");
        }
        Post post = new Post();
        BeanUtils.copyProperties(addPostRequest, post);
        String tagStr = JSONUtil.toJsonStr(tags);
        post.setTags(tagStr);
        if (id != 0) {
            User loginuser = userServiceImpl.getLoginuser(httpServletRequest);
            if (!loginuser.getId().equals(addPostRequest.getUserId()) || !loginuser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限");
            }
        }
        return this.saveOrUpdate(post);
    }
}

//private User loginUser(HttpServletRequest httpServletRequest) {
//    Object attribute = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
//
//}




