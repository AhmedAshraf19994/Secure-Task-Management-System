package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import com.ahmed.Secure.Task.Management.System.task.events.TaskReassignedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskActivityListener {

    private final TaskActivityService taskActivityService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskActivityExecutor")
    public void handleTaskAssignedEvent (TaskAssignedEvent event) {
        try {
            this.taskActivityService.handleTaskAssigned(event);

        } catch (Exception exception) {
            log.error("Failed to handle  task assigning event:{}", event, exception);
        }
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskActivityExecutor")
    public void handleTaskReassignedEvent (TaskReassignedEvent event) {
        try {
            this.taskActivityService.handleTaskReassigned(event);

        } catch (Exception exception) {
            log.error("Failed to handle task reassigning event:{}", event, exception);
        }
        
    }



}
