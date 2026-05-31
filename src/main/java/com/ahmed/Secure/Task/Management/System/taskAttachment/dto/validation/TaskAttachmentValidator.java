package com.ahmed.Secure.Task.Management.System.taskAttachment.dto.validation;

import com.ahmed.Secure.Task.Management.System.taskAttachment.config.TaskAttachmentProperties;
import com.ahmed.Secure.Task.Management.System.taskAttachment.dto.CreateTaskAttachmentDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAttachmentValidator implements ConstraintValidator<ValidTaskAttachment, CreateTaskAttachmentDto> {

    private final TaskAttachmentProperties taskAttachmentProperties;

    @Override
    public boolean isValid(CreateTaskAttachmentDto dto, ConstraintValidatorContext context) {
        //fallback to basic validation
        if (dto == null || dto.originalFileName() == null || dto.type() == null || dto.size() == null) {
            return true;
        }

        //disable the generic method
        context.disableDefaultConstraintViolation();

        // 1. Validate Size
        if (dto.size() > taskAttachmentProperties.maxFileSize()) {
            context.buildConstraintViolationWithTemplate("File size must be less than or equal to " + taskAttachmentProperties.maxFileSize())
                    .addPropertyNode("size")
                    .addConstraintViolation();
            return false;
        }

        // 2. Validate Type
        if (!taskAttachmentProperties.allowedFileTypes().contains(dto.type())) {
            context.buildConstraintViolationWithTemplate("Not supported file type: " + dto.type())
                    .addPropertyNode("type")
                    .addConstraintViolation();
            return false;
        }

        // 3. Validate Extension
        String fileExtension = getFileExtension(dto.originalFileName());
        if (!taskAttachmentProperties.allowedFileExtensions().contains(fileExtension)) {
            context.buildConstraintViolationWithTemplate("Not supported file extension: " + fileExtension)
                    .addPropertyNode("originalFileName")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    //including the dot
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf ).toLowerCase();
    }
}

