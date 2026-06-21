package com.ahmed.Secure.Task.Management.System.auth.refreshToken;

import com.ahmed.Secure.Task.Management.System.auth.session.Session;
import com.ahmed.Secure.Task.Management.System.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash"),
                @Index(name = "idx_refresh_tokens_session_id", columnList = "session_id")
        }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue
    private int id;

    @Column (name = "token_hash", nullable = false, unique = true, updatable = false)
    private String tokenHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    public void markRevoked() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }
    
}
