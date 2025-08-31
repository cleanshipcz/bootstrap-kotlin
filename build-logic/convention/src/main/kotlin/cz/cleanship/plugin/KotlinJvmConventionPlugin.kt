package cz.cleanship.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // access version catalog

            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "org.jlleitschuh.gradle.ktlint")

            extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java) {
                jvmToolchain(21)
            }

            // Ktlint
            extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class.java) {
                version.set(libs.findVersion("ktlint").get().toString())
                filter { exclude("**/generated/**") }
            }

            tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
                useJUnitPlatform()
                testLogging {
                    events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
                }
            }
            tasks.named("check").configure { dependsOn("ktlintCheck") }
        }
    }
}
