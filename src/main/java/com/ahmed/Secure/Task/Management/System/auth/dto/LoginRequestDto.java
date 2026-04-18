package com.ahmed.Secure.Task.Management.System.auth.dto;

import org.antlr.v4.runtime.misc.NotNull;

public record LoginRequestDto(
        String email,
        String password
) {
}
