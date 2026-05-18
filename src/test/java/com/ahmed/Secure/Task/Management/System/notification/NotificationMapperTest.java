package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    @Autowired
    NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);

    @Test
    void toNotification() {
        //given
        Task task = Task.builder().id(1).build();
         User actor = User.builder().id(1).build();
         User receiver = User.builder().id(2).build();
         NotificationType type = NotificationType.TASK_ASSIGNED;
         String message = "Ahmed has assigned you a new task.";

        //when
        Notification notification = this.notificationMapper.toNotification(task, actor, receiver, message, type);

        //then
        assertEquals(task , notification.getTask());
        assertEquals(actor , notification.getActor());
        assertEquals(receiver , notification.getReceiver());
        assertEquals(message , notification.getMessage());
        assertEquals(type , notification.getType());
        assertNull(notification.getCreatedAt());
        assertNull(notification.getId());
        assertFalse(notification.isRead());
    }
}