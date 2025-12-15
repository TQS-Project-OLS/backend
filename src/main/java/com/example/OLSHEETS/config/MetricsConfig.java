package com.example.OLSHEETS.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics Configuration
 * 
 * This class configures custom business metrics for the OLSHEETS application.
 * 
 * VALUABLE METRICS TO EXPOSE:
 * 
 * 1. HTTP REQUEST METRICS (Auto-provided by Spring Boot Actuator):
 *    - http.server.requests: Request count, duration, status codes
 *    - Access via: /actuator/metrics/http.server.requests
 * 
 * 2. JVM METRICS (Auto-provided):
 *    - jvm.memory.used: Memory usage
 *    - jvm.memory.max: Max memory
 *    - jvm.gc.pause: Garbage collection pauses
 *    - jvm.threads.live: Active threads
 *    - Access via: /actuator/metrics/jvm.memory.used
 * 
 * 3. DATABASE METRICS (Auto-provided):
 *    - jdbc.connections.active: Active database connections
 *    - jdbc.connections.idle: Idle connections
 *    - hikari.connections: Connection pool metrics (if using HikariCP)
 * 
 * 4. BUSINESS METRICS (Custom - examples below):
 *    - bookings.created: Total bookings created
 *    - bookings.approved: Total bookings approved
 *    - bookings.rejected: Total bookings rejected
 *    - instruments.registered: Total instruments registered
 *    - users.registered: Total user registrations
 *    - auth.login.attempts: Login attempts (success/failure)
 *    - auth.login.failures: Failed login attempts
 * 
 * 5. ERROR METRICS:
 *    - http.server.requests with status=4xx,5xx
 *    - Custom error counters for business logic errors
 * 
 * ACCESSING METRICS:
 * - Prometheus format: http://localhost:8080/actuator/prometheus
 * - JSON format: http://localhost:8080/actuator/metrics
 * - Health check: http://localhost:8080/actuator/health
 * 
 * INTEGRATION WITH GRAFANA:
 * Configure Prometheus to scrape from /actuator/prometheus endpoint
 * and visualize in Grafana dashboards.
 */
@Configuration
public class MetricsConfig {

    /**
     * Counter for tracking total bookings created
     */
    @Bean
    public Counter bookingsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("bookings.created")
                .description("Total number of bookings created")
                .tag("type", "booking")
                .register(registry);
    }

    /**
     * Counter for tracking approved bookings
     */
    @Bean
    public Counter bookingsApprovedCounter(MeterRegistry registry) {
        return Counter.builder("bookings.approved")
                .description("Total number of bookings approved")
                .tag("type", "booking")
                .register(registry);
    }

    /**
     * Counter for tracking rejected bookings
     */
    @Bean
    public Counter bookingsRejectedCounter(MeterRegistry registry) {
        return Counter.builder("bookings.rejected")
                .description("Total number of bookings rejected")
                .tag("type", "booking")
                .register(registry);
    }

    /**
     * Counter for tracking instrument registrations
     */
    @Bean
    public Counter instrumentsRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("instruments.registered")
                .description("Total number of instruments registered")
                .tag("type", "instrument")
                .register(registry);
    }

    /**
     * Counter for tracking user registrations
     */
    @Bean
    public Counter usersRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("users.registered")
                .description("Total number of user registrations")
                .tag("type", "user")
                .register(registry);
    }

    /**
     * Counter for tracking successful logins
     */
    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.success")
                .description("Total number of successful login attempts")
                .tag("type", "authentication")
                .register(registry);
    }

    /**
     * Counter for tracking failed login attempts
     */
    @Bean
    public Counter loginFailureCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.failure")
                .description("Total number of failed login attempts")
                .tag("type", "authentication")
                .register(registry);
    }

    /**
     * Timer for tracking booking creation duration
     */
    @Bean
    public Timer bookingCreationTimer(MeterRegistry registry) {
        return Timer.builder("bookings.creation.duration")
                .description("Time taken to create a booking")
                .register(registry);
    }

    /**
     * Timer for tracking authentication duration
     */
    @Bean
    public Timer authenticationTimer(MeterRegistry registry) {
        return Timer.builder("auth.duration")
                .description("Time taken for authentication")
                .register(registry);
    }
}

