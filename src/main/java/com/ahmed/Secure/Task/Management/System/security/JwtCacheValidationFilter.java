package com.ahmed.Secure.Task.Management.System.security;

import com.ahmed.Secure.Task.Management.System.client.jwtTokenCacheService.JwtTokenCacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtCacheValidationFilter extends OncePerRequestFilter {

    private final JwtTokenCacheService  jwtTokenCacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // only execute when bearer token exists
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            if (!jwtTokenCacheService.hasToken(accessToken)) {
                throw new InvalidBearerTokenException("invalid token");
            }
        }

       filterChain.doFilter(request,response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login") || path.startsWith("/h2-console") ;
    }
}
