package com.ahmed.Secure.Task.Management.System.task.dto;

import com.ahmed.Secure.Task.Management.System.task.TaskPriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTaskDto (

        @NotEmpty(message = "name is required")
         String title,
        String description,
        @NotNull
        @Future
        LocalDateTime dueDate,
        TaskPriority priority,
        Integer assignedTo

) {
}
