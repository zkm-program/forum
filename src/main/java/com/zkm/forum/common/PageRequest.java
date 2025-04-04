package com.zkm.forum.common;

import com.zkm.forum.constant.CommonConstant;
import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小(一页有几条数据)
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;

}
