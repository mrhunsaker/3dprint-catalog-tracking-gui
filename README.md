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

## Customization
- Add new project types or tags in `ProjectFormPanel.java`.
- Extend the database schema in `Main.java` and `Database.java`.
- Add new themes by updating the theme map in `Main.java`.

## License
MIT License

## Author
mrhunsaker

---
For questions or contributions, open an issue or pull request on GitHub.