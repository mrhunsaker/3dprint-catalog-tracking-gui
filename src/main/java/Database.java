import java.sql.*;

public class Database {
    private static final String URL = "jdbc:h2:./app_home/print_jobs";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }
}
