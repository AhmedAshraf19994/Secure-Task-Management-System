package com.ahmed.Secure.Task.Management.System.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrentUserService {

    public Integer getUserId () {
            String userIdAsString = getJwtPrincipal().getSubject(); //which is userId as string
            return Integer.parseInt(userIdAsString);
    }

    public boolean isAdmin () {
            List<String> authorities = getJwtPrincipal().getClaim("authorities");
            return authorities.stream().anyMatch(authority -> authority.equals("ROLE_admin"));
    }
    public boolean isResourceOwner (int resourceOwnerId) {
            return getUserId() == resourceOwnerId ;
    }

    private Jwt getJwtPrincipal () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return (Jwt) authentication.getPrincipal();
    }

    public boolean hasAuthority (int resourceOwnerId) {
        return isResourceOwner(resourceOwnerId) || isAdmin();
    }
}
