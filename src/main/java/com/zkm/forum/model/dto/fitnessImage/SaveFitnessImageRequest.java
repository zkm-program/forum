package com.zkm.forum.model.dto.fitnessImage;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SaveFitnessImageRequest {


    private int count;
    /**
     * fitness主键
     */
    private Long fitnessId;

    /**
     * 图片地址
     */
    private String pictureUrl;

    /**
     * 食物名称
     */
    private String foodName;

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
     * 用户创建时间
     */
    private Date createTime;

    /**
     * 0-没有吃，1-吃掉它
     */
    private Integer status;

}
