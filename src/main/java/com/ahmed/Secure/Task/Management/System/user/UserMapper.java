package com.ahmed.Secure.Task.Management.System.user;

import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public User toUser (CreateUserDto createUserDto) {
       return User.builder()
                .email(createUserDto.email())
                .password(createUserDto.password())
                .name(createUserDto.name())
                .build();
    }
}
