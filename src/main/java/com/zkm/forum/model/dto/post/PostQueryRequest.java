package com.zkm.forum.model.dto.post;

import com.zkm.forum.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class PostQueryRequest extends PageRequest {
    private Long id;
    private String title;
    private List<String> tags;
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
