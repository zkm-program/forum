package com.zkm.forum.rabbitmq;

import com.alibaba.excel.util.StringUtils;
import com.rabbitmq.client.Channel;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.entity.Fitness;
import com.zkm.forum.service.FitnessService;
import com.zkm.forum.utils.AIUtils;

import com.zkm.forum.utils.EncryptorUtils;
import com.zkm.forum.utils.UserBasicEncryptorUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.math.BigDecimal;

import static com.zkm.forum.constant.RabbitMqConstant.ANALYSE_USER_QUEUE;




@Slf4j
@Component
public class AnalyseUserConsumer {
    @Resource
    private AIUtils aiUtils;
    @Resource
    private FitnessService fitnessService;

    @SneakyThrows
    @RabbitListener(queues = ANALYSE_USER_QUEUE, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        Long fitnessId = Long.valueOf(message);
        Fitness fitness = fitnessService.getById(fitnessId);
        if (fitness == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "fitness不存在");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("用户基本信息").append("\n");
        stringBuilder.append("性别:" + fitness.getGender()).append("\n");
        stringBuilder.append("年龄:" + fitness.getAge() + "岁").append("\n");
        stringBuilder.append("身高:" + fitness.getHeight() + "cm").append("\n");
        stringBuilder.append("体重:" + fitness.getWeight() + "kg").append("\n");
        stringBuilder.append("目标:" + fitness.getTarget()).append("\n");
        fitness.setStatus(1);
        boolean result = fitnessService.updateById(fitness);
        if (!result) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改fitness状态失败");
        }
        String s = aiUtils.sendMsgToXingHuo(true, stringBuilder.toString());
        String[] split = s.split("【【【【");
        fitness.setCalorieTarget(extractNumber(split[1]));
        fitness.setProteinTarget(extractNumber(split[2]));
        fitness.setCarbohydrateTarget(extractNumber(split[3]));
        fitness.setFatTarget(extractNumber(split[4]));
        fitness.setStatus(2);
        result = fitnessService.updateById(fitness);
        if(!result){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片状态更新失败");
        }
        fitness.setHeight(BigDecimal.ZERO);
        fitness.setWeight(BigDecimal.ZERO);
        fitness.setDesensitization(1);
        // todo 如果抹除敏感信息不成功，把抹除失败的fitnessId设个状态，之后来个定时任务进行处理
        // todo 这样ack对吗？
        channel.basicAck(deliveryTag, false);
    }

    private BigDecimal extractNumber(String input) {
        if (input == null || input.isEmpty()) {
            return BigDecimal.ZERO; // 默认值
        }
        String numberPart = input.replaceAll("[^0-9.]", ""); // 提取数字和小数点
        return new BigDecimal(numberPart);
    }
}
