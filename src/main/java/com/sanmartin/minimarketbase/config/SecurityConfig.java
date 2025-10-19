package com.sanmartin.minimarketbase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // ✅ TODAS las requests permitidas sin login
            )
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable());
        
        return http.build();
    }
}