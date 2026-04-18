package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserMapper;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserMapper userMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    void shouldCreateUserSuccess () {
        //given
        CreateUserDto createUserDto = new CreateUserDto("test@test.com","Ahmed", "unencryptedPassword");
        User savedUser = User.builder().id(1).name("Ahmed").email("test@test.com").password("encryptedPassword").build();

        given(this.userMapper.toUser(createUserDto)).willReturn(savedUser);
        given (this.userRepository.save(Mockito.any(User.class))).willReturn(savedUser);
        given(this.passwordEncoder.encode(anyString())).willReturn("encryptedPassword");

        //when
        String result = this.authService.registerUser(createUserDto);

        //then
        assertEquals("User Successfully Registered", result);
        verify(this.userMapper, times(1)).toUser(createUserDto);
        verify(this.userRepository, times(1)).save(savedUser);
    }

    @Test
    void shouldLoginUserSuccess () {
        //given
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@test.com", "12345");
        User user = User.builder().id(1).name("Ahmed").email("test@test.com").password("encryptedPassword").build();

//        given()
        //when
        //then
    }



}