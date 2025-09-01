import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.*;
import javax.swing.LookAndFeel;
import java.util.Map;
import java.util.TreeMap;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;

public class Main extends JFrame {
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

    public Main() {
        setTitle("3D Print Job Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open maximized
        setLocationRelativeTo(null); // Center the window

        // Initialize the database
        initializeDatabase();

        // Create menu bar
        createMenuBar();

        // Create main content
        createMainContent();

        setVisible(true);
    }

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
            System.err.println(
                "Failed to initialize database: " + e.getMessage()
            );
            JOptionPane.showMessageDialog(
                this,
                "Failed to initialize database: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        Font menuFont = new Font("SansSerif", Font.PLAIN, 18);

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(menuFont);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(menuFont);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Project menu
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setFont(menuFont);
        JMenuItem searchItem = new JMenuItem("Search Projects");
        searchItem.setFont(menuFont);
        searchItem.addActionListener(e -> openSearchDialog());
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
            themeItem.addActionListener(e -> {
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

    private void openSearchDialog() {
        SearchDialog searchDialog = new SearchDialog(this);
        searchDialog.setVisible(true);
    }

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
