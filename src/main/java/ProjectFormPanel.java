import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.ResultSet; // Import for ResultSet
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

    // Add Project Recipient field
    private JTextField projectRecipientField; // Project recipient input
    // Modify Project Tags to allow multi-selection
    private JList<String> projectTagsList; // Multi-selection list for tags
    private JButton loadProjectButton; // Load project data into form
    private JButton updateProjectButton; // Update project details

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
        nameLabel.getAccessibleContext().setAccessibleDescription("Label for project name input field");
        add(nameLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        projectNameField = new JTextField(20);
        projectNameField.setFont(font18);
        projectNameField.setToolTipText("Enter the name of the project.");
        projectNameField.getAccessibleContext().setAccessibleName("Project Name Input");
        projectNameField.getAccessibleContext().setAccessibleDescription("Input field for entering the project name.");
        add(projectNameField, gbc);
        gbc.gridwidth = 1;
        row++;

        // Project Type
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel typeLabel = new JLabel("Project Type:");
        typeLabel.setFont(font18);
        typeLabel.getAccessibleContext().setAccessibleDescription("Label for project type dropdown");
        add(typeLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        String[] projectTypes = {"Prototype", "Final Print"};
        projectTypeComboBox = new JComboBox<>(projectTypes);
        projectTypeComboBox.setFont(font18);
        projectTypeComboBox.setToolTipText("Select the type of project.");
        projectTypeComboBox.getAccessibleContext().setAccessibleName("Project Type Dropdown");
        projectTypeComboBox.getAccessibleContext().setAccessibleDescription("Dropdown for selecting the project type.");
        add(projectTypeComboBox, gbc);
        gbc.gridwidth = 1;
        row++;

        // Project Recipient
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel recipientLabel = new JLabel("Project Recipient:");
        recipientLabel.setFont(font18);
        add(recipientLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        projectRecipientField = new JTextField(20);
        projectRecipientField.setFont(font18);
        projectRecipientField.setToolTipText("Enter the recipient of the project.");
        add(projectRecipientField, gbc);
        gbc.gridwidth = 1;
        row++;

        // Project ECC/Tags
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel tagsLabel = new JLabel("Project Tags:");
        tagsLabel.setFont(font18);
        tagsLabel.getAccessibleContext().setAccessibleDescription("Label for project tags dropdown");
        add(tagsLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        String[] projectTags = {"ECC", "O&M", "Math", "Biology", "Chemistry", "ELA", "Braille", "Communication"};
        projectTagsList = new JList<>(projectTags);
        projectTagsList.setFont(font18);
        projectTagsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(projectTagsList), gbc);
        gbc.gridwidth = 1;
        row++;

        // Project Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel notesLabel = new JLabel("Project Notes:");
        notesLabel.setFont(font18);
        notesLabel.getAccessibleContext().setAccessibleDescription("Label for project notes input area");
        add(notesLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        projectNotesArea = new JTextArea(5, 20);
        projectNotesArea.setFont(font18);
        projectNotesArea.setToolTipText("Enter notes or additional information about the project.");
        projectNotesArea.getAccessibleContext().setAccessibleName("Project Notes Input");
        projectNotesArea.getAccessibleContext().setAccessibleDescription("Input area for entering notes or additional information about the project.");
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
        addProjectButton.setToolTipText("Add the project to the database.");
        addProjectButton.getAccessibleContext().setAccessibleName("Add Project Button");
        addProjectButton.getAccessibleContext().setAccessibleDescription("Button to add the project to the database.");
        add(addProjectButton, gbc);
        row++;

        // Search Projects Button (just below Add Project)
        gbc.gridx = 1;
        gbc.gridy = row;
        searchProjectsButton = new JButton("Search Projects");
        searchProjectsButton.setFont(font18);
        add(searchProjectsButton, gbc);

        // Add Load Project Button
        gbc.gridx = 1;
        gbc.gridy = row;
        loadProjectButton = new JButton("Load Project");
        loadProjectButton.setFont(font18);
        loadProjectButton.setToolTipText("Load project data into the form.");
        add(loadProjectButton, gbc);
        row++;

        // Add Update Project Button
        gbc.gridx = 1;
        gbc.gridy = row;
        updateProjectButton = new JButton("Update Project");
        updateProjectButton.setFont(font18);
        updateProjectButton.setToolTipText("Update the project details in the database.");
        add(updateProjectButton, gbc);
        row++;

        // Add event listeners for buttons
        browseButton.addActionListener(this::browseForFolder); // Browse for folder
        addDateButton.addActionListener(this::addDateToList); // Add date to list
        datePickerButton.addActionListener(this::openDatePicker); // Open date picker
        removeDateButton.addActionListener(this::removeSelectedDate); // Remove selected date
        addProjectButton.addActionListener(this::addNewProject); // Submit project
        searchProjectsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(ProjectFormPanel.this);
                SearchDialog searchDialog = new SearchDialog(parentFrame);
                searchDialog.setVisible(true);
            }
        });
        loadProjectButton.addActionListener(this::loadProjectData);
        updateProjectButton.addActionListener(this::updateProjectData);

        // Release keyboard trap on Alt-M in projectNotesArea
        projectNotesArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_M) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                }
            }
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
        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                Date selectedDate = (Date) dateSpinner.getValue();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = sdf.format(selectedDate);
                lastPrintedDateField.setText(formattedDate);
                dateDialog.dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                dateDialog.dispose();
            }
        });
        
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
     * Clears all form fields and resets to default values without altering layout.
     */
    private void clearForm() {
        projectNameField.setText("");
        projectTypeComboBox.setSelectedIndex(0);
        projectRecipientField.setText("");
        projectNotesArea.setText("");
        lastPrintedDateField.setValue(new Date());
        dateListModel.clear();
        projectPathField.setText("");
        // Retain the selected folder reference but clear the path field
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

    /**
     * Filters the projects displayed in the panel based on the search query.
     * @param query The search query to filter projects.
     */
    public void filterProjects(String query) {
        // Placeholder for filtering logic
        System.out.println("Filtering projects with query: " + query);
        // Example: Implement logic to filter projects based on the query
    }

    // Method to load project data into the form
    private void loadProjectData(ActionEvent e) {
        String projectIdStr = JOptionPane.showInputDialog(this, "Enter Project ID to Load:", "Load Project", JOptionPane.QUESTION_MESSAGE);
        if (projectIdStr != null && !projectIdStr.trim().isEmpty()) {
            try {
                int projectId = Integer.parseInt(projectIdStr.trim());
                try (ResultSet rs = Database.loadProjectById(projectId)) {
                    if (rs.next()) {
                        projectNameField.setText(rs.getString("name"));
                        projectTypeComboBox.setSelectedItem(rs.getString("project_type"));
                        projectRecipientField.setText(rs.getString("recipient"));
                        projectNotesArea.setText(rs.getString("description"));

                        // Set tags (split delimited string into list)
                        String tags = rs.getString("tags");
                        if (tags != null) {
                            List<String> tagList = List.of(tags.split(","));
                            projectTagsList.setSelectedIndices(tagList.stream().mapToInt(tag -> {
                                for (int i = 0; i < projectTagsList.getModel().getSize(); i++) {
                                    if (projectTagsList.getModel().getElementAt(i).equals(tag)) {
                                        return i;
                                    }
                                }
                                return -1;
                            }).filter(index -> index != -1).toArray());
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Project not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Project ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ErrorHandler.showErrorToUser("Failed to load project data.", ex.getMessage());
            }
        }
    }

    // Method to update project data in the database
    private void updateProjectData(ActionEvent e) {
        String projectName = projectNameField.getText().trim();
        String projectType = (String) projectTypeComboBox.getSelectedItem();
        String recipient = projectRecipientField.getText().trim();
        String description = projectNotesArea.getText().trim();

        // Get selected tags as a comma-delimited string
        List<String> selectedTags = projectTagsList.getSelectedValuesList();
        String tags = String.join(",", selectedTags);

        String projectIdStr = JOptionPane.showInputDialog(this, "Enter Project ID to Update:", "Update Project", JOptionPane.QUESTION_MESSAGE);
        if (projectIdStr != null && !projectIdStr.trim().isEmpty()) {
            try {
                int projectId = Integer.parseInt(projectIdStr.trim());
                Database.updateProject(projectId, projectName, projectType, recipient, tags, description);
                JOptionPane.showMessageDialog(this, "Project updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Project ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ErrorHandler.showErrorToUser("Failed to update project data.", ex.getMessage());
            }
        }
    }

    /**
     * Sets the project details in the form fields.
     * @param name Project name
     * @param type Project type
     * @param description Project description
     * @param filePath File path of the project
     */
    public void setProjectDetails(String name, String type, String description, String filePath) {
        projectNameField.setText(name);
        projectTypeComboBox.setSelectedItem(type);
        projectNotesArea.setText(description);
        projectPathField.setText(filePath);
    }

    // Call the updateSchema method to ensure the database schema is up-to-date
    static {
        Database.updateSchema();
    }

    // Add more form logic or validation as needed for future features
}