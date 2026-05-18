package com.ahmed.Secure.Task.Management.System.emailService.emailTemplates;

import java.util.Map;

public record TaskAssignedEmailTemplate(
            String to,
            String taskTitle,
            String assigneeName,
            String actorName

) implements EmailTemplate {
    @Override
    public String subject() {
        return "Task assigned";
    }

    @Override
    public String templateName() {
        return "task-assigned-email";
    }

    @Override
    public Map<String, Object> templateVariables() {
        return Map.of(
                "taskTitle", taskTitle,
                "assigneeName", assigneeName,
                "actorName", actorName

        );
    }
}
