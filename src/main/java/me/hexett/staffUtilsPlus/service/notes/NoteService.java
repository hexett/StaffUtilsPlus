package me.hexett.staffUtilsPlus.service.notes;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing player notes.
 * 
 * @author Hexett
 */
public interface NoteService {
    
    /**
     * Add a note to a player.
     * 
     * @param target The UUID of the target player
     * @param issuer The UUID of the staff member adding the note
     * @param content The content of the note
     */
    void addNote(UUID target, UUID issuer, String content);
    
    /**
     * Remove a note from a player.
     * 
     * @param target The UUID of the target player
     * @param noteId The ID of the note to remove
     */
    void removeNote(UUID target, int noteId);
    
    /**
     * Get all notes for a player.
     * 
     * @param target The UUID of the target player
     * @return List of notes for the player
     */
    List<Note> getNotes(UUID target);
    
    /**
     * Get a specific note by ID.
     * 
     * @param noteId The ID of the note
     * @return The note, or null if not found
     */
    Note getNote(int noteId);
}
