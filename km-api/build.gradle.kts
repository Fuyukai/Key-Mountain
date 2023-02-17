import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api("com.squareup.okio:okio:3.3.0")
    api("org.apache.logging.log4j:log4j-api:2.19.0")

    api("it.unimi.dsi:fastutil:8.5.11")

    // todo: vaporise this, dont need these webshit-esque deps
    api("com.github.f4b6a3:uuid-creator:5.2.0")

    // lol imaggine sending json over the wire in a binary protocol
    api("com.fasterxml.jackson.core:jackson-core:2.14.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

    // TODO: rewrite this by ourselvees, rather than depending on someonee elses impl
    api("com.dyescape:jackson-dataformat-nbt:1.0")

}
val compileKotlin: KotlinCompile by tasks