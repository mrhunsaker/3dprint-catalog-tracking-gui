import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import utils.ErrorHandler;

/**
 * Panel for entering and submitting new 3D print project data.
 * Includes fields for name, type, tags, notes, dates, and folder selection.
 * Features date picker and form clearing after successful submission.
 * Extend this class to add more fields or validation logic.
 */
public class ProjectFormPanel extends JPanel {

    // Form fields and controls
    private JTextField projectNameField; // Project name input
    private JComboBox<String> projectTypeComboBox; // Project type dropdown
    private JComboBox<String> projectECCComboBox; // Project tags dropdown
    private JFormattedTextField lastPrintedDateField; // Date input
    private JTextArea projectNotesArea; // Notes input
    private JButton addDateButton; // Add date to list
    private JButton datePickerButton; // Open date picker
    private JButton removeDateButton; // Remove selected date from list
    private JTextField projectPathField; // Selected folder path
    private JButton browseButton; // Browse for folder
    private JButton addProjectButton; // Submit project
    private JButton searchProjectsButton; // Open search dialog
    private File selectedFolder; // Chosen folder
    private DefaultListModel<String> dateListModel; // List model for dates
    private JList<String> lastPrintedDatesList; // List UI for dates

    /**
     * Constructs the project form panel and lays out all fields and buttons.
     * All fields are mandatory for submission.
     */
    public ProjectFormPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font font18 = new Font("SansSerif", Font.PLAIN, 18);

        int row = 0;
        // Project Name
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel nameLabel = new JLabel("Project Name:");
        nameLabel.setFont(font18);
        add(nameLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        projectNameField = new JTextField(20);
        projectNameField.setFont(font18);
        add(projectNameField, gbc);
        gbc.gridwidth = 1;
        row++;

        // Project Type
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel typeLabel = new JLabel("Project Type:");
        typeLabel.setFont(font18);
        add(typeLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        String[] projectTypes = {"Model", "Prototype", "Final Print"};
        projectTypeComboBox = new JComboBox<>(projectTypes);
        projectTypeComboBox.setFont(font18);
        add(projectTypeComboBox, gbc);
        gbc.gridwidth = 1;
        row++;

        // Project ECC/Tags
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel tagsLabel = new JLabel("Project Tags:");
        tagsLabel.setFont(font18);
        add(tagsLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        String[] projectTags = {"ECC", "O&M", "Math", "Biology", "Chemistry"};
        projectECCComboBox = new JComboBox<>(projectTags);
        projectECCComboBox.setFont(font18);
        add(projectECCComboBox, gbc);
        gbc.gridwidth = 1;
        row++;

        // Project Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel notesLabel = new JLabel("Project Notes:");
        notesLabel.setFont(font18);
        add(notesLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        projectNotesArea = new JTextArea(5, 20);
        projectNotesArea.setFont(font18);
        add(new JScrollPane(projectNotesArea), gbc);
        gbc.gridwidth = 1;
        row++;

        // Last Printed Date
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel dateLabel = new JLabel("Last Printed Date:");
        dateLabel.setFont(font18);
        add(dateLabel, gbc);
        gbc.gridx = 1;
        lastPrintedDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        lastPrintedDateField.setValue(new Date());
        lastPrintedDateField.setFont(font18);
        add(lastPrintedDateField, gbc);
        gbc.gridx = 2;
        
        // Panel for date buttons
        JPanel dateButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        addDateButton = new JButton("Add Date");
        addDateButton.setFont(font18);
        datePickerButton = new JButton("📅");
        datePickerButton.setFont(font18);
        datePickerButton.setToolTipText("Open Date Picker");
        dateButtonPanel.add(addDateButton);
        dateButtonPanel.add(datePickerButton);
        add(dateButtonPanel, gbc);
        row++;

        // List of Dates
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dateListModel = new DefaultListModel<>();
        lastPrintedDatesList = new JList<>(dateListModel);
        lastPrintedDatesList.setFont(font18);
        lastPrintedDatesList.setPreferredSize(new Dimension(200, 80));
        JScrollPane dateScrollPane = new JScrollPane(lastPrintedDatesList);
        add(dateScrollPane, gbc);
        gbc.gridwidth = 1;
        row++;

        // Remove Date Button
        gbc.gridx = 1;
        gbc.gridy = row;
        removeDateButton = new JButton("Remove Selected Date");
        removeDateButton.setFont(font18);
        add(removeDateButton, gbc);
        row++;

        // Project Folder Path
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel folderLabel = new JLabel("Project Folder:");
        folderLabel.setFont(font18);
        add(folderLabel, gbc);
        gbc.gridx = 1;
        projectPathField = new JTextField(20);
        projectPathField.setEditable(false);
        projectPathField.setFont(font18);
        add(projectPathField, gbc);
        gbc.gridx = 2;
        browseButton = new JButton("Browse");
        browseButton.setFont(font18);
        add(browseButton, gbc);
        row++;

        // Add Project Button
        gbc.gridx = 1;
        gbc.gridy = row;
        addProjectButton = new JButton("Add Project");
        addProjectButton.setFont(font18);
        add(addProjectButton, gbc);
        row++;

        // Search Projects Button (just below Add Project)
        gbc.gridx = 1;
        gbc.gridy = row;
        searchProjectsButton = new JButton("Search Projects");
        searchProjectsButton.setFont(font18);
        add(searchProjectsButton, gbc);

        // Add event listeners for buttons
        browseButton.addActionListener(this::browseForFolder); // Browse for folder
        addDateButton.addActionListener(this::addDateToList); // Add date to list
        datePickerButton.addActionListener(this::openDatePicker); // Open date picker
        removeDateButton.addActionListener(this::removeSelectedDate); // Remove selected date
        addProjectButton.addActionListener(this::addNewProject); // Submit project
        searchProjectsButton.addActionListener(e -> { // `e` is unused but required by ActionListener
            // Open the SearchDialog for searching projects
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            SearchDialog dialog = new SearchDialog(parentFrame);
            dialog.setVisible(true);
        });
    }

    /**
     * Opens a folder chooser dialog for selecting the project folder.
     * Updates the projectPathField with the selected folder path.
     * @param e Action event from browse button
     */
    private void browseForFolder(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFolder = fileChooser.getSelectedFile();
            projectPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    /**
     * Adds the entered date to the list of last printed dates.
     * @param e Action event from add date button
     */
    private void addDateToList(ActionEvent e) {
        String date = lastPrintedDateField.getText();
        if (date != null && !date.trim().isEmpty()) {
            // Check if date is already in the list
            if (!dateListModel.contains(date)) {
                dateListModel.addElement(date);
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "This date is already in the list.",
                    "Duplicate Date",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    /**
     * Opens a date picker dialog for selecting a date.
     * @param e Action event from date picker button
     */
    private void openDatePicker(ActionEvent e) {
        // Create a simple date picker using JSpinner
        JDialog dateDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setLayout(new BorderLayout());
        
        // Create date spinner
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("SansSerif", Font.PLAIN, 16));
        
        // Set current value to today
        dateSpinner.setValue(new Date());
        
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(new JLabel("Select Date:"));
        centerPanel.add(dateSpinner);
        dateDialog.add(centerPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(ev -> { // `ev` is unused but required by ActionListener
            Date selectedDate = (Date) dateSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(selectedDate);
            lastPrintedDateField.setText(formattedDate);
            dateDialog.dispose();
        });
        
        cancelButton.addActionListener(ev -> dateDialog.dispose()); // `ev` is unused but required by ActionListener
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dateDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dateDialog.setSize(300, 150);
        dateDialog.setLocationRelativeTo(this);
        dateDialog.setVisible(true);
    }

    /**
     * Removes the selected date from the list of last printed dates.
     * @param e Action event from remove date button
     */
    private void removeSelectedDate(ActionEvent e) {
        int selectedIndex = lastPrintedDatesList.getSelectedIndex();
        if (selectedIndex != -1) {
            dateListModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Please select a date to remove.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Clears all form fields and resets to default values.
     */
    private void clearForm() {
        projectNameField.setText("");
        projectTypeComboBox.setSelectedIndex(0);
        projectECCComboBox.setSelectedIndex(0);
        projectNotesArea.setText("");
        lastPrintedDateField.setValue(new Date());
        dateListModel.clear();
        projectPathField.setText("");
        selectedFolder = null;
    }

    /**
     * Validates the project name for length and invalid characters.
     * @param name The project name to validate.
     * @return True if valid, false otherwise.
     */
    private boolean validateProjectName(String name) {
        if (name.length() > 255) {
            JOptionPane.showMessageDialog(this, "Project name must be 255 characters or less.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!name.matches("[a-zA-Z0-9 _-]+")) {
            JOptionPane.showMessageDialog(this, "Project name contains invalid characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Validates the date format and ensures it is not in the future.
     * @param date The date string to validate.
     * @return True if valid, false otherwise.
     */
    private boolean validateDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(date);
            if (parsedDate.after(new Date())) {
                JOptionPane.showMessageDialog(this, "Date cannot be in the future.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            ErrorHandler.showErrorToUser(
                "Invalid date format. Use yyyy-MM-dd.",
                e.getMessage()
            );
            return false;
        }
        return true;
    }

    /**
     * Validates the file path for existence and accessibility.
     * @param path The file path to validate.
     * @return True if valid, false otherwise.
     */
    private boolean validateFilePath(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Selected folder does not exist or is not a directory.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Validates the length of the project name and description.
     * @param name The project name to validate.
     * @param description The project description to validate.
     * @return True if both are valid, false otherwise.
     */
    private boolean validateTextLengths(String name, String description) {
        if (name.length() > 255) {
            JOptionPane.showMessageDialog(this, "Project name must be 255 characters or less.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (description.length() > 1000) {
            JOptionPane.showMessageDialog(this, "Project description must be 1000 characters or less.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Validates all fields and submits the new project to the database.
     * Copies the project folder, inserts project data, and adds print dates.
     * Clears the form after successful submission.
     * Shows error dialogs if any step fails.
     * @param e Action event from add project button
     */
    private void addNewProject(ActionEvent e) {
        String projectName = projectNameField.getText().trim();
        String filePath = (selectedFolder != null) ? selectedFolder.getAbsolutePath() : "";
        String date = lastPrintedDateField.getText().trim();

        // Validate inputs
        if (!validateProjectName(projectName) || !validateFilePath(filePath) || !validateDate(date)) {
            return;
        }

        // Validate text lengths
        if (!validateTextLengths(projectName, projectNotesArea.getText())) {
            return;
        }

        try {
            Path sourcePath = Paths.get(filePath);
            Path destPath = Paths.get("app_home", "projects", projectName);

            // Copy directory with conflict handling
            FileUtils.copyDirectoryWithConflictHandling(sourcePath, destPath);

            // Verify integrity after copying
            if (!FileUtils.verifyIntegrity(sourcePath, destPath)) {
                JOptionPane.showMessageDialog(
                    this,
                    "File integrity verification failed after copying.",
                    "Integrity Error",
                    JOptionPane.ERROR_MESSAGE
                );
                FileUtils.deleteDirectory(destPath);
                return;
            }

            // Insert project and dates into the database within a transaction
            Database.executeTransaction(conn -> {
                // Use the connection object to ensure the parameter is utilized
                if (conn == null) {
                    throw new SQLException("Database connection is null.");
                }

                int projectId = Database.insertProject(
                    projectName,
                    (String) projectTypeComboBox.getSelectedItem(),
                    destPath.toAbsolutePath().toString(),
                    projectNotesArea.getText()
                );

                List<String> dates = new ArrayList<>();
                for (int i = 0; i < dateListModel.size(); i++) {
                    dates.add(dateListModel.get(i));
                }
                Database.addLastPrintedDates(projectId, dates);
            });

            JOptionPane.showMessageDialog(
                this,
                "Project added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Clear the form after successful submission
            clearForm();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to copy project files: " + ex.getMessage(),
                "File Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to save project to database: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    // Add more form logic or validation as needed for future features
}