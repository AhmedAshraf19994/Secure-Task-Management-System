package com.ahmed.Secure.Task.Management.System.taskAttachment;


import com.ahmed.Secure.Task.Management.System.client.fileStorage.FileStorageClient;
import com.ahmed.Secure.Task.Management.System.security.CurrentUserService;
import com.ahmed.Secure.Task.Management.System.system.exceptions.BusinessException;
import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.TaskRepository;
import com.ahmed.Secure.Task.Management.System.taskAttachment.config.TaskAttachmentProperties;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.AttachmentResponseDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.CreateTaskAttachmentDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.DownloadUrlResponseDto;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.UploadUrlResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAttachmentService {

    private final TaskRepository taskRepository;

    private final CurrentUserService currentUserService;

    private final FileStorageClient fileStorageClient;

    private final TaskAttachmentRepository taskAttachmentRepository;

    private final TaskAttachmentProperties taskAttachmentProperties;



    @Transactional
    public UploadUrlResponseDto createTaskAttachment(CreateTaskAttachmentDto createTaskAttachmentDto, int taskId) {
        //verify task exist
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ObjectNotFoundException("task", taskId));

//        verify task belong to the user
        if(!this.currentUserService.hasPermission(task.getCreatedBy().getId())) {
            throw new AccessDeniedException("No permission");
        }

        // Generate unique file name
        String objectKey = generateUniqueFileName(createTaskAttachmentDto.originalFileName(), taskAttachmentProperties.prefix(), taskId);

        // Generate pre-signed URL
        Instant expiresAt = Instant.now().plusSeconds(taskAttachmentProperties.urlExpirationSeconds()); // URL valid for 15 minutes
        String uploadUrl = this.fileStorageClient
                .generateUploadUrl(taskAttachmentProperties.containerName(), objectKey, createTaskAttachmentDto.type(), createTaskAttachmentDto.size(), expiresAt);

        // Save attachment metadata to database
        TaskAttachment taskAttachment = TaskAttachment
                .builder()
                .type(createTaskAttachmentDto.type())
                .originalFileName(createTaskAttachmentDto.originalFileName())
                .size(createTaskAttachmentDto.size())
                .objectKey(objectKey)
                .status(TaskAttachmentStatus.PENDING)
                .task(task)
                .expiresAt(expiresAt)
                .build();

        TaskAttachment savedTaskAttachment = this.taskAttachmentRepository.save(taskAttachment);

        log.info("Generated upload URL: attachmentId={} task={} key={} expires={}",
                savedTaskAttachment.getId(), taskId, objectKey, expiresAt);

        return new UploadUrlResponseDto(
                savedTaskAttachment.getId(),
                uploadUrl,
                savedTaskAttachment.getStatus(),
                savedTaskAttachment.getExpiresAt(),
                taskAttachmentProperties.maxFileSize()
                );
    }

    public void confirmUpload (int attachmentId, int taskId) {
        //verify task attachment exist
        TaskAttachment taskAttachment = this.taskAttachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new ObjectNotFoundException("taskAttachment", attachmentId));

        //verify the user
        boolean hasAuthority = this.currentUserService.isResourceOwner(taskAttachment.getCreatedBy().getId());
        if(!hasAuthority) {
            throw new AccessDeniedException("No permission");
        }

        // Verify file actually exists in storage before marking complete
        boolean fileExists = this.fileStorageClient.fileExists(taskAttachment.getObjectKey(), taskAttachmentProperties.containerName());
        if (!fileExists) {
            throw new BusinessException("File not found in storage");
        }

        //mark the task attachment status completed
        //keep this method nontransactional to prevent holding the database connection open during transaction waiting for network call
        this.taskAttachmentRepository.updateStatus(attachmentId, TaskAttachmentStatus.COMPLETED);

        log.info("Upload file success for attachment={}", attachmentId);
    }

    @Transactional(readOnly = true)
    public DownloadUrlResponseDto generateDownloadLink (int attachmentId, int taskId) {
        //verify attachment exists
        TaskAttachment taskAttachment = this.taskAttachmentRepository.findByIdAndTaskId(attachmentId, taskId)
                .orElseThrow(() -> new ObjectNotFoundException("taskAttachment", attachmentId));

        // verify status is completed
        if(taskAttachment.getStatus() != TaskAttachmentStatus.COMPLETED) {
            throw new BusinessException("File is not ready for download");
        }

        //verify user can view attachment
        boolean canViewTask = this.currentUserService.canViewTask(taskAttachment.getTask());
        if(!canViewTask) {
            throw new AccessDeniedException("No permission");
        }

        Instant expiresAt = Instant.now().plusSeconds(taskAttachmentProperties.urlExpirationSeconds()); // Url valid for 15 minutes

        String downloadUrl = this.fileStorageClient.generateDownloadUrl(taskAttachmentProperties.containerName(), taskAttachment.getObjectKey(), expiresAt);

        return new DownloadUrlResponseDto(downloadUrl, expiresAt);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAllAttachments(int taskId) {
        //verify task exists
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new ObjectNotFoundException("task", taskId));

        //verify user can view task
        boolean hasPermission = this.currentUserService.hasPermission(task.getCreatedBy().getId());
        if(!hasPermission) {
            throw new AccessDeniedException("No permission");
        }

        List<TaskAttachment> taskAttachments = this.taskAttachmentRepository.findByTaskId(taskId);

        return taskAttachments.stream().map(att -> new AttachmentResponseDto(
                att.getId(),
                att.getOriginalFileName(),
                att.getSize(),
                att.getType(),
                att.getStatus(),
                att.getCreatedAt(),
                att.getCreatedBy().getName()
        )).toList();
    }



    private String getFileExtension(String fileName) {
        String lowercaseFileName = fileName.toLowerCase();
        int lastDotIndex = lowercaseFileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // No extension found
        }
        return fileName.substring(lastDotIndex ).toLowerCase();
    }

    private String generateUniqueFileName(String originalFileName, String prefix, int taskId) {
        String uniqueName = UUID.randomUUID().toString() + getFileExtension(originalFileName);
        return prefix + taskId + "/" + uniqueName;
    }




}
