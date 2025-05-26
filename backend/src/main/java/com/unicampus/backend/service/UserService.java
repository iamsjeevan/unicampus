package com.unicampus.backend.service; // Hardcoded package

import com.unicampus.backend.exception.ResourceNotFoundException;
import com.unicampus.backend.model.User;
import com.unicampus.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    @Autowired public UserService(UserRepository userRepository) { this.userRepository = userRepository; }
    public User getUserByEmail(String email) throws ExecutionException, InterruptedException {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
     public User getUserByUsername(String username) throws ExecutionException, InterruptedException {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}
