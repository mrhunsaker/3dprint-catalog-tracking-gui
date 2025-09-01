import java.sql.Timestamp;
import java.util.List;

public class Project {
    private int id;
    private String name;
    private String projectType;
    private String filePath;
    private String description;
    private Timestamp createdDate;
    private List<Timestamp> lastPrintedDates;

    public Project(int id, String name, String projectType, String filePath, String description, Timestamp createdDate) {
        this.id = id;
        this.name = name;
        this.projectType = projectType;
        this.filePath = filePath;
        this.description = description;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setLastPrintedDates(List<Timestamp> lastPrintedDates) {
        this.lastPrintedDates = lastPrintedDates;
    }

    public List<Timestamp> getLastPrintedDates() {
        return lastPrintedDates;
    }
}