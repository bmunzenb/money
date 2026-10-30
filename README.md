This is a Kotlin JVM project with a JavaFX desktop app.

* [/desktop-javafx](./desktop-javafx/src) is the JavaFX desktop app: the UI, stylesheet, and string resources.
* [/presentation](./presentation/src) is the UI-independent presentation layer: ViewModels, navigation,
  and dependency injection.
* [/core](./core/src) holds the use cases (business logic).
* [/data-api](./data-api/src) defines the repository interfaces, and [/data-sql](./data-sql/src)
  implements them with SQLDelight.

### Running the app

Use the run configurations provided by the run widget in your IDE's toolbar, or run `./gradlew :desktop-javafx:run`.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- All tests: `./gradlew test`

### Logging

App code logs using the JDK's built-in `java.util.logging`, under the `com.munzenberger.money` package — no external logging library or configuration is required.

For more verbose output during local development, create a `logging.local.properties` file at the repo root (it's `.gitignore`d, so it's per-developer and never committed). If present, `./gradlew :desktop-javafx:run` automatically picks it up via a `java.util.logging.config.file` JVM argument (see `desktop-javafx/build.gradle.kts`).

Example that logs everything down to `FINEST` for `com.munzenberger.money` code, while keeping everything else (JavaFX, third-party libraries, etc.) at `WARNING` and above:

```properties
handlers = java.util.logging.ConsoleHandler
.level = WARNING

com.munzenberger.money.level = FINEST

java.util.logging.ConsoleHandler.level = FINEST
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format = %1$tF %1$tT %4$s %2$s - %5$s%6$s%n
```

Note that `java.util.logging.ConsoleHandler.level` must be at least as verbose as the most detailed logger you want to see — it's a second filter applied after each logger's own level.