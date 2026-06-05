package com.ahmed.Secure.Task.Management.System.idempotency;

import com.ahmed.Secure.Task.Management.System.system.exceptions.IdempotencyCacheException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyService implements IdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> getCachedResult(String key) {
        try {
            String result = stringRedisTemplate.opsForValue().get(key);
            return Optional.ofNullable(result);
        } catch (DataAccessException exception) {
            log.error("Failed to retrieve cached result for key: {}", key, exception);
            throw new IdempotencyCacheException("Failed to retrieve cached result for key: " + key, exception);
        }
    }

    @Override
    public void cacheResult(String key, String result, Duration duration) {
        try {
            stringRedisTemplate.opsForValue().set(key, result, duration);
        } catch (DataAccessException exception) {
            log.error("Failed to cache result for key: {}", key, exception);
            throw new IdempotencyCacheException("Failed to cache result for key: " + key, exception);
        }
    }


}
