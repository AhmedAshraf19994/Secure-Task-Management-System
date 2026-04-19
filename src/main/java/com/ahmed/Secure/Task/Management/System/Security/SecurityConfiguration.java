package com.ahmed.Secure.Task.Management.System.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomBearerAuthenticationEntryPoint customBearerAuthenticationEntryPoint;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final JwtCacheValidationFilter jwtCacheValidationFilter;

    @Value("${api.endpoint.base-url}")
    String base_url;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) {
       return http
               .authorizeHttpRequests(authorizeHttpRequests  -> authorizeHttpRequests
                       .requestMatchers(base_url + "/auth/login").permitAll()
                       .requestMatchers(base_url + "/auth/register").permitAll()
                               .requestMatchers( "/h2-console/**").permitAll()
                               .anyRequest().authenticated()
                       )
               .oauth2ResourceServer(oauth -> oauth
                       .jwt(Customizer.withDefaults())
                       .authenticationEntryPoint(customBearerAuthenticationEntryPoint)
                       .accessDeniedHandler(customAccessDeniedHandler)
               )
               .addFilterAfter(jwtCacheValidationFilter, BearerTokenAuthenticationFilter.class)
               .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
               .cors(AbstractHttpConfigurer::disable)
               .csrf(AbstractHttpConfigurer::disable)
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .build();
    }
}
