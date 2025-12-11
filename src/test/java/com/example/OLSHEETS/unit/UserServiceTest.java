package com.example.OLSHEETS.unit;

import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.SignupRequest;
import com.example.OLSHEETS.exception.UserAlreadyExistsException;
import com.example.OLSHEETS.repository.UserRepository;
import com.example.OLSHEETS.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@test.com");
        signupRequest.setName("New User");
        signupRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@test.com");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.registerUser(signupRequest);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@test.com", result.getEmail());
        assertEquals("New User", result.getName());
        assertNotNull(result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterUser_UsernameExists() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(signupRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailExists() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(signupRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(userService.getPasswordEncoder().encode("password123"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticateUser("testuser", "password123");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(userService.getPasswordEncoder().encode("password123"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.authenticateUser("testuser", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticateUser("nonexistent", "password123");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("existinguser", result.get().getUsername());
    }

    @Test
    void testGetPasswordEncoder() {
        assertNotNull(userService.getPasswordEncoder());
    }
}

