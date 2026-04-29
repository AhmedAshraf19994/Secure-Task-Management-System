package com.ahmed.Secure.Task.Management.System.task;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.task.dto.CreateTaskDto;
import com.ahmed.Secure.Task.Management.System.task.dto.UpdateTaskDto;
import com.redis.testcontainers.RedisContainer;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("dev")
public class TaskControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));;


    @Value("${api.endpoint.base-url}")
    String base_url;
    
    String accessToken;
    
    @BeforeEach
    void setUp () throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("ahmed@mail.com", "12345");
        String contentAsString = this.mockMvc.perform(post(base_url + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = this.objectMapper.readTree(contentAsString);
        String accessTokenFromResponse = jsonNode.get("data").asString();
        this.accessToken = "Bearer " + accessTokenFromResponse;

    }

    @Test
    void shouldCreateTaskWithAssignedUserSuccess () throws Exception {
        //given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        CreateTaskDto createTaskDto = new CreateTaskDto("Design Page", "test description", dueDate,TaskPriority.MEDIUM,2);

        //when then
        this.mockMvc.perform(post(base_url + "/tasks")
                .content(this.objectMapper.writeValueAsString(createTaskDto))
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Create Task Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Design Page"))
                .andExpect(jsonPath("$.data.description").value("test description"))
                .andExpect(jsonPath("$.data.dueDate").isNotEmpty())
                .andExpect(jsonPath("$.data.priority").value(TaskPriority.MEDIUM.toString()))
                .andExpect(jsonPath("$.data.status").value(TaskStatus.TODO.toString()))
                .andExpect(jsonPath("$.data.assignedTo.id").value(2))
                .andExpect(jsonPath("$.data.createdBy.id").value(1));
    }

    @Test
    void shouldCreateTaskWithNotAssignedUserSuccess () throws Exception {
        //given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        CreateTaskDto createTaskDto = new CreateTaskDto("Design Page", "test description", dueDate,TaskPriority.HIGH,null);

        //when then
        this.mockMvc.perform(post(base_url + "/tasks")
                        .content(this.objectMapper.writeValueAsString(createTaskDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Create Task Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Design Page"))
                .andExpect(jsonPath("$.data.description").value("test description"))
                .andExpect(jsonPath("$.data.dueDate").isNotEmpty())
                .andExpect(jsonPath("$.data.priority").value(TaskPriority.HIGH.toString()))
                .andExpect(jsonPath("$.data.status").value(TaskStatus.TODO.toString()))
                .andExpect(jsonPath("$.data.assignedTo").isEmpty())
                .andExpect(jsonPath("$.data.createdBy.id").value(1));
    }

    @Test
    void shouldCreateTaskWithDefaultPrioritySetToLowSuccess () throws Exception {
        //given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        CreateTaskDto createTaskDto = new CreateTaskDto("Design Page", "test description", dueDate,null,null);

        //when then
        this.mockMvc.perform(post(base_url + "/tasks")
                        .content(this.objectMapper.writeValueAsString(createTaskDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Create Task Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Design Page"))
                .andExpect(jsonPath("$.data.description").value("test description"))
                .andExpect(jsonPath("$.data.dueDate").isNotEmpty())
                .andExpect(jsonPath("$.data.priority").value(TaskPriority.LOW.toString()))
                .andExpect(jsonPath("$.data.status").value(TaskStatus.TODO.toString()))
                .andExpect(jsonPath("$.data.assignedTo").isEmpty())
                .andExpect(jsonPath("$.data.createdBy.id").value(1));
    }

    @Test
    void shouldCreateTaskWithAssignedUserNotFoundFail () throws Exception {
        //given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        CreateTaskDto createTaskDto = new CreateTaskDto("Design Page", "test description", dueDate,null,6);

        //when then
        this.mockMvc.perform(post(base_url + "/tasks")
                        .content(this.objectMapper.writeValueAsString(createTaskDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find user with id: 6"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldCreateTaskWithInvalidInputFail () throws Exception {
        //given
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        CreateTaskDto createTaskDto = new CreateTaskDto(null, "test description", dueDate,null,6);

        //when then
        this.mockMvc.perform(post(base_url + "/tasks")
                        .content(this.objectMapper.writeValueAsString(createTaskDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid input check data"))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void shouldGetTaskSuccess () throws Exception {
        //given
        int taskId = 1;

        //when then
        this.mockMvc.perform(get(base_url + "/tasks/{taskId}", taskId)
                .accept(MediaType.APPLICATION_JSON).header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Get Task Success"))
                .andExpect(jsonPath("$.data.id").value(taskId))
                .andExpect(jsonPath("$.data.title").value("edit home page"));
    }

    @Test
    void shouldGetTaskWithNotFoundTaskFail () throws Exception {
        //given
        int taskId = 4;

        //when then
        this.mockMvc.perform(get(base_url + "/tasks/{taskId}", taskId)
                .accept(MediaType.APPLICATION_JSON).header("Authorization", accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find task with id: 4"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldGetAllTasksSuccess () throws Exception {
        //given
        MultiValueMap<@NotNull String,String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "1");
        params.add("sort","id,desc");

        //when then
        this.mockMvc.perform(get(base_url + "/tasks")
                .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .params(params))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Get All Tasks Success"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(3))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.last").value(false))
                .andExpect(jsonPath("$.data.first").value(true));
    }

    @Test
    void shouldUpdateTaskSuccess () throws Exception {
        //given
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(4);
        UpdateTaskDto updateTaskDto = new UpdateTaskDto(
                "test update",
                "test description",
                newDueDate,
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH);

        //when then
        this.mockMvc.perform(put(base_url + "/tasks/{taskId}",1)
                        .header("Authorization", this.accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(updateTaskDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Update Task Success"))
                .andExpect(jsonPath("$.data.id").value(1)) //not updated
                .andExpect(jsonPath("$.data.title").value("test update"))
                .andExpect(jsonPath("$.data.description").value("test description"))
                .andExpect(jsonPath("$.data.dueDate", startsWith(newDueDate.toString().substring(0,6))))
                .andExpect(jsonPath("$.data.status").value(TaskStatus.IN_PROGRESS.toString()))
                .andExpect(jsonPath("$.data.priority").value(TaskPriority.HIGH.toString()))
                .andExpect(jsonPath("$.data.assignedTo.id").value(1)) //not updated
                .andExpect(jsonPath("$.data.createdBy.id").value(2)); //not updated
    }

    @Test
    void shouldUpdateTaskWithNotFoundTaskFail () throws Exception {
        //given
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(4);
        UpdateTaskDto updateTaskDto = new UpdateTaskDto(
                "test update",
                "test description",
                newDueDate,
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH);

        //when then
        this.mockMvc.perform(put(base_url + "/tasks/{taskId}",5)
                        .header("Authorization", this.accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(updateTaskDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find task with id: 5"))
                .andExpect(jsonPath("$.data").isEmpty()) ;
    }

    @Test
    void shouldDeleteTaskSuccess () throws Exception {
        //given
        int taskId = 1;

        //when then
        this.mockMvc.perform(delete(base_url + "/tasks/{taskId}", taskId)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", this.accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Delete Task Success"));

        this.mockMvc.perform(get(base_url + "/tasks/{taskId}", taskId)
                        .accept(MediaType.APPLICATION_JSON).header("Authorization", accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find task with id: 1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldDeleteWithNoFoundTaskFail () throws Exception {
        //given
        int taskId = 4;

        //when then
        this.mockMvc.perform(get(base_url + "/tasks/{taskId}", taskId)
                        .accept(MediaType.APPLICATION_JSON).header("Authorization", accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find task with id: 4"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldAssignTaskSuccess () throws Exception {
        //given
        int taskId = 3 ;
        int assigneeId= 2;

        //when then
        this.mockMvc.perform(patch(base_url + "/tasks/{taskId}/assign/{assigneeId}", taskId, assigneeId)
                .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization",this.accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Assign Task Success"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.assignedTo.id").value(2));
    }

    @Test
    void shouldAssignTaskWithWithAdminAuthoritySuccess () throws Exception { //given
        int taskId = 2 ;
        int assigneeId= 1;

        //when then
        this.mockMvc.perform(patch(base_url + "/tasks/{taskId}/assign/{assigneeId}", taskId, assigneeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization",this.accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Assign Task Success"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.assignedTo.id").value(1));

    }

    @Test
    void shouldAssignTaskWithTaskOwnerAuthoritySuccess () throws Exception {
        //login with user with id 2
        loginWithUserAuthority();

        //given
        int taskId = 1 ;
        int assigneeId= 3;

        //when then
        this.mockMvc.perform(patch(base_url + "/tasks/{taskId}/assign/{assigneeId}", taskId, assigneeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization",this.accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Assign Task Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.assignedTo.id").value(3));
    }

    @Test
    void shouldAssignTaskWithWithNotTaskOwnerAuthorityFail () throws Exception {
        //login with user with id 2
        loginWithUserAuthority();

        //given
        int taskId = 3 ;
        int assigneeId= 3;

        //when then
        this.mockMvc.perform(patch(base_url + "/tasks/{taskId}/assign/{assigneeId}", taskId, assigneeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization",this.accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("no permission"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void shouldAssignTaskWithWithNotFoundTaskFail () throws Exception {
        //given
        int taskId = 5 ;
        int assigneeId= 3;

        //when then
        this.mockMvc.perform(patch(base_url + "/tasks/{taskId}/assign/{assigneeId}", taskId, assigneeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization",this.accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find task with id: 5"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldAssignTaskWithWithNotFoundAssigneeFail () throws Exception {
        //given
        int taskId = 3 ;
        int assigneeId= 6;

        //when then
        this.mockMvc.perform(patch(base_url + "/tasks/{taskId}/assign/{assigneeId}", taskId, assigneeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization",this.accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find user with id: 6"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    private void loginWithUserAuthority () throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("eric@mail.com", "678910");
        String contentAsString = this.mockMvc.perform(post(base_url + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = this.objectMapper.readTree(contentAsString);
        String accessTokenFromResponse = jsonNode.get("data").asString();
        this.accessToken = "Bearer " + accessTokenFromResponse;
    }


}



