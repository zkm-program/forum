package com.zkm.forum.model.dto.question;

import lombok.Data;

import java.util.List;
@Data
public class SaveQuestionRequest {
    /**
     * 问题
     */
    private String question;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;
}
