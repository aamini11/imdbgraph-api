import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    // https://docs.spring.io/spring-boot/gradle-plugin/managing-dependencies.html
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.6"

    id("org.flywaydb.flyway") version "11.1.0"

    idea // helper IntelliJ IDE plugin used on last line.
}

group = "org.aamini"
version = getVersionToUse()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.1.0")
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // Replace the default spring logger.
    // https://docs.spring.io/spring-boot/how-to/logging.html#howto.logging.log4j
    configurations {
        all {
            exclude("org.springframework.boot", "spring-boot-starter-logging")
        }
    }

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql:11.1.0")

    // Unit Testing Libraries
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Integration Testing Libraries
    sourceSets {
        create("integrationTest") {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }
    val integrationTestImplementation: Configuration by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly: Configuration by configurations.getting {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:postgresql")
    integrationTestImplementation("org.flywaydb:flyway-database-postgresql:11.1.0")
}

// Build final app image (OCI).
// https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html#build-image.examples.publish
tasks.named<BootBuildImage>("bootBuildImage") {
    docker {
        publishRegistry {
            url = System.getenv("CI_REGISTRY")
            username = System.getenv("CI_REGISTRY_USER")
            password = System.getenv("CI_REGISTRY_PASSWORD")
        }
    }
}

// ============================= Testing Setup =================================
tasks.withType<Test> {
    useJUnitPlatform()
    // Hide warning (https://stackoverflow.com/a/78188896)
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    useJUnitPlatform()

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    shouldRunAfter(tasks.test)
}

tasks.check { dependsOn(integrationTest) }

idea {
    module {
        // Fixes bug in IntelliJ where integrationTest library isn't green.
        // https://docs.gradle.org/userguide/idea_plugin.html#sec:idea_identify_additional_source_sets
        testSources.from(sourceSets["integrationTest"].java.srcDirs)
    }
}
// =============================================================================

// Used to set up Flyway commands that developers can run through gradle. These
// CLI commands let you use commands like migrate, clean, info, etc to test any
// new Flyway scripts being worked on.
flyway {
    // Enter your database info below:
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "YOUR_PASSWORD"
    locations = arrayOf("classpath:db/migration")
}

fun getVersionToUse(): String {
    // Hardcode version if not specified.
    return if (project.version == "unspecified" || project.version.toString().isBlank()) {
        "0.0.1-SNAPSHOT"
    } else {
        project.version.toString()
    }
}
