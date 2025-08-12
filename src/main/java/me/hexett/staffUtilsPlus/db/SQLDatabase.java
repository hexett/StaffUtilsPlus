package me.hexett.staffUtilsPlus.db;

import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.warnings.Warning;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQL database implementation for punishments.
 * Supports both MySQL and SQLite databases.
 * 
 * @author Hexett
 */
public class SQLDatabase implements Database {

    private final Plugin plugin;
    private final String type;
    private final String host;
    private final String dbName;
    private final String user;
    private final String pass;
    private final int port;
    
    private Connection connection;

    /**
     * Create a new SQLDatabase instance.
     * 
     * @param plugin The plugin instance
     * @param type The database type ("mysql" or "sqlite")
     * @param host The database host (ignored for SQLite)
     * @param port The database port (ignored for SQLite)
     * @param dbName The database name (ignored for SQLite)
     * @param user The database username (ignored for SQLite)
     * @param pass The database password (ignored for SQLite)
     */
    public SQLDatabase(Plugin plugin, String type, String host, int port, String dbName, String user, String pass) {
        this.plugin = plugin;
        this.type = type.toLowerCase();
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public void connect() {
        try {
            if ("sqlite".equals(type)) {
                connectSQLite();
            } else if ("mysql".equals(type)) {
                connectMySQL();
            } else {
                throw new IllegalArgumentException("Unsupported database type: " + type);
            }
            
            setupTables();
            plugin.getLogger().info("Database connected (" + type.toUpperCase() + ")");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * Connect to SQLite database.
     */
    private void connectSQLite() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        String dbPath = plugin.getDataFolder() + "/data.db";
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    /**
     * Connect to MySQL database.
     */
    private void connectMySQL() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
        connection = DriverManager.getConnection(url, user, pass);
    }

    /**
     * Set up the database tables.
     */
    private void setupTables() throws SQLException {
        String createPunishmentsTable;
        String createNotesTable;
        String createWarningsTable;
        
        if ("sqlite".equals(type)) {
            createPunishmentsTable = """
                CREATE TABLE IF NOT EXISTS punishments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    reason TEXT,
                    issuer_uuid VARCHAR(36),
                    issued_at BIGINT,
                    expires_at BIGINT,
                    ip_address VARCHAR(45),
                    active BOOLEAN DEFAULT 1
                )
                """;
            createNotesTable = """
                CREATE TABLE IF NOT EXISTS notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    issuer_uuid VARCHAR(36),
                    content TEXT,
                    timestamp BIGINT
                )
                """;
            createWarningsTable = """
                CREATE TABLE IF NOT EXISTS warnings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    issuer_uuid VARCHAR(36),
                    reason TEXT,
                    severity INTEGER,
                    timestamp BIGINT,
                    active BOOLEAN DEFAULT 1
                )
                """;
        } else {
            createPunishmentsTable = """
                CREATE TABLE IF NOT EXISTS punishments (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    reason TEXT,
                    issuer_uuid VARCHAR(36),
                    issued_at BIGINT,
                    expires_at BIGINT,
                    ip_address VARCHAR(45),
                    active BOOLEAN DEFAULT 1
                )
                """;
            createNotesTable = """
                CREATE TABLE IF NOT EXISTS notes (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    issuer_uuid VARCHAR(36),
                    content TEXT,
                    timestamp BIGINT
                )
                """;
            createWarningsTable = """
                CREATE TABLE IF NOT EXISTS warnings (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    issuer_uuid VARCHAR(36),
                    reason TEXT,
                    severity INTEGER,
                    timestamp BIGINT,
                    active BOOLEAN DEFAULT 1
                )
                """;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createPunishmentsTable);
            statement.executeUpdate(createNotesTable);
            statement.executeUpdate(createWarningsTable);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database connection: " + e.getMessage());
        }
    }

    @Override
    public void insertPunishment(Punishment punishment) {
        if (punishment == null) {
            return;
        }

        runAsync(() -> {
            String sql = """
                INSERT INTO punishments (target_uuid, type, reason, issuer_uuid, issued_at, expires_at, active) 
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """;
                
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, punishment.getTarget().toString());
                ps.setString(2, punishment.getType().name());
                ps.setString(3, punishment.getReason());
                ps.setString(4, punishment.getIssuer() != null ? punishment.getIssuer().toString() : null);
                ps.setLong(5, punishment.getIssuedAt());
                ps.setLong(6, punishment.getExpiresAt());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert punishment: " + e.getMessage());
            }
        });
    }

    @Override
    public List<Punishment> getPunishments(UUID target) {
        if (target == null) {
            return new ArrayList<>();
        }

        List<Punishment> punishments = new ArrayList<>();
        runSync(() -> {
            String sql = "SELECT * FROM punishments WHERE target_uuid = ? AND active = 1";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, target.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        punishments.add(createPunishmentFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get punishments: " + e.getMessage());
            }
        });
        
        return punishments;
    }

    @Override
    public void deactivatePunishment(UUID target, Punishment.Type type) {
        if (target == null || type == null) {
            return;
        }

        runAsync(() -> {
            String sql = "UPDATE punishments SET active = 0 WHERE target_uuid = ? AND type = ? AND active = 1";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, target.toString());
                ps.setString(2, type.name());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to deactivate punishment: " + e.getMessage());
            }
        });
    }

    @Override
    public List<Punishment> getPunishmentsByIP(String ipAddress) {
        if (ipAddress == null) {
            return new ArrayList<>();
        }

        List<Punishment> punishments = new ArrayList<>();
        runSync(() -> {
            String sql = "SELECT * FROM punishments WHERE ip_address = ? AND active = 1";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, ipAddress);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        punishments.add(createPunishmentFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get punishments by IP: " + e.getMessage());
            }
        });
        
        return punishments;
    }

    @Override
    public void deactivateIPBan(String ipAddress) {
        if (ipAddress == null) {
            return;
        }

        runAsync(() -> {
            String sql = "UPDATE punishments SET active = 0 WHERE ip_address = ? AND type = 'IP_BAN' AND active = 1";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, ipAddress);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to deactivate IP ban: " + e.getMessage());
            }
        });
    }

    /**
     * Create a Punishment object from a database result set.
     * 
     * @param rs The result set
     * @return The Punishment object
     * @throws SQLException If an error occurs reading the result set
     */
    private Punishment createPunishmentFromResultSet(ResultSet rs) throws SQLException {
        return new Punishment(
                UUID.fromString(rs.getString("target_uuid")),
                Punishment.Type.valueOf(rs.getString("type")),
                rs.getString("reason"),
                rs.getLong("issued_at"),
                rs.getLong("expires_at"),
                rs.getString("issuer_uuid") != null ? UUID.fromString(rs.getString("issuer_uuid")) : null
        );
    }

    // --- Notes Implementation ---
    @Override
    public void insertNote(Note note) {
        if (note == null) return;
        runAsync(() -> {
            String sql = "INSERT INTO notes (target_uuid, issuer_uuid, content, timestamp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, note.getTarget().toString());
                ps.setString(2, note.getIssuer() != null ? note.getIssuer().toString() : null);
                ps.setString(3, note.getContent());
                ps.setLong(4, note.getTimestamp());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert note: " + e.getMessage());
            }
        });
    }

    @Override
    public void removeNote(UUID target, int noteId) {
        runAsync(() -> {
            String sql = "DELETE FROM notes WHERE id = ? AND target_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, noteId);
                ps.setString(2, target.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to remove note: " + e.getMessage());
            }
        });
    }

    @Override
    public List<Note> getNotes(UUID target) {
        List<Note> notes = new ArrayList<>();
        runSync(() -> {
            String sql = "SELECT * FROM notes WHERE target_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, target.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        notes.add(createNoteFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get notes: " + e.getMessage());
            }
        });
        return notes;
    }

    @Override
    public Note getNote(int noteId) {
        final Note[] note = {null};
        runSync(() -> {
            String sql = "SELECT * FROM notes WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, noteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        note[0] = createNoteFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get note: " + e.getMessage());
            }
        });
        return note[0];
    }

    private Note createNoteFromResultSet(ResultSet rs) throws SQLException {
        return new Note(
            rs.getInt("id"),
            UUID.fromString(rs.getString("target_uuid")),
            rs.getString("issuer_uuid") != null ? UUID.fromString(rs.getString("issuer_uuid")) : null,
            rs.getString("content"),
            rs.getLong("timestamp")
        );
    }

    // --- Warnings Implementation ---
    @Override
    public void insertWarning(Warning warning) {
        if (warning == null) return;
        runAsync(() -> {
            String sql = "INSERT INTO warnings (target_uuid, issuer_uuid, reason, severity, timestamp, active) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, warning.getTarget().toString());
                ps.setString(2, warning.getIssuer() != null ? warning.getIssuer().toString() : null);
                ps.setString(3, warning.getReason());
                ps.setInt(4, warning.getSeverity());
                ps.setLong(5, warning.getTimestamp());
                ps.setBoolean(6, warning.isActive());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert warning: " + e.getMessage());
            }
        });
    }

    @Override
    public void removeWarning(UUID target, int warningId) {
        runAsync(() -> {
            String sql = "UPDATE warnings SET active = 0 WHERE id = ? AND target_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, warningId);
                ps.setString(2, target.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to remove warning: " + e.getMessage());
            }
        });
    }

    @Override
    public List<Warning> getWarnings(UUID target) {
        List<Warning> warnings = new ArrayList<>();
        runSync(() -> {
            String sql = "SELECT * FROM warnings WHERE target_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, target.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        warnings.add(createWarningFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get warnings: " + e.getMessage());
            }
        });
        return warnings;
    }

    @Override
    public Warning getWarning(int warningId) {
        final Warning[] warning = {null};
        runSync(() -> {
            String sql = "SELECT * FROM warnings WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, warningId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        warning[0] = createWarningFromResultSet(rs);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to get warning: " + e.getMessage());
            }
        });
        return warning[0];
    }

    private Warning createWarningFromResultSet(ResultSet rs) throws SQLException {
        return new Warning(
            rs.getInt("id"),
            UUID.fromString(rs.getString("target_uuid")),
            rs.getString("issuer_uuid") != null ? UUID.fromString(rs.getString("issuer_uuid")) : null,
            rs.getString("reason"),
            rs.getInt("severity"),
            rs.getLong("timestamp"),
            rs.getBoolean("active")
        );
    }

    /**
     * Run a task asynchronously.
     * 
     * @param task The task to run
     */
    private void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Run a task synchronously.
     * 
     * @param task The task to run
     */
    private void runSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}

