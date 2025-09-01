package cz.cleanship.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.Detekt

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // access version catalog

            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "org.jlleitschuh.gradle.ktlint")
            apply(plugin = "io.gitlab.arturbosch.detekt")

            extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java) {
                jvmToolchain(21)
            }

            // Ktlint
            extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class.java) {
                version.set(libs.findVersion("ktlint").get().toString())
                filter { exclude("**/generated/**") }
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
                "io.gitlab.arturbosch.detekt:detekt-formatting:${libs.findVersion("detekt").get()}"
            )

            tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
                useJUnitPlatform()
                testLogging {
                    events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
                }
            }
            tasks.withType<Detekt>().configureEach {
                jvmTarget = "21"
            }
            tasks.named("check").configure { dependsOn("ktlintCheck", "detekt") }
        }
    }
}

