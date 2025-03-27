package com.zkm.forum.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.zkm.forum.constant.CommonConstant;
import com.zkm.forum.mapper.PostMapper;
import com.zkm.forum.mapper.UserMapper;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.strategy.SearchStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

import java.util.List;

@Service("postMysqlSearchImpl")
public class PostMysqlSearchImpl implements SearchStrategy {

    @Resource
    PostMapper postMapper;
//    @Resource
//    CommonConstant constant;
    @Resource
    UserMapper userMapper;

    @Override
    public List<PostSearchVo> searchPost(String keyWords) {
        if (keyWords.isBlank()) {
            return new ArrayList<>();
        }
        QueryWrapper<Post> postSearchVoQueryWrapper = new QueryWrapper<>();
        postSearchVoQueryWrapper.eq("title", keyWords)
                .or()
                .eq("content", keyWords);
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
                        return null;
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
                    }

                    postSearchVo.setFavourNum(post.getFavourNum());
                    postSearchVo.setThumbNum(post.getThumbNum());
                    postSearchVo.setUpdateTime(post.getUpdateTime());
                    postSearchVo.setId(post.getId());
                    return postSearchVo;
                }


        ).toList();
    }
}
