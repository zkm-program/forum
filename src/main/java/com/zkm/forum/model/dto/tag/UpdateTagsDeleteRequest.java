package com.zkm.forum.model.dto.tag;

import lombok.Data;

import java.util.List;

@Data
public class UpdateTagsDeleteRequest {
    private List<Long> idList;
    private Integer isDelete;
}
