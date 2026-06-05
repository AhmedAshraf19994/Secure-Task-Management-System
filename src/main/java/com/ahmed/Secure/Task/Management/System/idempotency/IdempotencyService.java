package com.ahmed.Secure.Task.Management.System.idempotency;


import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyService {


    Optional<String> getCachedResult(String key);

    void cacheResult(String key, String result, Duration duration);

}
