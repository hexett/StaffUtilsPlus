package me.hexett.staffUtilsPlus.service.punishments;

import java.util.UUID;

/**
 * Represents a punishment applied to a player.
 * Supports various types of punishments with expiration times and active states.
 *
 * @author Hexett
 */
public class Punishment {

    /**
     * Types of punishments that can be applied.
     */
    public enum Type {
        BAN,
        TEMP_BAN,
        IP_BAN,
        MUTE,
        TEMP_MUTE,
        KICK
    }

    private final UUID target;
    private final Type type;
    private final String reason;
    private final long issuedAt;
    private final long expiresAt;
    private final UUID issuer;
    private final String ipAddress; // Nullable, for IP bans
    private boolean active;

    /**
     * Creates a new punishment.
     *
     * @param target The UUID of the punished player
     * @param type The type of punishment
     * @param reason The reason for the punishment
     * @param issuedAt When the punishment was issued (timestamp)
     * @param expiresAt When the punishment expires (timestamp, -1 for permanent)
     * @param issuer The UUID of the player who issued the punishment
     */
    public Punishment(UUID target, Type type, String reason, long issuedAt, long expiresAt, UUID issuer) {
        this(target, type, reason, issuedAt, expiresAt, issuer, null);
    }

    /**
     * Creates a new punishment with IP address support.
     *
     * @param target The UUID of the punished player
     * @param type The type of punishment
     * @param reason The reason for the punishment
     * @param issuedAt When the punishment was issued (timestamp)
     * @param expiresAt When the punishment expires (timestamp, -1 for permanent)
     * @param issuer The UUID of the player who issued the punishment
     * @param ipAddress The IP address for IP bans (can be null)
     */
    public Punishment(UUID target, Type type, String reason, long issuedAt, long expiresAt, UUID issuer, String ipAddress) {
        this.target = target;
        this.type = type;
        this.reason = reason;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.issuer = issuer;
        this.ipAddress = ipAddress;
        this.active = true; // Default active
    }

    /**
     * Creates a new punishment without expiration time (assumes permanent).
     *
     * @param target The UUID of the punished player
     * @param type The type of punishment
     * @param reason The reason for the punishment
     * @param issuedAt When the punishment was issued (timestamp)
     * @param issuer The UUID of the player who issued the punishment
     */
    public Punishment(UUID target, Type type, String reason, long issuedAt, UUID issuer) {
        this(target, type, reason, issuedAt, -1L, issuer, null);
    }

    // Getters

    public UUID getTarget() {
        return target;
    }

    public Type getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public UUID getIssuer() {
        return issuer;
    }

    /**
     * Get the IP address associated with this punishment (for IP bans).
     *
     * @return The IP address, or null if not an IP ban
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns true if the punishment is permanent (never expires).
     */
    public boolean isPermanent() {
        return expiresAt == -1L;
    }

    /**
     * Returns true if the punishment is currently active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active state of this punishment.
     *
     * @param active New active state
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
