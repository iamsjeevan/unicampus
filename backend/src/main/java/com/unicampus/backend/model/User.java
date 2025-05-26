// backend/src/main/java/com/unicampus/backend/model/User.java
package com.unicampus.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// --- Change Imports ---
import java.util.ArrayList; // Import ArrayList
import java.util.List;     // Import List instead of Set/HashSet
// --- End Change ---

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id; // Use email as ID
    private String username;
    private String email;
    private String password; // Hashed
    private String name;

    // --- Change Field Type and Initialization ---
    private List<String> roles = new ArrayList<>(); // Use List<String>
    // --- End Change ---

    private boolean enabled = true;

    // Constructor using List
    public User(String username, String email, String password, String name) {
        this.username = (username != null && !username.trim().isEmpty()) ? username : email; // Use email if username blank
        this.email = email;
        this.password = password; // Raw password initially, hash before saving
        this.name = name;
        // --- Change how role is added ---
        if (this.roles == null) { // Initialize if null (though field init covers this)
             this.roles = new ArrayList<>();
        }
        this.roles.add("ROLE_USER"); // Add default role to the List
        // --- End Change ---
        this.enabled = true;
        this.id = email; // Set ID based on email
    }

     // Optional: Add helper method if needed elsewhere
     public void addRole(String role) {
         if (this.roles == null) {
             this.roles = new ArrayList<>();
         }
         if (role != null && !this.roles.contains(role)) {
              this.roles.add(role);
         }
     }
}