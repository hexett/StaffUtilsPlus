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
import java.util.concurrent.locks.ReentrantLock;
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
    private final File dataFile;
    private final Gson gson;
    private final Type dataType;
    private final ReentrantLock lock;

    private Map<UUID, List<Punishment>> punishments;
    private Map<UUID, List<Note>> notes = new HashMap<>();
    private Map<UUID, List<Warning>> warnings = new HashMap<>();

    /**
     * Create a new LocalDatabase instance.
     * 
     * @param plugin The plugin instance
     */
    public LocalDatabase(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "punishments.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataType = new TypeToken<Map<UUID, List<Punishment>>>() {}.getType();
        this.lock = new ReentrantLock();
        this.punishments = new HashMap<>();
    }

    @Override
    public void connect() {
        lock.lock();
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                savePunishments();
            } else {
                loadPunishments();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to initialize local database: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            savePunishments();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save punishments on shutdown: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void insertPunishment(Punishment punishment) {
        if (punishment == null) {
            return;
        }

        lock.lock();
        try {
            punishments.computeIfAbsent(punishment.getTarget(), k -> new ArrayList<>()).add(punishment);
            savePunishments();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to insert punishment: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Punishment> getPunishments(UUID target) {
        if (target == null) {
            return Collections.emptyList();
        }

        lock.lock();
        try {
            List<Punishment> all = punishments.getOrDefault(target, Collections.emptyList());
            List<Punishment> activeOnly = new ArrayList<>();
            for (Punishment p : all) {
                if (p.isActive()) {
                    activeOnly.add(p);
                }
            }
            return activeOnly;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deactivatePunishment(UUID target, Punishment.Type type) {
        if (target == null || type == null) {
            return;
        }

        lock.lock();
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
                savePunishments();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deactivate punishment: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Punishment> getPunishmentsByIP(String ipAddress) {
        if (ipAddress == null) {
            return Collections.emptyList();
        }

        lock.lock();
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
            lock.unlock();
        }
    }

    @Override
    public void deactivateIPBan(String ipAddress) {
        if (ipAddress == null) {
            return;
        }

        lock.lock();
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
                savePunishments();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deactivate IP ban: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // --- Notes Implementation ---
    @Override
    public void insertNote(Note note) {
        if (note == null) return;
        lock.lock();
        try {
            notes.computeIfAbsent(note.getTarget(), k -> new ArrayList<>()).add(note);
            saveAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeNote(UUID target, int noteId) {
        lock.lock();
        try {
            List<Note> targetNotes = notes.get(target);
            if (targetNotes != null) {
                targetNotes.removeIf(n -> n.getId() == noteId);
                saveAll();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Note> getNotes(UUID target) {
        lock.lock();
        try {
            return new ArrayList<>(notes.getOrDefault(target, Collections.emptyList()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Note getNote(int noteId) {
        lock.lock();
        try {
            for (List<Note> noteList : notes.values()) {
                for (Note note : noteList) {
                    if (note.getId() == noteId) return note;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    // --- Warnings Implementation ---
    @Override
    public void insertWarning(Warning warning) {
        if (warning == null) return;
        lock.lock();
        try {
            warnings.computeIfAbsent(warning.getTarget(), k -> new ArrayList<>()).add(warning);
            saveAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeWarning(UUID target, int warningId) {
        lock.lock();
        try {
            List<Warning> targetWarnings = warnings.get(target);
            if (targetWarnings != null) {
                for (Warning w : targetWarnings) {
                    if (w.getId() == warningId) {
                        w.setActive(false);
                    }
                }
                saveAll();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Warning> getWarnings(UUID target) {
        lock.lock();
        try {
            return new ArrayList<>(warnings.getOrDefault(target, Collections.emptyList()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Warning getWarning(int warningId) {
        lock.lock();
        try {
            for (List<Warning> warningList : warnings.values()) {
                for (Warning warning : warningList) {
                    if (warning.getId() == warningId) return warning;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Load punishments from the JSON file.
     */
    private void loadPunishments() {
        try (FileReader reader = new FileReader(dataFile)) {
            Map<UUID, List<Punishment>> loaded = gson.fromJson(reader, dataType);
            if (loaded != null) {
                punishments = loaded;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load punishments from file: " + e.getMessage());
        }
    }

    /**
     * Save punishments to the JSON file.
     */
    private void savePunishments() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(punishments, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save punishments to file: " + e.getMessage());
        }
    }

    // Save/load all data (punishments, notes, warnings)
    private void saveAll() {
        savePunishments();
        saveNotes();
        saveWarnings();
    }
    private void saveNotes() {
        try (FileWriter writer = new FileWriter(new File(plugin.getDataFolder(), "notes.json"))) {
            gson.toJson(notes, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save notes to file: " + e.getMessage());
        }
    }
    private void saveWarnings() {
        try (FileWriter writer = new FileWriter(new File(plugin.getDataFolder(), "warnings.json"))) {
            gson.toJson(warnings, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save warnings to file: " + e.getMessage());
        }
    }
}
