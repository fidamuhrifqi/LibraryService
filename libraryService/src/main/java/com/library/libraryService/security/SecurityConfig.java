package com.library.libraryService.security;

import com.library.libraryService.common.handler.CustomAccessDeniedHandler;
import com.library.libraryService.security.jwt.JwtAuthFilter;
import com.library.libraryService.security.jwt.JwtAuthenticationEntryPoint;
import com.library.libraryService.security.ratelimit.RateLimiterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimiterFilter rateLimiterFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/users/login",
                        "/users/verify-otp"
                ).permitAll()
                .requestMatchers("/users/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/audit/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/articles/**").authenticated()
                .anyRequest().permitAll()
        );

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        );

        http.addFilterBefore(rateLimiterFilter, UsernamePasswordAuthenticationFilter.class).addFilterBefore(jwtAuthFilter, RateLimiterFilter.class);

        return http.build();
    }
}