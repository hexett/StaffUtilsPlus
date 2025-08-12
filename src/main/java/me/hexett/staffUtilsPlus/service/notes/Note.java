package me.hexett.staffUtilsPlus.service.notes;

import java.util.UUID;

/**
 * Represents a note about a player.
 *
 * @author Hexett
 */
public record Note(int id, UUID target, UUID issuer, String content, long timestamp) {

    public Note(UUID target, UUID issuer, String content) {
        this(-1, target, issuer, content, System.currentTimeMillis());
    }
}
