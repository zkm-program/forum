package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 标签匹配结果历史记录
 * @TableName match_tags
 */
@TableName(value ="match_tags")
@Data
public class MatchTags implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发起匹配用户id
     */
    private Long userId;

    /**
     * 被匹配到的用户id
     */
    private Long matchUserId;

    /**
     * 被匹配到的用户姓名
     */
    private String userName;

    /**
     * 被匹配到的用户标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 被匹配到的用户头像
     */
    private String userAvatar;

    /**
     * 被匹配到的用户性别
     */
    private String gender;

    /**
     * 被匹配到的用户QQ邮箱
     */
    private String userQqEmail;

    /**
     * 被匹配到的用户简介
     */
    private String introduction;

    /**
     * 匹配状态 0-匹配中，1-匹配成功，2-匹配失败，3-没有匹配任务
     */
    private Integer status;

    /**
     * 匹配结果状态 0-用户未确认，1-用户已确认
     */
    private Integer type;

    /**
     * 匹配模式种类 0-基本标签匹配，1-超级标签匹配
     */
    private Integer kind;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}