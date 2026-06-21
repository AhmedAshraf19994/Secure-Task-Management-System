package com.ahmed.Secure.Task.Management.System.auth.dto;

public record LoginResponseTokens(
        String accessToken,
        String refreshToken
) {
}
