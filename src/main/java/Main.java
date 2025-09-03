import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.LookAndFeel;
import java.util.Map;
import java.util.TreeMap;
import java.awt.*;
import javax.swing.*;
import utils.DatabaseBackup;
import utils.ErrorHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application window for the 3D Print Job Tracker GUI.
 * Handles initialization, menu bar, theme selection, and main content panel.
 * Extend this class to add more features or dialogs.
 */
public class Main extends JFrame {
    /**
     * Map of available IntelliJ themes for FlatLaf look and feel.
     * Used to populate the Theme menu and apply selected theme.
     */
    private static final Map<String, Class<? extends LookAndFeel>> INTELLIJ_THEMES = new TreeMap<>();
    static {
        INTELLIJ_THEMES.put("Arc", FlatArcIJTheme.class);
        INTELLIJ_THEMES.put("Arc Orange", FlatArcOrangeIJTheme.class);
        INTELLIJ_THEMES.put("Carbon", FlatCarbonIJTheme.class);
        INTELLIJ_THEMES.put("Cobalt 2", FlatCobalt2IJTheme.class);
        INTELLIJ_THEMES.put("Cyan Light", FlatCyanLightIJTheme.class);
        INTELLIJ_THEMES.put("Dark Purple", FlatDarkPurpleIJTheme.class);
        INTELLIJ_THEMES.put("Dracula", FlatDraculaIJTheme.class);
        INTELLIJ_THEMES.put("Gray", FlatGrayIJTheme.class);
        INTELLIJ_THEMES.put("Gruvbox Dark Hard", FlatGruvboxDarkHardIJTheme.class);
        INTELLIJ_THEMES.put("Hiberbee Dark", FlatHiberbeeDarkIJTheme.class);
        INTELLIJ_THEMES.put("High Contrast", FlatHighContrastIJTheme.class);
        INTELLIJ_THEMES.put("Light Flat", FlatLightFlatIJTheme.class);
        INTELLIJ_THEMES.put("Material Design Dark", FlatMaterialDesignDarkIJTheme.class);
        INTELLIJ_THEMES.put("Monocai", FlatMonocaiIJTheme.class);
        INTELLIJ_THEMES.put("Nord", FlatNordIJTheme.class);
        INTELLIJ_THEMES.put("One Dark", FlatOneDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Dark", FlatSolarizedDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Light", FlatSolarizedLightIJTheme.class);
        INTELLIJ_THEMES.put("Spacegray", FlatSpacegrayIJTheme.class);
        INTELLIJ_THEMES.put("Vuesion", FlatVuesionIJTheme.class);
    }

    private ProjectFormPanel projectForm;

    /**
     * Constructs the main application window, initializes database, menu bar, and content.
     * Sets the window to maximized and visible.
     */
    public Main() {
        setTitle("3D Print Job Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open maximized
        setLocationRelativeTo(null); // Center the window

        // Initialize the database
        initializeDatabase();

        // Verify database integrity on startup
        Database.verifyDatabaseIntegrity();

        // Check for database corruption on startup
        if (Database.isDatabaseCorrupted()) {
            int userChoice = JOptionPane.showConfirmDialog(
                this,
                "The database appears to be corrupted. Would you like to restore the most recent backup?",
                "Database Corruption Detected",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
            );

            if (userChoice == JOptionPane.YES_OPTION) {
                try {
                    String backupDir = "app_home/backups/";
                    File latestBackup = Files.list(Paths.get(backupDir))
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                        .orElse(null);

                    if (latestBackup != null) {
                        DatabaseBackup.restoreBackup(latestBackup.getAbsolutePath(), "app_home/print_jobs.mv.db");
                        ErrorHandler.showErrorToUser("Database restored successfully.", "Restored from: " + latestBackup.getAbsolutePath());
                    } else {
                        ErrorHandler.showErrorToUser("No backups available to restore.", "Please create a new database.");
                    }
                } catch (IOException e) {
                    ErrorHandler.showErrorToUser("Failed to restore database from backup.", e.getMessage());
                }
            } else {
                ErrorHandler.showErrorToUser("Application cannot proceed with a corrupted database.", "Please restore a backup manually.");
                System.exit(1);
            }
        }

        // Verify the integrity of the most recent backup
        String backupDir = "app_home/backups/";
        try {
            File latestBackup = Files.list(Paths.get(backupDir))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .orElse(null);

            if (latestBackup != null) {
                boolean isBackupValid = DatabaseBackup.verifyBackupIntegrity(latestBackup.getAbsolutePath());
                if (!isBackupValid) {
                    ErrorHandler.showErrorToUser("The most recent backup is invalid.", "Please create a new backup.");
                }
            } else {
                ErrorHandler.showErrorToUser("No backups available to verify.", "Please create a backup.");
            }
        } catch (IOException e) {
            ErrorHandler.showErrorToUser("Failed to verify the most recent backup.", e.getMessage());
        }

        // Verify database and schedule backups
        verifyDatabaseAndScheduleBackups();

        // Create menu bar
        createMenuBar();

        // Create main content
        createMainContent();

        setVisible(true);
    }

    /**
     * Initializes the database tables if they do not exist.
     * Shows an error dialog if initialization fails.
     */
    private void initializeDatabase() {
        try (Connection conn = Database.connect()) {
            // Create projects table if it doesn't exist
            Statement stmt = conn.createStatement();
            stmt.execute(
                """
                -- In Main.java's initializeDatabase() method
                CREATE TABLE IF NOT EXISTS projects (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    project_type VARCHAR(50),
                    file_path VARCHAR(255),
                    description TEXT,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                CREATE TABLE IF NOT EXISTS last_printed_dates (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    project_id INT,
                    print_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
                );
                """
            );
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            ErrorHandler.showErrorToUser(
                "Failed to initialize the database. Please check your configuration.",
                e.getMessage()
            );
        }
    }

    /**
     * Verifies the integrity of the database and schedules automatic backups.
     */
    private void verifyDatabaseAndScheduleBackups() {
        try {
            // Verify database integrity
            String dbFilePath = "app_home/print_jobs.mv.db";
            if (!DatabaseBackup.verifyBackup(dbFilePath)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Database integrity check failed. Please restore from a backup.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }

            // Schedule daily backups
            scheduleDailyBackups();
        } catch (Exception e) {
            ErrorHandler.logError("Failed to verify database or schedule backups", e);
            JOptionPane.showMessageDialog(
                this,
                "Critical error during database verification. Application will exit.",
                "Critical Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }

    /**
     * Creates the menu bar with File, Project, and Theme menus.
     * Theme menu allows user to select color scheme.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        Font menuFont = new Font("SansSerif", Font.PLAIN, 18);

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(menuFont);

        JMenuItem backupItem = new JMenuItem("Backup Database");
        backupItem.setFont(menuFont);
        backupItem.addActionListener(event -> {
            try {
                String dbFilePath = "app_home/print_jobs.mv.db"; // Path to the database file
                DatabaseBackup.createBackup(dbFilePath);
                ErrorHandler.showErrorToUser("Backup completed successfully.", "Backup created at: " + dbFilePath);
            } catch (IOException e) {
                ErrorHandler.showErrorToUser("Failed to create backup.", e.getMessage());
            }
        });
        fileMenu.add(backupItem);

        JMenuItem restoreItem = new JMenuItem("Restore Database");
        restoreItem.setFont(menuFont);
        restoreItem.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Backup File");
            int userSelection = fileChooser.showOpenDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String dbFilePath = "app_home/print_jobs.mv.db"; // Path to the database file
                    DatabaseBackup.restoreBackup(selectedFile.getAbsolutePath(), dbFilePath);
                    ErrorHandler.showErrorToUser("Restore completed successfully.", "Database restored from: " + selectedFile.getAbsolutePath());
                } catch (IOException e) {
                    ErrorHandler.showErrorToUser("Failed to restore database.", e.getMessage());
                }
            }
        });
        fileMenu.add(restoreItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(menuFont);
        exitItem.addActionListener(event -> {
            System.exit(0);
        });
        fileMenu.add(exitItem);

        // Project menu
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setFont(menuFont);
        JMenuItem searchItem = new JMenuItem("Search Projects");
        searchItem.setFont(menuFont);
        searchItem.addActionListener(event -> {
            openSearchDialog();
        });
        projectMenu.add(searchItem);

        // Theme menu
        JMenu themeMenu = new JMenu("Theme");
        themeMenu.setFont(menuFont);
        ButtonGroup themeGroup = new ButtonGroup();
        for (String themeName : INTELLIJ_THEMES.keySet()) {
            JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(themeName);
            themeItem.setFont(menuFont);
            themeMenu.add(themeItem);
            themeGroup.add(themeItem);
            themeItem.addActionListener(event -> {
                try {
                    UIManager.setLookAndFeel(INTELLIJ_THEMES.get(themeName).getDeclaredConstructor().newInstance());
                    SwingUtilities.updateComponentTreeUI(this);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to apply theme: " + ex.getMessage(), "Theme Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        menuBar.add(fileMenu);
        menuBar.add(projectMenu);
        menuBar.add(themeMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Sets up the main content area, including welcome label and project form panel.
     */
    private void createMainContent() {
        setLayout(new BorderLayout());

        // Add welcome message
        JLabel welcomeLabel = new JLabel(
            "3D Print Job Tracker",
            SwingConstants.CENTER
        );
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // Add project form
        projectForm = new ProjectFormPanel();
        add(projectForm, BorderLayout.CENTER);

    // Remove the bottom Search Projects button
    }

    /**
     * Opens the SearchDialog for searching projects.
     */
    private void openSearchDialog() {
        SearchDialog searchDialog = new SearchDialog(this);
        searchDialog.setVisible(true);
    }

    /**
     * Schedules automatic daily database backups.
     */
    private void scheduleDailyBackups() {
        Timer backupTimer = new Timer(24 * 60 * 60 * 1000, event -> {
            try {
                String dbFilePath = "app_home/print_jobs.mv.db"; // Path to the database file
                DatabaseBackup.createBackup(dbFilePath);
                ErrorHandler.logInfo("Daily database backup created successfully.");
            } catch (IOException e) {
                ErrorHandler.logError("Failed to create daily database backup.", e);
            }
        });
        backupTimer.setRepeats(true);
        backupTimer.start();
    }

    /**
     * Main entry point. Sets system look and feel and launches the application.
     * Ensures GUI is created on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println(
                "Failed to set look and feel: " + e.getMessage()
            );
        }

        // Ensure GUI creation happens on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            System.out.println("3D Print Job Tracker started...");
            new Main();
        });
    }
}
