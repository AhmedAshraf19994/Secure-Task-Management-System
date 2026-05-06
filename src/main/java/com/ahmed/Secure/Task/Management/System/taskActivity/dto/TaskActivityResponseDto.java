package com.ahmed.Secure.Task.Management.System.taskActivity.dto;

import com.ahmed.Secure.Task.Management.System.taskActivity.TaskActivityType;

import java.time.Instant;

public record TaskActivityResponseDto(

        Integer id,

        TaskActivityType type,

        String description,

        String actorName,

        String oldAssigneeName,

        String newAssigneeName,

        Instant createdAt


) {
}
