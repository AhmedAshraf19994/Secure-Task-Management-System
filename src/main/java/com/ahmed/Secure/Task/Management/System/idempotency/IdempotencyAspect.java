package com.ahmed.Secure.Task.Management.System.idempotency;


import com.ahmed.Secure.Task.Management.System.idempotency.config.IdempotencyProperties;
import com.ahmed.Secure.Task.Management.System.security.CurrentUserService;
import com.ahmed.Secure.Task.Management.System.system.exceptions.IdempotencyKeyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {

    private final ObjectMapper objectMapper;

    private final IdempotencyProperties idempotencyProperties;

    private final IdempotencyService  idempotencyService;

    private final CurrentUserService currentUserService;

    private static final int MAX_KEY_LENGTH = 255;

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        if (!idempotencyProperties.enabled()) {
            return joinPoint.proceed();
        }
        //check the method called through http request
       RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
       if (requestAttributes == null) {
           return joinPoint.proceed();
       }
       
        //extract the idempotency key from the request 
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
       String idempotencyKey = request.getHeader(idempotencyProperties.headerName());

       //validate the idempotency key
        validateIdempotencyKey(idempotencyKey);

        //hash the key
        String hashedKey = buildHashedKey(idempotencyKey, idempotent, joinPoint);

        //check if cache hit
        Optional<String> cachedResult = idempotencyService.getCachedResult(hashedKey);
        if (cachedResult.isPresent()) {
            log.info("Cache hit for key: {}", hashedKey);
            return this.objectMapper.readValue(cachedResult.get(), getReturnType(joinPoint));
        }

        //if the key does not exist, proceed with the method and cache the result
        Object result = joinPoint.proceed();
        Duration duration =  idempotent.timeToLive().getDuration();
        this.idempotencyService.cacheResult(hashedKey, this.objectMapper.writeValueAsString(result), duration);
        log.info("Cached result for key: {}", hashedKey);
        return result;
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() ) {
            throw new IdempotencyKeyException("Idempotency key is required");
        }
        if (idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new IdempotencyKeyException("Idempotency key exceeds maximum length of " + MAX_KEY_LENGTH + " characters");
        }
    }

    private Class<?> getReturnType(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getReturnType();
    }

    private String buildHashedKey(String idempotencyKey ,Idempotent idempotent, ProceedingJoinPoint joinPoint)  {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(idempotencyKey.getBytes(StandardCharsets.UTF_8));
            //hash the user id
            if(idempotent.hashUserId()) {
                int userId = currentUserService.getUserId();
                md.update(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
            }
            //hash the request body if enabled
            if(idempotent.hashRequestBody()) {
                //extract the request body from the business method arguments
                String body = this.objectMapper.writeValueAsString(joinPoint.getArgs());
                md.update(body.getBytes(StandardCharsets.UTF_8));
            }
            byte[] digest = md.digest();
            String hash = HexFormat.of().formatHex(digest);
            String prefix = idempotent.keyPrefix().isBlank() ? idempotencyProperties.keyPrefix() : idempotent.keyPrefix();
            return prefix + ":" + hash;

        } catch(Exception exception ) {
            log.error("Failed to build hashed key", exception);
            return  "fallback:" + idempotencyKey;
        }
    }
}
