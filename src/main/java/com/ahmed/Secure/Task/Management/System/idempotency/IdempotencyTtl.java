package com.ahmed.Secure.Task.Management.System.idempotency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum IdempotencyTtl {

    DEFAULT(Duration.ofHours(1)),
    TWO_HOURS(Duration.ofHours(2)),
    ONE_DAY(Duration.ofHours(24));

    private final Duration duration;

}
