package com.ahmed.Secure.Task.Management.System.system.exceptions;

public class IdempotencyCacheException extends RuntimeException {
    public IdempotencyCacheException(String message) {
        super(message);
    }

    public IdempotencyCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
