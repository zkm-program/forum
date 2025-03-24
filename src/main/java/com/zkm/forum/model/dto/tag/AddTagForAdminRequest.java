package com.zkm.forum.model.dto.tag;

import lombok.Data;

import java.io.Serializable;
@Data
public class AddTagForAdminRequest implements Serializable {
    /**
     * 标签id
     */
    private Long id;
    private String tageName;

    private Integer isParent;
    private String parentTagName;
}
