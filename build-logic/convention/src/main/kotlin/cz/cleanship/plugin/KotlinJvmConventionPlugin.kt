package cz.cleanship.plugin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // access version catalog

            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
            apply(plugin = "org.jlleitschuh.gradle.ktlint")
            apply(plugin = "io.gitlab.arturbosch.detekt")
            apply(plugin = "jacoco")

            extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java) {
                jvmToolchain(21)
            }

            // Ktlint
            extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class.java) {
                version.set(libs.findVersion("ktlint").get().toString())
                filter { exclude("**/generated/**") }
                reporters { reporter(ReporterType.CHECKSTYLE) }
            }

            // Detekt
            extensions.configure(DetektExtension::class.java) {
                toolVersion = libs.findVersion("detekt").get().toString()
                buildUponDefaultConfig = true
                config.setFrom(files(rootProject.file("detekt.yml")))
                allRules = false
                parallel = true
                basePath = projectDir.absolutePath
            }
            // Enable formatting rules for Detekt
            dependencies.add(
                "detektPlugins",
                "io.gitlab.arturbosch.detekt:detekt-formatting:${libs.findVersion("detekt").get()}",
            )

            tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
                useJUnitPlatform()
                testLogging {
                    events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
                }
                // Ensure coverage is generated after tests
                finalizedBy(tasks.named("jacocoTestReport"))
            }
            tasks.withType<Detekt>().configureEach {
                jvmTarget = "21"
                reports {
                    xml.required.set(true)
                    sarif.required.set(true)
                    html.required.set(false)
                    txt.required.set(false)
                }
            }
            // Configure JaCoCo XML reports for SonarCloud
            tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                }
            }

            tasks.named("check").configure { dependsOn("ktlintCheck", "detekt", "jacocoTestReport") }
        }
    }
}
