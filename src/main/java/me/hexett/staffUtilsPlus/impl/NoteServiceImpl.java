package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.notes.NoteService;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the NoteService interface.
 * 
 * @author Hexett
 */
public class NoteServiceImpl implements NoteService {

    private final Database database;

    public NoteServiceImpl(Database database, Plugin plugin) {
        this.database = database;
    }

    @Override
    public void addNote(UUID target, UUID issuer, String content) {
        Note note = new Note(target, issuer, content);
        database.insertNote(note);
    }

    @Override
    public void removeNote(UUID target, int noteId) {
        database.removeNote(target, noteId);
    }

    @Override
    public List<Note> getNotes(UUID target) {
        return database.getNotes(target);
    }

    @Override
    public Note getNote(int noteId) {
        return database.getNote(noteId);
    }
}
