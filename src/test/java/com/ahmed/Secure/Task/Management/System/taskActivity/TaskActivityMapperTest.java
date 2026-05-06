package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class TaskActivityMapperTest {


    private final TaskActivityMapper taskActivityMapper = Mappers.getMapper(TaskActivityMapper.class);

    @Test
    void toAssignedTaskActivity() {
        //given
        User actor = User.builder().id(1).name("marc").build();
        User assignee = User.builder().id(2).name("ali").build();
        Task task = Task.builder().id(1).build();

        //when
        TaskActivity taskActivity = this.taskActivityMapper.toAssignedTaskActivity(task, actor, assignee);

        //then
        assertEquals(actor.getId(), taskActivity.getActor().getId());
        assertEquals(assignee.getId(), taskActivity.getNewAssignee().getId());
        assertNull(taskActivity.getOldAssignee());
        assertNull(taskActivity.getCreatedAt());
        assertEquals(task.getId(), taskActivity.getTask().getId());
        assertEquals("marc assigned task to: ali", taskActivity.getDescription());
        assertEquals(TaskActivityType.TASK_ASSIGNED, taskActivity.getType());

    }



    @Test
    void createTaskAssignedDescription() {
        //given
        User actor = User.builder().id(1).name("marc").build();
        User assignee = User.builder().id(2).name("ali").build();

        //when
        String description = this.taskActivityMapper.createTaskAssignedDescription(actor, assignee);

        //then
        assertEquals("marc assigned task to: ali", description);
    }

    @Test
    void toReassignedTaskActivity() {
        //given
        User actor = User.builder().id(1).name("marc").build();
        User oldAssignee = User.builder().id(1).name("ahmed").build();
        User newAssignee = User.builder().id(2).name("ali").build();
        Task task = Task.builder().id(1).build();

        //when
        TaskActivity taskActivity = this.taskActivityMapper.toTaskReassignedActivity(task, actor, oldAssignee, newAssignee);

        //then
        assertEquals(actor.getId(), taskActivity.getActor().getId());
        assertEquals(newAssignee.getId(), taskActivity.getNewAssignee().getId());
        assertEquals(oldAssignee.getId(), taskActivity.getOldAssignee().getId());
        assertNull(taskActivity.getCreatedAt());
        assertEquals(task.getId(), taskActivity.getTask().getId());
        assertEquals("marc reassigned task from: ahmed to: ali", taskActivity.getDescription());
        assertEquals(TaskActivityType.TASK_REASSIGNED, taskActivity.getType());
    }


    @Test
    void createTaskReassignedDescription() {
        //given
        User actor = User.builder().id(1).name("marc").build();
        User newAssignee = User.builder().id(2).name("ali").build();
        User oldAssignee = User.builder().id(3).name("ahmed").build();

        //when
        String description = this.taskActivityMapper.createTaskReassignedDescription(actor, oldAssignee, newAssignee);

        //then
        assertEquals("marc reassigned task from: ahmed to: ali", description);
    }
}