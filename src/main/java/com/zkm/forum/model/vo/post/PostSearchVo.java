package com.zkm.forum.model.vo.post;

import com.zkm.forum.model.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 关键字搜索帖子
 */
@Data
public class PostSearchVo {
    private String authorName;
    private String title;
    private String content;
    private Integer thumbNum;
    private Integer favourNum;
    private Date updateTime;
}
