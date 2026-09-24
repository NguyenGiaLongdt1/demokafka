package com.example.demo.ratelimit;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class ApiRateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> SCRIPT =
            new DefaultRedisScript<>("""
                local count = redis.call('INCR', KEYS[1])
                if count == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end
                return count
                """, Long.class);

    public ApiRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allowRequest(String ip) {
        String key = "practice:api:hello:" + ip;

        Long count = redisTemplate.execute(
                SCRIPT,
                List.of(key),
                "60"
        );

        if (count == null) {
            throw new IllegalStateException("Không đọc được bộ đếm Redis");
        }

        return count <= 5;
    }
}