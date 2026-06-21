package com.ahmed.Secure.Task.Management.System.system.exceptions;

public class MissingRefreshTokenException extends RuntimeException {
    public MissingRefreshTokenException() {
        super("Refresh token is missing from the request.");
    }
}
