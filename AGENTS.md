# AGENTS.md

Conventions for working in this repo. This is a Kotlin JVM project with a JavaFX desktop app module
(`desktop-javafx`).

The UI-independent presentation layer lives in `presentation`: ViewModels and their UI states,
`Navigator` and the `Route` keys, `TopLevelDestination`, `MoneyRepositoryController`, the Koin
`PresentationModule`, and their tests. `desktop-javafx` holds the JavaFX UI (screens, components, `AppScreen`,
`MoneyApplication`, `Main.kt`, `navigationRouter`), its string resources and stylesheet, and depends on
`presentation`, so another UI module can reuse the same presentation layer. Both modules group code by the same
feature subpackages: `com.munzenberger.money.presentation.<feature>` in `presentation` and
`com.munzenberger.money.desktop.<feature>` in `desktop-javafx`. Keep UI-toolkit types out of `presentation`: it
depends only on the plain `lifecycle-viewmodel` and `koin-core-viewmodel`, and UI modules declare their own
toolkit dependencies. `presentation` also doesn't depend on a data implementation (only on `data-api`, through
`core`): the app module depends on `data-sql` and binds a `MoneyRepositoryConnectorFactory` built from its connectors in Koin
(`dataModule` in `desktop-javafx`'s `inject/DataModule.kt`), which `MoneyRepositoryController` uses to open databases. UI state carries plain values (e.g. an enum such as `DatabaseError`) that the UI module maps
to its own strings, rather than string resources.

## Navigation

Routes are defined in `presentation/src/main/kotlin/com/munzenberger/money/presentation/navigation/Route.kt`,
and wired to screens in `desktop-javafx/src/main/kotlin/com/munzenberger/money/desktop/navigation/NavigationRouter.kt`:

```kotlin
@Serializable
sealed interface Route {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route

    @Serializable
    data object NewAccount : Route
}

// desktop-javafx
val navigationRouter: (route: Route, scope: ScreenScope) -> Destination = { route, scope ->
    when (route) {
        Route.Welcome -> Destination.Screen(WelcomeScreen(scope))
        Route.AccountList -> Destination.Screen(AccountListScreen(scope))
        is Route.NewAccount -> Destination.Dialog(
            AccountSettingsScreen(scope, route),
            strings.getString("new_account_title"),
        )
    }
}
```

- Each route is a `@Serializable data object` (or `data class` if it needs args) nested in the
  `Route` sealed interface. `Route` is a plain Kotlin type with no UI-toolkit dependencies.
- Map each route to its screen in `navigationRouter`'s exhaustive `when`, so adding a `Route` fails to compile
  until it's mapped. A route without a screen yet uses `placeholderScreen(route)`.
- The router also decides how each screen is shown, through a `Destination` (`navigation/Destination.kt`):
  `Destination.Screen` shows it in the window's content pane, replacing the screen below it, and
  `Destination.Dialog` shows it in a modal window titled from a string resource, over the screen below it. A
  screen that passes its route on needs `is Route.X ->` even for a `data object`, so `route` is smart-cast to the
  screen's route type. Whether a route is a dialog is up to the UI module, so `Route` and the ViewModels don't
  change: a dialog's ViewModel navigates and goes back with `navigator.back(from = route)` like any screen's.
- Screens live in their own subpackage under
  `desktop-javafx/src/main/kotlin/com/munzenberger/money/desktop/<screenName>/`, e.g. `welcome/WelcomeScreen.kt`,
  `accounts/AccountListScreen.kt`.

The back stack is owned by the `Navigator` singleton
(`presentation/src/main/kotlin/com/munzenberger/money/presentation/navigation/Navigator.kt`, registered with
`single { Navigator() }` in `PresentationModule`). It exposes the back stack as a plain
`backStack: StateFlow<List<Route>>` (starting at `Route.Welcome`), with no UI-toolkit types, and
`currentRoute: StateFlow<Route?>` (the top of the stack). ViewModels take `Navigator` as a constructor
dependency and navigate through `navigate { ... }`, which applies the change to a mutable copy of the back
stack, publishes it, and updates `currentRoute` to its new top, so both `backStack.value` and
`currentRoute.value` reflect it as soon as `navigate` returns:

```kotlin
navigator.navigate { add(Route.AccountList) }
// or, to replace the stack rather than push onto it:
navigator.navigate { clear(); add(Route.AccountList) }
```

To go back, call `navigator.back(from = route)` with the screen's own route: it pops only that route, so a screen
that goes back after the user has left it (such as when a save finishes) doesn't pop another screen, and it never
pops the last route. `navigator.back()` pops whatever is on top, for back actions not tied to a screen, like the
toolbar's.

Reading `navigator.backStack.value` is fine, but prefer `currentRoute` when you only need the top route.

Top-level destinations are listed in the `TopLevelDestination` enum
(`presentation/.../presentation/rail/TopLevelDestination.kt`) and shown by their labels in
`desktop-javafx/.../desktop/rail/AppNavigationRail.kt`. `AppNavigationRailViewModel` selects the top-most
top-level destination in the back stack (so a screen pushed on top, like `NewAccount` over `AccountList`, keeps
its parent selected) and switches destinations by replacing the stack (`clear(); add(route)`), not pushing onto
it, so peers don't build up back history. To add a top-level screen, add a `Route`, map it in
`navigationRouter`, and add a `TopLevelDestination` entry with its label.

`NavigationHost` (`desktop-javafx/.../desktop/navigation/NavigationHost.kt`) collects `navigator.backStack`,
builds each entry's `Destination` with `navigationRouter`, and gives each entry a `ScreenScope`
(`navigation/ScreenScope.kt`). Its pane shows the top-most `Destination.Screen`, and each `Destination.Dialog`
above it is shown in a `ScreenDialog` (`navigation/ScreenDialog.kt`) owned by the window below it, so dialogs can
open over dialogs; dialogs below the top-most screen are hidden until going back shows them again. A dialog
opens at its content's preferred size, which becomes its minimum size. Closing a dialog's window goes back from
its route, so only the back stack opens and closes dialogs, and popping the entry closes its window.
`ScreenScope` holds that entry's `ViewModelStore`: `scope.viewModel<XViewModel>()` resolves the ViewModel from
Koin, and `scope.collect(viewModel.state) { ... }` updates nodes on the FX thread.
The scope is closed (clearing its ViewModels and cancelling its collectors) when the entry is popped, so
navigating to the screen again starts with a fresh ViewModel. `kotlinx-coroutines-javafx` supplies
`Dispatchers.Main` for `viewModelScope`.

`MoneyApplication` handles only the stage (title, scene size, stylesheet, saved window bounds); the scene's root
is `AppScreen` (`desktop-javafx/.../desktop/AppScreen.kt`), which holds the `AppViewModel` and the
`NavigationHost`, and shows `AppToolbar` and `AppNavigationRail` beside it while a repository is open.
`MoneyApplication` creates `AppScreen` with an app-lifetime `ScreenScope` in `start`, and closes both in `stop`,
then closes the `MoneyRepositoryController` so the open database is closed on exit. It closes the controller last,
so `AppViewModel` doesn't navigate to `Welcome` as the database closes.

App-wide reactive navigation lives in `AppViewModel` (`presentation/.../presentation/AppViewModel.kt`): in
`init` it collects `MoneyRepositoryController.connectionState` and does `navigator.navigate { clear(); add(...) }`
to `AccountList` while a repository is `Connected`, or to `Welcome` otherwise (including while `Connecting`). It
also exposes `state: StateFlow<AppUiState>` (`isRepositoryConnected`). Put new app-lifetime reactive navigation in
`AppViewModel`.

## ViewModels

Each screen that needs one has its own `ViewModel`, defined in `presentation` under the same feature subpackage as
the screen, e.g. `welcome/WelcomeViewModel.kt`, `accounts/AccountListViewModel.kt`. It extends
`androidx.lifecycle.ViewModel` and takes any dependencies (singletons like `MoneyRepositoryController`)
as constructor params:

```kotlin
class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() { /* ... */ }
```

Non-screen UI (e.g. `rail/AppNavigationRailViewModel.kt`) and the root
`AppViewModel` follow the same pattern.

Register it in `presentation/src/main/kotlin/com/munzenberger/money/presentation/inject/PresentationModule.kt` with Koin's
`viewModel { ... }` DSL, passing constructor deps via `get()`. Singletons are registered with
`single { ... }` in the same module:

```kotlin
val presentationModule = module {
    single { MoneyRepositoryController(/* ... */) }
    single { Navigator() }

    viewModel { AppViewModel(get(), get()) }
    viewModel { WelcomeViewModel(get()) }
    viewModel { AccountListViewModel(get()) }
}
```

A screen takes its `ScreenScope`, gets its ViewModel from it, and renders each state it collects:

```kotlin
class WelcomeScreen(scope: ScreenScope) : VBox(Spacing.CONTROL_SPACING) {
    private val viewModel: WelcomeViewModel = scope.viewModel()

    init {
        scope.collect(viewModel.state, ::render)
    }

    private fun render(state: WelcomeUiState) { /* ... */ }
}
```

A preference that changes what a screen shows, such as the account list's grouping, belongs to its ViewModel,
which reads and writes it in `UserPreferences`. A preference that only changes how one UI lays it out, such as a
table's column order and widths (`components/ColumnLayout.kt`) or the window's bounds
(`preferences/WindowBounds.kt`), stays in the UI module, which gets `UserPreferences` from Koin directly.

## String resources

Strings live in `desktop-javafx/src/main/resources/com/munzenberger/money/desktop/strings.properties`
(one module-wide file — not per-screen), and are read through the `strings` `ResourceBundle` in `Strings.kt`:

```properties
account_list_title=Accounts
```

```kotlin
Label(strings.getString("account_list_title"))
```

- Naming: snake_case, often suffixed by role (`_button_title`, `_dialog_title`), or a plain
  descriptive name when not tied to a specific widget (e.g. `app_title`, `default_file_name`).
- Text built from values keeps its format in the string, with `MessageFormat` placeholders (`{0}`, `{1}`, …)
  so a translation can reorder them, and is filled in with the `strings.getString(key, args...)` extension
  from `Strings.kt`, rather than concatenated in code:

  ```properties
  account_currency_value={0} ({1})
  ```

  ```kotlin
  strings.getString("account_currency_value", currency.displayName, currency.currencyCode)
  ```

  `MessageFormat` treats a single quote as an escape, so write an apostrophe as `''` in a formatted string.

## Data writes

`data-api` splits each entity's API into a read-only `XRepository` (`Flow`s) and an `XWriter` with
blocking `add`/`update`/`removeById` (e.g. `account/AccountRepository.kt`, `account/AccountWriter.kt`).
`MoneyRepository` exposes only the reads; writes are reachable only through its
`suspend fun <R> transaction(block: MoneyWriter.() -> R): R`, which switches to the IO context and runs the
block in a single database transaction (committed when it returns, rolled back if it throws):

```kotlin
repository.transaction {
    add(transaction)
    categoryEntries.forEach { add(it) }
}
```

Even a single write goes through `transaction { add(account) }`. The block can't suspend, so it can't leave
the thread holding the transaction or do slow work while holding the write lock. Add a new entity's writes
to an `XWriter` and to `MoneyWriter`, not to its repository. In `data-sql`, each `SqlXRepository`
implements both interfaces, and `SqlMoneyRepository` wires the same instances into its reads and its
writer.

## Use cases

Business logic that a ViewModel shouldn't own, such as validating input and writing it to the
repository, goes in a use case class in the `core` module, grouped by feature package (e.g.
`core/.../core/account/CreateAccountUseCase.kt`). A use case:

- takes the `MoneyRepository` it works on as the first param of its operator, not a
  `MoneyRepositoryController` in its constructor, so it doesn't depend on which repository is open;
  any other dependencies are constructor params;
- exposes a single `operator fun invoke(...)` (`suspend` if it does I/O), and nothing else public;
- validates all of its input before any database operation, and writes through
  `repository.transaction { ... }` (see Data writes);
- returns a sealed result rather than throwing, e.g. `Success`, `Invalid(errors)` with one error per
  invalid field, and `Failure(cause)` for a write that failed. It rethrows `CancellationException`.
- reads by returning a flow of `Result`s from `repository.resultFlow { ... }`, which logs a failed read
  and emits it as a failure.

```kotlin
class CreateAccountUseCase {
    suspend operator fun invoke(repository: MoneyRepository, input: AccountInput): SaveAccountResult { /* ... */ }
}
```

Register it in `PresentationModule` with `factory { ... }` and pass it to the ViewModel's constructor. The
ViewModel also takes the `MoneyRepositoryController`
(`presentation/.../presentation/database/MoneyRepositoryController.kt`), which tracks the open database as its
`connectionState` (`Disconnected`, `Connecting` or `Connected`; failures aren't part of it, since each open call
returns its own result), and supplies the open repository:

- to write, it reads the `repositoryController.repository` extension (the `repository` of the current
  `connectionState`) and calls the use case like a function (`createAccount(repository, input)`), treating no
  open repository as a failure;
- to read, it uses `repositoryController.flow { getAccountGroups(it, grouping) }`, which follows the
  open repository and emits nothing while none is open.

It then maps the result to UI state. For example, each `Invalid` error sets that field's `isError` and
its supporting text. In tests, call the use case directly in `core` with a mock repository, and mock it
with `coEvery { createAccount(any(), any()) }` in ViewModel tests, connecting a repository through
`MoneyRepositoryControllerFixture`.
