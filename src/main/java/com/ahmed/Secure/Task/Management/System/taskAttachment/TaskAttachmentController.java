package com.ahmed.Secure.Task.Management.System.taskAttachment;

import com.ahmed.Secure.Task.Management.System.system.Response;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.AttachmentResponseDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.CreateTaskAttachmentDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.DownloadUrlResponseDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.UploadUrlResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/tasks/{taskId}/attachments")
public class TaskAttachmentController {

    private final TaskAttachmentService taskAttachmentService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<UploadUrlResponseDto> createAttachmentUploadUrl(
            @PathVariable("taskId") int taskId,
            @Valid @RequestBody  CreateTaskAttachmentDto createTaskAttachmentDto
            )  {
        UploadUrlResponseDto uploadUrlResponseDto = taskAttachmentService.createTaskAttachment(createTaskAttachmentDto, taskId);
        return Response
                .<UploadUrlResponseDto>builder()
                .flag(true)
                .code(HttpStatus.CREATED.value())
                .message("Upload URL generated successfully")
                .data(uploadUrlResponseDto)
                .build();
    }



    @PostMapping("/{attachmentId}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Response<?> confirmUpload (
            @PathVariable("attachmentId") int attachmentId,
            @PathVariable("taskId") int taskId
    ) {
        this.taskAttachmentService.confirmUpload(attachmentId, taskId);
        return Response.builder()
                .flag(true)
                .code(HttpStatus.NO_CONTENT.value())
                .data(null)
                .message("Upload confirmed successfully")
                .build();
    }

    @GetMapping("/{attachmentId}/download")
    public Response<DownloadUrlResponseDto> getDownloadLink (
            @PathVariable("attachmentId") int attachmentId,
            @PathVariable("taskId") int taskId
    ) {
        DownloadUrlResponseDto downloadUrlResponseDto = this.taskAttachmentService.generateDownloadLink(attachmentId,taskId);

        return Response
                .<DownloadUrlResponseDto>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(downloadUrlResponseDto)
                .message("Download URL generated successfully")
                .build();
    }

    @GetMapping
    public Response<List<AttachmentResponseDto>> getAllAttachments (@PathVariable("taskId") int taskId) {
        List<AttachmentResponseDto> attachments = this.taskAttachmentService.getAllAttachments(taskId);

        return Response
                .<List<AttachmentResponseDto>>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(attachments)
                .message("Get all attachments success")
                .build();
    }




}
