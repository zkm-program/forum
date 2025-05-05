package com.zkm.forum.model.vo.postFavour;

import lombok.Data;

import java.util.List;

// todo 后续返回会增加创作者头像，创作者名字，少部分文章内容，几条评论
@Data
public class ListMyPostFavourVo {

    /**
     * 文章主键
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;
    /**
     * 收藏数
     */
    private Integer favourNum;
    private String authorAvatar;
    private String authorName;
    private int thumbNum;

}
