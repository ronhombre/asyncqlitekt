import org.jetbrains.dokka.gradle.DokkaTask
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka")  version "1.9.20" //KDocs
    id("maven-publish")
    id("signing") //GPG
}

group = "asia.hombre"
version = "0.0.2"
description = "An asynchronous wrapper for SQLiteConnection to support serial database access."

val projectName = "asyncqlitekt-jvm"
val baseProjectName = projectName.plus("-").plus(project.version)

val isAutomated = false

val mavenDir = projectDir.resolve("maven")
val mavenBundlingDir = mavenDir.resolve("bundling")
val mavenDeep = "$mavenBundlingDir/" + (project.group.toString().replace(".", "/")) + "/" + version

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha03")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("AsyncQLiteKt")
                description.set(project.description)
                url.set("https://github.com/ronhombre/asyncqlitekt")
                groupId = project.group.toString()
                artifactId = projectName
                version = project.version.toString()

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Ron Lauren Hombre")
                        email.set("ronlauren@hombre.asia")
                    }
                }
                scm {
                    url.set("https://github.com/ronhombre/asyncqlitekt")
                }
            }
        }
    }
    repositories {
        mavenLocal()
        maven {
            url = mavenDir.toURI()
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}

fun parseArtifactId(artifactId: String): String {
    val list = artifactId.splitToSequence("-").map { it.replaceFirstChar(Char::uppercase) }

    return list.joinToString("")
}

fun parseArtifactArchiveName(artifact: MavenPublication): String {
    return artifact.artifactId + "-" + artifact.version + "-bundle.zip"
}

for (publication in publishing.publications.asMap) {
    val artifact = publication.value as MavenPublication
    val parsedArtifactId = parseArtifactId(artifact.artifactId)
    val bundleFileName = parseArtifactArchiveName(artifact)

    tasks.register<Zip>("bundle$parsedArtifactId") {
        group = "Bundle"
        from(mavenDir)
        val mavenDeepDir = artifact.groupId.replace(".", "/") + "/" + artifact.artifactId
        include("$mavenDeepDir/*/*")
        destinationDirectory = mavenDir
        archiveFileName = parseArtifactArchiveName(artifact)
    }

    tasks.register<Exec>("publish" + parsedArtifactId + "ToMavenCentral") {
        mustRunAfter("bundle$parsedArtifactId")
        group = "Publish"

        commandLine(
            "curl", "-X", "POST",
            "https://central.sonatype.com/api/v1/publisher/upload?name=${artifact.artifactId}&publishingType=" + if(isAutomated) "AUTOMATED" else "USER_MANAGED",
            "-H", "accept: text/plain",
            "-H", "Content-Type: multipart/form-data",
            "-H", "Authorization: Bearer " + System.getenv("SONATYPE_TOKEN"),
            "-F", "bundle=@$bundleFileName;type=application/x-zip-compressed"
        )
        workingDir(mavenDir.toString())
        standardOutput = ByteArrayOutputStream()
        errorOutput = ByteArrayOutputStream()

        doLast {
            println("$standardOutput")
            println("$errorOutput")
        }
    }
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            perPackageOption {
                matchingRegex.set(".*")
                includeNonPublic.set(false)
            }
            reportUndocumented.set(true)
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    val dokkaBaseConfiguration = """
    {
      "footerMessage": "(C) 2024 Ron Lauren Hombre"
    }
    """
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to dokkaBaseConfiguration
        )
    )
}