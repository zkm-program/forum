package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
import com.zkm.forum.model.dto.post.GetPrivateRecommendRequest;
import com.zkm.forum.model.dto.post.PostSearchRequest;
import com.zkm.forum.model.dto.post.UpdatePostDeleteForMy;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.enums.UserRoleEnum;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.service.PostService;
import com.zkm.forum.mapper.PostMapper;
import com.zkm.forum.strategy.context.SearchStrategyContext;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zkm.forum.constant.RedisConstant.*;

/**
 * @author 张凯铭
 * @description 针对表【post(帖子)】的数据库操作Service实现
 * @createDate 2025-03-24 21:31:47
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {
    @Resource
    private UserServiceImpl userServiceImpl;
    @Resource
    private PostMapper postMapper;
    @Resource
    private SearchStrategyContext searchStrategyContext;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            20,
            50,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(10000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

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

    @Override
    public Boolean updatePostDeletForMy(UpdatePostDeleteForMy updatePostDeleteForMy, HttpServletRequest httpServletRequest) {
        User loginuser = userServiceImpl.getLoginuser(httpServletRequest);
        Long id = updatePostDeleteForMy.getId();
        Long userId = updatePostDeleteForMy.getUserId();
        Integer isDelete = updatePostDeleteForMy.getIsDelete();
        if (!loginuser.getId().equals(userId)) {
            if (!loginuser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限");
            }
        }

        return postMapper.updateDeleteById(id, isDelete);
    }

    @Override
    public List<PostSearchVo> searchPost(PostSearchRequest postSearchRequest) {
        return searchStrategyContext.excuteSearchStrategy(postSearchRequest.getKeyWords());
    }

    @Override
    public PostVo getPostById(Long id, HttpServletRequest httpServletRequest) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该文章不存在");
        }
        PostVo postVo = new PostVo();
        Post post = this.getById(id);
        BeanUtils.copyProperties(post, postVo);
        postVo.setTags(JSONUtil.toList(JSONUtil.parseArray(post.getTags()), String.class));
        updatePostViewCount(id);
        CompletableFuture.runAsync(() -> {
            User loginuser = userServiceImpl.getLoginuser(httpServletRequest);
            Long loginUserId = loginuser.getId();
            String userCommendKey = USER_RECOMMEND + loginUserId;
            String readKey = USER_POST_READ + loginUserId;
            Set<Object> readPostIds = redisTemplate.opsForSet().members(readKey);
//            List<Long> ;
            //id是文章id
            if (readPostIds == null) {
                readPostIds = new HashSet<Object>();
                readPostIds.add(id);
            }
            Long expire = redisTemplate.getExpire(userCommendKey, TimeUnit.SECONDS);
            if (expire < 60) {
                getPostCommendVoForRedis(userCommendKey, postVo.getTags(), readPostIds);
            }
            redisTemplate.opsForSet().add(readKey, id, readPostIds);
        }, threadPoolExecutor);
        Double viewsCount = redisTemplate.opsForZSet().score("POST_VIEWS_COUNT", id);
        postVo.setViewCount(viewsCount);
        return postVo;
    }

    private List<PostVo> getPostCommendVoForRedis(String userCommendKey, List<String> tags, Set<Object> readPostIds) {

        List<PostVo> recommendPostVolist = null;
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.in("tags", tags)
                .last("LIMIT 3").notIn("id", readPostIds);
        List<Post> recommendPostList = this.list(postQueryWrapper);
        recommendPostVolist = recommendPostList.stream().map(recommendPost -> PostVo.builder()
                .is_featured(recommendPost.getIs_featured())
                .is_top(recommendPost.getIs_top())
                .original_url(recommendPost.getOriginal_url())
                .createTime(recommendPost.getCreateTime())
                .favourNum(recommendPost.getFavourNum())
                .content(recommendPost.getContent())
                .tags(JSONUtil.toList(JSONUtil.parseArray(recommendPost.getTags()), String.class))
                .status(recommendPost.getStatus())
                .thumbNum(recommendPost.getThumbNum())
                .title(recommendPost.getTitle())
                .id(recommendPost.getId()).build()).toList();
        redisTemplate.opsForList().rightPush(userCommendKey, recommendPostVolist);
        redisTemplate.expire(userCommendKey, 5, TimeUnit.MINUTES);

        return recommendPostVolist;
    }


    @Override
    public List<PostVo> getPrivateRecommend(Long userId, GetPrivateRecommendRequest getPrivateRecommendRequest) {
        String readKey = USER_POST_READ + userId;
        List<Long> viewedIds = getPrivateRecommendRequest.getViewedIds();
        List<PostVo> postCommendVoForRedis;
        List<String> tags = getPrivateRecommendRequest.getTags();
        if (viewedIds != null && !viewedIds.isEmpty()) {
            redisTemplate.opsForSet().add(readKey, viewedIds);
        }
        String userCommendKey = USER_RECOMMEND + userId;
        Set<Object> readPostIds = redisTemplate.opsForSet().members(readKey);
        String recommendKey = "user:recommend:" + userId;
        Long expire = redisTemplate.getExpire(recommendKey, TimeUnit.SECONDS);
        if (expire < 60) {
            postCommendVoForRedis = getPostCommendVoForRedis(recommendKey, tags, readPostIds);
            return postCommendVoForRedis;
        }
        return listObjectToPostVo(Objects.requireNonNull(redisTemplate.opsForList().range(recommendKey, 0, -1)));
        //如果redis中的私人推荐过期了，则需要重新查询
    }

    private boolean isRead(Long userId, Set<Object> set) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember("user:read:" + userId, set)
        );
    }

    private Double updatePostViewCount(Long id) {
        return redisTemplate.opsForZSet().incrementScore(POST_VIEWS_COUNT, id, 1);
    }

    private static List<PostVo> listObjectToPostVo(List<Object> list) {
        List<PostVo> postVoList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof PostVo) {
                PostVo postVo = (PostVo) obj;
                postVoList.add(postVo);
            } else {
                throw new IllegalArgumentException("List contains non-PostVo elements");
            }
        }
        return postVoList;
    }
}





