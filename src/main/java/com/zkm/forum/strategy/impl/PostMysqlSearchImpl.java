package com.zkm.forum.strategy.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zkm.forum.constant.CommonConstant;
import com.zkm.forum.mapper.PostMapper;
import com.zkm.forum.mapper.UserMapper;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.service.UserService;
import com.zkm.forum.strategy.SearchStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zkm.forum.constant.LocalCacheConstant.USERID_USERNAME;

@Service("postMysqlSearchImpl")
public class PostMysqlSearchImpl implements SearchStrategy {

    @Resource
    PostMapper postMapper;
    @Resource
    UserService userService;
    @Resource
    private Cache<String, String> LOCAL_CACHE;


    @Override
    public List<PostSearchVo> searchPost(String keyWords) {
        if (keyWords.isBlank()) {
            return new ArrayList<>();
        }
        QueryWrapper<Post> postSearchVoQueryWrapper = new QueryWrapper<>();
        postSearchVoQueryWrapper.like("title", keyWords)
                .or()
                .like("content", keyWords).or()
                //只要标签【Json字符串】中包含搜索词，就会被返回
                .apply("JSON_CONTAINS(tags, {0})", "\"" + keyWords.replace("\"", "\\\"") + "\"");
//        postSearchVoQueryWrapper.like("title", keyWords)
//                .or()
//                .like("content", keyWords);
        List<Post> posts = postMapper.selectList(postSearchVoQueryWrapper);
        return posts.stream().map(post -> {
                    PostSearchVo postSearchVo = new PostSearchVo();
                    boolean lowerCase = true;
                    String content = post.getContent();
                    int keyIndex = content.indexOf(keyWords.toLowerCase());
                    if (keyIndex == -1) {
                        keyIndex = content.indexOf(keyWords.toUpperCase());
                        if (keyIndex != -1) {
                            lowerCase = false;
                        }
                    }
                    if (keyIndex != -1) {
                        int preIndx = keyIndex > 15 ? keyIndex - 15 : 0;
                        String preString = post.getContent().substring(preIndx, keyIndex);
                        int lastIndex = (post.getContent().length() - keyIndex) > 35 ? keyIndex + 35 : post.getContent().length();
                        String lastString = post.getContent().substring(keyIndex, lastIndex);
                        if (lowerCase) {
                            postSearchVo.setContent((preString + lastString).replaceAll(keyWords.toLowerCase(), CommonConstant.PRE_TAG + keyWords.toLowerCase() + CommonConstant.POST_TAG));
                        } else {
                            postSearchVo.setContent((preString + lastString).replaceAll(keyWords.toUpperCase(), CommonConstant.PRE_TAG + keyWords.toUpperCase() + CommonConstant.POST_TAG));
                        }
                    } else {
                        postSearchVo.setContent(content);
                    }
                    lowerCase = true;
                    int key = post.getTitle().indexOf(keyWords.toLowerCase());
                    if (key == -1) {
                        key = post.getTitle().indexOf(keyWords.toUpperCase());
                        if (key != -1) {
                            lowerCase = false;
                        }
                    }
                    if (key != -1) {
                        if (lowerCase) {
                            postSearchVo.setTitle(post.getTitle().replaceAll(keyWords.toLowerCase(), CommonConstant.PRE_TAG + keyWords.toLowerCase() + CommonConstant.POST_TAG));
                        } else {
                            postSearchVo.setTitle(post.getTitle().replaceAll(keyWords.toUpperCase(), CommonConstant.PRE_TAG + keyWords.toUpperCase() + CommonConstant.POST_TAG));
                        }
                    } else {
                        postSearchVo.setTitle(post.getTitle());
                    }
                    List<String> tagList = JSONUtil.toList(post.getTags(), String.class);
                    List<String> list = tagList.stream().filter(tag -> tag.contains(keyWords.toUpperCase())).filter(tag -> tag.contains(keyWords.toLowerCase())).toList();
                    postSearchVo.setFavourNum(post.getFavourNum());
                    postSearchVo.setThumbNum(post.getThumbNum());

                    postSearchVo.setId(post.getId());
                    postSearchVo.setTag(list);
                    postSearchVo.setType(post.getType());
                    String ifPresent = LOCAL_CACHE.getIfPresent(USERID_USERNAME + post.getUserId());
                    if (ifPresent != null) {
                        postSearchVo.setAuthorName(ifPresent);
                    } else {
                        CompletableFuture.runAsync(() -> {
                            User author = userService.getById(post.getUserId());
                            LOCAL_CACHE.put(USERID_USERNAME + author.getId(), author.getUserName());
                        });
                    }
                    return postSearchVo;
                }


        ).toList();
    }
}
