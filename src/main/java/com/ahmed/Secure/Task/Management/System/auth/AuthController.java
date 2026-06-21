package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.auth.dto.LoginResponseTokens;
import com.ahmed.Secure.Task.Management.System.auth.session.Session;
import com.ahmed.Secure.Task.Management.System.auth.session.SessionResponseDto;
import com.ahmed.Secure.Task.Management.System.system.Response;
import com.ahmed.Secure.Task.Management.System.system.exceptions.MissingRefreshTokenException;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.endpoint.base-url}/auth/")
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
    public Response<String> login (
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
            HttpServletResponse response
    ) {
        LoginResponseTokens tokens = this.authService.login(loginRequestDto, userAgent);

        //set refresh token cookie
        ResponseCookie  refreshTokenCookie = ResponseCookie
                .from("refreshToken", tokens.refreshToken())
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(7))
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return Response
                .<String>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(tokens.accessToken())
                .message("Login Success")
                .build();
    }

    @PostMapping("/logout")
    public Response<?> logout (@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new MissingRefreshTokenException();
        }

        String message = this.authService.logout(refreshToken);


        //set refresh token cookie
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return Response
                .builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(null)
                .message(message)
                .build();
    }

    @PostMapping("/refresh")
    public Response<String> refreshToken (@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if(refreshToken == null || refreshToken.isEmpty()){
            throw new MissingRefreshTokenException();
        }

        LoginResponseTokens tokens = this.authService.rotateRefreshToken(refreshToken);
        ResponseCookie  refreshTokenCookie = ResponseCookie
                .from("refreshToken", tokens.refreshToken())
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(7))
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", refreshTokenCookie.toString()); // <-- add this

        return Response
                .<String>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(tokens.accessToken())
                .message("Token Refreshed")
                .build();
    }

    @GetMapping("/sessions")
    public Response<List<SessionResponseDto>> getUserActiveSessions () {
        List<SessionResponseDto> sessions = this.authService.getUserActiveSessions();

        return Response
                .<List<SessionResponseDto>>builder()
                .flag(true)
                .code(HttpStatus.OK.value())
                .data(sessions)
                .message("Sessions retrieved successfully")
                .build();
    }

}
