package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.TaskRepository;
import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final MessageGenerator  messageGenerator;

    private final ApplicationEventPublisher publisher;

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
        notificationRepository.save(notification);
    }

}


