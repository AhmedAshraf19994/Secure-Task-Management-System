package com.ahmed.Secure.Task.Management.System.auth.session;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByUserIdAndRevokedFalse(int userId);

    @Transactional
    @Modifying
    @Query("UPDATE Session s Set revoked = true WHERE s.user.id = :userId AND revoked = false")
    void revokeUserAllActiveSessions(@Param("userId") int userId);
}
