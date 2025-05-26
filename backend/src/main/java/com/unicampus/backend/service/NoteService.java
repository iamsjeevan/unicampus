package com.unicampus.backend.service; // Hardcoded package

import com.unicampus.backend.exception.ResourceNotFoundException;
import com.unicampus.backend.model.Note;
import com.unicampus.backend.repository.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class NoteService {
    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);
    private final NoteRepository noteRepository;
    @Autowired
    public NoteService(NoteRepository noteRepository) { this.noteRepository = noteRepository; }
    public List<Note> getAllNotes() throws ExecutionException, InterruptedException { return noteRepository.findAll(); }
    public Note getNoteById(String id) throws ExecutionException, InterruptedException {
        return noteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
    }
    public Note createNote(Note note) throws ExecutionException, InterruptedException {
        note.setId(null); return noteRepository.save(note);
    }
    public Note updateNote(String id, Note noteDetails) throws ExecutionException, InterruptedException {
        Note existingNote = getNoteById(id);
        existingNote.setTitle(noteDetails.getTitle());
        existingNote.setSubject(noteDetails.getSubject());
        existingNote.setUploadedBy(noteDetails.getUploadedBy());
        existingNote.setDownloadUrl(noteDetails.getDownloadUrl());
        return noteRepository.save(existingNote);
    }
    public void deleteNote(String id) throws ExecutionException, InterruptedException {
        if (!noteRepository.existsById(id)) { throw new ResourceNotFoundException("Note", "id", id); }
        noteRepository.deleteById(id);
    }
}
