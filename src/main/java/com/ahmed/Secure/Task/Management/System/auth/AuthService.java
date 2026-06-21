package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.auth.dto.LoginResponseTokens;
import com.ahmed.Secure.Task.Management.System.auth.refreshToken.RefreshToken;
import com.ahmed.Secure.Task.Management.System.auth.refreshToken.RefreshTokenService;
import com.ahmed.Secure.Task.Management.System.auth.session.Session;
import com.ahmed.Secure.Task.Management.System.auth.session.SessionResponseDto;
import com.ahmed.Secure.Task.Management.System.auth.session.SessionService;
import com.ahmed.Secure.Task.Management.System.client.jwtTokenCacheService.JwtTokenCacheService;
import com.ahmed.Secure.Task.Management.System.security.CurrentUserService;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import com.ahmed.Secure.Task.Management.System.user.MyUserPrinciple;
import com.ahmed.Secure.Task.Management.System.user.UserMapper;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final CurrentUserService currentUserService;


    private final JwtTokenCacheService jwtTokenCacheService;

    private final SessionService sessionService;


    private final RefreshTokenService refreshTokenService;


    public String registerUser (CreateUserDto createUserDto) {
        User user = userMapper.toUser(createUserDto);
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRole("user");

        this.userRepository.save(user);

        return "User Successfully Registered";
    }

    @Transactional
    public LoginResponseTokens login (LoginRequestDto loginRequestDto, String userAgent) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );

        MyUserPrinciple myUserPrinciple = (MyUserPrinciple) authentication.getPrincipal();

        Session session = this.sessionService.createSession(myUserPrinciple.getUser(), userAgent);

        String refreshToken = this.refreshTokenService.createToken(myUserPrinciple.getUser(), session);

        String accessToken = this.jwtService.createToken(myUserPrinciple);

        return new LoginResponseTokens(accessToken, refreshToken);
    }

    @Transactional
    public String logout (String token)  {
        RefreshToken refreshToken = this.refreshTokenService.findByToken(token).orElse(null);
        if(refreshToken == null) {
            return "Logout Success";
        }
        this.sessionService.revokeSession(refreshToken.getSession());
        this.refreshTokenService.revokeToken(refreshToken);
        return "Logout Success";
    }

    @Transactional
    public LoginResponseTokens rotateRefreshToken(String token) {
        RefreshToken refreshToken = this.refreshTokenService.findByToken(token).orElseThrow(
                () -> new InsufficientAuthenticationException("Invalid refresh token")
        );

        String newRefreshToken = this.refreshTokenService.rotate(refreshToken.getUser() ,refreshToken);

        User user = refreshToken.getUser();
        MyUserPrinciple myUserPrinciple = new MyUserPrinciple(user);

        String accessToken = this.jwtService.createToken(myUserPrinciple);

        return new LoginResponseTokens(accessToken, newRefreshToken);
    }

    @Transactional(readOnly = true)
    public List<SessionResponseDto> getUserActiveSessions () {
        int userId = this.currentUserService.getUserId();
        return this.sessionService.getUserActiveSessions(userId).stream()
                .map(session -> new SessionResponseDto(
                        session.getId(),
                        session.getUserAgent(),
                        session.getCreatedAt(),
                        session.getExpiresAt(),
                        session.isRevoked()
                )).toList();
    }
}
