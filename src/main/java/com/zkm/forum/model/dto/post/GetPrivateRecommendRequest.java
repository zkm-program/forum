package com.zkm.forum.model.dto.post;

import lombok.Data;

import java.util.List;
@Data
public class GetPrivateRecommendRequest {
    /**
     * 用户已经看过的帖子
     */
    private List<Long> viewedIds;
    /**
     * 用户刚看过帖子带有的标签
     */
    private List<String> tags;
}
