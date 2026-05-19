package com.ahmed.Secure.Task.Management.System.notification.realtimeNotification;

import com.ahmed.Secure.Task.Management.System.notification.dto.RealtimeNotificationDto;

public interface RealtimeNotificationService {

    void sendToUser(String username, RealtimeNotificationDto dto);
}
