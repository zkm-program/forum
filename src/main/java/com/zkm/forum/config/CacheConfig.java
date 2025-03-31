package com.zkm.forum.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    @Bean
    public Cache<String, String> localCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10_000L)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build();
    }
}
