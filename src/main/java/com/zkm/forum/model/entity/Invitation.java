package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户邀请关系表
 * @TableName invitation
 */
@TableName(value ="invitation")
@Data
public class Invitation implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 奖励状态：0-未发放，1-已发放
     */
    private Integer rewardStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}