package com.ahmed.Secure.Task.Management.System.idempotency.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.idempotency")
@Validated
public record IdempotencyProperties (
        @NotNull
        String headerName,
        @NotNull
        String keyPrefix,
        @NotNull
        boolean enabled

) {

}
