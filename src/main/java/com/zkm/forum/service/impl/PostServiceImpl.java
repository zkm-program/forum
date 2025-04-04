package com.zkm.forum.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.constant.CommonConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.*;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.enums.UserRoleEnum;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.service.PostService;
import com.zkm.forum.mapper.PostMapper;
import com.zkm.forum.service.UserService;
import com.zkm.forum.strategy.context.SearchStrategyContext;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.*;
import org.springframework.beans.BeanUtils;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.print.DocFlavor;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
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
@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    @Resource
    private UserService userService;
    @Resource
    private PostMapper postMapper;
    @Resource
    private SearchStrategyContext searchStrategyContext;
    @Resource
    private StringRedisTemplate strngredisTemplate;
    @Resource
    private Cache<String, String> LOCAL_CACHE;

    //加上这个会报循环依赖错误
//    @Resource
//    private PostService postService;
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10000), new ThreadPoolExecutor.CallerRunsPolicy());
    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

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
            User loginuser = userService.getLoginUser(httpServletRequest);
            if (!loginuser.getId().equals(addPostRequest.getUserId()) || !loginuser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限");
            }
        }
        return this.saveOrUpdate(post);
    }

    @Override
    public Boolean updatePostDeletForMy(UpdatePostDeleteForMy updatePostDeleteForMy, HttpServletRequest httpServletRequest) {
        User loginuser = userService.getLoginUser(httpServletRequest);
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
        User loginuser = userService.getLoginUser(httpServletRequest);
        CompletableFuture.runAsync(() -> {
            try {
                Long loginUserId = loginuser.getId();
                String userCommendKey = USER_RECOMMEND + loginUserId;
                String readKey = USER_POST_READ + loginUserId;
                Set<String> readPostIds = Objects.requireNonNull(strngredisTemplate.opsForSet().members(readKey)).stream().map(JSONUtil::toJsonStr).collect(Collectors.toSet());
//            List<Long> ;
                //id是文章id
                if (readPostIds.isEmpty()) {
                    readPostIds = new HashSet<String>();
                    readPostIds.add(String.valueOf(id));
                }
                Long expire = strngredisTemplate.getExpire(userCommendKey, TimeUnit.SECONDS);
                if (expire < 60) {
                    getPostCommendVoForRedis(userCommendKey, postVo.getTags().get(0), readPostIds);
                }
                strngredisTemplate.opsForSet().add(readKey, String.valueOf(id), JSONUtil.toJsonStr(readPostIds));
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }

        }, threadPoolExecutor);
        Double viewsCount = strngredisTemplate.opsForZSet().score("POST_VIEWS_COUNT", String.valueOf(id));
        postVo.setViewCount(viewsCount);
        return postVo;
    }

    @Override
    public List<PostVo> getPrivateRecommend(Long userId, GetPrivateRecommendRequest getPrivateRecommendRequest) {
        String readKey = USER_POST_READ + userId;
        List<Long> viewedIds = getPrivateRecommendRequest.getViewedIds();
        List<PostVo> postCommendVoForRedis;
        List<String> tags = getPrivateRecommendRequest.getTags();
        if (viewedIds != null && !viewedIds.isEmpty()) {
            for (Long viewedId : viewedIds) {
                try {
                    strngredisTemplate.opsForSet().add(readKey, String.valueOf(viewedId));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        String userCommendKey = USER_RECOMMEND + userId;
        try {
            Set<String> readPostIds = strngredisTemplate.opsForSet().members(readKey).stream().map(obj -> JSONUtil.toJsonStr(obj)).collect(Collectors.toSet());
            Long expire = strngredisTemplate.getExpire(userCommendKey, TimeUnit.SECONDS);
            if (expire < 60) {
                postCommendVoForRedis = getPostCommendVoForRedis(userCommendKey, tags.get(0), readPostIds);
                return postCommendVoForRedis;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        List<PostVo> postVos = new ArrayList<>();
        List<String> range = strngredisTemplate.opsForList().range(userCommendKey, 0, -1).stream().map(obj -> JSONUtil.toJsonStr(obj)) // 直接转换（因配置了JSON序列化）
                .collect(Collectors.toList());
        postVos = range.stream().map(obj -> JSONUtil.toBean(obj, PostVo.class)).collect(Collectors.toList());
        return postVos;
//        return redisUtils.getList(userCommendKey, PostVo.class);
    }


    @Override
    public Page<Post> listPostForAdmin(PostQueryRequest postQueryRequest) {
        int current = postQueryRequest.getCurrent();
        int pageSize = postQueryRequest.getPageSize();
        Page<Post> postPage = new Page<>(current, pageSize);
        return this.page(postPage, getQuerWrapper(postQueryRequest));
    }

    @Override
    public Boolean reportPost(ReportPostRequest reportPostRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        boolean result;
        synchronized (String.valueOf(userId).intern()) {
            Post post = this.getById(reportPostRequest.getPostId());
            if (post.getIsReported() == 1) {
                return true;
            } else {
                post.setIsReported(1);
                post.setReportResults(reportPostRequest.getReportedResults());
                post.setReportUserId(reportPostRequest.getReportUserId());
                result = this.updateById(post);
            }
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统繁忙，请稍后再试");
            }
            return result;
        }
    }

    // todo 加到策略模式里面，同时返回样式中针对用户的搜索词进行加粗标识
    @Override
    public Page<PostSearchVo> searchEsPost(PostQueryRequest postQueryRequest) {
        List<String> orTags = postQueryRequest.getOrTags();
        String keyWords = postQueryRequest.getKeyWords();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        List<String> tags = postQueryRequest.getTags();
        String content = postQueryRequest.getContent();
        String userName = postQueryRequest.getUserName();
        Integer thumbNum = postQueryRequest.getThumbNum();
        Integer favourNum = postQueryRequest.getFavourNum();
        Long userId = postQueryRequest.getUserId();
        int isReported = postQueryRequest.getIsReported();
        Double viewCount = postQueryRequest.getViewCount();
        Date createTime = postQueryRequest.getCreateTime();
        Integer status = postQueryRequest.getStatus();
        Integer type = postQueryRequest.getType();
        LocalDateTime begin = postQueryRequest.getBegin();
        LocalDateTime end = postQueryRequest.getEnd();
        int current = postQueryRequest.getCurrent();
        int pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null && id != 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (userId != null && userId != 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        if (StringUtils.isNotBlank(userName)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userName", userName));
        }
        if (StringUtils.isNotBlank(keyWords)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        if (!tags.isEmpty()) {
            for (String tag : tags) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        if (CollUtil.isNotEmpty(orTags)) {
            BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTags) {
                orBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orBoolQueryBuilder);
        }
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        PageRequest pageRequest = PageRequest.of(current, pageSize);
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchTemplate.search(searchQuery, PostEsDTO.class);
        Page<PostSearchVo> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId()).toList();
            List<Post> posts = this.listByIds(postIdList);
            if (posts != null) {
                Map<Long, List<Post>> map = posts.stream().collect(Collectors.groupingBy(Post::getId));
                for (Long postId : postIdList) {
                    if (map.containsKey(postId)) {
                        resourceList.add(map.get(postId).get(0));
                    } else {
                        String delete = elasticsearchTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                }
            }


        }
        List<PostSearchVo> postSearchVos = new ArrayList<>();
        for (Post post : resourceList) {
            PostSearchVo postSearchVo = new PostSearchVo();
            BeanUtils.copyProperties(post, postSearchVo);
            postSearchVo.setTag(JSONUtil.toList(post.getTags(), String.class));
            postSearchVos.add(postSearchVo);
        }

        page.setRecords(postSearchVos);
        return page;
    }


    private List<PostVo> getPostCommendVoForRedis(String userCommendKey, String tag, Set<String> readPostIds) {

        List<PostVo> recommendPostVolist = null;
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.apply("JSON_CONTAINS(tags, {0})", "\"" + tag.replace("\"", "\\\"") + "\"").notIn(!readPostIds.isEmpty(), "id", readPostIds).last("LIMIT 3");
        List<Post> recommendPostList = this.list(postQueryWrapper);
        recommendPostVolist = recommendPostList.stream().map(recommendPost -> PostVo.builder().is_featured(recommendPost.getIs_featured()).is_top(recommendPost.getIs_top()).original_url(recommendPost.getOriginal_url()).createTime(recommendPost.getCreateTime()).favourNum(recommendPost.getFavourNum()).content(recommendPost.getContent()).tags(JSONUtil.toList(JSONUtil.parseArray(recommendPost.getTags()), String.class)).status(recommendPost.getStatus()).thumbNum(recommendPost.getThumbNum()).title(recommendPost.getTitle()).id(recommendPost.getId()).build()).toList();
        recommendPostVolist.forEach(postVo -> strngredisTemplate.opsForList().rightPush(userCommendKey, JSONUtil.toJsonStr(postVo)));
        strngredisTemplate.expire(userCommendKey, 5, TimeUnit.MINUTES);

        return recommendPostVolist;
    }

    private boolean isRead(Long userId, Set<Object> set) {
        return Boolean.TRUE.equals(strngredisTemplate.opsForSet().isMember("user:read:" + userId, set));
    }

    private Double updatePostViewCount(Long id) {
        return strngredisTemplate.opsForZSet().incrementScore(POST_VIEWS_COUNT, String.valueOf(id), 1);
    }

    private QueryWrapper<Post> getQuerWrapper(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        List<String> tags = postQueryRequest.getTags();

        Long userId = postQueryRequest.getUserId();
        int isReported = postQueryRequest.getIsReported();

        LocalDateTime end = postQueryRequest.getEnd();
        LocalDateTime begin = postQueryRequest.getBegin();
        Integer status = postQueryRequest.getStatus();
        Integer type = postQueryRequest.getType();

        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq(id != null, "id", id);
        postQueryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        postQueryWrapper.apply("JSON_CONTAINS(tags, {0})", "\"" + tags.get(0).replace("\"", "\\\"") + "\"");
        postQueryWrapper.eq(userId != null, "userId", userId);
        postQueryWrapper.eq("isReported", isReported);
        postQueryWrapper.between("createTime", begin, end);
        postQueryWrapper.eq(status != null, "status", status);
        postQueryWrapper.eq(type != null, "type", type);
        postQueryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), "sortField");

        return postQueryWrapper;
    }

}





