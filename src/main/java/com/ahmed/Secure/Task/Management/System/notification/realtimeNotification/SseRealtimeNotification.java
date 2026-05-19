package com.ahmed.Secure.Task.Management.System.notification.realtimeNotification;


import com.ahmed.Secure.Task.Management.System.notification.dto.RealtimeNotificationDto;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseRealtimeNotification implements RealtimeNotificationService {

    private final Map<String, SseEmitter> emitters =
            new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        return emitter;
    }

    @Override
    public void sendToUser(String username, RealtimeNotificationDto dto) {
        SseEmitter emitter = emitters.get(username);

        if(emitter == null) {
            System.out.println("No emitter found for user: " + username);
            return;
        }
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("notification")
                            .data(dto)
            );
        } catch (Exception e) {
            System.out.println("Error sending notification to user: " + username);
            emitters.remove(username);
        }

    }
}
