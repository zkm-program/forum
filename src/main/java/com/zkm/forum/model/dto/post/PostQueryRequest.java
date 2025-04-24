package com.zkm.forum.model.dto.post;

import com.zkm.forum.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class PostQueryRequest extends PageRequest {
    /**
     * 关键字搜索
     */
    private String keyWords;
    private Long id;
    private String title;
    private List<String> tags;
    private List<String> orTags;
    /**
     * 根据文章内容进行搜索
     */
    private String content;
    private String userName;
    private Integer thumbNum;
    private Integer favourNum;
    private Long userId;
    private int isReported;
    private Double viewCount;
    private Date createTime;
    private Integer status;
    private Integer type;
    private LocalDateTime begin;
    private LocalDateTime end;
}
