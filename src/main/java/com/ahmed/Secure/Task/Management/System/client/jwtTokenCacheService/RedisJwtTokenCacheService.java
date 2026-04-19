package com.ahmed.Secure.Task.Management.System.client.jwtTokenCacheService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisJwtTokenCacheService implements JwtTokenCacheService{

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addToken(String token, Long durationInHours) {
        this.stringRedisTemplate.opsForValue().set(token,"valid",durationInHours, TimeUnit.HOURS);
    }

    @Override
    public void deleteToken(String token) {
        this.stringRedisTemplate.delete(token);

    }

    @Override
    public boolean hasToken(String token) {
        return this.stringRedisTemplate.hasKey(token);
    }
}
