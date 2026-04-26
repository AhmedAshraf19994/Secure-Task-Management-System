package com.ahmed.Secure.Task.Management.System.task;

import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
import com.ahmed.Secure.Task.Management.System.task.dto.TaskResponseDto;
import com.ahmed.Secure.Task.Management.System.task.dto.UpdateTaskDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Transactional

public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final UserRepository userRepository;


    public TaskResponseDto createTask (CreateTaskDto createTaskDto) {
        User assignedTo = null ;
        //fetch the assignedTo user
        if(createTaskDto.assignedTo() != null) {
            assignedTo = this.userRepository.findById(createTaskDto.assignedTo())
                    .orElseThrow(() ->  new ObjectNotFoundException( "user",createTaskDto.assignedTo()));
        }

        Task task = this.taskMapper.toTask(createTaskDto, assignedTo);
        Task savedTask = this.taskRepository.save(task);
        return this.taskMapper.toTaskResponseDto(savedTask);
    }

    public TaskResponseDto getTask (int taskId) {
        Task task = this.taskRepository.findById(taskId).orElseThrow(
                () -> new ObjectNotFoundException("task",taskId)
        );

     return this.taskMapper.toTaskResponseDto(task);
    }

    public List<TaskResponseDto> getAllTasks () {
        List<Task> tasks = this.taskRepository.findAll();
        return tasks.stream().map(taskMapper::toTaskResponseDto).collect(Collectors.toList());
    }

    public TaskResponseDto updateTask (UpdateTaskDto updateTaskDto, int taskId) {
        // find the task
        Task oldTask = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ObjectNotFoundException("task", taskId));

        //update the task and skipping null values through mapper
        this.taskMapper.updateTaskFromDto(updateTaskDto, oldTask);

        return this.taskMapper.toTaskResponseDto(oldTask);
    }

    public void deleteTask (int taskId) {
        Task task = this.taskRepository.findById(taskId).orElseThrow(
                () -> new ObjectNotFoundException("task", taskId)
        );
        this.taskRepository.delete(task);
    }
}
