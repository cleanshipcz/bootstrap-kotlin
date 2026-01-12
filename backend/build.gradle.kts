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
    
    // Telemetry & Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test dependencies
    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.testcontainersBom))
    testImplementation(libs.testcontainersJunit)
    testImplementation(libs.testcontainersPostgres)
    testRuntimeOnly(libs.h2)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
