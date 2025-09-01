plugins {
    alias(libs.plugins.cleanship.kotlin.convention)
}

dependencies {
    // Core
    implementation(libs.opentelemetryApi)
    implementation(libs.opentelemetrySdk)
    implementation(libs.opentelemetryExporterOtlp)
    implementation(libs.opentelemetryExporterLogging)
    implementation(libs.opentelemetryContext)
    implementation(libs.opentelemetryExtensionKotlin)

    // Metrics
    implementation(libs.micrometerCore)
    implementation(libs.micrometerRegistryPrometheus)
    implementation(libs.micrometerRegistryOtlp)

    // Logging
    implementation(libs.logbackClassic)
    implementation(libs.logstashLogbackEncoder)

    // Coroutines
    implementation(libs.kotlinxCoroutines)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.opentelemetrySdkTesting)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.logbackClassic)
}
