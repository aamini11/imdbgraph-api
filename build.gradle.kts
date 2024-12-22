import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java

    // https://docs.spring.io/spring-boot/docs/3.2.5/gradle-plugin/reference/htmlsingle/#managing-dependencies.dependency-management-plugin
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"

    // helper IntelliJ IDE plugin used on last line.
    idea
}

group = "org.aamini"

// Hardcode version if not specified through CLI while running in CI.
if (project.version == "unspecified" || project.version.toString().isBlank()) {
    version = "0.0.1-SNAPSHOT"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.postgresql:postgresql:42.7.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Exclude the default spring-boot-starter-logging library since we include log4j2
configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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

// https://docs.spring.io/spring-boot/docs/3.2.7/gradle-plugin/reference/htmlsingle/#build-image.examples.publish
tasks.named<BootBuildImage>("bootBuildImage") {
    publish = true
    docker {
        builderRegistry {
            url = System.getenv("CI_REGISTRY")
            username = System.getenv("CI_REGISTRY_USER")
            password = System.getenv("CI_REGISTRY_PASSWORD")
        }
    }
}

// Marks the integrationTest folder green color for testing.
// https://docs.gradle.org/current/userguide/idea_plugin.html#sec:idea_identify_additional_source_sets
idea {
    module {
        testSources.from(sourceSets["integrationTest"].java.srcDirs)
    }
}