package utils;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility class for exporting data to JSON format.
 */
public class JsonExporter {

    /**
     * Exports a single project to a JSON file.
     *
     * @param projectData A map containing project metadata.
     * @param filePath The file path to save the JSON file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void exportProject(Map<String, Object> projectData, String filePath) throws IOException {
        JSONObject projectJson = new JSONObject(projectData);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(projectJson.toString(4)); // Pretty-printed JSON
        }
    }

    /**
     * Exports a list of projects to a JSON file.
     *
     * @param projects A list of maps, each containing project metadata.
     * @param filePath The file path to save the JSON file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void exportAllProjects(List<Map<String, Object>> projects, String filePath) throws IOException {
        JSONArray projectsJson = new JSONArray(projects);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(projectsJson.toString(4)); // Pretty-printed JSON
        }
    }

    /**
     * Exports a list of projects to a compact JSON file.
     *
     * @param projects A list of maps, each containing project metadata.
     * @param filePath The file path to save the JSON file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void exportAllProjectsCompact(List<Map<String, Object>> projects, String filePath) throws IOException {
        JSONArray projectsJson = new JSONArray(projects);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(projectsJson.toString()); // Compact JSON
        }
    }
}
