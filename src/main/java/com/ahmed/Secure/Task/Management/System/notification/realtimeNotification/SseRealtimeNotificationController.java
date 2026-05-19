package com.ahmed.Secure.Task.Management.System.notification.realtimeNotification;


import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}")
public class SseRealtimeNotificationController {

    private final SseRealtimeNotification realtimeNotificationService;

    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamNotifications(Authentication authentication) {
        String userId = authentication.getName(); //the bearer token is sent through cookies, so we can use the same authentication process oauth2resourceServer
        return realtimeNotificationService.subscribe(userId);
    }
}
