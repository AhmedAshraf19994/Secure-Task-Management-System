package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.notification.dto.NotificationResponseDto;
import com.ahmed.Secure.Task.Management.System.notification.realtimeNotification.RealtimeNotificationService;
import com.ahmed.Secure.Task.Management.System.security.CurrentUserService;
import com.ahmed.Secure.Task.Management.System.system.PageResponseDto;
import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.TaskRepository;
import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final MessageGenerator  messageGenerator;

    private final NotificationMapper notificationMapper;

    private final RealtimeNotificationService realtimeMessagingService;

    private final CurrentUserService currentUserService;

    @Transactional
    public void createNotification(TaskAssignedEvent event, NotificationType type) {
        //fetch the task
        Task task = this.taskRepository.findById(event.taskId())
                .orElseThrow(() -> new ObjectNotFoundException("task", event.taskId()));
        //fetch the actor
        User actor = this.userRepository.findById(event.actorId())
                .orElseThrow(() -> new ObjectNotFoundException("user", event.actorId()));
        //fetch the receiver
        User receiver = this.userRepository.findById(event.assigneeId())
                .orElseThrow(() -> new ObjectNotFoundException("user", event.assigneeId()));

        String message = this.messageGenerator.generateMessage(type, actor, task);

        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .type(type)
                .task(task)
                .actor(actor)
                .receiver(receiver)
                .build();

        // persist the notification
        Notification savedNotification = this.notificationRepository.save(notification);


        try {
            this.realtimeMessagingService.sendToUser(
                    receiver.getId().toString(),
                    this.notificationMapper.toRealtimeNotificationDto(savedNotification)
            );

        } catch (Exception e) {
            log.debug("Failed to send real-time notification to user {}: {}", receiver.getId(), e.getMessage());
        }
    }


    public PageResponseDto<NotificationResponseDto> getUnreadNotifications(Pageable pageable) {

        int userId = this.currentUserService.getUserId();

        Page< Notification> page = this.notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponseDto> content = page.getContent().stream()
                .map(notificationMapper::toNotificationResponseDto)
                .toList();

        return PageResponseDto
                .<NotificationResponseDto>builder()
                .content(content)
                .size(page.getSize())
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    @Transactional
    public void markAsRead(int notificationId) {
        Notification notification = this.notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ObjectNotFoundException("notification", notificationId));

        // Ensure the current user is the receiver of the notification
        int userId = this.currentUserService.getUserId();
        if (notification.getReceiver().getId() != userId) {
            throw new SecurityException("You are not authorized to mark this notification as read.");
        }

        notification.setRead(true);
        this.notificationRepository.save(notification);
    }
}


