package com.ahmed.Secure.Task.Management.System.task;//package com.ahmed.Secure.Task.Management.System.task;
//
//import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
//import com.ahmed.Secure.Task.Management.System.task.dto.TaskResponseDto;
//import com.ahmed.Secure.Task.Management.System.task.dto.UpdateTaskDto;
//import com.ahmed.Secure.Task.Management.System.user.User;
//import com.ahmed.Secure.Task.Management.System.user.UserMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class TaskMapper {
//
//    private final UserMapper userMapper;
//
//    public Task toTask (CreateTaskDto createTaskDto, User assignedTo) {
//        return Task.builder()
//                .title(createTaskDto.title())
//                .description(createTaskDto.description())
//                .dueDate(createTaskDto.dueDate())
//                .assignedTo(assignedTo)
//                .priority(createTaskDto.priority() == null ? TaskPriority.LOW : createTaskDto.priority())
//                .build();
//    }
//
//    public TaskResponseDto toTaskResponseDto (Task task) {
//        return new TaskResponseDto(
//                task.getId(),
//                task.getTitle(),
//                task.getDescription(),
//                task.getDueDate(),
//                task.getStatus(),
//                task.getPriority(),
//                this.userMapper.toUserResponseDto(task.getCreatedBy()),
//                task.getAssignedTo() == null ? null : this.userMapper.toUserResponseDto(task.getAssignedTo())
//
//        );
//    }
//
//    public Task fromUpdateTaskDtoToTask (UpdateTaskDto updateTaskDto) {
//        return Task
//                .builder()
//                .title(updateTaskDto.title() !null )
//
//    }

import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
import com.ahmed.Secure.Task.Management.System.task.dto.TaskResponseDto;
import com.ahmed.Secure.Task.Management.System.task.dto.UpdateTaskDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserMapper;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface  TaskMapper {

    // from dto to entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "priority", source = "createTaskDto.priority",defaultValue = "LOW")
    Task toTask (CreateTaskDto createTaskDto, User assignedTo);

    // from entity to dto
    TaskResponseDto toTaskResponseDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedTo", ignore = true )
    @Mapping(target="createdBy", ignore = true)
   void updateTaskFromDto (UpdateTaskDto updateTaskDto, @MappingTarget Task task);

}
