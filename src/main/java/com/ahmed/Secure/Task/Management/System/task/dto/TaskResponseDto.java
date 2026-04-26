package com.ahmed.Secure.Task.Management.System.task.dto;

import com.ahmed.Secure.Task.Management.System.task.TaskPriority;
import com.ahmed.Secure.Task.Management.System.task.TaskStatus;
import com.ahmed.Secure.Task.Management.System.user.Dto.UserResponseDto;

import java.time.LocalDateTime;

public record TaskResponseDto (
        Integer id,
        String title,
        String description,
        LocalDateTime dueDate,
        TaskStatus status,
        TaskPriority priority,
        UserResponseDto createdBy,
        UserResponseDto assignedTo

) {
}
