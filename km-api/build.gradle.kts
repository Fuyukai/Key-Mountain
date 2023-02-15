import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api("com.squareup.okio:okio:3.3.0")
    api("org.apache.logging.log4j:log4j-api:2.19.0")

    api("it.unimi.dsi:fastutil:8.5.11")
}
val compileKotlin: KotlinCompile by tasks