import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.io.FileInputStream
import java.util.*

plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "11.1.0"
}

group = "org.aamini"
version = "0.0.1-SNAPSHOT" // Default value if no version passed

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    configurations {
        // Replace the default spring logger.
        // https://docs.spring.io/spring-boot/how-to/logging.html#howto.logging.log4j
        all {
            exclude("org.springframework.boot", "spring-boot-starter-logging")
        }
    }

    // Azure
    implementation("com.azure:azure-security-keyvault-secrets:4.9.1")
    implementation("com.azure:azure-identity:1.14.2")

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql:11.1.0")

    // Unit Testing Libraries
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Integration Testing Libraries
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.flywaydb:flyway-database-postgresql:11.1.0")
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.1.0")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Hide warning (https://stackoverflow.com/a/78188896)
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

// Build final app image (OCI).
// https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html#build-image.examples.publish
tasks.named<BootBuildImage>("bootBuildImage") {
    docker {
        publishRegistry {
            url= "https://ghcr.io"
            username=getEnv("CI_REGISTRY_USER")
            password=getEnv("CI_REGISTRY_PASSWORD")
        }
    }
}

// Used to set up Flyway commands that developers can run through gradle. CLI
// commands like migrate, clean, info, etc. to test any new Flyway scripts on
// a local database.
flyway {
    url = "jdbc:postgresql://${getEnv("DATABASE_HOST")}:5432/${getEnv("DATABASE_NAME")}"
    user = getEnv("DATABASE_USER")
    password = getEnv("DATABASE_PASSWORD")
    locations = arrayOf("classpath:db/migration")
}

// ================================ HELPERS ====================================
fun loadEnvFile(): Properties? {
    val properties = Properties()
    val envFile = File(".env")
    if (!envFile.exists()) {
        return null
    }
    FileInputStream(envFile).use {
        properties.load(it)
    }
    return properties
}

val envFile = loadEnvFile()

fun getEnv(envName: String): String {
    return envFile?.getProperty(envName) ?: System.getenv(envName) ?: ""
}
// =============================================================================
