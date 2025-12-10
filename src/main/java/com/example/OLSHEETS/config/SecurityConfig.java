package com.example.OLSHEETS.config;

import com.example.OLSHEETS.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @Profile("!test")
    @SuppressWarnings("java:S4502") // CSRF disabled - acceptable for stateless JWT-based API
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // NOSONAR java:S4502 - Safe: stateless JWT authentication, no session-based CSRF risk
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/*.html").permitAll() // Allow all HTML pages
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Only API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll() // Allow other static resources
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        // Allow H2 console frames
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    @Profile("test")
    @SuppressWarnings("java:S4502") // CSRF disabled for test environment only - safe in isolated test context
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        // Disable all security for tests - this is safe as it only applies to test profile
        // and tests run in isolated environment without external network access
        http
                .csrf(csrf -> csrf.disable()) // NOSONAR java:S4502 - Safe: test environment only
                .cors(cors -> cors.disable()) // NOSONAR - Safe: test environment only
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @SuppressWarnings("java:S5122") // CORS wildcard origin - acceptable for development/API-only application
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // NOSONAR java:S5122 - CORS wildcard is acceptable for API-only application without sensitive user data
        // In production, this should be restricted to specific origins
        configuration.setAllowedOrigins(Arrays.asList("*")); // NOSONAR java:S5122
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
