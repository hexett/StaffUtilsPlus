package me.hexett.staffUtilsPlus.db;

import me.hexett.staffUtilsPlus.service.punishments.Punishment;
import me.hexett.staffUtilsPlus.service.notes.Note;
import me.hexett.staffUtilsPlus.service.warnings.Warning;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private final ExecutorService executor;
    private final Object connectionLock = new Object();

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
        this.executor = Executors.newFixedThreadPool(2, r -> {
            Thread thread = new Thread(r, "StaffUtilsPlus-DB");
            thread.setDaemon(true);
            return thread;
        });
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

        // Enable SQLite optimizations
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA synchronous=NORMAL");
            stmt.execute("PRAGMA cache_size=10000");
            stmt.execute("PRAGMA temp_store=MEMORY");
        }
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
        String url = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                host, port, dbName);
        connection = DriverManager.getConnection(url, user, pass);
    }

    /**
     * Ensure connection is alive, reconnect if necessary.
     */
    private void ensureConnection() throws SQLException {
        synchronized (connectionLock) {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                plugin.getLogger().warning("Database connection lost, reconnecting...");
                if ("sqlite".equals(type)) {
                    connectSQLite();
                } else if ("mysql".equals(type)) {
                    connectMySQL();
                }
            }
        }
    }

    /**
     * Set up the database tables.
     */
    private void setupTables() throws SQLException {
        String createPunishmentsTable;
        String createNotesTable;
        String createWarningsTable;
        String createPlayerIPsTable;

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
            createPlayerIPsTable = """
            CREATE TABLE IF NOT EXISTS player_ips (
                uuid VARCHAR(36) PRIMARY KEY,
                ip_address VARCHAR(45) NOT NULL,
                last_updated BIGINT NOT NULL
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
                    active BOOLEAN DEFAULT 1,
                    INDEX idx_target_active (target_uuid, active),
                    INDEX idx_issuer (issuer_uuid),
                    INDEX idx_ip_active (ip_address, active)
                )
                """;
            createNotesTable = """
                CREATE TABLE IF NOT EXISTS notes (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    target_uuid VARCHAR(36) NOT NULL,
                    issuer_uuid VARCHAR(36),
                    content TEXT,
                    timestamp BIGINT,
                    INDEX idx_target (target_uuid)
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
                    active BOOLEAN DEFAULT 1,
                    INDEX idx_target (target_uuid)
                )
                """;
            createPlayerIPsTable = """
                CREATE TABLE IF NOT EXISTS player_ips (
                    uuid VARCHAR(36) PRIMARY KEY,
                    ip_address VARCHAR(45) NOT NULL,
                    last_updated BIGINT NOT NULL,
                    INDEX idx_ip_address (ip_address)
                )
                """;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createPunishmentsTable);
            statement.executeUpdate(createNotesTable);
            statement.executeUpdate(createWarningsTable);
            statement.executeUpdate(createPlayerIPsTable);
        }

        // Create indexes for SQLite (MySQL has them in CREATE TABLE)
        if ("sqlite".equals(type)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE INDEX IF NOT EXISTS idx_punishments_target_active ON punishments(target_uuid, active)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_punishments_issuer ON punishments(issuer_uuid)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_punishments_ip_active ON punishments(ip_address, active)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_notes_target ON notes(target_uuid)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_warnings_target ON warnings(target_uuid)");
                statement.execute("CREATE INDEX IF NOT EXISTS idx_player_ips_ip ON player_ips(ip_address)");
            }
        }
    }

    @Override
    public void close() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

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

        CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO punishments (target_uuid, type, reason, issuer_uuid, issued_at, expires_at, active) 
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """;

            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, punishment.getTarget().toString());
                    ps.setString(2, punishment.getType().name());
                    ps.setString(3, punishment.getReason());
                    ps.setString(4, punishment.getIssuer() != null ? punishment.getIssuer().toString() : null);
                    ps.setLong(5, punishment.getIssuedAt());
                    ps.setLong(6, punishment.getExpiresAt());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert punishment: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor).exceptionally(throwable -> {
            plugin.getLogger().severe("Unexpected error inserting punishment: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public List<Punishment> getPunishments(UUID target) {
        if (target == null) {
            return new ArrayList<>();
        }

        try {
            return CompletableFuture.supplyAsync(() -> {
                List<Punishment> punishments = new ArrayList<>();
                String sql = "SELECT * FROM punishments WHERE target_uuid = ? AND active = 1";

                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, target.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                punishments.add(createPunishmentFromResultSet(rs));
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get punishments: " + e.getMessage());
                    e.printStackTrace();
                }
                return punishments;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting punishments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Punishment> getPunishmentsByIssuer(UUID target) {
        if (target == null) {
            return new ArrayList<>();
        }

        try {
            return CompletableFuture.supplyAsync(() -> {
                List<Punishment> punishments = new ArrayList<>();
                String sql = "SELECT * FROM punishments WHERE issuer_uuid = ?";

                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, target.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                punishments.add(createPunishmentFromResultSet(rs));
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get punishments: " + e.getMessage());
                    e.printStackTrace();
                }
                return punishments;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting punishments by issuer: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deactivatePunishment(UUID target, Punishment.Type type) {
        if (target == null || type == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE punishments SET active = 0 WHERE target_uuid = ? AND type = ? AND active = 1";

            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, target.toString());
                    ps.setString(2, type.name());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to deactivate punishment: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor).exceptionally(throwable -> {
            plugin.getLogger().severe("Unexpected error deactivating punishment: " + throwable.getMessage());
            return null;
        });
    }

    @Override
    public String getPlayerIP(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        try {
            return CompletableFuture.supplyAsync(() -> {
                String sql = "SELECT ip_address FROM player_ips WHERE uuid = ?";

                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, uuid.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                return rs.getString("ip_address");
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get player IP: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting player IP: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<UUID> getPlayersByIP(String ipAddress) {
        if (ipAddress == null) {
            return new ArrayList<>();
        }

        try {
            return CompletableFuture.supplyAsync(() -> {
                List<UUID> players = new ArrayList<>();
                String sql = "SELECT uuid FROM player_ips WHERE ip_address = ?";

                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, ipAddress);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                players.add(UUID.fromString(rs.getString("uuid")));
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get players by IP: " + e.getMessage());
                    e.printStackTrace();
                }
                return players;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting players by IP: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void recordPlayerIP(UUID uuid, String ipAddress) {
        if (uuid == null || ipAddress == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            String sql;
            if ("sqlite".equals(type)) {
                sql = "INSERT OR REPLACE INTO player_ips (uuid, ip_address, last_updated) VALUES (?, ?, ?)";
            } else {
                sql = "INSERT INTO player_ips (uuid, ip_address, last_updated) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE ip_address = VALUES(ip_address), last_updated = VALUES(last_updated)";
            }

            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, ipAddress);
                    ps.setLong(3, System.currentTimeMillis());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to record player IP: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor).exceptionally(throwable -> {
            plugin.getLogger().severe("Unexpected error recording player IP: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public List<Punishment> getPunishmentsByIP(String ipAddress) {
        if (ipAddress == null) {
            return new ArrayList<>();
        }

        try {
            return CompletableFuture.supplyAsync(() -> {
                List<Punishment> punishments = new ArrayList<>();
                String sql = "SELECT * FROM punishments WHERE ip_address = ? AND active = 1";

                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, ipAddress);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                punishments.add(createPunishmentFromResultSet(rs));
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get punishments by IP: " + e.getMessage());
                    e.printStackTrace();
                }
                return punishments;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting punishments by IP: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deactivateIPBan(String ipAddress) {
        if (ipAddress == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE punishments SET active = 0 WHERE ip_address = ? AND type = 'IP_BAN' AND active = 1";

            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, ipAddress);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to deactivate IP ban: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor).exceptionally(throwable -> {
            plugin.getLogger().severe("Unexpected error deactivating IP ban: " + throwable.getMessage());
            return null;
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
        CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO notes (target_uuid, issuer_uuid, content, timestamp) VALUES (?, ?, ?, ?)";
            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, note.target().toString());
                    ps.setString(2, note.issuer() != null ? note.issuer().toString() : null);
                    ps.setString(3, note.content());
                    ps.setLong(4, note.timestamp());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert note: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public void removeNote(UUID target, int noteId) {
        CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM notes WHERE id = ? AND target_uuid = ?";
            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, noteId);
                    ps.setString(2, target.toString());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to remove note: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public List<Note> getNotes(UUID target) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                List<Note> notes = new ArrayList<>();
                String sql = "SELECT * FROM notes WHERE target_uuid = ?";
                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, target.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                notes.add(createNoteFromResultSet(rs));
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get notes: " + e.getMessage());
                    e.printStackTrace();
                }
                return notes;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting notes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Note getNote(int noteId) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                String sql = "SELECT * FROM notes WHERE id = ?";
                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setInt(1, noteId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                return createNoteFromResultSet(rs);
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get note: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting note: " + e.getMessage());
            return null;
        }
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
        CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO warnings (target_uuid, issuer_uuid, reason, severity, timestamp, active) VALUES (?, ?, ?, ?, ?, ?)";
            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, warning.getTarget().toString());
                    ps.setString(2, warning.getIssuer() != null ? warning.getIssuer().toString() : null);
                    ps.setString(3, warning.getReason());
                    ps.setInt(4, warning.getSeverity());
                    ps.setLong(5, warning.getTimestamp());
                    ps.setBoolean(6, warning.isActive());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to insert warning: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public void removeWarning(UUID target, int warningId) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE warnings SET active = 0 WHERE id = ? AND target_uuid = ?";
            try {
                ensureConnection();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, warningId);
                    ps.setString(2, target.toString());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to remove warning: " + e.getMessage());
                e.printStackTrace();
            }
        }, executor);
    }

    @Override
    public List<Warning> getWarnings(UUID target) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                List<Warning> warnings = new ArrayList<>();
                String sql = "SELECT * FROM warnings WHERE target_uuid = ?";
                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setString(1, target.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                warnings.add(createWarningFromResultSet(rs));
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get warnings: " + e.getMessage());
                    e.printStackTrace();
                }
                return warnings;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting warnings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Warning getWarning(int warningId) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                String sql = "SELECT * FROM warnings WHERE id = ?";
                try {
                    ensureConnection();
                    try (PreparedStatement ps = connection.prepareStatement(sql)) {
                        ps.setInt(1, warningId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                return createWarningFromResultSet(rs);
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to get warning: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }, executor).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting warning: " + e.getMessage());
            return null;
        }
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
}