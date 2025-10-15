# Connect to H2 database
I keep my app at D:/GitHubRepos/3dprint-catalog-tracking-gui/app_home/print_jobs

## java script
```java
import java.sql.*;

public class ConnectH2 {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:D:/GitHubRepos/3dprint-catalog-tracking-gui/app_home/print_jobs";
        Connection conn = DriverManager.getConnection(url, "sa", "");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM YOUR_TABLE");
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
        conn.close();
    }
}
```
## Manual

### Steps
- install H2 console
    - [https://www.h2database.com/html/installation.html#installing](https://www.h2database.com/html/installation.html#installing)
    - [file:///C:/Users/Ryan%20Hunsaker/Downloads/h2.pdf](https://github.com/h2database/h2database/releases/download/version-2.3.232/h2.pdf)
- open H2 console
    - Windows:
        - java -jar 'C:\Program Files (x86)\H2\bin\h2-2.3.232.jar'
    - Linus
        -java -jar <path_to_H2_installation> 
- fill in fields:
    - Driver Class:  org.h2.driver
    - JDBC URL: D:/GitHubRepos/3dprint-catalog-tracking-gui/app_home/print_jobs
    - User: sa
    - Password:

---

## How to Access, Edit, and Search Your Database Entries

### Accessing the Database
- Use the H2 Console as described above to connect to your database.
- Once connected, you will see a SQL command window where you can run queries and make edits.

### Searching Entries
- To search for entries, use SQL `SELECT` statements. For example:
    ```sql
    SELECT * FROM YOUR_TABLE WHERE COLUMN_NAME = 'search_value';
    SELECT * FROM YOUR_TABLE WHERE COLUMN_NAME LIKE '%partial_value%';
    ```
- Replace `YOUR_TABLE` and `COLUMN_NAME` with your actual table and column names.

### Editing Entries
- To update an entry, use the SQL `UPDATE` statement:
    ```sql
    UPDATE YOUR_TABLE SET COLUMN_NAME = 'new_value' WHERE id = 1;
    ```
- To delete an entry:
    ```sql
    DELETE FROM YOUR_TABLE WHERE id = 1;
    ```
- To insert a new entry:
    ```sql
    INSERT INTO YOUR_TABLE (COLUMN1, COLUMN2) VALUES ('value1', 'value2');
    ```

### Tips
- Always back up your database before making bulk edits.
- You can export your data using the H2 Console's export features.
- Refer to the [H2 SQL documentation](https://www.h2database.com/html/commands.html) for more advanced queries and operations.

---

If you have any questions or need help with specific queries, consult the H2 documentation or reach out to your project administrator.

