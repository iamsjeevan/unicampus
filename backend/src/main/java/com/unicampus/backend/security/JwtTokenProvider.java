package com.unicampus.backend.security; // Hardcoded package

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${app.jwt.secret}") private String jwtSecretString;
    @Value("${app.jwt.expiration-ms}") private long jwtExpirationMs;
    private SecretKey jwtSecretKey;

    @PostConstruct
    protected void init() {
        try {
            jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
            logger.info("JWT Secret Key initialized.");
        } catch (Exception e) {
             logger.error("!!! CRITICAL: Failed to initialize JWT Secret Key: {}", e.getMessage());
             logger.warn("!!! WARNING: Using placeholder key. NOT FOR PRODUCTION. !!!");
             jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date(); Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder().setSubject(userPrincipal.getUsername()).setIssuedAt(now)
                .setExpiration(expiryDate).signWith(jwtSecretKey, SignatureAlgorithm.HS256).compact();
    }
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
    public boolean validateToken(String authToken) {
        if (authToken == null) return false;
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) { logger.error("Invalid JWT sig: {}", e.getMessage());
        } catch (MalformedJwtException e) { logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) { logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { logger.error("Unsupported JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) { logger.error("JWT claims empty: {}", e.getMessage()); }
        return false;
    }
}
