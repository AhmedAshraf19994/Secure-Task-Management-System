package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.TaskRepository;
import com.ahmed.Secure.Task.Management.System.task.events.TaskAssignedEvent;
import com.ahmed.Secure.Task.Management.System.task.events.TaskReassignedEvent;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;

    private final TaskActivityMapper taskActivityMapper;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    @Transactional
    public void handleTaskAssigned (TaskAssignedEvent event) {
        //fetch the task
        Task task = getTask(event.taskId());
        //fetch the actor
        User actor = getUser(event.actorId());
        //fetch the assignee
        User assignee = getUser(event.assigneeId());


        TaskActivity taskActivity = this.taskActivityMapper.toAssignedTaskActivity(task, actor, assignee);

        this.taskActivityRepository.save(taskActivity);

        log.info("Successfully created assigned activity for taskId={}, actorId={}, assigneeId={} ",
                event.taskId(), event.actorId(), event.assigneeId()
                );
    }

    @Transactional
    public void handleTaskReassigned(TaskReassignedEvent event) {
        //fetch the task
        Task task = getTask(event.taskId());
        //fetch the actor
        User actor = getUser(event.actorId());
        //fetch the oldAssignee
        User oldAssignee = getUser(event.oldAssigneeId());
        //fetch the newAssignee
        User newAssignee = getUser(event.newAssigneeId());

        TaskActivity taskActivity = this.taskActivityMapper.toTaskReassignedActivity(task, actor, oldAssignee, newAssignee);

        //save in database
        this.taskActivityRepository.save(taskActivity);

        log.info("Successfully created reassigned activity for taskId={}, actorId={}, oldAssigneeId={}, newAssigneeId={} ",
                event.taskId(), event.actorId(), event.oldAssigneeId(), event.newAssigneeId()
        );
    }

    private User getUser(int userId)  {
         return this.userRepository.findById(userId)
                .orElseThrow(()-> new ObjectNotFoundException("user", userId));
    }

    private Task getTask (int taskId) {
        return this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ObjectNotFoundException("task", taskId));
    }
}
