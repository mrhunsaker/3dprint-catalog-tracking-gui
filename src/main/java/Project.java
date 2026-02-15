import java.sql.Timestamp;
import java.util.List;

/**
 * Represents a single 3D print project stored by the application.
 * <p>
 * This value object contains the metadata persisted in the application's H2 database
 * (see `projects` table). Typical usage is to construct an instance from a
 * {@link java.sql.ResultSet} retrieved from the database and then expose the values
 * to UI components or reporting utilities.
 * </p>
 *
 * @since 1.0.0
 */
public class Project {
    /** Unique project ID (primary key in database) */
    private int id;
    /** Project name/title */
    private String name;
    /** Project type (e.g., Prototype, Final Print) */
    private String projectType;
    /** Absolute path to project files on disk */
    private String filePath;
    /** Project description or notes */
    private String description;
    /** Timestamp when project was created */
    private Timestamp createdDate;
    /** List of timestamps recording last printed dates */
    private List<Timestamp> lastPrintedDates;

    /**
     * Create a new Project instance with metadata values.
     *
     * @param id the unique project identifier (database primary key)
     * @param name human-readable project name
     * @param projectType a short category string describing the project
     * @param filePath absolute filesystem path where project files reside
     * @param description free-text description or notes
     * @param createdDate timestamp when the project record was created
     */
    public Project(int id, String name, String projectType, String filePath, String description, Timestamp createdDate) {
        this.id = id;
        this.name = name;
        this.projectType = projectType;
        this.filePath = filePath;
        this.description = description;
        this.createdDate = createdDate;
    }

    /**
     * Returns the unique database identifier for this project.
     *
     * @return the database id for this project
     */
    public int getId() { return id; }

    /**
     * Returns the human-readable project name.
     *
     * @return the project name
     */
    public String getName() { return name; }

    /**
     * Returns the project type or category (e.g., Prototype, Final Print).
     *
     * @return the project type/category
     */
    public String getProjectType() { return projectType; }

    /**
     * Returns the absolute filesystem path where the project's files reside.
     *
     * @return absolute path to the project's files
     */
    public String getFilePath() { return filePath; }

    /**
     * Returns the project's description or user-supplied notes.
     *
     * @return project description or notes
     */
    public String getDescription() { return description; }

    /**
     * Returns the timestamp when the project record was created.
     *
     * @return when the project was created in the system
     */
    public Timestamp getCreatedDate() { return createdDate; }

    /**
     * Replace the cached list of last-printed timestamps for the project.
     *
     * @param lastPrintedDates list of timestamps (may be null)
     */
    public void setLastPrintedDates(List<Timestamp> lastPrintedDates) {
        this.lastPrintedDates = lastPrintedDates;
    }

    /**
     * Retrieve the list of last-printed timestamps recorded for this project.
     *
     * @return list of timestamps or null if none have been set
     */
    public List<Timestamp> getLastPrintedDates() {
        return lastPrintedDates;
    }

    // Future convenience methods (e.g. lastPrintedMostRecent) can be added here
}