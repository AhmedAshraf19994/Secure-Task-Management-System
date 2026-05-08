package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.system.PageResponseDto;
import com.ahmed.Secure.Task.Management.System.system.Response;
import com.ahmed.Secure.Task.Management.System.taskActivity.dto.TaskActivityResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/tasks/")
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("{taskId}/activities")
    public Response<PageResponseDto<TaskActivityResponseDto>> getActivitiesByTaskId (
            @PathVariable("taskId") int taskId,
            Pageable pageable
    )  {
        PageResponseDto<TaskActivityResponseDto> pageResponseDto = this.taskActivityService.getActivitiesByTaskId(taskId, pageable);

        return Response
                .<PageResponseDto<TaskActivityResponseDto>>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .message("Get All Activities Success")
                .data(pageResponseDto)
                .build();

    }


}
