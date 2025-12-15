package com.example.OLSHEETS.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        // Disable all security for tests - this is safe as it only applies to test profile
        http
            .csrf(csrf -> csrf.disable()) // NOSONAR java:S4502 - Safe: test environment only
            .cors(cors -> cors.disable()) // NOSONAR - Safe: test environment only
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}