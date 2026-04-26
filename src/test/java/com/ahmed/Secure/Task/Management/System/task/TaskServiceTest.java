package com.ahmed.Secure.Task.Management.System.task;

import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
import com.ahmed.Secure.Task.Management.System.task.dto.TaskResponseDto;
import com.ahmed.Secure.Task.Management.System.user.Dto.UserResponseDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
class TaskServiceTest {

    @Mock
    TaskMapper taskMapper;

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TaskService taskService;

//    void shouldSaveTaskAssignedToUserSuccess () {
//        //given
//        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
//        User createdBy = User.builder().id(1).build();
//        User assignedTo = User.builder().id(2).build();
//        UserResponseDto createdByuserResponseDto = new UserResponseDto(1, "ahmed", "test@mail", "user");
//        UserResponseDto assignedToResponseDto = new UserResponseDto(1, "ahmed", "test@mail", "user");
//        Task task = Task.builder().id(1).title("design page").assignedTo(assignedTo).build();
//        TaskResponseDto taskResponseDto = new TaskResponseDto(
//                1, "design page","test", dueDate,TaskStatus.TODO, TaskPriority.LOW,);
//        CreateTaskDto createTaskDto = new CreateTaskDto("test","descTest", dueDate,null,1 );
//        given(this.userRepository.findById(1)).willReturn(Optional.of(user));
//        given(this.taskMapper.toTask(Mockito.any(CreateTaskDto.class),Mockito.any(User.class))).willReturn(task);
//        given(this.taskRepository.save(task)).willReturn(task);
//        given(this.taskMapper.toTaskResponseDto(task)).willReturn()
//
//        //when
//        //then
//    }

    @Test
    void shouldFailWithNoAssignedToUserFound () {

    }

}