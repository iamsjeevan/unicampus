package com.unicampus.backend.dto; // Hardcoded package
import lombok.Data;
import java.util.List;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String userId; private String email; private String name; private List<String> roles;
    public AuthResponse(String accessToken, String userId, String email, String name, List<String> roles) {
        this.accessToken = accessToken; this.userId = userId; this.email = email;
        this.name = name; this.roles = roles;
    }
}
