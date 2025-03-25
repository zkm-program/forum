package com.zkm.forum.model.dto.post;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePostDeleteForMy implements Serializable {

    private Long id;
    /**
     * 要删除文章的作者
     */
    private Long userId;
    private Integer isDelete;
}
