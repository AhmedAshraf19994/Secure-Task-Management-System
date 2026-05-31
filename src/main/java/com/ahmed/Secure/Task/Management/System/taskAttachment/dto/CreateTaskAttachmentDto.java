package com.ahmed.Secure.Task.Management.System.taskAttachment.dto;

import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.validation.ValidTaskAttachment;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;


@ValidTaskAttachment
public record CreateTaskAttachmentDto(
        @NotEmpty(message = "Original file name is required")
        String originalFileName,
        @NotEmpty(message = "File type is required")
        String type,
        @Positive(message = "File size must be positive")
        Long size
        ) {
}
