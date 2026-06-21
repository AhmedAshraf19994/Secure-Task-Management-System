package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.auth.refreshToken.RefreshTokenRepository;
import com.ahmed.Secure.Task.Management.System.auth.session.SessionRepository;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import com.redis.testcontainers.RedisContainer;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hibernate.validator.internal.util.Contracts.assertNotEmpty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
public class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Value("${api.endpoint.base-url}")
    private String base_url;

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @BeforeEach
    void cleanDb() {
        // keep tests idempotent: remove sessions / refresh tokens before each run
        this.refreshTokenRepository.deleteAll();
        this.sessionRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccess () throws Exception {
        //given
        CreateUserDto createUserDto = new CreateUserDto("test@test.com", "Ahmed", "12345");

        //when then
        this.mockMvc.perform(post(base_url + "/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(createUserDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("User Successfully Registered"));

    }


    @Test
    void shouldRegisterUserFailWithBadInput () throws Exception {
        //given
        CreateUserDto createUserDto = new CreateUserDto(null, null, "12345");

        //when then
        this.mockMvc.perform(post(base_url + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(createUserDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid input check data"))
                .andExpect(jsonPath("$.data",hasSize(2)));
    }

    @Test
    void shouldLoginUserSuccessWithValidUser () throws Exception {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto("ahmed@mail.com","12345");

        //when then
        MvcResult loginResult = this.mockMvc.perform(post(base_url + "/auth/login")
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.message").value("Login Success"))
                .andReturn();

        // then: Set-Cookie header present and contains refreshToken
        String setCookieHeader = loginResult.getResponse().getHeader("Set-Cookie");
        assertThat(setCookieHeader).as("Set-Cookie must be present after login").isNotNull();
        assertThat(setCookieHeader).contains("refreshToken=");

        // Session repository: should have at least one active session for user
        List<?> sessions = this.sessionRepository.findByUserIdAndRevokedFalse(1); // user id = 1
        assertFalse(sessions.isEmpty());


        // Refresh tokens: at least one token saved and linked to a session
        List<?> refreshTokens = this.refreshTokenRepository.findAll();
        assertFalse(refreshTokens.isEmpty());

    }

    @Test
    void shouldLoginUserFailWithNoUserFound () throws Exception {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto("ahmed1@mail.com","12345");

        //when then
        this.mockMvc.perform(post(base_url + "/auth/login")
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("username or password is wrong"));
    }

    @Test
    void shouldLoginUserFailWithWrongPassword () throws Exception {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto("ahmed@mail.com","123456");

        //when then
        this.mockMvc.perform(post(base_url + "/auth/login")
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("username or password is wrong"));
    }

    @Test
    void shouldLoginUserFailWithAccountIsNotActive () throws Exception {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto("sara@mail.com","678910");

        //when then
        this.mockMvc.perform(post(base_url + "/auth/login")
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Account is not active"));
    }

    @Test
    void shouldRequestFailWithInvalidToken () throws Exception {
        //given
        String token = "Bearer invalidToken";

        //when then
        this.mockMvc.perform(post(base_url + "/auth/logout")
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRotateRefreshTokenSuccess() throws Exception {
        // given: login to obtain a refresh token cookie
        String email = "ahmed@mail.com";
        String password = "12345";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        MvcResult loginResult = this.mockMvc.perform(post(base_url + "/auth/login")
                        .content(this.objectMapper.writeValueAsString(loginRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Cookie oldRawRefreshToken = loginResult.getResponse().getCookie("refreshToken");

        // when: call refresh endpoint with cookie
        MvcResult refreshResult = this.mockMvc.perform(post(base_url + "/auth/refresh")
                        .cookie(Objects.requireNonNull(oldRawRefreshToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // then: response contains new Set-Cookie for refreshToken and access token in body
        String newRawRefreshToken = refreshResult.getResponse().getCookie("refreshToken").getValue();
        assertThat(newRawRefreshToken).isNotBlank();
        // rotated token should differ from original
        assertThat(newRawRefreshToken).isNotEqualTo(oldRawRefreshToken);

        String refreshResponseBody = refreshResult.getResponse().getContentAsString();
        assertThat(refreshResponseBody).contains("\"data\"");
    }

    @Test
    void shouldLogoutClearCookieAndRevokeSession () throws Exception {
        // given: login to obtain a refresh token cookie and create session
        String email = "ahmed@mail.com";
        String password = "12345";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        MvcResult loginResult = this.mockMvc.perform(post(base_url + "/auth/login")
                        .content(this.objectMapper.writeValueAsString(loginRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        String accessToken = this.objectMapper.readTree(responseString).get("data").asString();


        Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");;


        // ensure there is an active session before logout
        List<?> beforeSessions = this.sessionRepository.findByUserIdAndRevokedFalse(1); // user id = 1
        assertFalse(beforeSessions.isEmpty());

        // when: call logout with refresh cookie
        MvcResult logoutResult = this.mockMvc.perform(post(base_url + "/auth/logout")
                        .header("Authorization", "Bearer" + " " + accessToken)
                        .cookie(Objects.requireNonNull(refreshTokenCookie))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then: Set-Cookie header to clear cookie (Max-Age=0) present
        String logoutSetCookie = logoutResult.getResponse().getHeader("Set-Cookie");
        assertThat(logoutSetCookie).isNotNull();
        assertThat(logoutSetCookie).contains("refreshToken=");
        // assert cookie cleared via Max-Age=0
        assertThat(logoutSetCookie).contains("Max-Age=0");

        // DB: no active sessions for user after logout
        List<?> afterSessions = this.sessionRepository.findByUserIdAndRevokedFalse(1); // user id = 1
        assertTrue(afterSessions.isEmpty());


    }


}
