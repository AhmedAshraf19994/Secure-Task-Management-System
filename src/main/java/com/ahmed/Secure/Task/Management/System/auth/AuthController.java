package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.system.Response;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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




}
