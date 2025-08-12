package me.hexett.staffUtilsPlus.db;

import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.warnings.Warning;

import java.util.List;
import java.util.UUID;

/**
 * Database interface for storing and retrieving punishments.
 * Implementations can use various storage backends (SQL, JSON, etc.).
 * 
 * @author Hexett
 */
public interface Database {
    
    /**
     * Connect to the database.
     * This method should initialize the connection and set up any required tables.
     */
    void connect();
    
    /**
     * Close the database connection.
     * This method should clean up resources and save any pending data.
     */
    void close();
    
    /**
     * Insert a new punishment into the database.
     * 
     * @param punishment The punishment to insert
     */
    void insertPunishment(Punishment punishment);
    
    /**
     * Retrieve all punishments for a specific target.
     * 
     * @param target The UUID of the target player
     * @return List of punishments for the target
     */
    List<Punishment> getPunishments(UUID target);
    
    /**
     * Deactivate a punishment for a specific target and type.
     * 
     * @param target The UUID of the target player
     * @param type The type of punishment to deactivate
     */
    void deactivatePunishment(UUID target, Punishment.Type type);
    
    /**
     * Retrieve all punishments for a specific IP address.
     * 
     * @param ipAddress The IP address to query
     * @return List of punishments for the IP address
     */
    List<Punishment> getPunishmentsByIP(String ipAddress);
    
    /**
     * Deactivate an IP ban for a specific IP address.
     * 
     * @param ipAddress The IP address to unban
     */
    void deactivateIPBan(String ipAddress);
    
    /**
     * Insert a new note into the database.
     * 
     * @param note The note to insert
     */
    void insertNote(Note note);
    
    /**
     * Remove a note from the database.
     * 
     * @param target The UUID of the target player
     * @param noteId The ID of the note to remove
     */
    void removeNote(UUID target, int noteId);
    
    /**
     * Retrieve all notes for a specific target.
     * 
     * @param target The UUID of the target player
     * @return List of notes for the target
     */
    List<Note> getNotes(UUID target);
    
    /**
     * Get a specific note by ID.
     * 
     * @param noteId The ID of the note
     * @return The note, or null if not found
     */
    Note getNote(int noteId);
    
    /**
     * Insert a new warning into the database.
     * 
     * @param warning The warning to insert
     */
    void insertWarning(Warning warning);
    
    /**
     * Remove a warning from the database.
     * 
     * @param target The UUID of the target player
     * @param warningId The ID of the warning to remove
     */
    void removeWarning(UUID target, int warningId);
    
    /**
     * Retrieve all warnings for a specific target.
     * 
     * @param target The UUID of the target player
     * @return List of warnings for the target
     */
    List<Warning> getWarnings(UUID target);
    
    /**
     * Get a specific warning by ID.
     * 
     * @param warningId The ID of the warning
     * @return The warning, or null if not found
     */
    Warning getWarning(int warningId);
}
