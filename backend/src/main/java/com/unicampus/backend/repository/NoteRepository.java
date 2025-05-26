package com.unicampus.backend.repository; // Hardcoded package

import com.unicampus.backend.model.Note;
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
public class NoteRepository {
    private static final Logger logger = LoggerFactory.getLogger(NoteRepository.class);
    private static final String COLLECTION_NAME = "notes";
    private final Firestore firestore;
    private CollectionReference notesCollection;
    @Autowired
    public NoteRepository(Firestore firestore) {
        this.firestore = firestore;
        this.notesCollection = firestore.collection(COLLECTION_NAME);
    }
    public Note save(Note note) throws ExecutionException, InterruptedException {
        DocumentReference docRef;
        if (note.getId() == null || note.getId().trim().isEmpty()) {
            docRef = notesCollection.document(); note.setId(docRef.getId());
            docRef.set(note).get();
        } else {
            docRef = notesCollection.document(note.getId());
            docRef.set(note, SetOptions.merge()).get();
        }
        logger.info("Note saved/updated with ID: {}", note.getId());
        return note;
    }
    public Optional<Note> findById(String id) throws ExecutionException, InterruptedException {
        if (id == null || id.trim().isEmpty()) return Optional.empty();
        DocumentReference docRef = notesCollection.document(id);
        DocumentSnapshot document = docRef.get().get();
        if (document.exists()) {
            Note note = document.toObject(Note.class);
            if (note != null) { note.setId(document.getId()); return Optional.of(note); }
            else { logger.error("Doc {} exists but failed conversion.", id); return Optional.empty(); }
        } else { return Optional.empty(); }
    }
    public List<Note> findAll() throws ExecutionException, InterruptedException {
        List<Note> noteList = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = notesCollection.orderBy("subject").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            Note note = document.toObject(Note.class);
             if (note != null) { note.setId(document.getId()); noteList.add(note); }
             else { logger.warn("Doc {} failed conversion.", document.getId()); }
        }
        return noteList;
    }
    public void deleteById(String id) throws ExecutionException, InterruptedException {
         if (id == null || id.trim().isEmpty()) return;
         notesCollection.document(id).delete().get();
         logger.info("Note deleted: {}", id);
    }
     public boolean existsById(String id) throws ExecutionException, InterruptedException {
         if (id == null || id.trim().isEmpty()) return false;
         DocumentSnapshot document = notesCollection.document(id).get().get();
         return document.exists();
     }
}
