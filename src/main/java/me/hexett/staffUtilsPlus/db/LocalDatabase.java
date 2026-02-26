package me.hexett.staffUtilsPlus.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.warnings.Warning;

/**
 * Local JSON-based database for punishments.
 * Thread-safe and provides a fallback when external databases are unavailable.
 *
 * @author Hexett
 */
public class LocalDatabase implements Database {

    private final Plugin plugin;
    private final File punishmentsFile;
    private final File notesFile;
    private final File warningsFile;
    private final File playerIPsFile;
    private final Gson gson;
    private final Type punishmentsType;
    private final Type notesType;
    private final Type warningsType;
    private final Type playerIPsType;
    private final ReadWriteLock lock;

    private Map<UUID, List<Punishment>> punishments;
    private Map<UUID, List<Note>> notes;
    private Map<UUID, List<Warning>> warnings;
    private Map<UUID, String> playerIPs;
    private Map<String, Set<UUID>> ipToPlayers;

    private volatile boolean isDirty = false;

    /**
     * Create a new LocalDatabase instance.
     *
     * @param plugin The plugin instance
     */
    public LocalDatabase(Plugin plugin) {
        this.plugin = plugin;
        this.punishmentsFile = new File(plugin.getDataFolder(), "punishments.json");
        this.notesFile = new File(plugin.getDataFolder(), "notes.json");
        this.warningsFile = new File(plugin.getDataFolder(), "warnings.json");
        this.playerIPsFile = new File(plugin.getDataFolder(), "player-ips.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.punishmentsType = new TypeToken<Map<UUID, List<Punishment>>>() {}.getType();
        this.notesType = new TypeToken<Map<UUID, List<Note>>>() {}.getType();
        this.warningsType = new TypeToken<Map<UUID, List<Warning>>>() {}.getType();
        this.playerIPsType = new TypeToken<Map<UUID, String>>() {}.getType();
        this.lock = new ReentrantReadWriteLock();
        this.punishments = new HashMap<>();
        this.notes = new HashMap<>();
        this.warnings = new HashMap<>();
        this.playerIPs = new HashMap<>();
        this.ipToPlayers = new HashMap<>();
    }

    @Override
    public void connect() {
        lock.writeLock().lock();
        try {
            // Ensure data folder exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            // Load or create punishments file
            if (!punishmentsFile.exists()) {
                punishmentsFile.createNewFile();
                savePunishments();
            } else {
                loadPunishments();
            }

            // Load or create notes file
            if (!notesFile.exists()) {
                notesFile.createNewFile();
                saveNotes();
            } else {
                loadNotes();
            }

            // Load or create warnings file
            if (!warningsFile.exists()) {
                warningsFile.createNewFile();
                saveWarnings();
            } else {
                loadWarnings();
            }

            // Load or create player IPs file
            if (!playerIPsFile.exists()) {
                playerIPsFile.createNewFile();
                savePlayerIPs();
            } else {
                loadPlayerIPs();
            }

            plugin.getLogger().info("Local database connected successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to initialize local database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        lock.writeLock().lock();
        try {
            if (isDirty) {
                saveAll();
            }
            plugin.getLogger().info("Local database closed successfully");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save data on shutdown: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │                    PUNISHMENT OPERATIONS                            │
    // └─────────────────────────────────────────────────────────────────────┘

    @Override
    public void insertPunishment(Punishment punishment) {
        if (punishment == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            punishments.computeIfAbsent(punishment.getTarget(), k -> new ArrayList<>()).add(punishment);
            isDirty = true;
            savePunishments();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to insert punishment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Punishment> getPunishments(UUID target) {
        if (target == null) {
            return Collections.emptyList();
        }

        lock.readLock().lock();
        try {
            List<Punishment> all = punishments.get(target);
            if (all == null) {
                return Collections.emptyList();
            }

            List<Punishment> activeOnly = new ArrayList<>();
            for (Punishment p : all) {
                if (p.isActive()) {
                    activeOnly.add(p);
                }
            }
            return activeOnly;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Punishment> getPunishmentsByIssuer(UUID issuer) {
        if (issuer == null) {
            return Collections.emptyList();
        }

        lock.readLock().lock();
        try {
            List<Punishment> result = new ArrayList<>();
            for (List<Punishment> punishmentList : punishments.values()) {
                for (Punishment p : punishmentList) {
                    if (issuer.equals(p.getIssuer())) {
                        result.add(p);
                    }
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Punishment> getPunishmentsByIP(String ipAddress) {
        if (ipAddress == null) {
            return Collections.emptyList();
        }

        lock.readLock().lock();
        try {
            List<Punishment> ipPunishments = new ArrayList<>();
            for (List<Punishment> targetPunishments : punishments.values()) {
                for (Punishment punishment : targetPunishments) {
                    if (ipAddress.equals(punishment.getIpAddress()) && punishment.isActive()) {
                        ipPunishments.add(punishment);
                    }
                }
            }
            return ipPunishments;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deactivatePunishment(UUID target, Punishment.Type type) {
        if (target == null || type == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            List<Punishment> targetPunishments = punishments.get(target);
            if (targetPunishments == null) {
                return;
            }

            boolean modified = false;
            for (Punishment punishment : targetPunishments) {
                if (punishment.getType() == type && punishment.isActive()) {
                    punishment.setActive(false);
                    modified = true;
                }
            }

            if (modified) {
                isDirty = true;
                savePunishments();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deactivate punishment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deactivateIPBan(String ipAddress) {
        if (ipAddress == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            boolean modified = false;
            for (List<Punishment> targetPunishments : punishments.values()) {
                for (Punishment punishment : targetPunishments) {
                    if (ipAddress.equals(punishment.getIpAddress()) &&
                            punishment.getType() == Punishment.Type.IP_BAN &&
                            punishment.isActive()) {
                        punishment.setActive(false);
                        modified = true;
                    }
                }
            }

            if (modified) {
                isDirty = true;
                savePunishments();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deactivate IP ban: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │                      NOTES OPERATIONS                               │
    // └─────────────────────────────────────────────────────────────────────┘

    @Override
    public void insertNote(Note note) {
        if (note == null) return;
        lock.writeLock().lock();
        try {
            notes.computeIfAbsent(note.target(), k -> new ArrayList<>()).add(note);
            isDirty = true;
            saveNotes();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to insert note: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeNote(UUID target, int noteId) {
        lock.writeLock().lock();
        try {
            List<Note> targetNotes = notes.get(target);
            if (targetNotes != null) {
                boolean removed = targetNotes.removeIf(n -> n.id() == noteId);
                if (removed) {
                    isDirty = true;
                    saveNotes();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove note: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Note> getNotes(UUID target) {
        lock.readLock().lock();
        try {
            List<Note> targetNotes = notes.get(target);
            return targetNotes != null ? new ArrayList<>(targetNotes) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Note getNote(int noteId) {
        lock.readLock().lock();
        try {
            for (List<Note> noteList : notes.values()) {
                for (Note note : noteList) {
                    if (note.id() == noteId) {
                        return note;
                    }
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │                    WARNINGS OPERATIONS                              │
    // └─────────────────────────────────────────────────────────────────────┘

    @Override
    public void insertWarning(Warning warning) {
        if (warning == null) return;
        lock.writeLock().lock();
        try {
            warnings.computeIfAbsent(warning.getTarget(), k -> new ArrayList<>()).add(warning);
            isDirty = true;
            saveWarnings();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to insert warning: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeWarning(UUID target, int warningId) {
        lock.writeLock().lock();
        try {
            List<Warning> targetWarnings = warnings.get(target);
            if (targetWarnings != null) {
                boolean modified = false;
                for (Warning w : targetWarnings) {
                    if (w.getId() == warningId && w.isActive()) {
                        w.setActive(false);
                        modified = true;
                    }
                }
                if (modified) {
                    isDirty = true;
                    saveWarnings();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove warning: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Warning> getWarnings(UUID target) {
        lock.readLock().lock();
        try {
            List<Warning> targetWarnings = warnings.get(target);
            return targetWarnings != null ? new ArrayList<>(targetWarnings) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Warning getWarning(int warningId) {
        lock.readLock().lock();
        try {
            for (List<Warning> warningList : warnings.values()) {
                for (Warning warning : warningList) {
                    if (warning.getId() == warningId) {
                        return warning;
                    }
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │                  ALT ACCOUNT OPERATIONS                             │
    // └─────────────────────────────────────────────────────────────────────┘

    @Override
    public String getPlayerIP(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        lock.readLock().lock();
        try {
            return playerIPs.get(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<UUID> getPlayersByIP(String ipAddress) {
        if (ipAddress == null) {
            return Collections.emptyList();
        }

        lock.readLock().lock();
        try {
            Set<UUID> players = ipToPlayers.get(ipAddress);
            return players != null ? new ArrayList<>(players) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void recordPlayerIP(UUID uuid, String ipAddress) {
        if (uuid == null || ipAddress == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            // Update player -> IP mapping
            String oldIP = playerIPs.put(uuid, ipAddress);

            // Remove from old IP's player set if IP changed
            if (oldIP != null && !oldIP.equals(ipAddress)) {
                Set<UUID> oldSet = ipToPlayers.get(oldIP);
                if (oldSet != null) {
                    oldSet.remove(uuid);
                    if (oldSet.isEmpty()) {
                        ipToPlayers.remove(oldIP);
                    }
                }
            }

            // Add to new IP's player set
            ipToPlayers.computeIfAbsent(ipAddress, k -> new HashSet<>()).add(uuid);

            isDirty = true;
            savePlayerIPs();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to record player IP: " + e.getMessage());
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │                    DATA LOADING METHODS                             │
    // └─────────────────────────────────────────────────────────────────────┘

    /**
     * Load punishments from the JSON file.
     */
    private void loadPunishments() {
        if (!punishmentsFile.exists() || punishmentsFile.length() == 0) {
            punishments = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(punishmentsFile)) {
            Map<UUID, List<Punishment>> loaded = gson.fromJson(reader, punishmentsType);
            if (loaded != null) {
                punishments = loaded;
            } else {
                punishments = new HashMap<>();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load punishments from file: " + e.getMessage());
            e.printStackTrace();
            punishments = new HashMap<>();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse punishments file (corrupted?): " + e.getMessage());
            e.printStackTrace();
            punishments = new HashMap<>();
        }
    }

    /**
     * Load notes from the JSON file.
     */
    private void loadNotes() {
        if (!notesFile.exists() || notesFile.length() == 0) {
            notes = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(notesFile)) {
            Map<UUID, List<Note>> loaded = gson.fromJson(reader, notesType);
            if (loaded != null) {
                notes = loaded;
            } else {
                notes = new HashMap<>();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load notes from file: " + e.getMessage());
            e.printStackTrace();
            notes = new HashMap<>();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse notes file (corrupted?): " + e.getMessage());
            e.printStackTrace();
            notes = new HashMap<>();
        }
    }

    /**
     * Load warnings from the JSON file.
     */
    private void loadWarnings() {
        if (!warningsFile.exists() || warningsFile.length() == 0) {
            warnings = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(warningsFile)) {
            Map<UUID, List<Warning>> loaded = gson.fromJson(reader, warningsType);
            if (loaded != null) {
                warnings = loaded;
            } else {
                warnings = new HashMap<>();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load warnings from file: " + e.getMessage());
            e.printStackTrace();
            warnings = new HashMap<>();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse warnings file (corrupted?): " + e.getMessage());
            e.printStackTrace();
            warnings = new HashMap<>();
        }
    }

    /**
     * Load player IPs from the JSON file.
     */
    private void loadPlayerIPs() {
        if (!playerIPsFile.exists() || playerIPsFile.length() == 0) {
            playerIPs = new HashMap<>();
            ipToPlayers = new HashMap<>();
            return;
        }

        try (FileReader reader = new FileReader(playerIPsFile)) {
            Map<UUID, String> loaded = gson.fromJson(reader, playerIPsType);
            if (loaded != null) {
                playerIPs = loaded;

                // Rebuild the IP -> players mapping
                ipToPlayers.clear();
                for (Map.Entry<UUID, String> entry : playerIPs.entrySet()) {
                    ipToPlayers.computeIfAbsent(entry.getValue(), k -> new HashSet<>())
                            .add(entry.getKey());
                }
            } else {
                playerIPs = new HashMap<>();
                ipToPlayers = new HashMap<>();
            }

            plugin.getLogger().info("Loaded " + playerIPs.size() + " player IP records");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load player IPs from file: " + e.getMessage());
            e.printStackTrace();
            playerIPs = new HashMap<>();
            ipToPlayers = new HashMap<>();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse player IPs file (corrupted?): " + e.getMessage());
            e.printStackTrace();
            playerIPs = new HashMap<>();
            ipToPlayers = new HashMap<>();
        }
    }

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │                    DATA SAVING METHODS                              │
    // └─────────────────────────────────────────────────────────────────────┘

    /**
     * Save punishments to the JSON file.
     */
    private void savePunishments() {
        try (FileWriter writer = new FileWriter(punishmentsFile)) {
            gson.toJson(punishments, writer);
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save punishments to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save notes to the JSON file.
     */
    private void saveNotes() {
        try (FileWriter writer = new FileWriter(notesFile)) {
            gson.toJson(notes, writer);
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save notes to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save warnings to the JSON file.
     */
    private void saveWarnings() {
        try (FileWriter writer = new FileWriter(warningsFile)) {
            gson.toJson(warnings, writer);
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warnings to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save player IPs to the JSON file.
     */
    private void savePlayerIPs() {
        try (FileWriter writer = new FileWriter(playerIPsFile)) {
            gson.toJson(playerIPs, writer);
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player IPs to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save all data (punishments, notes, warnings, player IPs).
     */
    private void saveAll() {
        savePunishments();
        saveNotes();
        saveWarnings();
        savePlayerIPs();
        isDirty = false;
    }
}