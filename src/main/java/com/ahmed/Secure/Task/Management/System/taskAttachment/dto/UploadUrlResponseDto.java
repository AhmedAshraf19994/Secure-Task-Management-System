package com.ahmed.Secure.Task.Management.System.taskAttachment.dto;

import com.ahmed.Secure.Task.Management.System.taskAttachment.TaskAttachmentStatus;

import java.time.Instant;

public record UploadUrlResponseDto(
        int id,
        String uploadUrl,          // Presigned URL for uploading the file
        TaskAttachmentStatus status,
        Instant expiresAt,
        Long maxFileSizeBytes
)  {
}
