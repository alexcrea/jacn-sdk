plugins {
    id("java")
}

group = "xyz.alexcrea.jacn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")

    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}