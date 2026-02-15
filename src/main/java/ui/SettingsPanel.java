package ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.awt.event.ActionEvent;

/**
 * Simple settings UI panel that allows editing small application preferences
 * persisted to `app_settings.properties` in the application working directory.
 *
 * The panel currently supports the database path and a theme selection.
 */
public class SettingsPanel extends JPanel {

    /** Input field for the database path. */
    private JTextField databasePathField;

    /** Selector for choosing the application theme. */
    private JComboBox<String> themeSelector;

    /** In-memory settings loaded from `app_settings.properties`. */
    private Properties settings;

    /** Filename where application settings are persisted. */
    private static final String SETTINGS_FILE = "app_settings.properties";

    /**
     * Create the settings panel and load persisted values.
     */
    public SettingsPanel() {
        setLayout(new BorderLayout());
        settings = new Properties();
        loadSettings();

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        // Database Path
        formPanel.add(new JLabel("Database Path:"));
        databasePathField = new JTextField(settings.getProperty("databasePath", ""));
        formPanel.add(databasePathField);

        // Theme Selector
        formPanel.add(new JLabel("Theme:"));
        themeSelector = new JComboBox<>(new String[]{"Light", "Dark"});
        themeSelector.setSelectedItem(settings.getProperty("theme", "Light"));
        formPanel.add(themeSelector);

        add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(this::saveSettings);
        add(saveButton, BorderLayout.SOUTH);
    }

    /**
     * Load settings from the properties file. Missing file is treated as defaults.
     */
    private void loadSettings() {
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            settings.load(input);
        } catch (IOException e) {
            System.err.println("Could not load settings: " + e.getMessage());
        }
    }

    /**
     * Persist the current values to the properties file.
     *
     * @param event action event from the Save button
     */
    private void saveSettings(ActionEvent event) {
        settings.setProperty("databasePath", databasePathField.getText());
        settings.setProperty("theme", (String) themeSelector.getSelectedItem());
        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            settings.store(output, null);
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
