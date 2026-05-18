package com.ahmed.Secure.Task.Management.System.notification;


import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "message", source = "message")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "actor", source = "actor")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "receiver", source = "receiver")
    public Notification toNotification(Task task, User actor, User receiver, String message, NotificationType type);
}
