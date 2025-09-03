package utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 * Utility class for database backup and recovery.
 */
public class DatabaseBackup {

    private static final String BACKUP_DIR = "app_home/backups/";

    /**
     * Creates a backup of the database.
     *
     * @param dbFilePath Path to the database file.
     * @throws IOException If an I/O error occurs.
     */
    public static void createBackup(String dbFilePath) throws IOException {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path source = Paths.get(dbFilePath);
        Path target = Paths.get(BACKUP_DIR, "backup_" + timestamp + ".mv.db");

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        JOptionPane.showMessageDialog(null, "Backup created successfully at: " + target.toString(), "Backup Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Restores the database from a backup file.
     *
     * @param backupFilePath Path to the backup file.
     * @param dbFilePath Path to the database file to restore.
     * @throws IOException If an I/O error occurs.
     */
    public static void restoreBackup(String backupFilePath, String dbFilePath) throws IOException {
        Path source = Paths.get(backupFilePath);
        Path target = Paths.get(dbFilePath);

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Verifies the integrity of a backup file.
     *
     * @param backupFilePath Path to the backup file.
     * @return True if the file is valid, false otherwise.
     */
    public static boolean verifyBackup(String backupFilePath) {
        File file = new File(backupFilePath);
        return file.exists() && file.length() > 0;
    }

    /**
     * Verifies the integrity of a backup file by attempting to restore it to a temporary location.
     * @param backupFilePath Path to the backup file.
     * @return True if the backup is valid, false otherwise.
     */
    public static boolean verifyBackupIntegrity(String backupFilePath) {
        Path tempDir = Paths.get(BACKUP_DIR, "temp_restore");
        try {
            // Create a temporary directory for testing the restore
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            Path tempDbFile = tempDir.resolve("test_restore.mv.db");

            // Attempt to restore the backup to the temporary file
            Files.copy(Paths.get(backupFilePath), tempDbFile, StandardCopyOption.REPLACE_EXISTING);

            // Check if the restored file is valid (basic check: file size > 0)
            if (Files.size(tempDbFile) > 0) {
                return true;
            }
        } catch (IOException e) {
            System.err.println("Backup verification failed: " + e.getMessage());
        } finally {
            // Clean up the temporary directory
            try {
                Files.deleteIfExists(tempDir.resolve("test_restore.mv.db"));
                Files.deleteIfExists(tempDir);
            } catch (IOException cleanupException) {
                System.err.println("Failed to clean up temporary restore directory: " + cleanupException.getMessage());
            }
        }
        return false;
    }
}
