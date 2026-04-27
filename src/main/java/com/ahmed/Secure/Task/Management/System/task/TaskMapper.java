package com.ahmed.Secure.Task.Management.System.task;//package com.ahmed.Secure.Task.Management.System.task;

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
