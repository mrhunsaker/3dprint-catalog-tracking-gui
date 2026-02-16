import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
//import java.util.Arrays;
import java.util.stream.Stream;
import utils.DatabaseBackup;

/**
 * CLI utility to import a directory of project files into the application's
 * projects repository and persist a corresponding database record.
 * <p>
 * Usage:
 * <pre>
 *   java DatabaseImporter --import-folder /path/to/folder --recipient NAME --projectType TYPE
 * </pre>
 * On success the tool prints a single JSON line including the newly created
 * project's id and name.
 */
public class DatabaseImporter {

    /**
     * Recursively copy files from {@code src} to {@code dst}, preserving
     * directory structure. Throws {@link IOException} on failure.
     *
     * @param src source path
     * @param dst destination path
     * @throws IOException when IO operations fail
     */
    private static void copyRecursive(Path src, Path dst) throws IOException {
        if (!Files.exists(src)) throw new IOException("Source not found: " + src);
        try (Stream<Path> paths = Files.walk(src)) {
            paths.forEach(p -> {
                try {
                    Path rel = src.relativize(p);
                    Path target = dst.resolve(rel.toString());
                    if (Files.isDirectory(p)) {
                        if (!Files.exists(target)) Files.createDirectories(target);
                    } else {
                        if (!Files.exists(target.getParent())) Files.createDirectories(target.getParent());
                        Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * CLI entry point for importing a folder of project files into the application.
     *
     * @param args command-line arguments. Expected options:
     *             <ul>
     *               <li>{@code --import-folder <path>} (required)</li>
     *               <li>{@code --recipient <name>}</li>
     *               <li>{@code --projectType <type>}</li>
     *             </ul>
     */
    public static void main(String[] args) {
        String folder = null;
        String recipient = "Bulk_Import";
        String projectType = "Prototype";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--import-folder": if (i + 1 < args.length) { folder = args[++i]; } break;
                case "--recipient": if (i + 1 < args.length) { recipient = args[++i]; } break;
                case "--projectType": if (i + 1 < args.length) { projectType = args[++i]; } break;
                case "--help":
                    System.out.println("Usage: DatabaseImporter --import-folder <path> [--recipient NAME] [--projectType TYPE]");
                    return;
            }
        }
        if (folder == null) {
            System.err.println("Missing --import-folder");
            System.exit(2);
        }

        Path src = Paths.get(folder).toAbsolutePath();
        if (!Files.exists(src)) {
            System.err.println("Import folder does not exist: " + src);
            System.exit(3);
        }

        // Compute a destination under app_home/projects
        Path repoRoot = Paths.get(".").toAbsolutePath().normalize();
        Path targetBase = repoRoot.resolve("app_home").resolve("projects");
        String folderName = src.getFileName().toString();
        Path dest = targetBase.resolve(folderName);

        try {
            // Ensure a backup exists before any DB write. Failures here are non-fatal
            // for the importer since a separate backup strategy may be in place.
            try {
                DatabaseBackup.createBackup(repoRoot.resolve("app_home").resolve("print_jobs.mv.db").toString());
            } catch (Exception e) {
                // If backup helper path differs, ignore - caller may manage backups externally.
            }

            if (!Files.exists(dest)) Files.createDirectories(dest);
            copyRecursive(src, dest);

            // Insert into DB using existing Database helper
            int id = Database.insertProject(folderName, projectType, dest.toString(), "Imported via DatabaseImporter by " + recipient);
            System.out.println("{\"projectId\":" + id + ",\"name\":\"" + folderName + "\"}");
            System.exit(0);
        } catch (SQLException se) {
            System.err.println("SQL error: " + se.getMessage());
            se.printStackTrace(System.err);
            System.exit(4);
        } catch (RuntimeException re) {
            System.err.println("Runtime error: " + re.getMessage());
            re.printStackTrace(System.err);
            System.exit(5);
        } catch (IOException ioe) {
            System.err.println("IO error: " + ioe.getMessage());
            ioe.printStackTrace(System.err);
            System.exit(6);
        }
    }
    
    /**
     * Private constructor to prevent instantiation; this class is a CLI utility.
     */
    private DatabaseImporter() {
        // utility class
    }
}
