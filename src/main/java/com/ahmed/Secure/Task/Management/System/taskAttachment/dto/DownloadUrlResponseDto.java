package com.ahmed.Secure.Task.Management.System.taskAttachment.dto;

import java.time.Instant;

public record DownloadUrlResponseDto(
        String url,
        Instant expiresAt
) {
}
