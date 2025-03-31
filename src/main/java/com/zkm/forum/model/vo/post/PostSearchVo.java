package com.zkm.forum.model.vo.post;

import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 关键字搜索帖子
 */
@Data
public class PostSearchVo {
    private Long id;
    private String title;
    private String content;
    private Integer thumbNum;
    private Integer favourNum;
    private Integer type;
    private String tag;
    private String authorName;
}
