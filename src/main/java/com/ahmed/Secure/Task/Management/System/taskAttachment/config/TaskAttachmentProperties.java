package com.ahmed.Secure.Task.Management.System.taskAttachment.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app.task-attachments-storage")
public record TaskAttachmentProperties(
        String containerName,
        String prefix,
        Long maxFileSize,
        Set<String> allowedFileTypes,
        Set<String> allowedFileExtensions,
        int urlExpirationSeconds
) {

}
