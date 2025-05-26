package com.unicampus.backend.security; // Hardcoded package

import com.unicampus.backend.model.User;
import com.unicampus.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.Collection; // Can use Collection or List here
import java.util.List;       // Or List specifically
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            Set<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), user.isEnabled(),
                    true, true, true, authorities);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding user by email {}: {}", email, e.getMessage());
            Thread.currentThread().interrupt();
            throw new UsernameNotFoundException("Error retrieving user: " + email, e);
        }
    }
}
