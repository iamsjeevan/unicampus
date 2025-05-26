package com.unicampus.backend.config; // Hardcoded package

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // Use ClassPathResource
import java.io.InputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String CLASSPATH_KEY_PATH = "firebase-service-account.json";

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("Initializing Firebase Admin SDK using classpath resource: {}", CLASSPATH_KEY_PATH);
                ClassPathResource resource = new ClassPathResource(CLASSPATH_KEY_PATH);
                if (!resource.exists()) {
                     logger.error("Firebase service account key file NOT FOUND on classpath: {}", CLASSPATH_KEY_PATH);
                     throw new RuntimeException("Firebase key file not found on classpath: " + CLASSPATH_KEY_PATH + ". Ensure it's in src/main/resources/");
                }
                logger.info("Found Firebase key file resource via classpath: {}", resource.getURL());
                try (InputStream serviceAccount = resource.getInputStream()) {
                    if (serviceAccount.available() <= 0) { // Check if stream has data
                         logger.error("Firebase key file appears to be empty at classpath: {}", CLASSPATH_KEY_PATH);
                         throw new RuntimeException("Firebase key file is empty at classpath: " + CLASSPATH_KEY_PATH);
                    }
                    try (InputStream credentialsStream = new ClassPathResource(CLASSPATH_KEY_PATH).getInputStream()) {
                         FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                            .build();
                         FirebaseApp.initializeApp(options);
                         logger.info("Firebase Admin SDK initialized successfully.");
                    }
                } catch (IOException ioException) {
                     logger.error("IOException reading Firebase key: {}", CLASSPATH_KEY_PATH, ioException);
                     throw new RuntimeException("Failed to read Firebase key file stream.", ioException);
                }
            } else {
                logger.info("Firebase Admin SDK already initialized.");
            }
        } catch (Exception e) {
            logger.error("FATAL: Error initializing Firebase: {}", e.getMessage(), e);
             throw new RuntimeException("Failed to initialize Firebase Admin SDK.", e);
        }
    }

    @Bean
    public Firestore firestore() {
         if (FirebaseApp.getApps().isEmpty()) {
             throw new IllegalStateException("FirebaseApp not initialized.");
         }
         return FirestoreClient.getFirestore();
    }
}
