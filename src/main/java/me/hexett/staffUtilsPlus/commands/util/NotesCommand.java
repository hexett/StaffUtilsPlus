package me.hexett.staffUtilsPlus.commands.util;

import me.hexett.staffUtilsPlus.commands.BaseCommand;
import me.hexett.staffUtilsPlus.service.ServiceRegistry;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.notes.NoteService;
import me.hexett.staffUtilsPlus.utils.MessagesConfig;
import me.hexett.staffUtilsPlus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command for managing player notes.
 * 
 * @author Hexett
 */
public class NotesCommand extends BaseCommand {

    public NotesCommand() {
        super(
            "staffutils.notes",
            "/notes <player> [add/remove] [content/id]",
            "Manage player notes",
            false,
            1
        );
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        String targetName = args[0];
        
        // Get target UUID
        UUID targetUUID = PlayerUtils.getPlayerUUID(targetName);
        if (targetUUID == null) {
            sendMessage(sender, MessagesConfig.get("errors.player-not-found").replace("%player%", targetName));
            return true;
        }

        if (args.length == 1) {
            // Show notes
            showNotes(sender, targetUUID, targetName);
        } else if (args.length >= 3 && args[1].equalsIgnoreCase("add")) {
            // Add note
            String content = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
            addNote(sender, targetUUID, content);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
            // Remove note
            try {
                int noteId = Integer.parseInt(args[2]);
                removeNote(sender, targetUUID, noteId);
            } catch (NumberFormatException e) {
                sendMessage(sender, MessagesConfig.get("errors.invalid-note-id"));
            }
        } else {
            sendMessage(sender, getUsage());
        }

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab complete player names
            String partialName = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            completions.add("add");
            completions.add("remove");
        }

        return completions;
    }

    /**
     * Show all notes for a player.
     * 
     * @param sender The command sender
     * @param targetUUID The target player's UUID
     * @param targetName The target player's name
     */
    private void showNotes(CommandSender sender, UUID targetUUID, String targetName) {
        NoteService noteService = ServiceRegistry.get(NoteService.class);
        if (noteService == null) {
            sendMessage(sender, MessagesConfig.get("errors.service-unavailable"));
            return;
        }

        List<Note> notes = noteService.getNotes(targetUUID);
        
        if (notes.isEmpty()) {
            sendMessage(sender, MessagesConfig.get("notes.none").replace("%target%", targetName));
            return;
        }

        sendMessage(sender, MessagesConfig.get("notes.header").replace("%target%", targetName));
        for (Note note : notes) {
            String issuerName = getPlayerName(note.issuer());
            String timestamp = formatTimestamp(note.timestamp());
            
            String noteMessage = MessagesConfig.get("notes.format")
                    .replace("%id%", String.valueOf(note.id()))
                    .replace("%content%", note.content())
                    .replace("%issuer%", issuerName)
                    .replace("%timestamp%", timestamp);
            
            sendMessage(sender, noteMessage);
        }
    }

    /**
     * Add a note to a player.
     * 
     * @param sender The command sender
     * @param targetUUID The target player's UUID
     * @param content The note content
     */
    private void addNote(CommandSender sender, UUID targetUUID, String content) {
        if (content.isEmpty()) {
            sendMessage(sender, MessagesConfig.get("errors.note-content-empty"));
            return;
        }

        NoteService noteService = ServiceRegistry.get(NoteService.class);
        if (noteService == null) {
            sendMessage(sender, MessagesConfig.get("errors.service-unavailable"));
            return;
        }

        UUID issuerUUID = null;
        if (sender instanceof Player) {
            issuerUUID = ((Player) sender).getUniqueId();
        }

        noteService.addNote(targetUUID, issuerUUID, content);
        
        String targetName = getPlayerName(targetUUID);
        String successMessage = MessagesConfig.get("notes.added")
                .replace("%target%", targetName)
                .replace("%content%", content);
        sendMessage(sender, successMessage);
    }

    /**
     * Remove a note from a player.
     * 
     * @param sender The command sender
     * @param targetUUID The target player's UUID
     * @param noteId The note ID to remove
     */
    private void removeNote(CommandSender sender, UUID targetUUID, int noteId) {
        NoteService noteService = ServiceRegistry.get(NoteService.class);
        if (noteService == null) {
            sendMessage(sender, MessagesConfig.get("errors.service-unavailable"));
            return;
        }

        noteService.removeNote(targetUUID, noteId);
        
        String targetName = getPlayerName(targetUUID);
        String successMessage = MessagesConfig.get("notes.removed")
                .replace("%target%", targetName)
                .replace("%id%", String.valueOf(noteId));
        sendMessage(sender, successMessage);
    }

    /**
     * Get a player's name from their UUID.
     * 
     * @param uuid The player's UUID
     * @return The player's name, or "Unknown" if not found
     */
    private String getPlayerName(UUID uuid) {
        if (uuid == null) {
            return "Console";
        }
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unknown";
    }

    /**
     * Format a timestamp into a readable string.
     * 
     * @param timestamp The timestamp in milliseconds
     * @return The formatted timestamp
     */
    private String formatTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;
        
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            long minutes = diff / 60000;
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (diff < 86400000) { // Less than 1 day
            long hours = diff / 3600000;
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else {
            long days = diff / 86400000;
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
    }
}
