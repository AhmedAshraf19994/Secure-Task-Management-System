package com.ahmed.Secure.Task.Management.System.notification.events;

public record NotificationCreatedEvent(
        String actorName,
        String receiverName,
        String message,
        int taskId
) {
}
