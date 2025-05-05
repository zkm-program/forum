package com.zkm.forum.model.vo.fitness;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class GetUserInfoVo {
    /**
     * fitness主键
     */
    private  Long fitnessId;
    private int age;
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal calorieTarget;
    private BigDecimal proteinTarget;
    private BigDecimal carbohydrateTarget;
    private BigDecimal fatTarget;
    private int status;
}
