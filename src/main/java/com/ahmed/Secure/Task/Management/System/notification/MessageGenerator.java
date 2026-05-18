package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.springframework.stereotype.Service;

@Service
public class MessageGenerator {

    public String generateMessage(NotificationType type, User actor, Task task) {
        return switch (type) {
            case TASK_ASSIGNED -> actor.getName() + " assigned you a new task: " + task.getTitle() + "priority: " + task.getPriority();
        };
    }

}
