package com.unicampus.backend.model; // Hardcoded package
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class Note {
    private String id;
    private String title;
    private String subject;
    private String uploadedBy;
    private String downloadUrl;
}
