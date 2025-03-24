package com.zkm.forum.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private String userQqEmail;
    private String userPassword;
    private static final long serialVersionUID = 3191241716373120793L;
}
