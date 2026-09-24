package com.example.demo.otp;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final StringRedisTemplate redis;

    private static final DefaultRedisScript<Long> COUNTER_SCRIPT =
            new DefaultRedisScript<>("""
                local count = redis.call('INCR', KEYS[1])
                if count == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end
                return count
                """, Long.class);

    public OtpService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean allowSend(String phone) {
        String key = "practice:otp:limit:" + phone;

        Long count = redis.execute(
                COUNTER_SCRIPT,
                List.of(key),
                "60"
        );

        if (count == null) {
            throw new IllegalStateException("Khong doc duoc bo dem Redis");
        }

        System.out.println("So lan yeu cau: " + count);
        return count <= 3;
    }
}