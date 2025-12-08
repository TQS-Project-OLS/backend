package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.security.JwtAuthenticationFilter;
import com.example.OLSHEETS.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(new SecurityContextImpl());
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(jwtAuthenticationFilter)
                .build();
    }

    @Test
    void testFilter_ValidToken() throws Exception {
        String token = "valid-token";
        String username = "testuser";
        String authHeader = "Bearer " + token;

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token, username)).thenReturn(true);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound()); // Endpoint doesn't exist, but filter should process

        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil).validateToken(token, username);
    }

    @Test
    void testFilter_NoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isNotFound());

        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void testFilter_InvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isNotFound());

        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void testFilter_ExceptionExtractingUsername() throws Exception {
        String token = "invalid-token";
        String authHeader = "Bearer " + token;

        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/api/test")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());

        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    void testFilter_InvalidToken() throws Exception {
        String token = "invalid-token";
        String username = "testuser";
        String authHeader = "Bearer " + token;

        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token, username)).thenReturn(false);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());

        verify(jwtUtil).extractUsername(token);
        verify(jwtUtil).validateToken(token, username);
    }
}

