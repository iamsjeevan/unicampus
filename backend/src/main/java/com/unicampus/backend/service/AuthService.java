package com.unicampus.backend.service; // Hardcoded package

import com.unicampus.backend.dto.AuthResponse;
import com.unicampus.backend.dto.LoginRequest;
import com.unicampus.backend.dto.SignUpRequest;
import com.unicampus.backend.exception.AppException;
import com.unicampus.backend.model.User;
import com.unicampus.backend.repository.UserRepository;
import com.unicampus.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    @Autowired public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager; this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; this.tokenProvider = tokenProvider;
    }
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Auth attempt for: {}", loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new AppException("User data not found post-auth."));
            List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());
            return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getName(), roles);
        } catch (AuthenticationException ex) {
             logger.warn("Auth failed for {}: {}", loginRequest.getEmail(), ex.getMessage());
             throw ex; // Let GlobalExceptionHandler handle BadCredentialsException
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error fetching user post-auth for {}", loginRequest.getEmail(), e);
            Thread.currentThread().interrupt();
            throw new AppException("Error retrieving user details post-login.", e);
        } catch (AppException e) {
             logger.error("AppException post-auth for {}: {}", loginRequest.getEmail(), e.getMessage());
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }
    public User registerUser(SignUpRequest signUpRequest) throws ExecutionException, InterruptedException {
        logger.info("Registering user: {}", signUpRequest.getEmail());
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email address already in use!");
        }
         String username = (signUpRequest.getUsername() != null && !signUpRequest.getUsername().trim().isEmpty())
                            ? signUpRequest.getUsername() : signUpRequest.getEmail();
        if (userRepository.existsByUsername(username)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already in use!");
        }
        User user = new User(username, signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()), signUpRequest.getName());
        User result = userRepository.save(user);
        logger.info("User registered: {}", signUpRequest.getEmail());
        return result;
    }
}
