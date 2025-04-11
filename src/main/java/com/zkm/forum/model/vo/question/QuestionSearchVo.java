package com.zkm.forum.model.vo.question;

import lombok.Data;

import java.util.List;
@Data
public class QuestionSearchVo {

    private Long questionId;
    /**
     * 问题
     */
    private String question;
    /**
     * 作者名字
     */
    private String authorName;
    /**
     * 标签列表（json 数组）
     */
    private List<String> tag;
    /**
     * 关注数
     */
    private Integer concernNum;
    /**
     * 浏览量
     */
    private Integer viewCount;
}
