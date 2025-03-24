package com.zkm.forum.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateForAdminRequest implements Serializable {
    private String userRole;
    private int  matchCount;
    private String userQqEmail;

}
