package com.zkm.forum.strategy;

import com.zkm.forum.model.vo.post.PostSearchVo;

import java.util.List;

public interface PostSearchStrategy {
    List<PostSearchVo> searchPost(String keyWords);
}
