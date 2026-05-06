package com.ahmed.Secure.Task.Management.System.task.events;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;

public record TaskAssignedEvent(
        int taskId,
        int actorId,
        int assigneeId
) {
}
