import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileUtils {
    public static void copyFile(String source, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Path destPath = Paths.get(destDir, new File(source).getName());
        Files.copy(Paths.get(source), destPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
