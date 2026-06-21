package com.ahmed.Secure.Task.Management.System.system.exceptions;

import com.ahmed.Secure.Task.Management.System.system.Response;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Response<?> handleMethodArgumentNoValidException (MethodArgumentNotValidException exception) {
        Map<String, Object> errorsResult = new HashMap<>();

        List<ObjectError> errors = exception.getBindingResult().getAllErrors();
        errors.forEach(error -> {
            String value = error.getDefaultMessage();
            String key = ((FieldError) error).getField();
            errorsResult.put(key, value);
        });

        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Invalid input check data")
                .data(errors)
                .build();

    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Response<?> handleUsernameNotFoundExceptionAndBadCredentialsException (Exception exception) {
        return Response.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .flag(false)
                .message("username or password is wrong")
                .data(null)
                .build();
    }


    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Response<?> handleAccountStatusException (AccountStatusException exception) {
        return Response.builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .flag(false)
                .message("Account is not active")
                .data(null)
                .build();
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Response<?> handleInsufficientAuthenticationException (InsufficientAuthenticationException exception) {
            return Response.builder()
                    .flag(false)
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .data(null)
                    .message("please login")
                    .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Response<?> handleAccessDeniedException (AccessDeniedException exception) {
            return Response.builder()
                    .flag(false)
                    .code(HttpStatus.FORBIDDEN.value())
                    .data(null)
                    .message("no permission")
                    .build();
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Response<?> handleInvalidBearerTokenException (InvalidBearerTokenException exception) {
            return Response.builder()
                    .flag(false)
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .data(null)
                    .message("Invalid token")
                    .build();
    }

    // to catch unhandled exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Response<?> handleException (Exception exception) {
        return Response.builder()
                .flag(false)
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .data(exception.getMessage())
                .build();
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Response<?> handleObjectNotFoundException (ObjectNotFoundException exception) {
        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .data(null)
                .build();
    }

    @ExceptionHandler(CustomEmailSendingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Response<?> handleCustomEmailSendingException (CustomEmailSendingException exception) {
        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .data(exception.getCause().getMessage())
                .build();
    }

    @ExceptionHandler(CustomFileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Response<?> handleCustomFileStorageException (CustomFileStorageException exception) {
        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(IdempotencyCacheException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    Response<?> handleIdempotencyCacheException (IdempotencyCacheException exception) {
        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(exception.getMessage())
                .build();
    }
    @ExceptionHandler(IdempotencyKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Response<?> handleIdempotencyCacheException (IdempotencyKeyException exception) {
        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler({MissingRefreshTokenException.class,InvalidRefreshTokenException.class,RefreshTokenReuseException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Response<?> handleMissingRefreshTokenException (Exception exception) {
        return Response
                .builder()
                .flag(false)
                .code(HttpStatus.UNAUTHORIZED.value())
                .message(exception.getMessage())
                .build();
    }



}
