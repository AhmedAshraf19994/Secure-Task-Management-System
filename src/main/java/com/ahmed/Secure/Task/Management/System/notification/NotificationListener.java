package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("notificationExecutor")
    public void handleTaskAssignedEvent(TaskAssignedEvent event) {
        try {
            this.notificationService.createNotification(
                   event,
                    NotificationType.TASK_ASSIGNED
            );

        } catch (Exception e) {
            // Log the exception and event
            log.error("Failed to create notification for TaskAssignedEvent: {}", event, e);
        }

    }
}
