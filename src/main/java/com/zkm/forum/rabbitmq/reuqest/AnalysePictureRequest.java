package com.zkm.forum.rabbitmq.reuqest;

import com.zkm.forum.model.entity.FitnessImage;
import com.zkm.forum.model.entity.User;
import lombok.Data;

@Data
public class AnalysePictureRequest {
    /**
     * 执行操作用户
     */
    private User user;
    private FitnessImage fitnessImage;
}
