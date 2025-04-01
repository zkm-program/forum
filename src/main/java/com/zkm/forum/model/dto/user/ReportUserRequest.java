package com.zkm.forum.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportUserRequest implements Serializable {
    /**
     * 被举报人id
     */
    private Long userId;
    private String reportedResults;
    /**
     * 举报人id
     */
    private Long reportUserId;
}
