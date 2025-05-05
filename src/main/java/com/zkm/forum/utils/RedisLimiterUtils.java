package com.zkm.forum.utils;

import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class RedisLimiterUtils {
    @Resource
    private RedissonClient redissonClient;
    /**
     * 限流操作
     * @param key 区分不同的限流器，比如不同的用户 id 应该分别统计
     */
    public void doRateLimit(String key) {
        // 创建一个名称为user_limiter的限流器，每秒最多访问 2 次，每个用户都有属于自己的限流器，每个用户的限流器的额度不一样
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //在每个时间窗口（1 秒）内，最多允许 2 次请求打到后端，令牌桶限流法
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        // 每当一个操作来了后，请求一个令牌，后端每秒会生成令牌，下方表示1个请求耗费1张令牌，
        // 如果请求到达后端时，没有令牌了那就返回false，不能去使用智能bi，请求拿到令牌程序才能继续执行
        boolean canOp = rateLimiter.tryAcquire(1);
        if (!canOp) {
            throw new BusinessException(ErrorCode.Many_Times);
        }
    }
}
