package com.ahmed.Secure.Task.Management.System.auth.session;

import com.ahmed.Secure.Task.Management.System.auth.config.SecurityProps;
import com.ahmed.Secure.Task.Management.System.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    private final SecurityProps securityProperties;

    public Session createSession(User user, String userAgent) {
        Instant expiresAt = Instant.now().plus(securityProperties.refreshTokenExpiration());
        Session session = Session.builder()
                .user(user)
                .userAgent(userAgent)
                .expiresAt(expiresAt)
                .build();
        return sessionRepository.save(session);
    }

    public void revokeSession (Session session) {
        session.markRevoked();
        this.sessionRepository.save(session);
    }

    public List<Session> getUserActiveSessions(int userId) {
        return this.sessionRepository.findByUserIdAndRevokedFalse(userId);
    }

    public void revokeUserAllActiveSessions (int userId) {
        this.sessionRepository.revokeUserAllActiveSessions(userId);
    }

}
