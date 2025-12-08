package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.dto.LoginRequest;
import com.example.OLSHEETS.dto.SignupRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testLoginRequest_Constructor() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setName("New User");
        request.setPassword("password123");

        assertEquals("newuser", request.getUsername());
        assertEquals("newuser@test.com", request.getEmail());
        assertEquals("New User", request.getName());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testSignupRequest_Constructor() {
        SignupRequest request = new SignupRequest("newuser", "newuser@test.com", "New User", "password123");
        assertEquals("newuser", request.getUsername());
        assertEquals("newuser@test.com", request.getEmail());
        assertEquals("New User", request.getName());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testAuthResponse() {
        com.example.OLSHEETS.dto.AuthResponse response = new com.example.OLSHEETS.dto.AuthResponse();
        response.setToken("test-token");
        response.setUsername("testuser");
        response.setName("Test User");

        assertEquals("test-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getName());
    }

    @Test
    void testAuthResponse_Constructor() {
        com.example.OLSHEETS.dto.AuthResponse response = new com.example.OLSHEETS.dto.AuthResponse("test-token", "testuser", "Test User");
        assertEquals("test-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getName());
    }
}


