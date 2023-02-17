import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api("com.squareup.okio:okio:3.3.0")
    api("org.apache.logging.log4j:log4j-api:2.19.0")

    api("it.unimi.dsi:fastutil:8.5.11")

    // todo: vaporise this, dont need these webshit-esque deps
    api("com.github.f4b6a3:uuid-creator:5.2.0")

    api("com.fasterxml.jackson.core:jackson-annotations:2.14.2")

}
val compileKotlin: KotlinCompile by tasks