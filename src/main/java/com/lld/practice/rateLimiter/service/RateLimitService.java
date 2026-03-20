package com.lld.practice.rateLimiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW = 60_000;
    private final StringRedisTemplate redisTemplate;
    public RateLimitService(final StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public boolean isNotAllowed(final String userId){
        long currentTime = System.currentTimeMillis();
        long windowStartTime = currentTime - WINDOW;
        redisTemplate.opsForZSet().removeRangeByScore(userId, 0, windowStartTime);
        Long currentCount = redisTemplate.opsForZSet().zCard(userId);

        if (currentCount != null && currentCount < MAX_REQUESTS) {
            redisTemplate.opsForZSet().add(userId, String.valueOf(currentTime), currentTime);
            redisTemplate.expire(userId, Duration.ofSeconds(WINDOW));
            return false;
        }
        return true;
    }
}
