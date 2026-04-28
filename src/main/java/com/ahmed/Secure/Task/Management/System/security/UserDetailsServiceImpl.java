package com.ahmed.Secure.Task.Management.System.security;

import com.ahmed.Secure.Task.Management.System.user.MyUserPrinciple;
import com.ahmed.Secure.Task.Management.System.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(MyUserPrinciple::new)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

    };
}
