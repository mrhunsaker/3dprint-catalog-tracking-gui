plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.h2database:h2:2.2.224")
}

application {
    mainClass.set("Main")
}
