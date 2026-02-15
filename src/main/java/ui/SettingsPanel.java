package ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.awt.event.ActionEvent;

public class SettingsPanel extends JPanel {

    private JTextField databasePathField;
    private JComboBox<String> themeSelector;
    private Properties settings;
    private static final String SETTINGS_FILE = "app_settings.properties";

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

    private void loadSettings() {
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            settings.load(input);
        } catch (IOException e) {
            System.err.println("Could not load settings: " + e.getMessage());
        }
    }

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
