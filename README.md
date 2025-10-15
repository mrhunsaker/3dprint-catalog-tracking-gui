# 3D Print Catalog Tracking GUI

A desktop application for cataloging, tracking, and searching 3D print projects. Built with Java Swing, H2 database, and FlatLaf themes for a modern, customizable interface.

## Features

- **Add Projects:**
  - Enter project name, type, tags, notes, last printed dates, and select a project folder.
  - All fields are mandatory for submission.
  - Project files are copied to the app's home directory for organization.
- **Search Projects:**
  - Search by project name or description.
  - Results displayed in a sortable table.
- **Theme Selection:**
  - Choose from a variety of FlatLaf IntelliJ themes via the menu bar for light/dark/colorful UI.
- **Database:**
  - Uses H2 embedded database for storing project metadata and print history.
- **Modern UI:**
  - Large, accessible fonts (18pt) throughout the interface.
  - Maximized window on startup.

## Getting Started

### Prerequisites
- Java 17 or newer
- Maven, Gradle, or Ant (for building)

### Build & Run

#### Maven
```sh
mvn clean package
java -jar target/print-job-tracker-2025-08-beta.jar
```

#### Gradle
```sh
gradle build
java -jar build/libs/print-job-tracker-2025-08-beta.jar
```

#### Ant
```sh
ant
java -jar dist/print-job-tracker-2025-08-beta.jar
```

#### Ivy
```sh
ant ivy
java -jar dist/print-job-tracker-2025-08-beta.jar
```

### Dependencies
- [FlatLaf](https://www.formdev.com/flatlaf/) (core and IntelliJ themes)
- H2 Database
- JCalendar for date picker

All dependencies are specified in `pom.xml`, `build.gradle`, `build.gradle.kts`, and `ivy.xml`.

## Project Structure
```
app_home/           # Application data and copied project folders
lib/                # External JARs (for Ant/Ivy)
src/main/java/      # Source code
  Main.java         # Main application window
  ProjectFormPanel.java # Project entry form
  SearchDialog.java # Project search dialog
  Database.java     # Database utilities
  FileUtils.java    # File operations
  Project.java      # Project data model
```

## Database Schema

The application uses an embedded H2 database. Below is the schema for the main tables:

### Table: projects
| Column      | Type         | Description                       |
|------------ |------------- |-----------------------------------|
| id          | INT (PK)     | Unique project ID                 |
| name        | VARCHAR(255) | Project name/title                |
| project_type| VARCHAR(255) | Project type (e.g., Prototype)    |
| file_path   | VARCHAR(1024)| Absolute path to project files    |
| description | VARCHAR(1000)| Project description/notes         |
| created_date| TIMESTAMP    | Timestamp when project created    |
| recipient   | VARCHAR(255) | Project recipient                 |
| tags        | VARCHAR(1024)| Comma-separated tags              |

### Table: last_printed_dates
| Column      | Type         | Description                       |
|------------ |------------- |-----------------------------------|
| id          | INT (PK)     | Unique entry ID                   |
| project_id  | INT (FK)     | Linked project ID                 |
| print_date  | TIMESTAMP    | Date project was printed          |

#### Example SQL to view all projects:
```sql
SELECT * FROM projects;
```

#### Example SQL to view print history for a project:
```sql
SELECT * FROM last_printed_dates WHERE project_id = 1;
```

## How to Use

1. **Add a Project:**
   - Click "Add Project" and fill in all required fields (name, type, recipient, tags, notes, last printed dates, folder).
   - Use the date picker to add print dates.
   - Select the project folder to copy files into the app's home directory.
   - Click "Add Project" to save.

2. **Search Projects:**
   - Click "Search Projects" to open the search dialog.
   - Enter keywords to search by name or description.
   - Double-click a result to open its folder, or use the buttons to load details or open the folder.

3. **Update Projects:**
   - Use "Load Project" to populate the form with an existing project's data.
   - Make changes and click "Update Project" to save.

4. **Backup & Restore:**
   - Backups are created automatically. Restore from backup via the app or by replacing the database file in `app_home/`.

5. **Change Theme:**
   - Use the menu bar to select a preferred UI theme.

6. **Access Database Directly:**
   - See `connect_h2.md` for instructions on accessing and editing the database using the H2 Console.

## Troubleshooting

- If you encounter errors, check that Java 17+ is installed and all dependencies are present.
- For database issues, verify the integrity using the app or H2 Console.
- For file copy errors, ensure you have permission to access the selected folders.

## How to Contribute

We welcome contributions from everyone, regardless of technical experience!

### For Non-Technical Users
- **Suggest Features or Report Bugs:**
  - Go to the GitHub repository and click on the "Issues" tab.
  - Click "New Issue" and describe your suggestion or problem in detail.
- **Test the App:**
  - Download the latest release from the "Releases" section.
  - Try out features and let us know what works or what could be improved.

### For Developers
- **Fork the Repository:**
  - Click "Fork" at the top right of the GitHub page.
- **Clone Your Fork:**
  - Use `git clone <your-fork-url>` to get a local copy.
- **Create a Branch:**
  - Use `git checkout -b feature/your-feature-name`.
- **Make Changes:**
  - Edit code, documentation, or add tests.
- **Commit and Push:**
  - Use `git commit -m "Describe your change"` and `git push`.
- **Open a Pull Request:**
  - Go to your fork on GitHub, click "Pull Requests", and open a new PR to the main repository.

### Code Style & Guidelines
- Write clear, descriptive commit messages.
- Add Javadoc comments for new methods/classes.
- Test your changes before submitting.
- For questions, open an issue or ask in the discussions tab.

---
For questions or contributions, open an issue or pull request on GitHub.

## Customization
- Add new project types or tags in `ProjectFormPanel.java`.
- Extend the database schema in `Main.java` and `Database.java`.
- Add new themes by updating the theme map in `Main.java`.

## License
Apache 2.0 License

## Author
Michael Ryan Hunsaker, M.Ed., Ph.D. <hunsakerconsulting@gmaillcom>

---
For questions or contributions, open an issue or pull request on GitHub.