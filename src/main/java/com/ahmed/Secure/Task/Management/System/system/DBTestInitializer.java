package com.ahmed.Secure.Task.Management.System.system;

import com.ahmed.Secure.Task.Management.System.notification.Notification;
import com.ahmed.Secure.Task.Management.System.notification.NotificationRepository;
import com.ahmed.Secure.Task.Management.System.notification.NotificationType;
import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.TaskPriority;
import com.ahmed.Secure.Task.Management.System.task.TaskRepository;
import com.ahmed.Secure.Task.Management.System.taskActivity.TaskActivity;
import com.ahmed.Secure.Task.Management.System.taskActivity.TaskActivityRepository;
import com.ahmed.Secure.Task.Management.System.taskActivity.TaskActivityType;
import com.ahmed.Secure.Task.Management.System.taskAttachment.TaskAttachment;
import com.ahmed.Secure.Task.Management.System.taskAttachment.TaskAttachmentRepository;
import com.ahmed.Secure.Task.Management.System.taskAttachment.TaskAttachmentStatus;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
@Profile("dev")
public class DBTestInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TaskRepository taskRepository;

    private final TaskActivityRepository taskActivityRepository;

    private final NotificationRepository notificationRepository;

    private final TaskAttachmentRepository taskAttachmentRepository;

    @Override
    public void run(String... args) throws Exception {
        User userA = User.builder().name("Ahmed")
                .email("ahmed@mail.com").password(passwordEncoder.encode("12345")).role("admin").enabled(true).build();
        User userB = User.builder().name("Eric")
                .email("eric@mail.com").password(passwordEncoder.encode("678910")).role("user").enabled(true).build();
        User userC = User.builder().name("Sara")
                .email("sara@mail.com").password(passwordEncoder.encode("678910")).role("user").enabled(false).build();

        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

        Task taskA = Task.builder()
                .title("edit home page")
                .description("modify the nav bar")
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(2))
                .createdBy(userB)
                .assignedTo(userA)
                .build();

                Task taskB = Task.builder()
                .title("add auth to backend ")
                .description("implement auth feature in the backend ")
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(2))
                        .createdBy(userC)
                .assignedTo(userB)
                .build();

                Task taskC = Task.builder()
                .title("dockerize the app")
                .description("add docker file and create image to run the app")
                .priority(TaskPriority.LOW)
                .dueDate(LocalDateTime.now().plusDays(6))
                        .createdBy(userA)
                .build();Task taskD = Task.builder()
                .title("dockerize the app")
                .description("add docker file and create image to run the app")
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(8))
                        .createdBy(userA)
                .build();Task taskE = Task.builder()
                .title("dockerize the app")
                .description("add docker file and create image to run the app")
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDateTime.now().plusDays(10))
                        .createdBy(userA)
                .build();

                this.taskRepository.save(taskA);
                this.taskRepository.save(taskB);
                this.taskRepository.save(taskC);
                this.taskRepository.save(taskD);
                this.taskRepository.save(taskE);

                 TaskActivity taskActivityA = TaskActivity.builder()
                .task(taskA)
                .actor(userA)
                .oldAssignee(userC)
                .description("marc assigned task to: ali")
                .type(TaskActivityType.TASK_REASSIGNED)
                .newAssignee(userC)
                .build();
                TaskActivity taskActivityB = TaskActivity.builder()
                .task(taskA)
                .actor(userA)
                .oldAssignee(userC)
                .description("marc assigned task to: ali")
                .type(TaskActivityType.TASK_REASSIGNED)
                .newAssignee(userC)
                .build();
                TaskActivity taskActivityC = TaskActivity.builder()
                .task(taskA)
                .actor(userA)
                .description("marc assigned task to: ali")
                .type(TaskActivityType.TASK_ASSIGNED)
                .newAssignee(userC)
                .build();
                TaskActivity taskActivityD = TaskActivity.builder()
                .task(taskB)
                .actor(userA)
                .description("marc assigned task to: ali")
                .type(TaskActivityType.TASK_ASSIGNED)
                .newAssignee(userC)
                .build();

                this.taskActivityRepository.save(taskActivityA);
                this.taskActivityRepository.save(taskActivityB);
                this.taskActivityRepository.save(taskActivityC);
                this.taskActivityRepository.save(taskActivityD);

                //notification for taskA reassignment
        Notification notification =
                Notification.builder()
                        .message("Ahmed assigned task Login Bug")
                        .isRead(false)
                        .type(NotificationType.TASK_ASSIGNED)
                        .receiver(userA)
                        .actor(userB)
                        .task(taskA)
                        .build();

        this.notificationRepository.save(notification);


    }
}
