package com.ahmed.Secure.Task.Management.System.emailService;

import com.ahmed.Secure.Task.Management.System.emailService.emailTemplates.EmailTemplate;
import com.ahmed.Secure.Task.Management.System.emailService.emailTemplates.TaskAssignedEmailTemplate;
import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async("emailServiceExecutor")
    public void handleTaskAssignedEvent(TaskAssignedEvent event) {
        try {
            // generate the task assigned email template
            EmailTemplate taskAssignedEmail = new TaskAssignedEmailTemplate(
                    event.assigneeEmail(),
                    event.taskTitle(),
                    event.assigneeName(),
                    event.actorName()
            );

            this.emailService.sendHtmlEmail(taskAssignedEmail);

        } catch (Exception e) {
            // Log the exception and continue without affecting the main transaction
            log.error("Failed to send task assigned email for taskId {}: {}", event.taskId(), e.getMessage());

        }

    }
}
