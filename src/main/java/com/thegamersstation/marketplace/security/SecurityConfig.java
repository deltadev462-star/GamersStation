package com.thegamersstation.marketplace.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origin-patterns:}")
    private String additionalCorsOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Authentication
                .requestMatchers(
                    "/auth/otp/request",
                    "/auth/otp/verify",
                    "/auth/refresh",
                    "/auth/logout"
                ).permitAll()
                
                // Public endpoints - Documentation
                .requestMatchers(
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // Public endpoints - Actuator health
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                
                // Public endpoints - Read-only access to categories and cities
                .requestMatchers(HttpMethod.GET, 
                    "/categories/**",
                    "/cities/**"
                ).permitAll()
                
                // Public endpoints - Read posts (search, browse)
                .requestMatchers(HttpMethod.GET, 
                    "/posts",
                    "/posts/**"
                ).permitAll()
                
                // Public endpoints - Static resources (uploaded files)
                .requestMatchers("/uploads/**").permitAll()
                
                // Public endpoints - WebSocket handshake and SockJS info
                .requestMatchers(
                    "/ws",
                    "/ws/**"
                ).permitAll()
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Production origins + any additional origins from config (dev overrides)
        List<String> origins = new java.util.ArrayList<>(Arrays.asList(
            "https://thegamersstation.com",
            "https://www.thegamersstation.com",
            "https://gamers-station.com",
            "https://www.gamers-station.com"
        ));
        if (additionalCorsOrigins != null && !additionalCorsOrigins.isBlank()) {
            origins.addAll(Arrays.asList(additionalCorsOrigins.split(",")));
        }
        configuration.setAllowedOriginPatterns(origins);
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Accept-Language",
            "X-Requested-With"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
