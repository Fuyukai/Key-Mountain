import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Kotlin plugin: Used for Kotlin support.
    id("org.jetbrains.kotlin.jvm").version("1.8.0").apply(false)
    // Spotless plugin: Used for licence headers.
    id("com.diffplug.spotless").version("6.6.1").apply(false)
    // Versions plugin: Used to keep dependencies updated.
    id("com.github.ben-manes.versions").version("0.39.0").apply(false)
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    // == Plugins == //
    apply(plugin = "kotlin")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.ben-manes.versions")
    //apply(plugin = "io.gitlab.arturbosch.detekt")

    // == Dependencies == //
    val implementation by configurations
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
    }

    // == Language == //
    configure<JavaPluginExtension> {
        withSourcesJar()
        // withJavadocJar()

        toolchain {
            // this should pick loom EAP
            languageVersion.set(JavaLanguageVersion.of(19))
        }
    }

    configure<KotlinJvmProjectExtension> {
        explicitApi = ExplicitApiMode.Strict
    }

    // == Lint == //
    configure<SpotlessExtension> {
        kotlin {
            targetExclude("build/generated/**")

            licenseHeaderFile(project.file("LICENCE-HEADER"))
                .onlyIfContentMatches("package tf\\.veriny")
        }
    }

    // == Task Setup == //
    tasks {
        filter { it.name.startsWith("spotless") }
            .forEach { it.group = "lint" }
        filter { it.name.startsWith("depend") }.forEach { it.group = "dependencies" }

        withType<KotlinCompile>().configureEach {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-opt-in=kotlin.RequiresOptIn",  // Enable @OptIn
                    "-Xstring-concat=indy-with-constants",  // Enable invokedynamic string concat
                    "-Xjvm-default=all",  // Forcibly enable Java 8+ default interface methods
                    "-Xassertions=always-enable",  // Forcibly enable assertions
                    "-Xlambdas=indy",  // Forcibly use invokedynamic for all lambdas.
                )
                jvmTarget = "17"
            }
        }

        withType<JavaCompile>().configureEach {
            targetCompatibility = "19"
        }
    }
}