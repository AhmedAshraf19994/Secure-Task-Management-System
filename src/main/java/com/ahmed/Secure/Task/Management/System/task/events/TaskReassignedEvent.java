package com.ahmed.Secure.Task.Management.System.task.events;

public record TaskReassignedEvent(
        int taskId,
        int actorId,
        int oldAssigneeId,
        int newAssigneeId

) {
}
