package com.zkm.forum.model.dto.fitness;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaveOrUpdateMessageRequest {
    private Long id;
    private Integer age;
    private BigDecimal height;
    private BigDecimal weight;
    /**
     * 塑性目标
     */
    private String target;
}
