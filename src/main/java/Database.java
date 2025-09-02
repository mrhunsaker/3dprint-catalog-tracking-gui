import java.sql.*;
import java.util.List;
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
        String sql = "INSERT INTO projects(name, project_type, file_path, description) VALUES(?, ?, ?, ?)";
        int generatedId = -1;

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            pstmt.setString(1, name);
            pstmt.setString(2, projectType);
            pstmt.setString(3, filePath);
            pstmt.setString(4, description);
            pstmt.executeUpdate();

            // Retrieve the generated ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }
        }
        return generatedId;
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
        String sql = "INSERT INTO last_printed_dates(project_id, print_date) VALUES(?, ?)";

        try (
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            for (String date : dates) {
                // Convert date string to SQL Timestamp (midnight)
                pstmt.setInt(1, projectId);
                pstmt.setTimestamp(2, Timestamp.valueOf(date + " 00:00:00.0"));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    // Add more database utility methods as needed
}
