package com.ahmed.Secure.Task.Management.System.auth.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security")
@Validated
public record SecurityProps(

    @NotEmpty
     String jwtIssuer ,

    Duration jwtExpiration,

    Duration refreshTokenExpiration,

    String refreshTokenHmacSecret

) {}
