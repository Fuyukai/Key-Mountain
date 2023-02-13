import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api("com.squareup.okio:okio:3.3.0")
    api("org.apache.logging.log4j:log4j-api:2.19.0")
}
val compileKotlin: KotlinCompile by tasks