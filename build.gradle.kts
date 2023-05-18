plugins {
    kotlin("jvm") version "1.8.0"

//    id("edu.wpi.first.NativeUtils") version "2024.0.0" apply false
}

group = "com.ghsrobo.harmony"
version = "1.0-SNAPSHOT"

val wpiLibVersion = "2023.4.2"

repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/") }
}

dependencies {
    // FRC
    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpiLibVersion")

    // testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}