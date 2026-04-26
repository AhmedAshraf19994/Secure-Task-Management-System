package com.ahmed.Secure.Task.Management.System.config;

import com.ahmed.Secure.Task.Management.System.user.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApplicationAuditAware implements AuditorAware<User> {

    private final EntityManager entityManager;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()|| !(authentication.getPrincipal() instanceof Jwt)) {
            return Optional.empty();
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        assert jwt != null;
        Integer userId = Integer.parseInt(jwt.getClaim("sub"));

        return Optional.of(entityManager.getReference(User.class, userId));
    }
}
