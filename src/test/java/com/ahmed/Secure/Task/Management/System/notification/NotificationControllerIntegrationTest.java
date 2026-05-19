package com.ahmed.Secure.Task.Management.System.notification;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.redis.testcontainers.RedisContainer;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("dev")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String accessToken;

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));


    @BeforeEach
    void setup() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("ahmed@mail.com", "12345");
        String contentAsString = this.mockMvc.perform(post(baseUrl + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = this.objectMapper.readTree(contentAsString);
        String accessTokenFromResponse = jsonNode.get("data").asString();
        this.accessToken = "Bearer " + accessTokenFromResponse;

    }

    @Test
    void shouldReturnUnreadNotifications() throws Exception {

        mockMvc.perform(
                        get( baseUrl+ "/notifications")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", accessToken)

                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.flag")
                        .value(true))

                .andExpect(jsonPath("$.code")
                        .value(200))

                .andExpect(jsonPath("$.message")
                        .value("Get unread notifications success"))

                .andExpect(jsonPath("$.data.content.length()")
                        .value(1))

                .andExpect(jsonPath("$.data.content[0].message")
                        .value("Ahmed assigned task Login Bug"))

                .andExpect(jsonPath("$.data.content[0].isRead")
                        .value(false))

                .andExpect(jsonPath("$.data.totalElements")
                        .value(1))

                .andExpect(jsonPath("$.data.page")
                        .value(0));
    }

    @Test
    void shouldMarkNotificationAsReadSuccess() throws Exception {
        // given
        int notificationId = 1;  // from DBTestInitializer - unread notification for user with id 1

        // when then
        this.mockMvc.perform(MockMvcRequestBuilders.patch(baseUrl + "/notifications/{notificationId}/read", notificationId)
                        .header("Authorization", this.accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(204))
                .andExpect(jsonPath("$.message").value("Mark notification as read success"));

        // Verify notification is marked as read in database
        Notification notificationAfter = this.notificationRepository.findById(notificationId).orElseThrow();
        assertTrue(notificationAfter.isRead());
    }

    @Test
    void shouldMarkNotificationAsReadNotFoundFail() throws Exception {
        // given
        int notificationId = 5;

        // when then
        this.mockMvc.perform(patch(baseUrl + "/notifications/{notificationId}/read", notificationId)
                        .header("Authorization", this.accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("could not find notification with id: 5"));
    }
    }