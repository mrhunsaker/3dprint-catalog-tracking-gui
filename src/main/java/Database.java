import java.sql.*;
import java.util.List;
import utils.ErrorHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import utils.DatabaseBackup;

/**
 * Database helper utilities used by the application for H2 persistence.
 *
 * <p>
 * This class centralizes connection handling, simple CRUD helpers and
 * transaction execution helpers. It is intentionally minimal and
 * synchronous to match the small desktop application's usage pattern.
 * </p>
 *
 * <h2>H2 database details</h2>
 * <ul>
 *   <li>JDBC URL: <code>jdbc:h2:./app_home/print_jobs</code> — relative to the
 *       repository root; the actual data file is <code>app_home/print_jobs.mv.db</code>.</li>
 *   <li>Default credentials: user <code>sa</code> with an empty password (used
 *       intentionally for local desktop deployments; secure or change in
 *       production).</li>
 *   <li>Backups: The project includes {@link utils.DatabaseBackup} which copies
 *       the MV store file into <code>app_home/backups/</code>. Prefer creating
 *       a backup before performing operations that modify the database.</li>
 *   <li>Recovery: Use {@link utils.DatabaseBackup#restoreBackup(String,String)}
 *       to restore an archived file into <code>app_home/print_jobs.mv.db</code>.
 *       Also see {@link #recoverDatabase()} which attempts to restore the most
 *       recent backup found in the backups folder.</li>
 *   <li>Concurrency: This application uses embedded H2 file mode. Avoid opening
 *       the same database file from multiple JVMs concurrently — prefer the
 *       single-process desktop usage pattern. When necessary, configure a
 *       networked H2 server or use a separate DB server.</li>
 *   <li>Lock timeout: {@link #connectWithTimeout()} sets a 5s lock timeout to
 *       reduce UI hangs when a lock is held by another process.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class Database {

    /** JDBC URL for H2 database file location */
    private static final String URL = "jdbc:h2:./app_home/print_jobs";

    /**
     * Open a JDBC connection using the configured URL and default credentials.
     *
     * @return a new {@link Connection}
     * @throws SQLException if a connection cannot be established
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    /**
     * Open a JDBC connection and configure a shorter lock timeout to reduce UI hangs
     * when another process holds database locks.
     *
     * @return a configured {@link Connection}
     * @throws SQLException if a connection cannot be established
     */
    public static Connection connectWithTimeout() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, "sa", "");
        conn.createStatement().execute("SET LOCK_TIMEOUT 5000"); // 5 seconds
        return conn;
    }

    /**
     * Insert a new project record and return the generated primary key.
     *
     * @param name the project name
     * @param projectType the project type string
     * @param filePath absolute filesystem path to project files
     * @param description free-text description
     * @return generated id for the new project
     * @throws SQLException if insertion fails
     *
    * Example:
    * <pre>
    * int id = Database.insertProject("Box", "Prototype", "/tmp/box", "Student project");
    * </pre>
     */
    public static int insertProject(
        String name,
        String projectType,
        String filePath,
        String description
    ) throws SQLException {
        final String sql = "INSERT INTO projects(name, project_type, file_path, description) VALUES(?, ?, ?, ?)";
        final int[] generatedId = {-1};

        executeTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, projectType);
                pstmt.setString(3, filePath);
                pstmt.setString(4, description);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId[0] = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                ErrorHandler.logError("Failed to execute transaction for adding project: " + name, e);
                throw e;
            }
        });

        return generatedId[0];
    }

    /**
     * Add a collection of last-printed dates for a project in a single transaction.
     * Dates must be provided as strings parsable to a timestamp using the
     * pattern yyyy-MM-dd (time portion is set to midnight).
     *
     * @param projectId the project primary key
     * @param dates list of date strings in yyyy-MM-dd format
     * @throws SQLException on DB errors
     */
    public static void addLastPrintedDates(int projectId, List<String> dates) throws SQLException {
        final String sql = "INSERT INTO last_printed_dates(project_id, print_date) VALUES(?, ?)";

        executeTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String date : dates) {
                    pstmt.setInt(1, projectId);
                    pstmt.setTimestamp(2, Timestamp.valueOf(date + " 00:00:00.0"));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            } catch (SQLException e) {
                ErrorHandler.logError("Failed to execute transaction for adding last printed dates for project ID: " + projectId, e);
                throw e;
            }
        });
    }

    /**
     * Execute code inside a transaction. The {@code operations} lambda receives
     * a {@link Connection} which it must use for all statements to ensure they
     * participate in the same transaction.
     *
     * @param operations lambda executed within a transaction
     * @throws SQLException if the execution or commit fails
     */
    public static void executeTransaction(DatabaseOperation operations) throws SQLException {
        try (Connection conn = connectWithTimeout()) {
            try {
                conn.setAutoCommit(false);
                operations.execute(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Transaction failed and was rolled back.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Similar to {@link #executeTransaction(DatabaseOperation)} but logs the error
     * before rethrowing so callers may avoid duplicating logging logic.
     *
     * @param operations transactional operations
     * @throws SQLException on failure
     */
    public static void executeTransactionWithRollback(DatabaseOperation operations) throws SQLException {
        try (Connection conn = connectWithTimeout()) {
            try {
                conn.setAutoCommit(false);
                operations.execute(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                ErrorHandler.logError("Transaction failed and was rolled back.", e);
                throw new SQLException("Transaction failed and was rolled back.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Functional interface used by transaction helpers.
     */
    @FunctionalInterface
    public interface DatabaseOperation {
        /**
         * Execute database operations using the provided connection. Implementations
         * must use the supplied {@code conn} for all statements so they participate
         * in the surrounding transaction.
         *
         * @param conn active JDBC connection
         * @throws SQLException when a database error occurs
         */
        void execute(Connection conn) throws SQLException;
    }

    /**
     * Backwards-compatible alias used in some places in the codebase.
     */
    @FunctionalInterface
    public interface TransactionCode {
        /**
         * Execute transactional code using the provided connection.
         *
         * @param conn active JDBC connection
         * @throws SQLException on database errors
         */
        void execute(Connection conn) throws SQLException;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Database() {
        // utility class
    }

    /**
     * Basic integrity check ensuring required tables exist. Any missing table
     * will be logged via {@link utils.ErrorHandler}.
     */
    public static void verifyDatabaseIntegrity() {
        try (Connection conn = connectWithTimeout()) {
            String[] requiredTables = {"projects", "last_printed_dates"};

            for (String table : requiredTables) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT 1 FROM " + table + " LIMIT 1")) {
                    pstmt.executeQuery();
                } catch (SQLException e) {
                    ErrorHandler.logError("Missing or corrupted table: " + table, e);
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logError("Failed to verify database integrity.", e);
        }
    }

    /**
     * Quick corruption test: attempt a simple query and report failure.
     *
     * @return true when corruption is detected, false otherwise
     */
    public static boolean isDatabaseCorrupted() {
        try (Connection conn = connectWithTimeout()) {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1")) {
                pstmt.executeQuery();
                return false; // Database is not corrupted
            }
        } catch (SQLException e) {
            ErrorHandler.logError("Database corruption detected.", e);
            return true; // Database is corrupted
        }
    }

    /**
     * Recover from the most recent backup found in `app_home/backups/`.
     * This method attempts to locate, verify and restore the latest backup file.
     */
    public static void recoverDatabase() {
        try {
            String backupDir = "app_home/backups/";
            File latestBackup = Files.list(Paths.get(backupDir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .orElse(null);

            if (latestBackup != null) {
                DatabaseBackup.restoreBackup(latestBackup.getAbsolutePath(), "app_home/print_jobs.mv.db");
                System.out.println("Database successfully recovered from backup: " + latestBackup.getAbsolutePath());
            } else {
                System.err.println("No backups available for recovery.");
            }
        } catch (IOException e) {
            System.err.println("Failed to recover database: " + e.getMessage());
        }
    }

    /**
     * Apply simple schema updates to ensure optional columns exist.
     * This is idempotent and safe to call at startup.
     */
    public static void updateSchema() {
        try (Connection conn = connectWithTimeout()) {
            try (Statement stmt = conn.createStatement()) {
                // Add recipient column to projects table
                stmt.executeUpdate("ALTER TABLE projects ADD COLUMN IF NOT EXISTS recipient VARCHAR(255)");

                // Modify tags to support multiple values (if using delimited string)
                stmt.executeUpdate("ALTER TABLE projects ADD COLUMN IF NOT EXISTS tags VARCHAR(1024)");

                // Ensure project_type column is updatable (no schema change needed if already present)
                System.out.println("Schema updated successfully.");
            }
        } catch (SQLException e) {
            ErrorHandler.logError("Failed to update database schema.", e);
        }
    }

    /**
     * Load project record by id. The returned ResultSet is connected to an open
     * {@link Connection} which the caller is responsible for closing. Callers
     * should ensure they close both the ResultSet and the underlying connection.
     *
     * @param projectId project primary key
     * @return ResultSet positioned before first row
     * @throws SQLException on failure
     */
    public static ResultSet loadProjectById(int projectId) throws SQLException {
        final String sql = "SELECT * FROM projects WHERE id = ?";
        Connection conn = connectWithTimeout();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, projectId);
        return pstmt.executeQuery();
    }

    /**
     * Update a project's metadata in the database.
     *
     * @param projectId id of the project to update
     * @param name updated project name
     * @param projectType updated type
     * @param recipient updated recipient
     * @param tags comma-separated tags string
     * @param description updated description
     * @throws SQLException on failure
     */
    public static void updateProject(
        int projectId,
        String name,
        String projectType,
        String recipient,
        String tags,
        String description
    ) throws SQLException {
        final String sql = "UPDATE projects SET name = ?, project_type = ?, recipient = ?, tags = ?, description = ? WHERE id = ?";

        executeTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, projectType);
                pstmt.setString(3, recipient);
                pstmt.setString(4, tags);
                pstmt.setString(5, description);
                pstmt.setInt(6, projectId);
                pstmt.executeUpdate();
            }
        });
    }

    // Add more database utility methods as needed
}
