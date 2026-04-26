package com.ahmed.Secure.Task.Management.System.task.dto;

import com.ahmed.Secure.Task.Management.System.task.TaskPriority;
import com.ahmed.Secure.Task.Management.System.task.TaskStatus;

import java.time.LocalDateTime;

public record UpdateTaskDto (
        String title,
        String description,
        LocalDateTime dueDate,
        TaskStatus status,
        TaskPriority priority
){

}
