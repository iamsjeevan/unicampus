package com.unicampus.backend.controller; // Hardcoded package

import com.unicampus.backend.dto.AuthResponse;
import com.unicampus.backend.dto.LoginRequest;
import com.unicampus.backend.dto.SignUpRequest;
import com.unicampus.backend.model.User;
import com.unicampus.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    @Autowired public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(authResponse);
        // Exceptions handled globally
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            User result = authService.registerUser(signUpRequest);
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{id}")
                    .buildAndExpand(result.getId()).toUri();
            return ResponseEntity.created(location).body(Map.of("message", "User registered successfully!", "userId", result.getId()));
        } catch (ResponseStatusException ex) {
             return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", ex.getReason()));
        } catch (ExecutionException | InterruptedException e) {
             logger.error("Signup error for {}", signUpRequest.getEmail(), e);
             Thread.currentThread().interrupt();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error registering user."));
        }
    }
}
