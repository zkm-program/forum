package com.zkm.forum.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 卡路里分析表
 * @TableName fitness_analyse
 */
@TableName(value ="fitness_analyse")
@Data
public class FitnessAnalyse implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户主键
     */
    private Long userId;

    /**
     * fitness主键
     */
    private Long fitnessId;

    /**
     * fitnessImage主键
     */
    private Long fitnessImageId;

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
     * 1-近一周，2-近两周，3-近三周，4-近四周
     */
    private Integer time;

    /**
     * 0-没有吃，1-吃掉它，3-用户没有选择
     */
    private Integer status;
    /**
     * 目标完成率
     */
    private BigDecimal targetComplete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}