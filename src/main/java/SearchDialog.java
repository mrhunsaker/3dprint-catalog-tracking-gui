import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SearchDialog extends JDialog {

    private JTextField searchField;
    private JButton searchButton;
    private JButton closeButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;

    public SearchDialog(JFrame parent) {
        super(parent, "Search Projects", true);
        initializeComponents();
        layoutComponents();
        addEventHandlers();
        loadAllProjects(); // Load all projects initially

        setSize(600, 400);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        closeButton = new JButton("Close");

        // Create table model with columns
        String[] columnNames = { "ID", "Name", "Description", "Created Date" };
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
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
    }

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
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

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
    }

    private void loadAllProjects() {
        try (Connection conn = Database.connect()) {
            String sql =
                "SELECT id, name, description, created_date FROM projects ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            // Clear existing data
            tableModel.setRowCount(0);

            // Add rows to table
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getTimestamp("created_date"),
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to load projects: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadAllProjects();
            return;
        }

        try (Connection conn = Database.connect()) {
            String sql =
                "SELECT id, name, description, created_date FROM projects " +
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
                    rs.getString("description"),
                    rs.getTimestamp("created_date"),
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
            JOptionPane.showMessageDialog(
                this,
                "Search failed: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
