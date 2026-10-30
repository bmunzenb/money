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
toolkit dependencies. UI state carries plain values (e.g. an enum such as `WelcomeError`) that the UI module maps
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
}

// desktop-javafx
val navigationRouter: (route: Route, scope: ScreenScope) -> Node = { route, scope ->
    when (route) {
        Route.Welcome -> WelcomeScreen(scope)
        Route.AccountList -> AccountListScreen(scope)
    }
}
```

- Each route is a `@Serializable data object` (or `data class` if it needs args) nested in the
  `Route` sealed interface. `Route` is a plain Kotlin type with no UI-toolkit dependencies.
- Map each route to its screen in `navigationRouter`'s exhaustive `when`, so adding a `Route` fails to compile
  until it's mapped. A route without a screen yet uses `placeholderScreen(route)`.
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
// back:
navigator.navigate { removeLast() }
```

Reading `navigator.backStack.value` is fine, but prefer `currentRoute` when you only need the top route.

Top-level destinations are listed in the `TopLevelDestination` enum
(`presentation/.../presentation/rail/TopLevelDestination.kt`) and shown, with their icons and labels, in
`desktop-javafx/.../desktop/rail/AppNavigationRail.kt`. `AppNavigationRailViewModel` selects the top-most
top-level destination in the back stack (so a screen pushed on top, like `NewAccount` over `AccountList`, keeps
its parent selected) and switches destinations by replacing the stack (`clear(); add(route)`), not pushing onto
it, so peers don't build up back history. To add a top-level screen, add a `Route`, map it in
`navigationRouter`, and add a `TopLevelDestination` entry with its icon and label.

`NavigationHost` (`desktop-javafx/.../desktop/navigation/NavigationHost.kt`) collects `navigator.backStack`,
builds each entry's node with `navigationRouter`, and gives each entry a `ScreenScope`
(`navigation/ScreenScope.kt`). `ScreenScope` holds that entry's `ViewModelStore`: `scope.viewModel<XViewModel>()`
resolves the ViewModel from Koin, and `scope.collect(viewModel.state) { ... }` updates nodes on the FX thread.
The scope is closed (clearing its ViewModels and cancelling its collectors) when the entry is popped, so
navigating to the screen again starts with a fresh ViewModel. `kotlinx-coroutines-javafx` supplies
`Dispatchers.Main` for `viewModelScope`.

`MoneyApplication` handles only the stage (title, scene size, stylesheet, saved window bounds); the scene's root
is `AppScreen` (`desktop-javafx/.../desktop/AppScreen.kt`), which holds the `AppViewModel` and the
`NavigationHost`, and shows `AppToolbar` and `AppNavigationRail` beside it while a repository is open.
`MoneyApplication` creates `AppScreen` with an app-lifetime `ScreenScope` in `start`, and closes both in `stop`.

App-wide reactive navigation lives in `AppViewModel` (`presentation/.../presentation/AppViewModel.kt`): in
`init` it collects `MoneyRepositoryController.moneyRepository` and does `navigator.navigate { clear(); add(...) }`
to `AccountList`/`Welcome` depending on whether a repository is open. It also exposes
`state: StateFlow<AppUiState>` (`isRepositoryConnected`). Put new app-lifetime reactive navigation in
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
(`presentation/.../presentation/database/MoneyRepositoryController.kt`), which tracks the open database, and
supplies the open repository:

- to write, it reads `repositoryController.moneyRepository.value` and calls the use case like a
  function (`createAccount(repository, input)`), treating no open repository as a failure;
- to read, it uses `repositoryController.flow { getAccountGroups(it, grouping) }`, which follows the
  open repository and emits nothing while none is open.

It then maps the result to UI state. For example, each `Invalid` error sets that field's `isError` and
its supporting text. In tests, call the use case directly in `core` with a mock repository, and mock it
with `coEvery { createAccount(any(), any()) }` in ViewModel tests, connecting a repository through
`MoneyRepositoryControllerFixture`.
