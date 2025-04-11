package com.zkm.forum.model.dto.question;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
@Data
public class AnswerQuestionRequest {
    Long questionId;
    @NotBlank(message = "文章标题不能为空")
    private String title;
    @NotBlank(message = "文章内容不能为空")
    private String content;
    private String article_abstract;
    @NotBlank(message = "文章标签不能为空")
    private List<String> tags;
    @NotBlank(message = "文章状态不能为空")
    private Integer status;
    @NotBlank(message = "文章类型不能为空")
    private Integer type;
    /**
     * 原文链接
     */
    private String original_url;
}
