package com.zkm.forum.model.vo.post;

import com.zkm.forum.model.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 点进某个帖子查看详情
 */
@Data
public class PostVo implements Serializable {


    private String title;
    private String content;
    private String tags;
    private Integer thumbNum;
    private Integer favourNum;
    private User author;
    private Date createTime;
    private Date updateTime;
    private Integer status;
    private String article_abstract;
    private Integer is_top;
    private Integer is_featured;
    private String original_url;
}
