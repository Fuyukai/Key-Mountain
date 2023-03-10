
dependencies {
    api(project(":km-api"))
    implementation("it.unimi.dsi:fastutil:8.5.11")

    // == Logging == //
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")

    // used in the server networker
    implementation("com.github.marianobarrios:linked-blocking-multi-queue:0.4.0")
}