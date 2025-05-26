package com.unicampus.backend.controller; // Hardcoded package

import com.unicampus.backend.dto.UserDto;
import com.unicampus.backend.exception.ResourceNotFoundException;
import com.unicampus.backend.model.User;
import com.unicampus.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutionException;
import java.util.Set; // Import Set for UserDto

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    @Autowired public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
         if (currentUser == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        try {
            User user = userService.getUserByEmail(currentUser.getUsername());
            return ResponseEntity.ok(mapToUserDto(user));
        } catch (ExecutionException | InterruptedException e) {
             logger.error("Error fetching profile for {}", currentUser.getUsername(), e);
             Thread.currentThread().interrupt();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } // ResourceNotFound handled globally
    }
    private UserDto mapToUserDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(),
                user.getName(), user.getRoles(), user.isEnabled());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        try {
            User user = userService.getUserByEmail(id); // Assuming email is used as ID here
            return ResponseEntity.ok(mapToUserDto(user));
        } catch (ExecutionException | InterruptedException e) {
             logger.error("Error fetching user by ID {}", id, e);
             Thread.currentThread().interrupt();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } // ResourceNotFound handled globally
    }
}
