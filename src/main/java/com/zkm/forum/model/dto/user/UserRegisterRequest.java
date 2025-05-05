package com.zkm.forum.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private String userPassword;
    private String userName;
    private String userQqEmail;
    private String checkPassword;
    /**
     * 验证码
     */
    private String userCode;
    private String gender;
    
    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;
    /**
     * 用户头像
     */
//    private String userAvatar;
}
