package com.ahmed.Secure.Task.Management.System.taskAttachment.dto;

import com.ahmed.Secure.Task.Management.System.taskAttachment.TaskAttachment;
import com.ahmed.Secure.Task.Management.System.taskAttachment.TaskAttachmentStatus;

import java.time.Instant;

public record AttachmentResponseDto(
        int id,
        String originalFileName,
        long size,
        String type,
        TaskAttachmentStatus status,
        Instant createdAt,
        String createdBy
) {
}
