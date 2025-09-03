package reports;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for generating various reports based on project data.
 */
public class ReportGenerator {

    /**
     * Generates a project frequency report (most/least printed projects).
     *
     * @param projects A list of maps, each representing a project.
     * @return A JSON object containing the frequency report.
     */
    public static JSONObject generateProjectFrequencyReport(List<Map<String, Object>> projects) {
        Map<String, Long> frequencyMap = projects.stream()
            .collect(Collectors.groupingBy(
                project -> (String) project.get("name"),
                Collectors.counting()
            ));

        JSONObject report = new JSONObject();
        Map.Entry<String, Long> mostPrinted = frequencyMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElseThrow(() -> new IllegalStateException("No projects available"));
        Map.Entry<String, Long> leastPrinted = frequencyMap.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .orElseThrow(() -> new IllegalStateException("No projects available"));

        report.put("most_printed", new JSONObject()
            .put("key", mostPrinted.getKey())
            .put("value", mostPrinted.getValue().intValue()));
        report.put("least_printed", new JSONObject()
            .put("key", leastPrinted.getKey())
            .put("value", leastPrinted.getValue().intValue()));

        return report;
    }

    /**
     * Exports a report to a JSON file.
     *
     * @param report The JSON object representing the report.
     * @param filePath The file path to save the JSON file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void exportReportToJson(JSONObject report, String filePath) throws IOException {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(report.toString(4)); // Pretty-printed JSON
        }
    }

    /**
     * Exports a report to a CSV file.
     *
     * @param report The JSON object representing the report.
     * @param filePath The file path to save the CSV file.
     * @throws IOException If an error occurs during file writing.
     */
    public static void exportReportToCsv(JSONObject report, String filePath) throws IOException {
        StringBuilder csvContent = new StringBuilder();
        report.keySet().forEach(key -> {
            csvContent.append(key).append(",").append(report.get(key)).append("\n");
        });

        try (FileWriter file = new FileWriter(filePath)) {
            file.write(csvContent.toString());
        }
    }
}
