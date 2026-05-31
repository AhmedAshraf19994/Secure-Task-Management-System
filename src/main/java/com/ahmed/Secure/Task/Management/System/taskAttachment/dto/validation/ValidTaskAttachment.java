package com.ahmed.Secure.Task.Management.System.taskAttachment.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Constraint(validatedBy=TaskAttachmentValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTaskAttachment {
    String message() default "Invalid task attachment";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
