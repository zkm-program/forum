package com.zkm.forum.model.vo.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionListVo {
    /**
     * 提问者名字
     */
    private String autorName;
    /**
     * 问题名字
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
     * 回答个数
     */
    private int answerCount;
}
