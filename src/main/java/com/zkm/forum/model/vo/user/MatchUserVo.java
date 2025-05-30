package com.zkm.forum.model.vo.user;

import lombok.Data;

import java.util.List;
@Data
public class MatchUserVo {
    /**
     * 匹配到的用户主键
     */
    private Long id;
    private String userName;
    private List<String> tags;
    private String userQqEmail;
    private String userAvatar;
    private String gender;
    private String introduction;
}
