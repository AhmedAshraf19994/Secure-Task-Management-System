package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.user.MyUserPrinciple;
import com.ahmed.Secure.Task.Management.System.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;


    public String createToken (Authentication authentication, long durationInHours) {
        MyUserPrinciple myUserPrinciple = (MyUserPrinciple) authentication.getPrincipal();
        assert myUserPrinciple != null;
        User user = myUserPrinciple.getUser();

        List<String> authorities = myUserPrinciple.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();

        Instant now = Instant.now();
      
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(durationInHours, ChronoUnit.HOURS))
                .subject(user.getId().toString())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    }
}
