package com.zkm.forum.model.vo.questionConcern;

import lombok.Data;

import java.util.List;

// todo 后续返回会增加创作者头像，创作者名字
@Data
public class ListQuestionConcernVo {
    private Long questionId;
    /**
     * 问题
     *
     */
    private String question;
    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;
    /**
     * 关注数
     */
    private int concernNum;
    /**
     * 浏览量
     */
    private Integer viewCount;
    /**
     * 回答个数
     */
    private int questionCount;


}
