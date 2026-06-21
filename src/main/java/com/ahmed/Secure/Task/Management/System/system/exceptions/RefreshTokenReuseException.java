package com.ahmed.Secure.Task.Management.System.system.exceptions;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException() {
        super("Refresh token has been reused");
    }
}
