plugins {
    alias(libs.plugins.cleanship.kotlin.convention)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":telemetry"))
    implementation(libs.bundles.springBootWeb)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.postgresql)

    // Test dependencies
    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.testcontainersBom))
    testImplementation(libs.testcontainersJunit)
    testImplementation(libs.testcontainersPostgres)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
