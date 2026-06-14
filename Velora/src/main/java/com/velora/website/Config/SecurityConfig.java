package com.velora.website.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Quan trọng: Tắt bảo vệ CSRF để API gọi vào được
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/thuong-hieu/**", "/api/san-pham").permitAll() // Mở cửa cho mọi thứ trong /api/auth/
                .anyRequest().authenticated()
            );
        return http.build();
    }
}