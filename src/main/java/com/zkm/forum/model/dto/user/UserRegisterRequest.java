package com.zkm.forum.model.dto.user;

import lombok.Getter;

import java.io.Serializable;
@Getter
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
}
