import java.sql.Timestamp;
import java.util.List;

/**
 * Represents a 3D print project with metadata and print history.
 * Extend this class to add more fields or methods as needed.
 */
public class Project {
    /** Unique project ID (primary key in database) */
    private int id;
    /** Project name/title */
    private String name;
    /** Project type (e.g., Model, Prototype, Final Print) */
    private String projectType;
    /** Absolute path to project files */
    private String filePath;
    /** Project description/notes */
    private String description;
    /** Timestamp when project was created */
    private Timestamp createdDate;
    /** List of timestamps for last printed dates */
    private List<Timestamp> lastPrintedDates;

    /**
     * Constructs a Project object with all metadata except print history.
     * @param id Project ID
     * @param name Project name
     * @param projectType Project type
     * @param filePath Absolute path to files
     * @param description Project description
     * @param createdDate Timestamp of creation
     */
    public Project(int id, String name, String projectType, String filePath, String description, Timestamp createdDate) {
        this.id = id;
        this.name = name;
        this.projectType = projectType;
        this.filePath = filePath;
        this.description = description;
        this.createdDate = createdDate;
    }

    /** @return Project ID */
    public int getId() { return id; }
    /** @return Project name */
    public String getName() { return name; }
    /** @return Project type */
    public String getProjectType() { return projectType; }
    /** @return Absolute path to project files */
    public String getFilePath() { return filePath; }
    /** @return Project description */
    public String getDescription() { return description; }
    /** @return Timestamp when project was created */
    public Timestamp getCreatedDate() { return createdDate; }

    /**
     * Sets the list of last printed dates for this project.
     * @param lastPrintedDates List of timestamps
     */
    public void setLastPrintedDates(List<Timestamp> lastPrintedDates) {
        this.lastPrintedDates = lastPrintedDates;
    }

    /**
     * Gets the list of last printed dates for this project.
     * @return List of timestamps
     */
    public List<Timestamp> getLastPrintedDates() {
        return lastPrintedDates;
    }
    // Add more fields or methods as needed for future features
}