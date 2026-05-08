package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.redis.testcontainers.RedisContainer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TaskActivityControllerIntegrationTest {

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
    void shouldGetAllTaskActivitiesSuccess () throws Exception {
        //given
        int taskId = 1;
        MultiValueMap<@NotNull String, String> params = new LinkedMultiValueMap<>();
        params.add("page","1");
        params.add("size","1");
        params.add("sort","createdAt,desc");

        //when then
        this.mockMvc.perform(MockMvcRequestBuilders.get(base_url + "/tasks/{taskId}/activities", taskId)
                .accept(MediaType.APPLICATION_JSON)
                                .params(params)
                                .header("Authorization", this.accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Get All Activities Success"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.last").value(false))
                .andExpect(jsonPath("$.data.first").value(false));
    }

}
