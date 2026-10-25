# AGENTS.md

Conventions for working in this repo. This is a Kotlin JVM project with a Compose Desktop app module
(`desktop-material`). The Material theme (`MoneyTheme`, color scheme, typography, shapes, spacing) and all
Material dependencies live in `desktop-material` (`desktop-material/.../desktop/theme/`), so `presentation` stays free of
Material.

The UI-independent presentation layer lives in `presentation`: ViewModels and their UI states,
`Navigator` and the `Route` keys, `TopLevelDestination`, `MoneyRepositoryController`, the Koin
`PresentationModule`, and their tests. `desktop-material` holds the Compose UI (screens, components, `App.kt`,
`Main.kt`, `navigationRouter`), its string resources and composable helpers such as the database file
pickers, and depends on `presentation`, so another UI module can reuse the same presentation layer. Both modules group code by the same feature subpackages:
`com.munzenberger.money.presentation.<feature>` in `presentation` and
`com.munzenberger.money.desktop.<feature>` in `desktop-material`. Keep Material, Compose Resources, composables
and other UI-toolkit types out of `presentation`: it has no Compose or Navigation3 dependencies, only the plain
`lifecycle-viewmodel` and `koin-core-viewmodel`, and UI modules declare their own Compose, Navigation3 and
Koin-Compose dependencies. UI state carries plain values (e.g. an enum such as
`WelcomeError`) that the UI module maps to its own strings, rather than string resources.

## Navigation (Navigation3)

Routes are defined in `presentation/src/main/kotlin/com/munzenberger/money/presentation/navigation/Route.kt`,
and wired to screens in `desktop-material/src/main/kotlin/com/munzenberger/money/desktop/navigation/NavigationRouter.kt`:

```kotlin
@Serializable
sealed interface Route {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object AccountList : Route
}

// desktop-material
val navigationRouter = entryProvider<Route> {
    entry<Route.Welcome> { WelcomeScreen() }
    entry<Route.AccountList> { AccountListScreen() }
}
```

- Each route is a `@Serializable data object` (or `data class` if it needs args) nested in the
  `Route` sealed interface. `Route` is a plain Kotlin type, not a Navigation3 `NavKey`, so `presentation`
  doesn't depend on Navigation3 types; `NavDisplay` accepts any key type.
- Wire each route to its composable with `entry<Route.X> { XScreen() }` inside `navigationRouter`.
- Screen composables live in their own subpackage under
  `desktop-material/src/main/kotlin/com/munzenberger/money/desktop/<screenName>/`, e.g. `welcome/WelcomeScreen.kt`,
  `accounts/AccountListScreen.kt`.

The back stack is owned by the `Navigator` singleton
(`presentation/src/main/kotlin/com/munzenberger/money/presentation/navigation/Navigator.kt`, registered with
`single { Navigator() }` in `PresentationModule`). It exposes the back stack as a plain
`backStack: StateFlow<List<Route>>` (starting at `Route.Welcome`), with no Navigation3 or Compose types, and
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
(`presentation/.../presentation/rail/TopLevelDestination.kt`: route; its icon and label are mapped in
`desktop-material/.../desktop/rail/TopLevelDestinationIcons.kt` and `TopLevelDestinationLabels.kt`) and shown in the Material 3
Expressive `WideNavigationRail` in `rail/AppNavigationRail.kt`, which the user can collapse or expand.
`AppNavigationRailViewModel` selects the top-most top-level destination in the back stack (so a
screen pushed on top, like `NewAccount` over `AccountList`, keeps its parent selected) and switches destinations by
replacing the stack (`clear(); add(route)`), not pushing onto it, so peers don't build up back history. To
add a top-level screen, add a `Route`, its `entry`, a `TopLevelDestination` entry, and its icon and label.

`App.kt` (the root composable) injects the `Navigator` with `koinInject()`, collects
`navigator.backStack` with `collectAsState()`, and hands it and `navigationRouter` to the single `NavDisplay`.
Since that list is read-only, `NavDisplay`'s `onBack` pops through `navigator.navigate { ... }` (never
below the last route); otherwise `App.kt` doesn't apply navigation itself. `NavDisplay` is given `rememberSaveableStateHolderNavEntryDecorator()` and
`rememberViewModelStoreNavEntryDecorator()` (in that order), so each back stack entry has its own
`ViewModelStoreOwner`: a screen's `koinViewModel()` is scoped to its entry and cleared when the entry is
popped, and navigating to the screen again starts with a fresh ViewModel. App-wide reactive navigation lives in `AppViewModel` (`presentation/.../presentation/AppViewModel.kt`):
in `init` it collects `MoneyRepositoryController.moneyRepository` and does
`navigator.navigate { clear(); add(...) }` to `AccountList`/`Welcome` depending on whether a
repository is open. It also exposes `state: StateFlow<AppUiState>` (`isRepositoryConnected`), which
`App.kt` uses to decide whether to show `AppNavigationRail` beside the `NavDisplay`. Put new app-lifetime reactive navigation in
`AppViewModel`.

`desktop-javafx` uses the same `Navigator` and ViewModels without Navigation3. `NavigationHost`
(`desktop-javafx/.../desktop/navigation/NavigationHost.kt`) is its `NavDisplay`: it collects `navigator.backStack`,
builds each entry's node with its router, and gives each entry a `ScreenScope`. `ScreenScope` holds that entry's
`ViewModelStore`: `scope.viewModel<XViewModel>()` resolves the ViewModel from Koin, and
`scope.collect(viewModel.state) { ... }` updates nodes on the FX thread. The scope is closed (clearing its
ViewModels) when the entry is popped. `kotlinx-coroutines-javafx` supplies `Dispatchers.Main` for `viewModelScope`.

## ViewModels

Each screen that needs one has its own `ViewModel`, defined in `presentation` under the same feature subpackage as
the screen composable, e.g. `welcome/WelcomeViewModel.kt`, `accounts/AccountListViewModel.kt`. It extends
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

The public screen composable takes the ViewModel as a default parameter injected with `koinViewModel()`,
collects its state, and immediately delegates to the stateless `XScreenContent` composable (see
Compose previews below) — the ViewModel itself never leaks past the top-level `XScreen` function:

```kotlin
@Composable
fun AccountListScreen(viewModel: AccountListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState(initial = AccountListUiState.Loading)

    AccountListScreenContent(state = state)
}
```

Requires `org.koin.compose.viewmodel.koinViewModel` import. A screen with no state/behavior yet can
still take an empty `ViewModel` — this keeps the wiring in place so behavior can be added to the
ViewModel later without changing the composable's shape.

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

- `PreviewThemed` (`com.munzenberger.money.desktop.theme.PreviewThemed`) wraps the preview content in
  the app theme, on a full-size box filled with the theme's background color.
- The preview function is `private`, named `<Screen>Preview`, and placed at the bottom of the same
  file as the screen.
- If the real screen composable takes a Koin-injected ViewModel (e.g.
  `fun WelcomeScreen(viewModel: WelcomeViewModel = koinViewModel())`), extract a stateless
  `private fun XScreenContent(...)` composable that takes plain params/lambdas, and preview that
  instead of the ViewModel-backed entry point.
- If the screen has no ViewModel/params, the preview can call it directly.

## List screens

List screens put their title and primary action (e.g. "New account") in `ListScreenHeader`
(`desktop-material/.../desktop/components/ListScreenHeader.kt`), fixed above the list. The list goes in a
`weight(1f)` container below it, so only the list scrolls and the action stays visible:

```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    ListScreenHeader(
        title = stringResource(Res.string.account_list_title),
        actionLabel = stringResource(Res.string.add_account_button_title),
        onActionClick = onAddAccountClick,
    )

    Box(modifier = Modifier.weight(1f)) {
        AccountListBody(state = state)
    }
}
```

The Categories and Payees screens follow this pattern. A screen with other controls above its list, like
the Accounts screen's "Group by" dropdown, can instead use the title-only `ListScreenHeader(title)` and
put the action in a row with those controls, using `ListScreenActionButton` (from the same file) so it
looks the same.

## Scrolling

Every scrollable area shows a scrollbar. Instead of a bare `LazyColumn` or
`Column(Modifier.verticalScroll(...))`, use `ScrollableLazyColumn` or `ScrollableColumn`
(`desktop-material/.../desktop/components/Scrollables.kt`). They overlay a theme-tinted `VerticalScrollbar` on the
end edge. Pass the padding to `ScrollableColumn`'s `contentPadding` so the scrollbar sits in the end
gutter instead of over the content.

## Text fields

Use Material's `OutlinedTextField`, which also works as the anchor of an `ExposedDropdownMenuBox`. For
the options in those menus, use Material's `DropdownMenuItem` with
`contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding`, so the option text lines up with the
field's text.

## Form sections

Group related fields of a form in a `FormSectionCard`
(`desktop-material/.../desktop/components/FormSectionCard.kt`): an outlined card on `surfaceContainerLowest` with a `titleMedium` heading. Its
content is a `Column` that spaces the fields `MoneyTheme.spacing.small` apart:

```kotlin
FormSectionCard(title = stringResource(Res.string.account_section_title)) {
    NameField(name = state.name, onNameChange = onNameChange)
    AccountTypeField(/* ... */)
}
```

## Detail screens

Screens pushed on top of another screen (e.g. `NewAccount` over `AccountList`) start with
`DetailScreenHeader` (`desktop-material/.../desktop/components/DetailScreenHeader.kt`): a back button followed by
the title. Its back arrow lines up with `ListScreenHeader`'s title. Wire `onBackClick` to a ViewModel
function that does `navigator.navigate { removeLast() }`:

```kotlin
Column(modifier = Modifier.fillMaxSize()) {
    DetailScreenHeader(
        title = stringResource(Res.string.new_account_title),
        onBackClick = onBackClick,
    )

    // screen content
}
```

## String resources

Strings live in `desktop-material/src/main/composeResources/values/strings.xml` (standard Android-style
`strings.xml`, one module-wide file — not per-screen):

```xml
<string name="account_list_title">Accounts</string>
```

- Naming: snake_case, often suffixed by role (`_button_title`, `_dialog_title`), or a plain
  descriptive name when not tied to a specific widget (e.g. `app_title`, `default_file_name`).
- No `translatable`/`formatted` attributes or comments are currently used — keep entries plain.
- Usage in Compose code:

  ```kotlin
  import money.desktop_material.generated.resources.Res
  import money.desktop_material.generated.resources.account_list_title
  import org.jetbrains.compose.resources.stringResource

  Text(text = stringResource(Res.string.account_list_title))
  ```

- The `Res.string.<name>` accessor is generated by the Compose Resources Gradle plugin from
  `strings.xml` — no manual codegen step needed, but a build may be required for the IDE/compiler to
  pick up a newly added entry.

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
