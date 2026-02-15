package utils;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Helpers to export project metadata to JSON files. Produces prettified
 * (indented) JSON by default and supports a compact output when desired.
 */
public class JsonExporter {

    /**
     * Write a single project map to disk as a pretty-printed JSON object.
     *
     * @param projectData map with keys and values representing project fields
     * @param filePath destination path for JSON file
     * @throws IOException on file write failure
     */
    public static void exportProject(Map<String, Object> projectData, String filePath) throws IOException {
        JSONObject projectJson = new JSONObject(projectData);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(projectJson.toString(4)); // Pretty-printed JSON
        }
    }

    /**
     * Export a list of projects to a pretty-printed JSON array file.
     *
     * @param projects list of project maps
     * @param filePath destination path
     * @throws IOException on write failure
     */
    public static void exportAllProjects(List<Map<String, Object>> projects, String filePath) throws IOException {
        JSONArray projectsJson = new JSONArray(projects);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(projectsJson.toString(4)); // Pretty-printed JSON
        }
    }

    /**
     * Export projects in compact (no indentation) form suitable for smaller files.
     *
     * @param projects list of project maps
     * @param filePath destination path
     * @throws IOException on write failure
     */
    public static void exportAllProjectsCompact(List<Map<String, Object>> projects, String filePath) throws IOException {
        JSONArray projectsJson = new JSONArray(projects);
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(projectsJson.toString()); // Compact JSON
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JsonExporter() {
        // utility class
    }
}
