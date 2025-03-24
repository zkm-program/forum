package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签
 * @TableName tag
 */


@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value ="tag")
@Data
public class Tag implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标签名称
     */
    private String tageName;

    /**
     * 父标签名称
     */
    private String parentTagName;

    /**
     * 0 -不是，1-是
     */
    private Integer isParent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创修改时间
     */
    private Date updateTime;

    /**
     * 0是没删，1是没删
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;



}