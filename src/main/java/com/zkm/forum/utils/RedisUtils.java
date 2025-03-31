//package com.zkm.forum.utils;
//
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
//import com.zkm.forum.model.vo.post.PostVo;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//@Deprecated
//@Slf4j
//@Component
//public class RedisUtils {
//    @Resource
//    private  RedisTemplate<String, Object> redisTemplate;
//
//    // 通用类型安全获取方法
//    /**
//     * 通用的从Redis获取List的方法
//     * @param key Redis键
//     * @param type 目标类型Class对象
//     * @param <T> 泛型类型
//     * @return List<T> 或空列表（不会返回null）
//     */
//    /**
//     * 从Redis获取对象列表（适配你的存储结构）
//     * @param key Redis键
//     * @param type 目标类型Class对象
//     * @param <T> 泛型类型
//     * @return List<T> 或空列表（不会返回null）
//     */
//    public <T> List<T> getList(String key, Class<T> type) {
//        try {
//            // 1. 获取原始数据（使用JSON序列化后的字符串）
//            List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
//            if (CollectionUtils.isEmpty(rawList)) {
//                return Collections.emptyList();
//            }
//
//            // 2. 处理每条记录
//            return rawList.stream()
//                    .map(obj -> {
//                        try {
//                            // 情况1：已经是目标类型（可能发生在本地缓存等场景）
//                            if (type.isInstance(obj)) {
//                                return type.cast(obj);
//                            }
//
//                            // 情况2：处理JSON字符串（你的实际存储格式）
//                            if (obj instanceof String) {
//                                String jsonStr = ((String) obj).trim();
//
//                                // 处理PostVo的特殊情况（你存储的是List<PostVo>的JSON数组）
//                                if (type == PostVo.class) {
//                                    if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
//                                        // 解析整个JSON数组
//                                        JSONArray jsonArray = JSONUtil.parseArray(jsonStr);
//                                        return jsonArray.stream()
//                                                .map(item -> JSONUtil.toBean(item.toString(), type))
//                                                .collect(Collectors.toList());
//                                    }
//                                }
//
//                                // 默认处理单个JSON对象
//                                return JSONUtil.toBean(jsonStr, type);
//                            }
//
//                            log.warn("无法处理的类型: {}", obj.getClass());
//                            return null;
//                        } catch (Exception e) {
//                            log.error("数据转换失败，内容: {}, 错误: {}", obj, e.getMessage());
//                            return null;
//                        }
//                    })
//                    // 扁平化处理（处理可能返回的List）
//                    .flatMap(item -> {
//                        if (item instanceof List) {
//                            return ((List<?>) item).stream()
//                                    .filter(type::isInstance)
//                                    .map(type::cast);
//                        }
//                        return item != null ? Stream.of((T) item) : Stream.empty();
//                    })
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            log.error("Redis操作失败，key: {}, 错误: {}", key, e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//}
//
