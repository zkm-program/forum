package com.zkm.forum.model.vo.tag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ListTagsForUserVo {
    private String tageName;
    private String parentTagName;
    private Integer isParent;
    private List<ListTagsForUserVo> list;
}
