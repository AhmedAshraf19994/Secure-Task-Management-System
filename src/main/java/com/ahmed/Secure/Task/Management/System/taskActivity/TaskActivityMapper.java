package com.ahmed.Secure.Task.Management.System.taskActivity;


import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.events.TaskReassignedEvent;
import com.ahmed.Secure.Task.Management.System.taskActivity.dto.TaskActivityResponseDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface TaskActivityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actor", source = "actor")
    @Mapping(target = "newAssignee", source = "assignee")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "type", constant = "TASK_ASSIGNED")
    @Mapping(target = "description", expression = "java(createTaskAssignedDescription(actor, assignee))")
    TaskActivity toAssignedTaskActivity(Task task, User actor, User assignee);

    @Mapping(target = "actorName", source = "actor.name")
    @Mapping(target = "newAssigneeName", source = "newAssignee.name")
    @Mapping(target = "oldAssigneeName", source = "oldAssignee.name")
    TaskActivityResponseDto toTaskActivityResponseDto(TaskActivity savedTaskActivity);

    default String createTaskAssignedDescription(User actor, User assignee) {
        return actor.getName() + " assigned task to: " + assignee.getName();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actor", source = "actor")
    @Mapping(target = "oldAssignee", source = "oldAssignee")
    @Mapping(target = "newAssignee", source = "newAssignee")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "type", constant = "TASK_REASSIGNED")
    @Mapping(target = "description", expression = "java(createTaskReassignedDescription(actor, oldAssignee, newAssignee))")
    TaskActivity toTaskReassignedActivity(Task task, User actor, User oldAssignee, User newAssignee);

    default String createTaskReassignedDescription (User actor, User oldAssignee, User newAssignee) {
        return actor.getName() + " reassigned task from: " + oldAssignee.getName() + " to: " + newAssignee.getName();
    }
}
