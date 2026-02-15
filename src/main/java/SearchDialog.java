import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utils.ErrorHandler;

/**
 * Modal dialog used to search and browse existing projects stored in the
 * application's database.
 * <p>
 * The dialog provides a text search field that matches project name and
 * description, displays results in a read-only table and allows opening the
 * associated project folder or loading project details into the main form.
 * </p>
 *
 * @since 1.0.0
 */
public class SearchDialog extends JDialog {
    // UI components
    /** Search input field. */
    private JTextField searchField;

    /** Button to trigger a search. */
    private JButton searchButton;

    /** Button to close the dialog. */
    private JButton closeButton;

    /** Button to load the selected project into the main form. */
    private JButton loadProjectButton;

    /** Button to open the selected project's folder in the system file explorer. */
    private JButton openFolderButton;

    /** Table displaying search results. */
    private JTable resultsTable;

    /** Table model backing the results table. */
    private DefaultTableModel tableModel;

    /**
     * Constructs the search dialog and initializes all UI components.
     * Loads all projects initially.
     * @param parent Parent JFrame for modal dialog
     */
    public SearchDialog(JFrame parent) {
        super(parent, "Search Projects", true);
        initializeComponents();
        layoutComponents();
        addEventHandlers();
        loadAllProjects(); // Load all projects initially

        setSize(800, 500);
        setLocationRelativeTo(parent);
    }

    /**
     * Initializes all UI components and configures the results table.
     */
    private void initializeComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        closeButton = new JButton("Close");
        loadProjectButton = new JButton("Load Project");
        openFolderButton = new JButton("Open Project Folder");

        // Create table model with columns including file_path
        String[] columnNames = { "ID", "Name", "Type", "Description", "Created Date", "File Path" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(200);
    }

    /**
     * Lays out all UI components in the dialog using BorderLayout.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // Results table
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loadProjectButton);
        buttonPanel.add(openFolderButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds event handlers for search, enter key, close button, and double-click on table.
     */
    private void addEventHandlers() {
        searchButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performSearch();
                }
            }
        );

        // Allow search by pressing Enter in the search field
        searchField.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performSearch();
                }
            }
        );

        closeButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            }
        );

        loadProjectButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadSelectedProject();
                }
            }
        );

        openFolderButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openSelectedProjectFolder();
                }
            }
        );

        // Add double-click handler to open project folder
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedProjectFolder();
                }
            }
        });
    }

    /**
     * Opens the selected project's folder in the system file explorer.
     * Shows error dialog if opening fails or no row is selected.
     */
    private void openSelectedProjectFolder() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a project to open its folder.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get the file path from the selected row (column index 5)
        String filePath = (String) tableModel.getValueAt(selectedRow, 5);
        
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No file path found for this project.",
                "Path Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            FileUtils.openFolder(filePath);
        } catch (IOException ex) {
            ErrorHandler.showErrorToUser(
                "Failed to open folder. Please check the file path and try again.",
                ex.getMessage()
            );
        }
    }

    /**
     * Loads all projects from the database and displays them in the results table.
     * Shows error dialog if loading fails, with retry mechanism for transient errors.
     */
    private void loadAllProjects() {
        int retryCount = 0;
        while (retryCount < 3) {
            try (Connection conn = Database.connect()) {
                String sql =
                    "SELECT id, name, project_type, description, created_date, file_path FROM projects ORDER BY created_date DESC";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();

                // Clear existing data
                tableModel.setRowCount(0);

                // Add rows to table
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("project_type"),
                        rs.getString("description"),
                        rs.getTimestamp("created_date"),
                        rs.getString("file_path")
                    };
                    tableModel.addRow(row);
                }
                return; // Exit method if successful
            } catch (SQLException e) {
                retryCount++;
                ErrorHandler.logError("Failed to load projects. Attempt " + retryCount, e);
                if (retryCount == 3) {
                    ErrorHandler.showErrorToUser(
                        "Failed to load projects after multiple attempts. Please check your database connection.",
                        e.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Performs a search for projects by name or description using the search field value.
     * Updates the results table with matching projects.
     * Shows info dialog if no results found.
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadAllProjects();
            return;
        }

        try (Connection conn = Database.connect()) {
            String sql =
                "SELECT id, name, project_type, description, created_date, file_path FROM projects " +
                "WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ? " +
                "ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            // Clear existing data
            tableModel.setRowCount(0);

            // Add matching rows to table
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("project_type"),
                    rs.getString("description"),
                    rs.getTimestamp("created_date"),
                    rs.getString("file_path")
                };
                tableModel.addRow(row);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No projects found matching your search criteria.",
                    "No Results",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            ErrorHandler.logError("Search failed", e);
            ErrorHandler.showErrorToUser(
                "Search failed due to a database error. Please try again later.",
                e.getMessage()
            );
        }
    }

    /**
     * Loads the details of the selected project into the main form.
     * The selected row's project values (name, type, description and file path)
     * are read from the table model and passed to the main frame.
     */
    private void loadSelectedProject() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a project to load.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get project details from the selected row
        String projectName = (String) tableModel.getValueAt(selectedRow, 1);
        String projectType = (String) tableModel.getValueAt(selectedRow, 2);
        String description = (String) tableModel.getValueAt(selectedRow, 3);
        String filePath = (String) tableModel.getValueAt(selectedRow, 5);

        // Load project details into the main form
        if (getParent() instanceof Main) {
            Main mainFrame = (Main) getParent();
            mainFrame.loadProjectDetails(projectName, projectType, description, filePath);
        }

        dispose();
    }
}