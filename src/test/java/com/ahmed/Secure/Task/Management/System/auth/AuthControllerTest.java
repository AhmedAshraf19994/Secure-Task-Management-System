package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.client.jwtTokenCacheService.JwtTokenCacheService;
import com.ahmed.Secure.Task.Management.System.config.ApplicationAuditAware;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;


    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtTokenCacheService jwtTokenCacheService;


    @Value("${api.endpoint.base-url}/auth")
     String base_url;

    @Test
    void shouldRegisterUserSuccess () throws Exception {
        //given
        CreateUserDto createUserDto = new CreateUserDto("test@test.com","Ahmed", "unencryptedPassword");

        given(this.authService.registerUser(createUserDto)).willReturn("User Successfully Registered");

        //when then
        this.mockMvc.perform(post(base_url + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(createUserDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("User Successfully Registered"));
    }

}