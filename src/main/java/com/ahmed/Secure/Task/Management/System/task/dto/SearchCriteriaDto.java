package com.ahmed.Secure.Task.Management.System.task.dto;

import com.ahmed.Secure.Task.Management.System.task.TaskPriority;
import com.ahmed.Secure.Task.Management.System.task.TaskStatus;

import java.time.LocalDateTime;

public record SearchCriteriaDto(
        String title,
        LocalDateTime dueBefore,
        LocalDateTime dueAfter,
        TaskPriority priority,
        TaskStatus status
) {
}
