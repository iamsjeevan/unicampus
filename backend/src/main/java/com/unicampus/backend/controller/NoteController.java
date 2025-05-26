package com.unicampus.backend.controller; // Hardcoded package

import com.unicampus.backend.model.Note;
import com.unicampus.backend.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);
    private final NoteService noteService;
    @Autowired
    public NoteController(NoteService noteService) { this.noteService = noteService; }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() throws ExecutionException, InterruptedException {
        List<Note> notes = noteService.getAllNotes(); return ResponseEntity.ok(notes);
    }
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) throws ExecutionException, InterruptedException {
        Note createdNote = noteService.createNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) throws ExecutionException, InterruptedException {
        Note note = noteService.getNoteById(id); return ResponseEntity.ok(note);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @RequestBody Note noteDetails) throws ExecutionException, InterruptedException {
        Note updatedNote = noteService.updateNote(id, noteDetails); return ResponseEntity.ok(updatedNote);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) throws ExecutionException, InterruptedException {
        noteService.deleteNote(id); return ResponseEntity.noContent().build();
    }
}
