package com.ahmed.Secure.Task.Management.System.auth.session;

import java.time.Instant;
import java.util.UUID;

public record SessionResponseDto(
        UUID id,
        String userAgent,
        Instant createdAt,
        Instant expiresAt,
        boolean revoked
) {
}
