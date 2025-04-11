package com.zkm.forum.model.vo.question;

public class QuestionSearchVo {

    private Long questionId;
    /**
     * 问题
     */
    private String question;
    /**
     * 标签列表（json 数组）
     */
    private String tags;
    /**
     * 关注数
     */
    private Integer concernNum;
    /**
     * 浏览量
     */
    private Integer viewCount;
}
