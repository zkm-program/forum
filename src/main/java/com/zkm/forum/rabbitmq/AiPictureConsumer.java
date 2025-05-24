package com.zkm.forum.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.zkm.forum.common.ErrorCode;

import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.entity.FitnessImage;

import com.zkm.forum.model.entity.User;
import com.zkm.forum.rabbitmq.reuqest.AnalysePictureRequest;
import com.zkm.forum.service.FitnessImageService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.AiPictureUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.json.Json;
import java.math.BigDecimal;

import static com.zkm.forum.constant.RabbitMqConstant.AI_PICTURE_QUEUE;

// todo 编写定时任务，删除fitnessImage的type=3的数据，图片也要删，彻底删除
// todo rabbitmq后续会引入死信队列，目前是想当消息被nack后再重试5次，5次之后直接丢弃，同时用户的额度不会被扣减
// todo 如果队列传过来的消息，我消费者没有ack，这时候消息会在队列中堆积占用资源，需要优化。还有什么重试机制【消费者和生产者】
@Slf4j
@Component
@RabbitListener(queues = AI_PICTURE_QUEUE, ackMode = "MANUAL")
public class AiPictureConsumer {
    @Resource
    private FitnessImageService fitnessImageService;
    @Resource
    private AiPictureUtils aiPictureUtils;
    @Resource
    private UserService userService;

    @SneakyThrows
    @RabbitHandler
    public void process(byte[] data, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (data==null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        AnalysePictureRequest analysePictureRequest = JSON.parseObject(new String(data), AnalysePictureRequest.class);
        FitnessImage fitnessImage = analysePictureRequest.getFitnessImage();
        if (fitnessImage ==null) {
            channel.basicNack(deliveryTag, false, false);
            fitnessImage.setType(3);
            fitnessImageService.updateById(fitnessImage);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片不存在");
        }
        fitnessImage.setType(1);
        boolean update = fitnessImageService.updateById(fitnessImage);
        if (!update) {
            fitnessImage.setType(3);
            fitnessImageService.updateById(fitnessImage);
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片状态更新失败");
        }
        try {
            String s = aiPictureUtils.analyzeImage(fitnessImage.getDescription(), fitnessImage.getPictureUrl());
            String[] split = s.split("【【【【");
            fitnessImage.setCalorie(extractNumber(split[1]));
            fitnessImage.setProtein(extractNumber(split[2]));
            fitnessImage.setCarbohydrate(extractNumber(split[3]));
            fitnessImage.setFat(extractNumber(split[4]));
            fitnessImage.setType(2);
            boolean result = fitnessImageService.updateById(fitnessImage);
            if (!result) {
                fitnessImage.setType(3);
                fitnessImageService.updateById(fitnessImage);
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片状态更新失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        // todo 这样ack对吗？
        channel.basicAck(deliveryTag, false);
        User user = analysePictureRequest.getUser();
        boolean result = userService.update().eq("id", user.getId()).setSql("analysePictureCount=analysePictureCount-1").update();
        // todo 将扣减失败的信息记录到新建的表中
        if (!result) {
            // 记录扣减失败的用户信息
            log.error("扣减用户分析图片次数失败，用户ID: {}", user.getId());
            // 可以将失败信息保存到数据库或其他存储中
            // 例如：failedReductionService.saveFailedReduction(user.getId(), "analysePictureCount");
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
