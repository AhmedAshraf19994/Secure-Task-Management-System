package com.ahmed.Secure.Task.Management.System.task;

import com.ahmed.Secure.Task.Management.System.security.CurrentUserService;
import com.ahmed.Secure.Task.Management.System.system.PageResponseDto;
import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
import com.ahmed.Secure.Task.Management.System.task.dto.SearchCriteriaDto;
import com.ahmed.Secure.Task.Management.System.task.dto.TaskResponseDto;
import com.ahmed.Secure.Task.Management.System.task.dto.UpdateTaskDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@RequiredArgsConstructor
@Service
@Transactional

public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final UserRepository userRepository;

    private final CurrentUserService currentUserService;


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

    public PageResponseDto<TaskResponseDto> getAllTasks (Pageable pageable) {
        //validate the max number of tasks to query
        if(pageable.getPageSize() > 50 ) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    50,
                    pageable.getSort()
            );
        }

        Page<Task> pageOfTasks = this.taskRepository.findAll(pageable);

        List<TaskResponseDto> tasks = pageOfTasks.getContent().stream().map(taskMapper::toTaskResponseDto).toList();

        return PageResponseDto
                .<TaskResponseDto>builder()
                .content(tasks)
                .page(pageOfTasks.getNumber())
                .size(pageOfTasks.getSize())
                .totalElements(pageOfTasks.getTotalElements())
                .totalPages(pageOfTasks.getTotalPages())
                .isFirst(pageOfTasks.isFirst())
                .isLast(pageOfTasks.isLast())
                .build();
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

    public TaskResponseDto assignTask (int taskId, int assigneeId) throws AccessDeniedException {
        //fetch the task
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ObjectNotFoundException("task", taskId));

        //prevent reassigning to the same user
        if(task.getAssignedTo() != null && task.getAssignedTo().getId() == assigneeId) {
            return this.taskMapper.toTaskResponseDto(task);
        }

        //authorize only task owner or admin can do that
        // manual check could have used preAuthorize but don't want to hit the  database twice for task retrieval
        if(!this.currentUserService.hasAuthority(task.getCreatedBy().getId())) {
            throw new AccessDeniedException("no permission");
        }

        //fetch the assigne user
        User userTobeAssignedTo = this.userRepository.findById(assigneeId)
                .orElseThrow(() -> new ObjectNotFoundException("user", assigneeId));

        // doing the reassign
        task.setAssignedTo(userTobeAssignedTo);

        return this.taskMapper.toTaskResponseDto(task);
    }

    public PageResponseDto<TaskResponseDto> searchByCriteria (SearchCriteriaDto searchCriteriaDto, Pageable pageable) {
        Specification<Task> spec = Specification.unrestricted();

        if (searchCriteriaDto.status() != null) {
            spec = spec.and(TaskSpecifications.hasStatus(searchCriteriaDto.status()));
        }

        if (searchCriteriaDto.priority() != null) {
            spec = spec.and(TaskSpecifications.hasPriority(searchCriteriaDto.priority()));
        }

        if (StringUtils.hasText(searchCriteriaDto.title())) {
            spec = spec.and(TaskSpecifications.titleContains(searchCriteriaDto.title()));
        }

        if (searchCriteriaDto.dueBefore() != null && searchCriteriaDto.dueAfter() == null) {
           spec = spec.and(TaskSpecifications.dueBefore(searchCriteriaDto.dueBefore()));
        }

        if (searchCriteriaDto.dueBefore() != null && searchCriteriaDto.dueAfter() != null) {
           spec = spec.and(TaskSpecifications.dueBetween(searchCriteriaDto.dueBefore(), searchCriteriaDto.dueAfter()));
        }

        Page<Task> pageOfTasks = this.taskRepository.findAll(spec, pageable);

        List<TaskResponseDto> tasks = pageOfTasks.getContent().stream().map(taskMapper::toTaskResponseDto).toList();

        return PageResponseDto
                .<TaskResponseDto>builder()
                .content(tasks)
                .page(pageOfTasks.getNumber())
                .size(pageOfTasks.getSize())
                .totalPages(pageOfTasks.getTotalPages())
                .totalElements(pageOfTasks.getTotalElements())
                .isLast(pageOfTasks.isLast())
                .isFirst(pageOfTasks.isFirst())
                .build();
    }

}
