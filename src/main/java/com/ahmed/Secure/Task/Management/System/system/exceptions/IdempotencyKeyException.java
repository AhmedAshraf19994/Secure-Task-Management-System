package com.ahmed.Secure.Task.Management.System.system.exceptions;

public class IdempotencyKeyException extends RuntimeException {
    public IdempotencyKeyException(String message) {
        super(message);
    }


}
