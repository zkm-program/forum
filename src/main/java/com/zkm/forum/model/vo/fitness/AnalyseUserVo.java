package com.zkm.forum.model.vo.fitness;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalyseUserVo {
    private BigDecimal calorieTarget;
    private BigDecimal proteinTarget;
    private BigDecimal carbohydrateTarget;
    private BigDecimal fatTarget;
}
