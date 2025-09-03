import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1" // For fat Jar
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    // Set source compatibility
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // H2 Database
    implementation("com.h2database:h2:2.2.224")

    // JCalendar for Date Chooser
    implementation("com.toedter:jcalendar:1.4")

    // JUnit 5 for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

application {
    mainClass.set("Main")
}

// Shadow JAR configuration for a fat JAR
tasks.withType<ShadowJar> {
    archiveBaseName.set("print-job-tracker")
    archiveClassifier.set("")
}