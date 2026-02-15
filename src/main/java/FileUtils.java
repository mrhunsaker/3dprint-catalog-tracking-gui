import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import utils.ErrorHandler;

/**
 * Utility class for common file operations used by the UI and import helpers.
 * <p>
 * Methods in this class are convenience wrappers around {@link java.nio.file.Files}
 * which add simple error handling and small UI prompts when user interaction is required.
 * </p>
 *
 * @since 1.0.0
 */
public class FileUtils {

    /**
     * Recursively copy a directory tree from {@code sourceDir} to {@code destDir}.
     * Existing files at the destination will be replaced.
     *
     * @param sourceDir root path to copy from
     * @param destDir root path to copy to
     * @throws IOException when IO fails during traversal or copy
     */
    public static void copyDirectory(Path sourceDir, Path destDir) throws IOException {
        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                Path destPath = destDir.resolve(sourceDir.relativize(sourcePath));
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                ErrorHandler.logError("Failed to copy file: " + sourcePath, e);
            }
        });
    }

    /**
     * Open the operating system's file browser at the given path.
     *
     * @param path filesystem path to open
     * @throws IOException if the platform does not support Desktop operations or the path cannot be opened
     */
    public static void openFolder(String path) throws IOException {
        Desktop.getDesktop().open(new File(path));
    }

    /**
     * Estimate whether the destination has enough usable space to copy the full
     * contents of the source directory. This is a best-effort check.
     *
     * @param sourceDir path to source directory
     * @param destDir path to destination directory (used to query filesystem stats)
     * @return true when available space >= required size
     * @throws IOException on IO errors while computing sizes
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
     * Attempt to copy an entire directory atomically; if any copy fails the
     * destination tree will be deleted to avoid partial state.
     *
     * @param sourceDir source directory
     * @param destDir destination directory
     * @throws IOException when copy or rollback fails
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
     * Recursively delete a directory. Errors during deletion are logged but
     * do not stop the traversal.
     *
     * @param dir path to remove
     * @throws IOException if an IO error prevents traversal
     */
    public static void deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
            .sorted((path1, path2) -> path2.compareTo(path1)) // reverse order
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    ErrorHandler.logError("Failed to delete file: " + path, e);
                }
            });
    }

    /**
     * Verify copied files match the source by checking file presence and size.
     *
     * @param sourceDir source
     * @param destDir destination
     * @return true when every file exists in the destination and sizes match
     * @throws IOException on IO error
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
     * Prompt the user for conflict resolution when a destination path already exists.
     *
     * @param destPath conflicted destination path
     * @return true when user chooses to overwrite
     */
    public static boolean handleFileConflict(Path destPath) {
        // In headless environments (CI/tests) default to overwriting to avoid blocking.
        if (GraphicsEnvironment.isHeadless()) {
            return true;
        }

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
     * Copy directory tree but prompt for conflicts. If the user declines for a
     * specific file, that file is skipped while others continue.
     *
     * @param sourceDir source path
     * @param destDir destination path
     * @throws IOException on IO failure
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

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FileUtils() {
        // utility class
    }
}
