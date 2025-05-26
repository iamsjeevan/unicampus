package com.unicampus.backend.dto; // Hardcoded package
import lombok.AllArgsConstructor; import lombok.Data; import lombok.NoArgsConstructor;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private String id; private String username; private String email;
    private String name;  private List<String> roles;; private boolean enabled;
}
