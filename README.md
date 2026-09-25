This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Desktop app:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Desktop tests: `./gradlew :shared:jvmTest`

### Logging

App code logs using the JDK's built-in `java.util.logging`, under the `com.munzenberger.money` package — no external logging library or configuration is required.

For more verbose output during local development, create a `logging.local.properties` file at the repo root (it's `.gitignore`d, so it's per-developer and never committed). If present, `./gradlew :desktop:run` automatically picks it up via a `java.util.logging.config.file` JVM argument (see `desktop/build.gradle.kts`).

Example that logs everything down to `FINEST` for `com.munzenberger.money` code, while keeping everything else (AWT/Swing, third-party libraries, etc.) at `WARNING` and above:

```properties
handlers = java.util.logging.ConsoleHandler
.level = WARNING

com.munzenberger.money.level = FINEST

java.util.logging.ConsoleHandler.level = FINEST
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format = %1$tF %1$tT %4$s %2$s - %5$s%6$n
```

Note that `java.util.logging.ConsoleHandler.level` must be at least as verbose as the most detailed logger you want to see — it's a second filter applied after each logger's own level.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…