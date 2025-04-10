package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 健身表
 * @TableName fitness
 */
@TableName(value ="fitness")
@Data
public class Fitness implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户年龄
     */
    private Integer age;

    /**
     * 用户性别
     */
    private String gender;

    /**
     * 用户身高(米)
     */
    private BigDecimal height;

    /**
     * 用户体重(kg,公斤)
     */
    private BigDecimal weight;

    /**
     * 图片地址
     */
    private String pictureUrl;

    /**
     * 塑性目标
     */
    private String target;

    /**
     * 卡路里含量
     */
    private BigDecimal calorie;

    /**
     * 蛋白质含量
     */
    private BigDecimal protein;

    /**
     * 碳水化合物含量
     */
    private BigDecimal carbohydrate;

    /**
     * 脂肪含量
     */
    private BigDecimal fat;

    /**
     * 根据用户的 年龄、性别、体重、身高 等数据，提供定制化的饮食建议
     */
    private String foodAdvice;

    /**
     * 用户分析完视频脂肪等含量给出怎么运动来达到减脂等目的
     */
    private String sportAdvice;

    /**
     * 根据上面sportAdvice，用户选择做什么运动
     */
    private String sport;

    /**
     * 用户创建时间
     */
    private Date createTime;

    /**
     * 用户更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 0-分析中，1-已完成，2-分析失败
     */
    private Integer status;

    /**
     * 用户每天不动的基础代谢
     */
    private BigDecimal BMR;

    /**
     * 用户为了到达目标需要每天摄入多少卡路里
     */
    private BigDecimal getKcal;

    /**
     * 0-文字ai，1-图片ai
     */
    private Integer type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}