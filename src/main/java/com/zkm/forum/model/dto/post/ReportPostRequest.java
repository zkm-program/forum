package com.zkm.forum.model.dto.post;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportPostRequest implements Serializable {
    private Long postId;
    private String reportedResults;
    private Long reportUserId;
}
