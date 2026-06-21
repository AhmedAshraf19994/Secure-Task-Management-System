package com.ahmed.Secure.Task.Management.System.auth.refreshToken;

import com.ahmed.Secure.Task.Management.System.auth.config.SecurityProps;
import com.ahmed.Secure.Task.Management.System.auth.session.Session;
import com.ahmed.Secure.Task.Management.System.system.exceptions.InvalidRefreshTokenException;
import com.ahmed.Secure.Task.Management.System.system.exceptions.ObjectNotFoundException;
import com.ahmed.Secure.Task.Management.System.system.exceptions.RefreshTokenReuseException;
import com.ahmed.Secure.Task.Management.System.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final HashService hashService;

    private static final SecureRandom random = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecurityProps securityProperties;

    public String createToken(User user, Session session) {
       String rawToken = generateRawToken();

       String hashedToken = this.hashService.hash(rawToken);

       Instant expiresAt = Instant.now().plus(securityProperties.refreshTokenExpiration());

       RefreshToken refreshToken = RefreshToken.builder()
               .user(user)
               .tokenHash(hashedToken)
               .expiresAt(expiresAt)
               .session(session)
               .build();

       this.refreshTokenRepository.save(refreshToken);

       return rawToken;
    }

    public Optional<RefreshToken> findByToken(String rawToken) {
        String tokenHash = this.hashService.hash(rawToken);
        return this.refreshTokenRepository.findByTokenHash(tokenHash);
    }

    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.markRevoked();
        this.refreshTokenRepository.save(refreshToken);
    }

    public String rotate (User user,RefreshToken token) {

        if ( token.isExpired()) {
            throw new InvalidRefreshTokenException();
        }

        if(token.isRevoked()) {
            //reuse detection, revoke all user sessions alert user
            throw new RefreshTokenReuseException();
        }

        token.markRevoked();
        this.refreshTokenRepository.save(token);
        return this.createToken(user,token.getSession());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32]; // 256 bits
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
