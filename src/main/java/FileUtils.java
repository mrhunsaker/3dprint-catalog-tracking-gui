import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

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
                // Log error, but continue copying other files
                System.err.println("Failed to copy file: " + e.getMessage());
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
    // Future file utility methods can be added here
}
