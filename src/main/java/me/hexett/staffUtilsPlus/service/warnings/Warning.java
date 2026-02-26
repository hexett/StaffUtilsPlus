package me.hexett.staffUtilsPlus.service.warnings;

import java.util.UUID;

/**
 * Represents a warning given to a player.
 * 
 * @author Hexett
 */
public class Warning {
    
    private final int id;
    private final UUID target;
    private final UUID issuer;
    private final String reason;
    private final int severity;
    private final long timestamp;
    private boolean active;
    
    public Warning(int id, UUID target, UUID issuer, String reason, int severity, long timestamp, boolean active) {
        this.id = id;
        this.target = target;
        this.issuer = issuer;
        this.reason = reason;
        this.severity = severity;
        this.timestamp = timestamp;
        this.active = active;
    }
    
    public Warning(UUID target, UUID issuer, String reason, int severity) {
        this(-1, target, issuer, reason, severity, System.currentTimeMillis(), true);
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
    
    public String getReason() {
        return reason;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
