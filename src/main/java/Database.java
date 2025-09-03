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
 * Utility class for database operations related to 3D print projects.
 * Provides methods for connecting, inserting projects, and adding print dates.
 * Extend this class to add more queries or update logic as needed.
 */
public class Database {

    /** JDBC URL for H2 database file location */
    private static final String URL = "jdbc:h2:./app_home/print_jobs";

    /**
     * Connects to the H2 database using default credentials.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    /**
     * Connects to the H2 database with lock handling and timeout management.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection connectWithTimeout() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, "sa", "");
        conn.createStatement().execute("SET LOCK_TIMEOUT 5000"); // Set lock timeout to 5 seconds
        return conn;
    }

    /**
     * Inserts a new project into the database and returns its generated ID.
     * @param name        The project name.
     * @param projectType The project type.
     * @param filePath    The absolute path to the project files.
     * @param description The project description.
     * @return The ID of the newly inserted project.
     * @throws SQLException if a database access error occurs.
     *
     * Example usage:
     * <pre>
     *     int id = Database.insertProject("Test", "Model", "/path", "desc");
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
     * Adds a list of last printed dates for a given project.
     * Each date string should be in yyyy-MM-dd format.
     * @param projectId The ID of the project.
     * @param dates     A list of date strings to add.
     * @throws SQLException if a database access error occurs.
     *
     * Example usage:
     * <pre>
     *     Database.addLastPrintedDates(1, Arrays.asList("2025-09-01"));
     * </pre>
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
     * Executes a set of database operations within a transaction.
     * Rolls back the transaction if any operation fails.
     *
     * @param operations A lambda containing the database operations to execute.
     * @throws SQLException if a database access error occurs.
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
     * Rolls back a transaction if any operation fails.
     * This method ensures that the database remains in a consistent state.
     * @param operations A lambda containing the database operations to execute.
     * @throws SQLException if a database access error occurs.
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
     * Functional interface for database operations within a transaction.
     */
    @FunctionalInterface
    public interface DatabaseOperation {
        void execute(Connection conn) throws SQLException;
    }

    /**
     * Functional interface for executing code within a transaction.
     */
    @FunctionalInterface
    public interface TransactionCode {
        void execute(Connection conn) throws SQLException;
    }

    /**
     * Verifies the integrity of the database by checking for required tables.
     * Logs errors if any required tables are missing.
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
     * Checks if the database is corrupted by attempting a simple query.
     * Logs an error and returns true if corruption is detected.
     *
     * @return True if the database is corrupted, false otherwise.
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
     * Recovers the database from the most recent backup.
     * Finds the latest backup file and restores the database from it.
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
     * Updates the database schema to include new columns and tables.
     * Adds recipient column, modifies tags, and ensures project_type is updatable.
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
     * Loads project data by ID.
     * @param projectId The ID of the project to load.
     * @return A ResultSet containing the project data.
     * @throws SQLException if a database access error occurs.
     */
    public static ResultSet loadProjectById(int projectId) throws SQLException {
        final String sql = "SELECT * FROM projects WHERE id = ?";
        Connection conn = connectWithTimeout();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, projectId);
        return pstmt.executeQuery();
    }

    /**
     * Updates project details in the database.
     * @param projectId   The ID of the project to update.
     * @param name        The updated project name.
     * @param projectType The updated project type.
     * @param recipient   The updated recipient name.
     * @param tags        The updated tags (as a delimited string).
     * @param description The updated project description.
     * @throws SQLException if a database access error occurs.
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
