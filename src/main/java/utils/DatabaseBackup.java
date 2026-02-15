package utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 * Utilities for creating, restoring and validating simple file-based backups
 * of the H2 database used by the application.
 *
 * These methods perform straightforward file copy operations and basic
 * verification based on file existence and size. They are intended for small
 * desktop deployments and are not a replacement for enterprise backup tools.
 *
 * @since 1.0.0
 */
public class DatabaseBackup {

    /** Directory where timestamped backups are stored. */
    private static final String BACKUP_DIR = "app_home/backups/";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseBackup() {
        // utility class
    }

    /**
     * Create a timestamped copy of the database file under the backups folder.
     * A simple GUI confirmation is shown on success.
     *
     * @param dbFilePath path to the live database file
     * @throws IOException when file operations fail
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
     * Restore a backup file to the live database location by copying the file.
     *
     * @param backupFilePath path to the archived backup file
     * @param dbFilePath destination path for the live database file
     * @throws IOException when file operations fail
     */
    public static void restoreBackup(String backupFilePath, String dbFilePath) throws IOException {
        Path source = Paths.get(backupFilePath);
        Path target = Paths.get(dbFilePath);

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Basic existence check for a backup file. Returns true when file exists and
     * has non-zero length.
     *
     * @param backupFilePath path to backup file
     * @return true if file exists and is non-empty
     */
    public static boolean verifyBackup(String backupFilePath) {
        File file = new File(backupFilePath);
        return file.exists() && file.length() > 0;
    }

    /**
     * More thorough verification that attempts to copy the backup to a temporary
     * location and checks that the temporary file is usable (non-zero size).
     *
     * @param backupFilePath path to the backup file
     * @return true when the backup appears valid
     */
    public static boolean verifyBackupIntegrity(String backupFilePath) {
        Path tempDir = Paths.get(BACKUP_DIR, "temp_restore");
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            Path tempDbFile = tempDir.resolve("test_restore.mv.db");

            Files.copy(Paths.get(backupFilePath), tempDbFile, StandardCopyOption.REPLACE_EXISTING);

            if (Files.size(tempDbFile) > 0) {
                return true;
            }
        } catch (IOException e) {
            System.err.println("Backup verification failed: " + e.getMessage());
        } finally {
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
