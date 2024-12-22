plugins {
    java
    idea
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    `maven-publish`
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

// https://docs.gitlab.com/ee/user/packages/maven_repository/?tab=gradle#edit-the-configuration-file-for-publishing
publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://gitlab.com/api/v4/projects/19488309/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "ci-package-deploy-token"
                value = project.findProperty("gitLabPrivateToken").toString()
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
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