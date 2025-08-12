package me.hexett.staffUtilsPlus.service.notes;

import java.util.UUID;

/**
 * Represents a note about a player.
 * 
 * @author Hexett
 */
public class Note {
    
    private final int id;
    private final UUID target;
    private final UUID issuer;
    private final String content;
    private final long timestamp;
    
    public Note(int id, UUID target, UUID issuer, String content, long timestamp) {
        this.id = id;
        this.target = target;
        this.issuer = issuer;
        this.content = content;
        this.timestamp = timestamp;
    }
    
    public Note(UUID target, UUID issuer, String content) {
        this(-1, target, issuer, content, System.currentTimeMillis());
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public UUID getIssuer() {
        return issuer;
    }
    
    public String getContent() {
        return content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
