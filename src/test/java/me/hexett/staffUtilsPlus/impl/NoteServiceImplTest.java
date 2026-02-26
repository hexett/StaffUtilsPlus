package me.hexett.staffUtilsPlus.impl;

import me.hexett.staffUtilsPlus.db.Database;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.notes.NoteService;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NoteServiceImpl.
 * Tests note creation, retrieval, and removal functionality.
 */
@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {

    @Mock
    private Database mockDatabase;

    @Mock
    private Plugin mockPlugin;

    private NoteService noteService;
    private UUID testPlayer;
    private UUID testIssuer;

    @BeforeEach
    public void setUp() {
        noteService = new NoteServiceImpl(mockDatabase, mockPlugin);
        testPlayer = UUID.randomUUID();
        testIssuer = UUID.randomUUID();
    }

    @Test
    public void testAddNote() {
        // Arrange
        String content = "Player shown suspicious behavior";

        // Act
        noteService.addNote(testPlayer, testIssuer, content);

        // Assert
        verify(mockDatabase, times(1)).insertNote(any(Note.class));
    }

    @Test
    public void testRemoveNote() {
        // Arrange
        int noteId = 1;

        // Act
        noteService.removeNote(testPlayer, noteId);

        // Assert
        verify(mockDatabase, times(1)).removeNote(eq(testPlayer), eq(noteId));
    }

    @Test
    public void testGetNotes() {
        // Arrange
        List<Note> mockNotes = new ArrayList<>();
        mockNotes.add(new Note(testPlayer, testIssuer, "First note"));
        mockNotes.add(new Note(testPlayer, testIssuer, "Second note"));

        when(mockDatabase.getNotes(testPlayer)).thenReturn(mockNotes);

        // Act
        List<Note> result = noteService.getNotes(testPlayer);

        // Assert
        assertEquals(2, result.size());
        verify(mockDatabase, times(1)).getNotes(eq(testPlayer));
    }

    @Test
    public void testGetNote() {
        // Arrange
        int noteId = 1;
        Note expectedNote = new Note(testPlayer, testIssuer, "Test note");

        when(mockDatabase.getNote(noteId)).thenReturn(expectedNote);

        // Act
        Note result = noteService.getNote(noteId);

        // Assert
        assertNotNull(result);
        assertEquals("Test note", result.content());
        verify(mockDatabase, times(1)).getNote(eq(noteId));
    }

    @Test
    public void testGetNotesEmptyList() {
        // Arrange
        List<Note> emptyList = new ArrayList<>();
        when(mockDatabase.getNotes(testPlayer)).thenReturn(emptyList);

        // Act
        List<Note> result = noteService.getNotes(testPlayer);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAddNoteWithLongContent() {
        // Arrange
        String longContent = "A".repeat(500);

        // Act & Assert
        assertDoesNotThrow(() -> {
            noteService.addNote(testPlayer, testIssuer, longContent);
            verify(mockDatabase, times(1)).insertNote(any(Note.class));
        });
    }

    @Test
    public void testNoteRecordStructure() {
        // Verify Note record has all expected fields
        Note note = new Note(testPlayer, testIssuer, "Test content");
        
        assertEquals(testPlayer, note.target());
        assertEquals(testIssuer, note.issuer());
        assertEquals("Test content", note.content());
        assertNotNull(note.timestamp());
    }
}
