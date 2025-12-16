package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.boundary.AuthController;
import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.AuthResponse;
import com.example.OLSHEETS.dto.LoginRequest;
import com.example.OLSHEETS.dto.SignupRequest;
import com.example.OLSHEETS.exception.UserAlreadyExistsException;
import com.example.OLSHEETS.security.JwtUtil;
import com.example.OLSHEETS.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public com.example.OLSHEETS.security.JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(com.example.OLSHEETS.security.JwtUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean(name = "loginSuccessCounter")
    private Counter loginSuccessCounter;

    @MockBean(name = "loginFailureCounter")
    private Counter loginFailureCounter;

    @MockBean(name = "authenticationTimer")
    private Timer authenticationTimer;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");

        testToken = "test-jwt-token";
    }

    @Test
    void testSignup_Success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setName("New User");
        request.setPassword("password123");

        when(userService.registerUser(any(SignupRequest.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"email\":\"newuser@test.com\",\"name\":\"New User\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testSignup_Error() throws Exception {
        when(userService.registerUser(any(SignupRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"existing\",\"email\":\"existing@test.com\",\"name\":\"Existing User\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userService.authenticateUser("testuser", "password")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);
        // Mock timer to execute the supplier and return its result
        when(authenticationTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(loginSuccessCounter, times(1)).increment();
        verify(authenticationTimer, times(1)).record(any(Supplier.class));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(userService.authenticateUser("testuser", "wrongpassword")).thenReturn(Optional.empty());
        // Mock timer to execute the supplier and return its result
        when(authenticationTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));

        verify(loginFailureCounter, times(1)).increment();
        verify(authenticationTimer, times(1)).record(any(Supplier.class));
    }

    @Test
    void testValidateToken_Success() throws Exception {
        String authHeader = "Bearer " + testToken;

        when(jwtUtil.extractUsername(testToken)).thenReturn("testuser");
        when(jwtUtil.validateToken(testToken, "testuser")).thenReturn(true);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void testValidateToken_InvalidToken() throws Exception {
        String authHeader = "Bearer invalid-token";

        when(jwtUtil.extractUsername("invalid-token")).thenReturn("testuser");
        when(jwtUtil.validateToken("invalid-token", "testuser")).thenReturn(false);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    void testValidateToken_UserNotFound() throws Exception {
        String authHeader = "Bearer " + testToken;

        when(jwtUtil.extractUsername(testToken)).thenReturn("testuser");
        when(jwtUtil.validateToken(testToken, "testuser")).thenReturn(true);
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    void testValidateToken_Exception() throws Exception {
        String authHeader = "Bearer invalid";

        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token format"));

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        // Mock authentication
        org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        org.springframework.security.core.context.SecurityContext securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() throws Exception {
        // Mock null authentication
        org.springframework.security.core.context.SecurityContext securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Not authenticated"));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_UserNotFound() throws Exception {
        // Mock authentication with existing auth but user not in DB
        org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("nonexistent");
        org.springframework.security.core.context.SecurityContext securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_Exception() throws Exception {
        // Mock authentication that throws exception
        org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        org.springframework.security.core.context.SecurityContext securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        when(userService.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error retrieving user information"));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_NullAuthenticationName() throws Exception {
        // Mock authentication with null name
        org.springframework.security.core.Authentication auth = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(auth.getName()).thenReturn(null);
        org.springframework.security.core.context.SecurityContext securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Not authenticated"));

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}

