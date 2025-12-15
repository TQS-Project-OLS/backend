package com.example.OLSHEETS.config;

import com.example.OLSHEETS.security.JwtAuthenticationFilter;
import com.example.OLSHEETS.security.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return new JwtUtil() {
            @Override
            public String extractUsername(String token) {
                return "testuser";
            }

            @Override
            public Boolean validateToken(String token, String username) {
                return true;
            }
        };
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, 
                                          jakarta.servlet.http.HttpServletResponse response, 
                                          jakarta.servlet.FilterChain chain) 
                throws java.io.IOException, jakarta.servlet.ServletException {
                chain.doFilter(request, response);
            }
        };
    }
}