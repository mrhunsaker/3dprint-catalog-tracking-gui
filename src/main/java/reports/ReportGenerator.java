package reports;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simple reporting utilities which produce JSON or CSV summaries from the
 * in-memory project representations. These methods are stateless and useful
 * for generating artifacts that can be embedded in the project's site.
 */
public class ReportGenerator {

    /**
     * Create a frequency report indicating the most- and least-printed project
     * names from a list of project maps.
     *
     * @param projects list of project maps (each expected to have a `name` key)
     * @return JSONObject containing `most_printed` and `least_printed` entries
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
     * Persist a JSON report to disk as pretty-printed JSON.
     *
     * @param report JSON object to write
     * @param filePath destination file path
     * @throws IOException when file write fails
     */
    public static void exportReportToJson(JSONObject report, String filePath) throws IOException {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(report.toString(4)); // Pretty-printed JSON
        }
    }

    /**
     * Export a JSON report to a simple CSV text representation. Each top-level
     * key/value pair becomes a CSV row in the form `key,value`.
     *
     * @param report JSON object to convert to CSV
     * @param filePath destination CSV file
     * @throws IOException when file write fails
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

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ReportGenerator() {
        // utility class
    }
}
