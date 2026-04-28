package com.ahmed.Secure.Task.Management.System.system;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.task.TaskPriority;
import com.ahmed.Secure.Task.Management.System.task.TaskRepository;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
@Profile("dev")
public class DBTestInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TaskRepository taskRepository;

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
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now().plusDays(2))
                        .createdBy(userA)
                .build();

                this.taskRepository.save(taskA);
                this.taskRepository.save(taskB);
                this.taskRepository.save(taskC);
    }
}
