# AGENTS.md

Conventions for working in this repo. This is a Kotlin Multiplatform project with a Compose Desktop
presentation module (`desktop`); shared UI/theming/resources live in `shared`.

## Navigation (Navigation3)

Routes are defined in `desktop/src/main/kotlin/com/munzenberger/money/desktop/navigation/Route.kt`:

```kotlin
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route
}

val navigationRouter = entryProvider<Route> {
    entry<Route.Welcome> { WelcomeScreen() }
    entry<Route.AccountList> { AccountListScreen() }
}
```

- Each route is a `@Serializable data object` (or `data class` if it needs args) nested in the
  `Route` sealed interface.
- Wire each route to its composable with `entry<Route.X> { XScreen() }` inside `navigationRouter`.
- Screen composables live in their own subpackage under
  `desktop/src/main/kotlin/com/munzenberger/money/desktop/<screenName>/`, e.g. `welcome/WelcomeScreen.kt`,
  `accounts/AccountListScreen.kt`.

Navigation is triggered via the `Navigator` singleton
(`desktop/src/main/kotlin/com/munzenberger/money/desktop/navigation/Navigator.kt`), not by directly
mutating the back stack from a screen/ViewModel:

```kotlin
navigator.navigate { add(Route.AccountList) }
// or, to replace the stack rather than push onto it:
navigator.navigate { clear(); add(Route.AccountList) }
```

`App.kt` (the root composable) owns the actual `NavBackStack<Route>` and is the one place that
collects `navigator.events` and applies them to the back stack. It's also the established place to
observe app-lifetime singleton flows and react with navigation — e.g. it collects
`MoneyRepositoryController.moneyRepository` and navigates to `AccountList`/`Welcome` based on whether
a repository is open. Use a `LaunchedEffect(Unit) { someSingleton.someFlow.collect { ... } }` block in
`App.kt` for this kind of app-wide reactive navigation rather than introducing a dedicated
`AppViewModel`.

## ViewModels

Each screen that needs one has its own `ViewModel`, defined alongside the screen composable in the
same package, e.g. `welcome/WelcomeViewModel.kt`, `accounts/AccountListViewModel.kt`. It extends
`androidx.lifecycle.ViewModel` and takes any dependencies (singletons like `MoneyRepositoryController`)
as constructor params:

```kotlin
class AccountListViewModel : ViewModel()

class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() { /* ... */ }
```

Register it in `desktop/src/main/kotlin/com/munzenberger/money/desktop/inject/AppModule.kt` with Koin's
`viewModel { ... }` DSL, passing constructor deps via `get()`:

```kotlin
val appModule = module {
    viewModel { WelcomeViewModel(get()) }
    viewModel { AccountListViewModel() }
}
```

The public screen composable takes the ViewModel as a default parameter injected with `koinViewModel()`,
and immediately delegates to the stateless `XScreenContent` composable (see Compose previews below) —
the ViewModel itself never leaks past the top-level `XScreen` function:

```kotlin
@Composable
fun AccountListScreen(viewModel: AccountListViewModel = koinViewModel()) {
    AccountListScreenContent()
}
```

Requires `org.koin.compose.viewmodel.koinViewModel` import. A screen with no state/behavior yet (like
`AccountListScreen` today) can still take an empty `ViewModel` — this keeps the wiring in place so
behavior can be added to the ViewModel later without changing the composable's shape.

## Compose previews

Follow the pattern in `WelcomeScreen.kt` / `AccountListScreen.kt`:

```kotlin
@Preview
@Composable
private fun XScreenPreview() {
    PreviewThemed {
        XScreenContent(/* stub callbacks/params */)
    }
}
```

- `PreviewThemed` (`com.munzenberger.money.shared.theme.PreviewThemed`) wraps the preview content in
  the app theme.
- The preview function is `private`, named `<Screen>Preview`, and placed at the bottom of the same
  file as the screen.
- If the real screen composable takes a Koin-injected ViewModel (e.g.
  `fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel())`), extract a stateless
  `private fun XScreenContent(...)` composable that takes plain params/lambdas, and preview that
  instead of the ViewModel-backed entry point.
- If the screen has no ViewModel/params (e.g. `AccountListScreen`), the preview can call it directly.

## String resources

Strings live in `shared/src/commonMain/composeResources/values/strings.xml` (standard Android-style
`strings.xml`, one module-wide file — not per-screen):

```xml
<string name="account_list_title">Accounts</string>
```

- Naming: snake_case, often suffixed by role (`_button_title`, `_dialog_title`), or a plain
  descriptive name when not tied to a specific widget (e.g. `app_title`, `default_file_name`).
- No `translatable`/`formatted` attributes or comments are currently used — keep entries plain.
- Usage in Compose code:

  ```kotlin
  import money.shared.generated.resources.Res
  import money.shared.generated.resources.account_list_title
  import org.jetbrains.compose.resources.stringResource

  Text(text = stringResource(Res.string.account_list_title))
  ```

- The `Res.string.<name>` accessor is generated by the Compose Resources Gradle plugin from
  `strings.xml` — no manual codegen step needed, but a build may be required for the IDE/compiler to
  pick up a newly added entry.
