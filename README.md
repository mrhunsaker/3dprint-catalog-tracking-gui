# 3D Print Job Tracker

A desktop Swing application to manage 3D printing projects with an embedded H2 database.

## Features
- Add and search projects
- Store files per project
- Cross-platform (Windows, Linux, macOS)
- Portable database in `./app_home`

## Run

### Maven
```bash
mvn clean package
./run.sh
```

### Gradle
```bash
gradle build
./run.sh
```

### Windows
```cmd
run.bat
```

## CI/CD
- GitHub Actions builds with Maven and Gradle
- GPG-signed artifacts with checksums
- Release automation

## License
MIT
