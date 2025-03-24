package com.zkm.forum.model.vo.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ListTagsForAdminVo {
    private String tageName;
    private String parentTagName;
    private Integer isParent;
    private Date createTime;
    private Date updateTime;
    private Integer isDelete;
    private List<ListTagsForAdminVo> list;
}
