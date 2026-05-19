package com.ahmed.Secure.Task.Management.System.notification.dto;

import com.ahmed.Secure.Task.Management.System.notification.NotificationType;

import java.time.Instant;

public record NotificationResponseDto  (
    int id,
    String message,
    boolean isRead,
    NotificationType type,
    Instant createdAt,
    int taskId

){
}
