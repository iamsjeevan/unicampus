package com.unicampus.backend.repository; // Hardcoded package

import com.unicampus.backend.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.ArrayList; // Ensure import
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private static final String COLLECTION_NAME = "users";
    private final Firestore firestore;
    private CollectionReference usersCollection;
    @Autowired
    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
        this.usersCollection = firestore.collection(COLLECTION_NAME);
    }
    public User save(User user) throws ExecutionException, InterruptedException {
        String docId = user.getEmail(); // Use email as ID
        if (docId == null || docId.trim().isEmpty()) { throw new IllegalArgumentException("User email cannot be null."); }
        user.setId(docId);
        usersCollection.document(docId).set(user, SetOptions.merge()).get();
        logger.info("User saved/updated: {}", docId);
        return user;
    }
    public Optional<User> findByEmail(String email) throws ExecutionException, InterruptedException {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        DocumentSnapshot document = usersCollection.document(email).get().get();
        if (document.exists()) {
            User user = document.toObject(User.class);
            if (user != null) { user.setId(document.getId()); return Optional.of(user); }
            else { logger.error("User doc {} exists but failed conversion.", email); return Optional.empty(); }
        } else { return Optional.empty(); }
    }
     public boolean existsByEmail(String email) throws ExecutionException, InterruptedException {
         if (email == null || email.trim().isEmpty()) return false;
         return usersCollection.document(email).get().get().exists();
     }
    public Optional<User> findByUsername(String username) throws ExecutionException, InterruptedException {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        Query query = usersCollection.whereEqualTo("username", username).limit(1);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
        if (!documents.isEmpty()) {
            DocumentSnapshot document = documents.get(0); User user = document.toObject(User.class);
             if (user != null) { user.setId(document.getId()); return Optional.of(user); }
             else { logger.error("User doc {} failed conversion.", document.getId()); return Optional.empty(); }
        } else { return Optional.empty(); }
    }
     public boolean existsByUsername(String username) throws ExecutionException, InterruptedException {
         if (username == null || username.trim().isEmpty()) return false;
         Query query = usersCollection.whereEqualTo("username", username).limit(1);
         return !query.get().get().isEmpty();
     }
}
