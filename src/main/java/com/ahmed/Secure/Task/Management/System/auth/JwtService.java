package com.ahmed.Secure.Task.Management.System.auth;

import com.ahmed.Secure.Task.Management.System.auth.config.SecurityProps;
import com.ahmed.Secure.Task.Management.System.user.MyUserPrinciple;
import com.ahmed.Secure.Task.Management.System.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    private final SecurityProps securityProps;

        public String createToken (MyUserPrinciple myUserPrinciple) {
        User user = myUserPrinciple.getUser();

        List<String> authorities = myUserPrinciple.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();

        Instant now = Instant.now();

        Instant expiresAt = now.plus(securityProps.jwtExpiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(
                        securityProps.jwtIssuer()
                )
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("authorities", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    }
}
