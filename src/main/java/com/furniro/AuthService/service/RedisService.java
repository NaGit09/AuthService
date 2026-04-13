package com.furniro.AuthService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate caching;

    @Async
    public void addData(String key, String value, long timeout, TimeUnit unit) {
        caching.opsForValue().set(key, value, timeout, unit);
    }

    @Async
    public void removeData(String key) {
        caching.delete(key);
    }

    public boolean isCaching (String key) {
        return caching.hasKey(key);
    }

    public String getData(String key) {
        return caching.opsForValue().get(key);
    }
}
