package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.AddPostRequest;
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
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
            //id是文章id
            List<Long> readPostIds = List.of();
            readPostIds.add(id);
            Long loginUserId = loginuser.getId();
            String userCommendKey = USER_RECOMMEND + loginUserId;
            String readKey = USER_POST_READ + loginUserId;
            Long expire = redisTemplate.getExpire(userCommendKey, TimeUnit.SECONDS);
            if (expire < 60) {
                List<String> tags = postVo.getTags();
                QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
                postQueryWrapper.in("tags", postVo.getTags())
                        .last("LIMIT 3").notIn("id", readPostIds);
                List<Post> recommendPostList = this.list(postQueryWrapper);
                List<PostVo> recommendPostVolist = recommendPostList.stream().map(recommendPost -> PostVo.builder()
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

            }

            redisTemplate.opsForSet().add(readKey, id);


        }, threadPoolExecutor);
        Double viewsCount = redisTemplate.opsForZSet().score("POST_VIEWS_COUNT", id);
        postVo.setViewCount(viewsCount);
        return postVo;
    }

    @Override
    public List<PostVo> getPrivateRecommend(Long userId, List<Long> viewedIds) {
        String readKey = USER_POST_READ + userId;
        if (viewedIds != null && !viewedIds.isEmpty()) {
            redisTemplate.opsForSet().add(readKey, viewedIds.toArray());
        }
        String recommendKey = "user:recommend:" + userId;
        List<Object> collect = redisTemplate.opsForList().range(recommendKey, 0, -1).stream()
                .filter(post -> !isRead(userId, redisTemplate.opsForSet().members(readKey)))
                .limit(3)
                .collect(Collectors.toList());
        //如果redis中的私人推荐过期了，则需要重新查询
        if (ObjectUtils.isEmpty(collect)|| collect.size()<3){

        }
            return List.of();
    }

    private boolean isRead(Long userId, Set<Object> set) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember("user:read:" + userId, set)
        );
    }

    private Double updatePostViewCount(Long id) {
        return redisTemplate.opsForZSet().incrementScore(POST_VIEWS_COUNT, id, 1);
    }
}





