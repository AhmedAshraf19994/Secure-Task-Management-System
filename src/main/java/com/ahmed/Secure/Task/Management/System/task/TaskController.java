package com.ahmed.Secure.Task.Management.System.task;

import com.ahmed.Secure.Task.Management.System.system.PageResponseDto;
import com.ahmed.Secure.Task.Management.System.system.Response;
import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
import com.ahmed.Secure.Task.Management.System.task.dto.SearchCriteriaDto;
import com.ahmed.Secure.Task.Management.System.task.dto.TaskResponseDto;
import com.ahmed.Secure.Task.Management.System.task.dto.UpdateTaskDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response<TaskResponseDto> createTask (@Valid @RequestBody CreateTaskDto createTaskDto) {
        TaskResponseDto task = this.taskService.createTask(createTaskDto);

        return Response
                .<TaskResponseDto> builder()
                .flag(true)
                .code(HttpStatus.CREATED.value())
                .message("Create Task Success")
                .data(task)
                .build();
    }

    @GetMapping("/{taskId}")
    Response<TaskResponseDto> getTask (@PathVariable("taskId") int taskId) {
        TaskResponseDto task = this.taskService.getTask(taskId);

        return Response
                .<TaskResponseDto> builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Get Task Success")
                .data(task)
                .build();
    }

    @GetMapping
    Response<PageResponseDto<TaskResponseDto>> getAllTasks (Pageable pageable) {
        PageResponseDto<TaskResponseDto> page = this.taskService.getAllTasks(pageable);

        return Response
                .<PageResponseDto<TaskResponseDto>> builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Get All Tasks Success")
                .data(page)
                .build();
    }

    @PutMapping("/{taskId}")
    public Response<TaskResponseDto> updateTask (
            @PathVariable("taskId") int taskId
            , @RequestBody UpdateTaskDto updateTaskDto
            ) {
        TaskResponseDto task = this.taskService.updateTask(updateTaskDto, taskId);

        return Response.
                <TaskResponseDto>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Update Task Success")
                .data(task)
                .build();
    }

    @DeleteMapping("/{taskId}")
    public Response<?> deleteTask (@PathVariable("taskId") int taskId) {
        this.taskService.deleteTask(taskId);
        return Response
                .builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(null)
                .message("Delete Task Success")
                .build();
    }

    @PatchMapping("/{taskId}/assign/{assigneeId}")
    public Response<TaskResponseDto> assignTask (
            @PathVariable("taskId") int taskId,
             @PathVariable("assigneeId") int assigneeId
    ) throws AccessDeniedException {
        TaskResponseDto task = this.taskService.assignTask(taskId, assigneeId);

        return Response.
                <TaskResponseDto>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Assign Task Success")
                .data(task)
                .build();
    }

    @PostMapping("/search")
    Response<PageResponseDto<TaskResponseDto>> searchByCriteria (
            @RequestBody SearchCriteriaDto searchCriteriaDto,
            Pageable pageable
    ) {
        PageResponseDto<TaskResponseDto> page = this.taskService.searchByCriteria(searchCriteriaDto, pageable);

        return Response
                .<PageResponseDto<TaskResponseDto>> builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Search Success")
                .data(page)
                .build();
    }

}
