package com.ahmed.Secure.Task.Management.System.system;

import com.ahmed.Secure.Task.Management.System.user.User;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Profile("dev")
public class DBTestInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User userA = User.builder().name("Ahmed")
                .email("ahmed@mail.com").password(passwordEncoder.encode("12345")).role("admin").enabled(true).build();
        User userB = User.builder().name("Eric")
                .email("eric@mail.com").password(passwordEncoder.encode("678910")).role("user").enabled(true).build();
        User userC = User.builder().name("Sara")
                .email("sara@mail.com").password(passwordEncoder.encode("678910")).role("user").enabled(false).build();

        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.save(userC);

    }
}
