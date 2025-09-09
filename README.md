[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=cleanshipcz_bootstrap-kotlin)

# Bootstrap Kotlin Application

## Build and Run

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

### Project Structure

This project follows the suggested multi-module setup and consists of the `app` and `utils` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

### Dependency Management

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).

### Code style

* This project uses [ktlint](https://ktlint.github.io/) to enforce a consistent code style.
* You can run `./gradlew ktlintCheck` to check the code style and `./gradlew ktlintFormat` to automatically format the code.
* You can also install the [ktlint IntelliJ plugin](https://plugins.jetbrains.com/plugin/15036-ktlint) to get real-time feedback in the IDE.
* To disable a rule, add a rule to the `.editorconfig` file in the root of the project.
    * Tip: use the plugin to generate the suppression for the given line, then copy the suppression to the `.editorconfig` file (replace `:` with `-`)

#### Static analysis

* This project uses [Detekt](https://detekt.dev/) for Kotlin static code analysis.
* Run `./gradlew detekt` to analyze the code. Detekt is also wired into `./gradlew check`.
* Configuration lives in `detekt.yml` at the project root. Adjust rules there to fit your needs.
* Formatting rules are enabled via `detekt-formatting` to align with ktlint.
* To use a baseline, generate one with `./gradlew detektBaseline` and configure the `baseline` property in the Detekt settings (see `build-logic/convention/src/main/kotlin/cz/cleanship/plugin/KotlinJvmConventionPlugin.kt`).
