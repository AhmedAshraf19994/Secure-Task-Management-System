package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.dto.LoginRequestDto;
import com.ahmed.Secure.Task.Management.System.user.Dto.CreateUserDto;
import com.ahmed.Secure.Task.Management.System.user.MyUserPrinciple;
import com.ahmed.Secure.Task.Management.System.user.UserMapper;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ahmed.Secure.Task.Management.System.user.User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public String registerUser (CreateUserDto createUserDto) {
        User user = userMapper.toUser(createUserDto);
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setRole("user");

        this.userRepository.save(user);

        return "User Successfully Registered";
    }

    public String login (LoginRequestDto loginRequestDto) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );

        return this.jwtService.createToken(authentication,2); //create access token that lasts for 2 hours
    }
}
