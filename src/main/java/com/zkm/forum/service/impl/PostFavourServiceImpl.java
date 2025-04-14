package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.PostFavourMapper;
import com.zkm.forum.model.dto.postfavour.PostFavourRequest;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.PostFavour;
import com.zkm.forum.model.entity.User;

import com.zkm.forum.model.vo.postFavour.ListMyPostFavourVo;
import com.zkm.forum.service.PostFavourService;
import com.zkm.forum.service.PostService;
import com.zkm.forum.service.UserService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 张凯铭
 * @description 针对表【post_favour(帖子收藏)】的数据库操作Service实现
 * @createDate 2025-03-29 18:04:53
 */
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    UserService userService;
    @Resource
    PostService postService;
    // todo 有这个会有循环依赖报错。循环依赖是什么？
//    @Resource
//    PostFavourService postFavourService;

    @Override
    public Integer doPostFavour(PostFavourRequest postFavourRequest, HttpServletRequest request) {
        User loginuser = userService.getLoginUser(request);
        Long postId = postFavourRequest.getPostId();
        Long userId = loginuser.getId();
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该帖子不存在");
        }

        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    @Override
    @Transactional
    public Integer doPostFavourInner(Long userId, Long postId) {
        PostFavour postFavour = new PostFavour();
        postFavour.setPostId(postId);
        postFavour.setUserId(userId);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour oldPostFavour = this.getOne(postFavourQueryWrapper);
        boolean flag;
        boolean result;
        if (oldPostFavour == null) {
            flag = this.save(postFavour);
            if (flag) {
                result = postService.update()
                        .eq("id", postId)
                        .setSql("favourNum=favourNum+1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常请稍后再试");
            }
        } else {
            flag = this.remove(postFavourQueryWrapper);
            if (flag) {
                result = postService.update()
                        .eq("id", postId)
                        .ge("favourNum", 0)
                        .setSql("favourNum=favourNum-1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常请稍后再试");
            }
        }


//        return 0;
    }


    @Override
    public List<ListMyPostFavourVo> listMyPostFavourVo(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
        postFavourQueryWrapper.select("postId");
        postFavourQueryWrapper.eq("postId",loginUser.getId());
        List<PostFavour> postFavourlist = this.list(postFavourQueryWrapper);
        List<Long> postIdList = postFavourlist.stream().map(PostFavour::getPostId).toList();
        List<ListMyPostFavourVo> listMyPostFavourVos = postService.listByIds(postIdList).stream().map(this::postToListMyPostFavourVo).toList();
        return listMyPostFavourVos;
    }
    private ListMyPostFavourVo postToListMyPostFavourVo(Post post){
        ListMyPostFavourVo listMyPostFavourVo = new ListMyPostFavourVo();
        listMyPostFavourVo.setPostId(post.getId());
        listMyPostFavourVo.setTitle(post.getTitle());
        listMyPostFavourVo.setTags(JSONUtil.toList(post.getTags(),String.class));
        listMyPostFavourVo.setFavourNum(post.getFavourNum());
        return listMyPostFavourVo;
    }

}




