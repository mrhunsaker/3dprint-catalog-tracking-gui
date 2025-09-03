import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import javax.swing.JOptionPane;
import utils.ErrorHandler;

/**
 * Utility class for file operations such as copying directories and opening folders.
 * Extend this class with additional file-related utilities as needed.
 */
public class FileUtils {

    /**
     * Recursively copies a directory and its contents from sourceDir to destDir.
     * If files already exist in the destination, they will be replaced.
     *
     * @param sourceDir The source directory to copy.
     * @param destDir   The destination directory.
     * @throws IOException if an I/O error occurs during copying.
     *
     * Example usage:
     * <pre>
     *     FileUtils.copyDirectory(Paths.get("srcDir"), Paths.get("destDir"));
     * </pre>
     */
    public static void copyDirectory(Path sourceDir, Path destDir) throws IOException {
        // Walk through all files and directories in sourceDir
        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                // Calculate destination path by preserving relative structure
                Path destPath = destDir.resolve(sourceDir.relativize(sourcePath));
                // Copy file or directory, replacing if exists
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                ErrorHandler.logError("Failed to copy file: " + sourcePath, e);
            }
        });
    }

    /**
     * Opens a folder in the native file explorer (Windows, macOS, Linux).
     *
     * @param path The path of the folder to open.
     * @throws IOException if an I/O error occurs or the folder cannot be opened.
     *
     * Example usage:
     * <pre>
     *     FileUtils.openFolder("C:/Users/Example/Folder");
     * </pre>
     */
    public static void openFolder(String path) throws IOException {
        // Uses Desktop API to open the folder in the system's file explorer
        Desktop.getDesktop().open(new File(path));
    }

    /**
     * Checks if there is enough disk space available before copying a directory.
     *
     * @param sourceDir The source directory to copy.
     * @param destDir   The destination directory.
     * @return True if there is enough space, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean hasEnoughDiskSpace(Path sourceDir, Path destDir) throws IOException {
        long requiredSpace = Files.walk(sourceDir)
            .filter(Files::isRegularFile)
            .mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    ErrorHandler.logError("Failed to calculate file size for: " + path, e);
                    return 0L;
                }
            })
            .sum();

        FileStore fileStore = Files.getFileStore(destDir);
        long availableSpace = fileStore.getUsableSpace();

        return availableSpace >= requiredSpace;
    }

    /**
     * Copies a directory atomically, rolling back on failure.
     *
     * @param sourceDir The source directory to copy.
     * @param destDir   The destination directory.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyDirectoryAtomically(Path sourceDir, Path destDir) throws IOException {
        if (!hasEnoughDiskSpace(sourceDir, destDir)) {
            throw new IOException("Not enough disk space to copy directory.");
        }

        try {
            Files.walk(sourceDir).forEach(sourcePath -> {
                try {
                    Path destPath = destDir.resolve(sourceDir.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(destPath);
                    } else {
                        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            // Rollback on failure
            deleteDirectory(destDir);
            throw new IOException("Failed to copy directory atomically. Rolled back changes.", e);
        }
    }

    /**
     * Deletes a directory and its contents.
     *
     * @param dir The directory to delete.
     * @throws IOException if an I/O error occurs.
     */
    public static void deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
            .sorted((path1, path2) -> path2.compareTo(path1)) // Reverse order to delete children first
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    ErrorHandler.logError("Failed to delete file: " + path, e);
                }
            });
    }

    /**
     * Verifies file permissions and integrity after copying.
     *
     * @param sourceDir The source directory.
     * @param destDir   The destination directory.
     * @return True if all files are accessible and match the source, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean verifyIntegrity(Path sourceDir, Path destDir) throws IOException {
        return Files.walk(sourceDir)
            .allMatch(sourcePath -> {
                try {
                    Path destPath = destDir.resolve(sourceDir.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        return Files.isDirectory(destPath);
                    } else {
                        return Files.isRegularFile(destPath) && Files.size(sourcePath) == Files.size(destPath);
                    }
                } catch (IOException e) {
                    ErrorHandler.logError("Integrity verification failed for: " + sourcePath, e);
                    return false;
                }
            });
    }

    /**
     * Handles file conflicts by prompting the user for action.
     *
     * @param destPath The destination path where the conflict occurred.
     * @return True if the user chooses to overwrite, false otherwise.
     */
    public static boolean handleFileConflict(Path destPath) {
        int choice = JOptionPane.showConfirmDialog(
            null,
            "The file or directory " + destPath.getFileName() + " already exists. Do you want to overwrite it?",
            "File Conflict",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return choice == JOptionPane.YES_OPTION;
    }

    /**
     * Copies a directory with conflict handling.
     *
     * @param sourceDir The source directory to copy.
     * @param destDir   The destination directory.
     * @throws IOException if an I/O error occurs.
     */
    public static void copyDirectoryWithConflictHandling(Path sourceDir, Path destDir) throws IOException {
        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                Path destPath = destDir.resolve(sourceDir.relativize(sourcePath));
                if (Files.exists(destPath)) {
                    if (!handleFileConflict(destPath)) {
                        return; // Skip copying this file or directory
                    }
                }
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(destPath);
                } else {
                    Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                ErrorHandler.logError("Failed to copy file or directory: " + sourcePath, e);
                throw new RuntimeException(e);
            }
        });
    }
    // Future file utility methods can be added here
}
