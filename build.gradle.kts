import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import java.io.FileInputStream
import java.util.*

plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "11.1.0"
    jacoco
}

group = "org.aamini"
version = "0.0.1-SNAPSHOT" // Default value if no version passed

val envFile = loadEnvFile()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
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

    // Testing Libraries
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
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

tasks.bootRun {
    args("--spring.profiles.active=dev")
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

task<Exec>("runAnsible") {
    group = "infrastructure"

    workingDir = File("infra/ansible")

    environment("DATABASE_HOST", getEnv("DATABASE_HOST"))
    environment("DATABASE_NAME", getEnv("DATABASE_NAME"))
    environment("DATABASE_USER", getEnv("DATABASE_USER"))
    environment("DATABASE_PASSWORD", getEnv("DATABASE_PASSWORD"))

    environment("OMDB_KEY", getEnv("OMDB_KEY"))

    commandLine("ansible", "-i", "staging", "main.yml")
}

task<Exec>("runTerraform") {
    group = "infrastructure"

    workingDir = File("infra/terraform/live/staging")

    environment("ARM_CLIENT_ID", getEnv("ARM_CLIENT_ID"))
    environment("ARM_CLIENT_SECRET", getEnv("ARM_CLIENT_SECRET"))
    environment("ARM_SUBSCRIPTION_ID", getEnv("ARM_SUBSCRIPTION_ID"))
    environment("ARM_TENANT_ID", getEnv("ARM_TENANT_ID"))

    commandLine("terraform", "apply")
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

fun getEnv(envName: String): String {
    return envFile?.getProperty(envName) ?: System.getenv(envName) ?: ""
}
// =============================================================================
