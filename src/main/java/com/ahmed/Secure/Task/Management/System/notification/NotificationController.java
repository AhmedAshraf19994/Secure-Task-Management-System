package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.notification.dto.NotificationResponseDto;
import com.ahmed.Secure.Task.Management.System.system.PageResponseDto;
import com.ahmed.Secure.Task.Management.System.system.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Response<PageResponseDto<NotificationResponseDto>> getUnreadNotifications (Pageable pageable) {
        PageResponseDto<NotificationResponseDto> page = this.notificationService.getUnreadNotifications(pageable);

        return Response
                .<PageResponseDto<NotificationResponseDto>>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Get unread notifications success")
                .data(page)
                .build();
    }

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response<?> markAsRead (@PathVariable("notificationId") int notificationId) {
        this.notificationService.markAsRead(notificationId);

        return Response
                .builder()
                .flag(true)
                .code(HttpStatus.NO_CONTENT.value())
                .message("Mark notification as read success")
                .build();
    }

}
