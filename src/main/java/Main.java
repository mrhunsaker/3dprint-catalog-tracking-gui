import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import javax.swing.AbstractButton;

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

    private static Main instance;

    public static synchronized Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }
        return instance;
    }

    /**
     * Constructs the main application window, initializes database, menu bar, and content.
     * Sets the window to maximized and visible.
     */
    public Main() {
        if (instance != null) {
            throw new IllegalStateException("An instance of Main already exists.");
        }
        instance = this;

        setTitle("3D Print Job Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure only one instance closes the application
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
        backupItem.setAccelerator(KeyStroke.getKeyStroke('B', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        backupItem.addActionListener(this::performBackup);
        fileMenu.add(backupItem);

        JMenuItem restoreItem = new JMenuItem("Restore Database");
        restoreItem.setFont(menuFont);
        restoreItem.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        restoreItem.addActionListener(this::performRestore);
        fileMenu.add(restoreItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(menuFont);
        exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exitItem.addActionListener(this::exitApplication);
        fileMenu.add(exitItem);

        // Project menu
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setFont(menuFont);
        JMenuItem searchItem = new JMenuItem("Search Projects");
        searchItem.setFont(menuFont);
        searchItem.setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        searchItem.addActionListener(this::openSearchDialog);
        projectMenu.add(searchItem);

        // Theme menu
        JMenu themeMenu = new JMenu("Theme");
        themeMenu.setFont(menuFont);
        ButtonGroup themeGroup = new ButtonGroup();
        themeMenu.putClientProperty("themeGroup", themeGroup); // Store ButtonGroup in client property

        for (String themeName : INTELLIJ_THEMES.keySet()) {
            JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(themeName);
            themeItem.setFont(menuFont);
            themeMenu.add(themeItem);
            themeGroup.add(themeItem);
            themeItem.addActionListener(this::toggleTheme);
        }

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(menuFont);

        JMenuItem userGuideItem = new JMenuItem("User Guide");
        userGuideItem.setFont(menuFont);
        userGuideItem.addActionListener(this::showUserGuide);
        helpMenu.add(userGuideItem);

        JMenuItem shortcutsItem = new JMenuItem("Keyboard Shortcuts");
        shortcutsItem.setFont(menuFont);
        shortcutsItem.setAccelerator(KeyStroke.getKeyStroke('?', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        shortcutsItem.addActionListener(this::showShortcutsDialog);
        helpMenu.add(shortcutsItem);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setFont(menuFont);
        aboutItem.addActionListener(this::showAboutDialog);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(projectMenu);
        menuBar.add(themeMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void showShortcutsDialog(ActionEvent e) {
        String shortcuts = "Keyboard Shortcuts:\n" +
            "Ctrl+B: Backup Database\n" +
            "Ctrl+R: Restore Database\n" +
            "Ctrl+Q: Exit Application\n" +
            "Ctrl+F: Search Projects\n" +
            "Ctrl+?: Show Keyboard Shortcuts";

        JOptionPane.showMessageDialog(
            this,
            shortcuts,
            "Keyboard Shortcuts",
            JOptionPane.INFORMATION_MESSAGE
        );
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
    }

    /**
     * Opens the SearchDialog for searching projects.
     */
    private void openSearchDialog(ActionEvent e) {
        SearchDialog searchDialog = new SearchDialog(this);
        searchDialog.setVisible(true);
    }

    /**
     * Schedules automatic daily database backups.
     */
    private void scheduleDailyBackups() {
        Timer backupTimer = new Timer(24 * 60 * 60 * 1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performScheduledBackup();
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
        // Load saved theme from settings
        Properties settings = new Properties();
        String theme = "Light"; // Default theme
        try (FileInputStream input = new FileInputStream("app_settings.properties")) {
            settings.load(input);
            theme = settings.getProperty("theme", "Light");
        } catch (IOException e) {
            System.err.println("Could not load settings: " + e.getMessage());
        }

        // Apply the selected theme
        try {
            if (theme.equals("Dark")) {
                FlatDarkLaf.setup();
            } else if (INTELLIJ_THEMES.containsKey(theme)) {
                UIManager.setLookAndFeel(INTELLIJ_THEMES.get(theme).getDeclaredConstructor().newInstance());
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            System.err.println("Failed to set theme: " + e.getMessage());
        }

        // Ensure GUI creation happens on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            System.out.println("3D Print Job Tracker started...");
            getInstance();
        });
    }

    private void performBackup(ActionEvent e) {
        try {
            String dbFilePath = "app_home/print_jobs.mv.db";
            DatabaseBackup.createBackup(dbFilePath);
            JOptionPane.showMessageDialog(
                this,
                "Database backup created successfully.",
                "Backup Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            ErrorHandler.showErrorToUser("Failed to create database backup.", ex.getMessage());
        }
    }

    private void performScheduledBackup() {
        try {
            String dbFilePath = "app_home/print_jobs.mv.db";
            DatabaseBackup.createBackup(dbFilePath);
            System.out.println("Scheduled database backup created successfully.");
        } catch (IOException ex) {
            ErrorHandler.logError("Failed to create scheduled database backup.", ex);
        }
    }

    private void performRestore(ActionEvent e) {
        // Placeholder for restore logic
    }

    private void exitApplication(ActionEvent e) {
        System.exit(0);
    }

    private void toggleTheme(ActionEvent e) {
        String selectedTheme = null;
        JMenu themeMenu = (JMenu) getJMenuBar().getMenu(2); // Assuming Theme menu is the third menu
        ButtonGroup themeGroup = (ButtonGroup) themeMenu.getClientProperty("themeGroup");

        if (themeGroup != null) {
            for (AbstractButton button : Collections.list(themeGroup.getElements())) {
                if (button.isSelected()) {
                    selectedTheme = button.getText();
                    break;
                }
            }
        }

        if (selectedTheme != null) {
            try {
                if (selectedTheme.equals("Dark")) {
                    FlatDarkLaf.setup();
                } else if (INTELLIJ_THEMES.containsKey(selectedTheme)) {
                    UIManager.setLookAndFeel(INTELLIJ_THEMES.get(selectedTheme).getDeclaredConstructor().newInstance());
                } else {
                    FlatLightLaf.setup();
                }
                SwingUtilities.updateComponentTreeUI(this);

                // Save the selected theme to settings
                Properties settings = new Properties();
                settings.setProperty("theme", selectedTheme);
                try (FileOutputStream output = new FileOutputStream("app_settings.properties")) {
                    settings.store(output, null);
                }
            } catch (Exception ex) {
                ErrorHandler.showErrorToUser("Failed to apply theme.", ex.getMessage());
            }
        }
    }

    private void showUserGuide(ActionEvent e) {
        String userGuide = "User Guide:\n\n" +
            "- Main Window:\n" +
            "  The main application window provides access to all features, including project management, database operations, and theme selection.\n\n" +
            "- Menu Bar:\n" +
            "  File Menu:\n" +
            "    - Backup Database: Create a backup of the current database.\n" +
            "    - Restore Database: Restore the database from a backup.\n" +
            "    - Exit: Close the application.\n" +
            "  Project Menu:\n" +
            "    - Search Projects: Open the search dialog to find projects.\n" +
            "  Theme Menu:\n" +
            "    - Select a theme to customize the application's appearance.\n\n" +
            "- Keyboard Shortcuts:\n" +
            "  Ctrl+B: Backup Database\n" +
            "  Ctrl+R: Restore Database\n" +
            "  Ctrl+Q: Exit Application\n" +
            "  Ctrl+F: Search Projects\n" +
            "  Ctrl+?: Show Keyboard Shortcuts\n";

        JOptionPane.showMessageDialog(
            this,
            userGuide,
            "User Guide",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAboutDialog(ActionEvent e) {
        String aboutText = "3D Print Job Tracker\n" +
            "Version: 2025-08-beta\n" +
            "Developed by: Michael Ryan Hunsaker, M.Ed., Ph.D.\n" +
            "hunsakerconsulting@gmail.com\n" +
            "Copyright © 2025\n" +
            "Description: A GUI application for managing 3D print jobs, including project tracking, database backups, and theme customization.";

        JOptionPane.showMessageDialog(
            this,
            aboutText,
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void ensureConsistentFontSize() {
        Font universalFont = new Font("SansSerif", Font.PLAIN, 18);
        UIManager.put("Label.font", universalFont);
        UIManager.put("Button.font", universalFont);
        UIManager.put("Menu.font", universalFont);
        UIManager.put("MenuItem.font", universalFont);
        UIManager.put("TextField.font", universalFont);
        UIManager.put("TextArea.font", universalFont);
        UIManager.put("ComboBox.font", universalFont);
        UIManager.put("List.font", universalFont);
    }

    // Call ensureConsistentFontSize at the start of the application
    static {
        new Main().ensureConsistentFontSize();
    }

    /**
     * Loads the selected project details into the main form.
     * @param name Project name
     * @param type Project type
     * @param description Project description
     * @param filePath File path of the project
     */
    public void loadProjectDetails(String name, String type, String description, String filePath) {
        if (projectForm != null) {
            projectForm.setProjectDetails(name, type, description, filePath);
        }
    }
}
