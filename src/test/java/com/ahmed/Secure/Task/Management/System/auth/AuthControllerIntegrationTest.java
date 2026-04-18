package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("dev")
public class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    private String base_url;

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
        this.mockMvc.perform(post(base_url + "/auth/login")
                .content(this.objectMapper.writeValueAsString(loginRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.message").value("Login Success"));
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
    } @Test
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
}
