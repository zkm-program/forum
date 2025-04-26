package com.zkm.forum.rabbitmq;

import com.rabbitmq.client.Channel;
import com.zkm.forum.common.ErrorCode;

import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.entity.FitnessImage;

import com.zkm.forum.service.FitnessImageService;
import com.zkm.forum.utils.AiPictureUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static com.zkm.forum.constant.RabbitMqConstant.AI_PICTURE_QUEUE;

@Slf4j
@Component
public class AiPictureConsumer {
    @Resource
    private FitnessImageService fitnessImageService;
    @Resource
    private AiPictureUtils aiPictureUtils;
    @SneakyThrows
    @RabbitListener(queues =AI_PICTURE_QUEUE, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        Long fitnessImageId = Long.valueOf(message);
        FitnessImage fitnessImage = fitnessImageService.getById(fitnessImageId);
        if (fitnessImage == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片不存在");
        }
        fitnessImage.setType(1);
        boolean update = fitnessImageService.updateById(fitnessImage);
        if (!update){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片状态更新失败");
        }
        String s = aiPictureUtils.analyzeImage(fitnessImage.getDescription(), fitnessImage.getPictureUrl());
        String[] split = s.split("【【【【");
        fitnessImage.setCalorie(extractNumber(split[1]));
        fitnessImage.setProtein(extractNumber(split[2]));
        fitnessImage.setCarbohydrate(extractNumber(split[3]));
        fitnessImage.setFat(extractNumber(split[4]));
        fitnessImage.setType(2);
        boolean result = fitnessImageService.updateById(fitnessImage);
        if(!result){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片状态更新失败");
        }
    }
    // 方法：提取字符串中的数字部分
    private BigDecimal extractNumber(String input) {
        if (input == null || input.isEmpty()) {
            return BigDecimal.ZERO; // 默认值
        }
        String numberPart = input.replaceAll("[^0-9.]", ""); // 提取数字和小数点
        return new BigDecimal(numberPart);
    }

}
