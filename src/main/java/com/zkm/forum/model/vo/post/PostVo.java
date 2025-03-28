package com.zkm.forum.model.vo.post;

import com.zkm.forum.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 点进某个帖子查看详情
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PostVo implements Serializable {


    private Long id;
    private String title;
    private Double viewCount;
    private String content;
    private List<String> tags;
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
