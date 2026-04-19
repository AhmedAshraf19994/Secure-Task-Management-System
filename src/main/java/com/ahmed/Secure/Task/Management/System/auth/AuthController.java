package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.system.Response;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(("${api.endpoint.base-url}/auth/"))
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<?> registerUser (@Valid  @RequestBody CreateUserDto createUserDto) {
        String message = this.authService.registerUser(createUserDto);

        return Response
                .builder()
                .flag(true)
                .code(HttpStatus.CREATED.value())
                .data(null)
                .message(message)
                .build();
    }

    @PostMapping("login")
    public Response<String> login (@Valid @RequestBody LoginRequestDto loginRequestDto) {
        String accessToken = this.authService.login(loginRequestDto);

        return Response
                .<String>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(accessToken)
                .message("Login Success")
                .build();
    }

    @PostMapping("/logout")
    public Response<?> logout (HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InsufficientAuthenticationException("missing header authorization");
        }
        String accessToken = authHeader.substring(7);


        String message = this.authService.logout(accessToken);

        return Response
                .builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(null)
                .message(message)
                .build();
    }




}
