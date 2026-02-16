package utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the {@link JsonImporter} utility. */
public class JsonImporterTest {

    public JsonImporterTest() {}

    @Test
    public void testImportProjectsFromJsonFile() throws IOException {
        JSONArray arr = new JSONArray();
        JSONObject p1 = new JSONObject();
        p1.put("name", "Proj1");
        arr.put(p1);

        Path temp = Files.createTempFile("projects", ".json");
        try (FileWriter w = new FileWriter(temp.toFile())) {
            w.write(arr.toString());
        }

        List<?> projects = JsonImporter.importProjectsFromJsonFile(temp.toString());
        assertNotNull(projects);
        assertEquals(1, projects.size());

        Files.deleteIfExists(temp);
    }

    @Test
    public void testImportEmptyFileReturnsEmptyList() throws IOException {
        Path temp = Files.createTempFile("projects-empty", ".json");
        try (FileWriter w = new FileWriter(temp.toFile())) {
            w.write("[]");
        }

        List<?> projects = JsonImporter.importProjectsFromJsonFile(temp.toString());
        assertNotNull(projects);
        assertEquals(0, projects.size());

        Files.deleteIfExists(temp);
    }
}
