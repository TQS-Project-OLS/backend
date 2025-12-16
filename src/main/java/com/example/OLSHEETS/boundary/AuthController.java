package com.example.OLSHEETS.boundary;

import com.example.OLSHEETS.data.User;
import com.example.OLSHEETS.dto.AuthResponse;
import com.example.OLSHEETS.dto.LoginRequest;
import com.example.OLSHEETS.dto.SignupRequest;
import com.example.OLSHEETS.exception.UserAlreadyExistsException;
import com.example.OLSHEETS.security.JwtUtil;
import com.example.OLSHEETS.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // NOSONAR java:S5122 - CORS wildcard acceptable for API-only application
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private Counter loginSuccessCounter;

    @Autowired(required = false)
    private Counter loginFailureCounter;

    @Autowired(required = false)
    private Timer authenticationTimer;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            User user = userService.registerUser(request);
            String token = jwtUtil.generateToken(user.getUsername());

            AuthResponse response = new AuthResponse(token, user.getUsername(), user.getName());
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        java.util.function.Supplier<ResponseEntity<?>> loginSupplier = () -> {
            Optional<User> userOpt = userService.authenticateUser(request.getUsername(), request.getPassword());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtUtil.generateToken(user.getUsername());

                AuthResponse response = new AuthResponse(token, user.getUsername(), user.getName());
                if (loginSuccessCounter != null) {
                    loginSuccessCounter.increment();
                }
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid username or password");
                if (loginFailureCounter != null) {
                    loginFailureCounter.increment();
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        };

        if (authenticationTimer != null) {
            return authenticationTimer.record(loginSupplier);
        } else {
            return loginSupplier.get();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String username = jwtUtil.extractUsername(token);

            if (jwtUtil.validateToken(token, username)) {
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, String> response = new HashMap<>();
                    response.put("username", user.getUsername());
                    response.put("name", user.getName());
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Extract username from JWT token via SecurityContext
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("name", user.getName());
                response.put("email", user.getEmail());
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error retrieving user information"));
        }
    }
}
