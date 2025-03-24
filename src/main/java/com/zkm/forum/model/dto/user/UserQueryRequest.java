package com.zkm.forum.model.dto.user;

import com.zkm.forum.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    private String gender;
    private String isReported;

}
