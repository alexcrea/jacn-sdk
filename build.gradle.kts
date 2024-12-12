import cn.lalaki.pub.BaseCentralPortalPlusExtension
import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("java")
    // Maven publish
    `maven-publish`
    signing
    id("cn.lalaki.central").version("1.2.5")
}

group = "xyz.alexcrea.jacn"
version = "0.0.3"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    implementation("com.networknt:json-schema-validator:1.5.4")

    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(java.sourceSets.main.get().java)
}

val javadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

// ------------------------------------
// PUBLISHING TO SONATYPE CONFIGURATION
// ------------------------------------

// The path is recommended to be set to an empty directory
val localMavenRepo = uri(
    project.findProperty("localMavenRepo") as String?
        ?: rootProject.layout.buildDirectory.dir("local-maven-repo").get().asFile.toURI() // Convert to URI
)

centralPortalPlus {
    url = localMavenRepo
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
    publishingType = BaseCentralPortalPlusExtension.PublishingType.USER_MANAGED // or PublishingType.AUTOMATIC
}

object Meta {
    const val desc = "alexcrea's Java Neuro SDK API implementation"
    const val license = "MIT"
    const val licensePath = "LICENSE.md"
    const val githubRepo = "alexcrea/JACN_SDK"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

publishing {
    repositories {
        maven {
            url = localMavenRepo // Specify the same local repo path in the configuration.
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set(Meta.licensePath)
                    }
                }
                developers {
                    developer {
                        id.set("alexcrea")
                        name.set("alexcrea")
                        email.set("contact@alexcrea.xyz")
                        url.set("https://github.com/alexcrea")
                    }
                }
                scm {
                    url.set(
                        "https://github.com/${Meta.githubRepo}.git"
                    )
                    connection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }

                withXml {
                    val selfNode = asNode().get("dependencies") as NodeList
                    selfNode.forEach { dependencies ->
                        (dependencies as Node).children().forEach {
                            val scope = ((it as Node).get("scope") as NodeList)[0] as Node

                            if (scope.text() == "runtime") {
                                scope.setValue("compile")
                            }
                        }
                    }

                }
            }
        }
    }
}