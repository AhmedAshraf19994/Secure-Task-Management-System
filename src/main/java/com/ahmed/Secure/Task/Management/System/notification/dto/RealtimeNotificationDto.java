package com.ahmed.Secure.Task.Management.System.notification.dto;

import java.time.Instant;

public record RealtimeNotificationDto(
        String actorName,
        String receiverName,
        String message,
        int taskId,
        Instant createdAt
) {
}
