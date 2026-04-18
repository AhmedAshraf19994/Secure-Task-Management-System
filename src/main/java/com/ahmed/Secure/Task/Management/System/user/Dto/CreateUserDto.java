package com.ahmed.Secure.Task.Management.System.user.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateUserDto  (

        @NotBlank
        @Email
        String email,

        @NotBlank
        String name,

        @NotBlank
        String password

){
}
