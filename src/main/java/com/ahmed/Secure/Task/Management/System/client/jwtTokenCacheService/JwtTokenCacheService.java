package com.ahmed.Secure.Task.Management.System.client.jwtTokenCacheService;

public interface JwtTokenCacheService {

    void addToken (String token, Long durationInHours);

    void deleteToken(String token);

    boolean hasToken(String token);
}
