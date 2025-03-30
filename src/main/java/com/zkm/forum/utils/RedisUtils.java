package com.zkm.forum.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class RedisUtils {
    @Resource
    private  RedisTemplate<String, Object> redisTemplate;

    // 通用类型安全获取方法
    public <T> List<T> getList(String key, Class<T> type) {
        List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
        return rawList.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }
}

