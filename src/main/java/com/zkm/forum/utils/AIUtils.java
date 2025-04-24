package com.zkm.forum.utils;

import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class AIUtils {

    @Resource
    private SparkClient sparkClient;

    public String sendMsgToXingHuo(boolean isNeedTemplate, String content) {
        List<SparkMessage> messages = new ArrayList<>();
        if (isNeedTemplate) {
            // AI 生成问题的预设条件
            String predefinedInformation = "你是一个营养师，请严格按以下步骤执行：\n" +
                    "2. 假设用户每天不运动，根据用户的目标(如每周增肌0.5g)计算每日推荐摄入卡路里（BMR-500kcal）\n" +
                    "3. 根据用户的目标计算每日推荐摄入的蛋白质、碳水化合物和脂肪的量\n" +
                    "4. 按以下格式返回，注意【【【【把返回的结果分成了四组，仅包含数字和单位，不要任何额外文字：\n" +
                    "【【【【\n" +
                    "推荐摄入:[计算结果]卡路里\n" +
                    "【【【【\n" +
                    "蛋白质:[计算结果]克\n" +
                    "【【【【\n" +
                    "碳水化合物:[计算结果]克\n" +
                    "【【【【\n" +
                    "脂肪:[计算结果]克\n" +
                    "期望输出\n" +
                    "【【【【\n" +
                    "推荐摄入:658卡路里\n" +
                    "【【【【\n" +
                    "蛋白质:100克\n" +
                    "【【【【\n" +
                    "碳水化合物:200克\n" +
                    "【【【【\n" +
                    "脂肪:50克";
            messages.add(SparkMessage.systemContent(predefinedInformation + "\n" + "----------------------------------"));
        }
        messages.add(SparkMessage.userContent(content));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.6)
                // 指定请求版本
                .apiVersion(SparkApiVersion.V4_0)
                .build();
        // 同步调用
        String responseContent = sparkClient.chatSync(sparkRequest).getContent().trim();
        if (!isNeedTemplate) {
            return responseContent;
        }
        log.info("星火 AI 返回的结果 {}", responseContent);
//        AtomicInteger atomicInteger = new AtomicInteger(1);
//        while (responseContent.split("【【【【").length < 5) {
//            responseContent = sparkClient.chatSync(sparkRequest).getContent().trim();
//            if (atomicInteger.incrementAndGet() >= 4) {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "星火 AI 生成失败");
//            }
//        }
        return responseContent;
    }
}