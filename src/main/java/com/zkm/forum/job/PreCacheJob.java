package com.zkm.forum.job;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.service.PostService;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.zkm.forum.constant.LocalCacheConstant.USERID_USERNAME;
import static com.zkm.forum.constant.RedisConstant.PRE_CACHE_POST_LOGIN;
import static com.zkm.forum.constant.RedisConstant.PRE_CACHE_POST_LOGOUT;

@Component
public class PreCacheJob {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PostService postService;
    @Resource
    private Cache<String,String> LOCAL_CACHE;

    @Scheduled(cron = "0 31 0 * * *")
    public void doCacheRecommendPost() {
        LocalDateTime yesterdayStart = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1);

        QueryWrapper<Post> postLoginQueryWrapper = new QueryWrapper<>();
        postLoginQueryWrapper
                // 筛选昨天创建的帖子
                .between("create_time", yesterdayStart, yesterdayEnd)
                // 按(点赞数+收藏数)的总和降序排序
                .orderByDesc("(thumb_num + favour_num)")
                // 限制15条
                .last("LIMIT 15");
        List<Post> preLoginPostList = postService.list(postLoginQueryWrapper);
        stringRedisTemplate.opsForList().rightPush(PRE_CACHE_POST_LOGIN, JSONUtil.toJsonStr(postToPostVo(preLoginPostList)));
        QueryWrapper<Post> postLogoutQueryWrapper = new QueryWrapper<>();
        postLogoutQueryWrapper.orderByDesc("(thumb_num + favour_num)")
                // 限制15条
                .last("LIMIT 15");
        List<Post> preLogoutPostList = postService.list(postLogoutQueryWrapper);
        stringRedisTemplate.opsForList().rightPush(PRE_CACHE_POST_LOGOUT, JSONUtil.toJsonStr(postToPostVo(preLogoutPostList)));
    }

    private List<PostVo> postToPostVo(List<Post> postList) {
        return postList.stream().map(post -> PostVo.builder()
                .title(post.getTitle())
                .tags(JSONUtil.toList(post.getTags(),String.class))
                .content(post.getContent())
                .status(post.getStatus())
                .id(post.getId())
                .thumbNum(post.getThumbNum())
                .favourNum(post.getFavourNum())
                .authorName(LOCAL_CACHE.getIfPresent(USERID_USERNAME))
                .original_url(post.getOriginal_url())
                .is_top(post.getIs_top())
                .is_featured(post.getIs_featured())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .article_abstract(post.getArticle_abstract()).build()).toList();
    }
}
