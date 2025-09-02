import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
/**
 * Panel for entering and submitting new 3D print project data.
 * Includes fields for name, type, tags, notes, dates, and folder selection.
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
        projectNameField = new JTextField(20);
        projectNameField.setFont(font18);
        add(projectNameField, gbc);
        row++;

        // Project Type
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel typeLabel = new JLabel("Project Type:");
        typeLabel.setFont(font18);
        add(typeLabel, gbc);
        gbc.gridx = 1;
        String[] projectTypes = {"Model", "Prototype", "Final Print"};
        projectTypeComboBox = new JComboBox<>(projectTypes);
        projectTypeComboBox.setFont(font18);
        add(projectTypeComboBox, gbc);
        row++;

        // Project ECC/Tags
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel tagsLabel = new JLabel("Project Tags:");
        tagsLabel.setFont(font18);
        add(tagsLabel, gbc);
        gbc.gridx = 1;
        String[] projectTags = {"ECC", "O&M", "Math", "Biology", "Chemistry"};
        projectECCComboBox = new JComboBox<>(projectTags);
        projectECCComboBox.setFont(font18);
        add(projectECCComboBox, gbc);
        row++;

        // Project Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel notesLabel = new JLabel("Project Notes:");
        notesLabel.setFont(font18);
        add(notesLabel, gbc);
        gbc.gridx = 1;
        projectNotesArea = new JTextArea(5, 20);
        projectNotesArea.setFont(font18);
        add(new JScrollPane(projectNotesArea), gbc);
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
        addDateButton = new JButton("Add Date");
        addDateButton.setFont(font18);
        add(addDateButton, gbc);
        row++;

        // List of Dates
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dateListModel = new DefaultListModel<>();
        lastPrintedDatesList = new JList<>(dateListModel);
        lastPrintedDatesList.setFont(font18);
        lastPrintedDatesList.setPreferredSize(new Dimension(200, 50));
        add(new JScrollPane(lastPrintedDatesList), gbc);
        gbc.gridwidth = 1;
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
        addProjectButton.addActionListener(this::addNewProject); // Submit project
        searchProjectsButton.addActionListener(e -> {
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
            dateListModel.addElement(date);
        }
    }

    /**
     * Validates all fields and submits the new project to the database.
     * Copies the project folder, inserts project data, and adds print dates.
     * Shows error dialogs if any step fails.
     * @param e Action event from add project button
     */
    private void addNewProject(ActionEvent e) {
        String projectName = projectNameField.getText();
        String projectType = (String) projectTypeComboBox.getSelectedItem();
        String projectTag = (String) projectECCComboBox.getSelectedItem();
        String projectNotes = projectNotesArea.getText();
        String filePath = (selectedFolder != null) ? selectedFolder.getAbsolutePath() : "";

        // Validate all fields are mandatory
        if (projectName.trim().isEmpty() || projectType == null || projectTag == null || projectNotes.trim().isEmpty() || filePath.isEmpty() || dateListModel.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "All fields are required, including at least one date.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            // 1. Copy the project folder to the app home directory
            Path sourcePath = Paths.get(filePath);
            Path destPath = Paths.get("app_home", "projects", projectName);
            FileUtils.copyDirectory(sourcePath, destPath);

            // 2. Insert project data into the database
            int projectId = Database.insertProject(
                projectName,
                projectType,
                destPath.toAbsolutePath().toString(),
                projectNotes // Now using notes from the form
            );

            // 3. Insert last printed dates
            List<String> dates = new ArrayList<>();
            for (int i = 0; i < dateListModel.size(); i++) {
                dates.add(dateListModel.get(i));
            }
            Database.addLastPrintedDates(projectId, dates);

            JOptionPane.showMessageDialog(
                this,
                "Project added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

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