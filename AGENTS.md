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

The back stack is owned by the `Navigator` singleton
(`desktop/src/main/kotlin/com/munzenberger/money/desktop/navigation/Navigator.kt`, registered with
`single { Navigator() }` in `AppModule`). It holds the `NavBackStack<Route>` (starting at
`Route.Welcome`) and exposes `currentRoute: StateFlow<Route?>` (the top of the stack). ViewModels take
`Navigator` as a constructor dependency and navigate through `navigate { ... }`, which applies the
change to the back stack immediately and updates `currentRoute` — don't mutate `navigator.backStack`
directly:

```kotlin
navigator.navigate { add(Route.AccountList) }
// or, to replace the stack rather than push onto it:
navigator.navigate { clear(); add(Route.AccountList) }
// back:
navigator.navigate { removeLast() }
```

Reading `navigator.backStack` is fine, but prefer `currentRoute` when you only need the top route.

Top-level destinations are listed in the `TopLevelDestination` enum
(`desktop/.../desktop/rail/TopLevelDestination.kt`: route, icon, label) and shown in the Material 3
Expressive `WideNavigationRail` in `rail/AppNavigationRail.kt`, which the user can collapse or expand.
`AppNavigationRailViewModel` selects the top-most top-level destination in the back stack (so a
screen pushed on top, like `NewAccount` over `AccountList`, keeps its parent selected) and switches destinations by
replacing the stack (`clear(); add(route)`), not pushing onto it, so peers don't build up back history. To
add a top-level screen, add a `Route`, its `entry`, and a `TopLevelDestination` entry.

`App.kt` (the root composable) injects the `Navigator` with `koinInject()` and hands
`navigator.backStack` and `navigationRouter` to the single `NavDisplay`; it doesn't apply navigation
itself. `NavDisplay` is given `rememberSaveableStateHolderNavEntryDecorator()` and
`rememberViewModelStoreNavEntryDecorator()` (in that order), so each back stack entry has its own
`ViewModelStoreOwner`: a screen's `koinViewModel()` is scoped to its entry and cleared when the entry is
popped, and navigating to the screen again starts with a fresh ViewModel. App-wide reactive navigation lives in `AppViewModel` (`desktop/.../desktop/AppViewModel.kt`):
in `init` it collects `MoneyRepositoryController.moneyRepository` and does
`navigator.navigate { clear(); add(...) }` to `AccountList`/`Welcome` depending on whether a
repository is open. It also exposes `state: StateFlow<AppUiState>` (`isRepositoryConnected`), which
`App.kt` uses to decide whether to show `AppNavigationRail` beside the `NavDisplay`. Put new app-lifetime reactive navigation in
`AppViewModel`.

## ViewModels

Each screen that needs one has its own `ViewModel`, defined alongside the screen composable in the
same package, e.g. `welcome/WelcomeViewModel.kt`, `accounts/AccountListViewModel.kt`. It extends
`androidx.lifecycle.ViewModel` and takes any dependencies (singletons like `MoneyRepositoryController`)
as constructor params:

```kotlin
class WelcomeViewModel(
    private val repositoryController: MoneyRepositoryController
) : ViewModel() { /* ... */ }
```

Non-screen UI (e.g. `rail/AppNavigationRailViewModel.kt`) and the root
`AppViewModel` follow the same pattern.

Register it in `desktop/src/main/kotlin/com/munzenberger/money/desktop/inject/AppModule.kt` with Koin's
`viewModel { ... }` DSL, passing constructor deps via `get()`. Singletons are registered with
`single { ... }` in the same module:

```kotlin
val appModule = module {
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

- `PreviewThemed` (`com.munzenberger.money.shared.theme.PreviewThemed`) wraps the preview content in
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
(`desktop/.../desktop/components/ListScreenHeader.kt`), fixed above the list. The list goes in a
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

The Accounts, Categories, and Payees screens all follow this pattern.

## Scrolling

Every scrollable area shows a scrollbar. Instead of a bare `LazyColumn` or
`Column(Modifier.verticalScroll(...))`, use `ScrollableLazyColumn` or `ScrollableColumn`
(`desktop/.../desktop/components/Scrollables.kt`). They overlay a theme-tinted `VerticalScrollbar` on the
end edge. Pass the padding to `ScrollableColumn`'s `contentPadding` so the scrollbar sits in the end
gutter instead of over the content.

## Text fields

Use `DesktopOutlinedTextField` (`desktop/.../desktop/components/DesktopOutlinedTextField.kt`) instead of
Material's `OutlinedTextField`. It takes the same parameters, but is sized for mouse and keyboard: 40dp tall
instead of 56dp, with tighter content padding and 40dp icon targets (see
`DesktopOutlinedTextFieldDefaults`). It works as the anchor of an `ExposedDropdownMenuBox` too.

For the options in those menus, use `DesktopDropdownMenuItem` (`desktop/.../desktop/components/DesktopDropdownMenuItem.kt`)
instead of `DropdownMenuItem`. Its rows are 32dp tall instead of 48dp, its text is a single line that ends
with an ellipsis, and its padding lines the option text up with the field's text, so don't pass
`ExposedDropdownMenuDefaults.ItemContentPadding`.

## Form sections

Group related fields of a form in a `FormSectionCard`
(`desktop/.../desktop/components/FormSectionCard.kt`): an outlined card on `surfaceContainerLowest` with a `titleMedium` heading. Its
content is a `Column` that spaces the fields `MoneyTheme.spacing.small` apart:

```kotlin
FormSectionCard(title = stringResource(Res.string.account_section_title)) {
    NameField(name = state.name, onNameChange = onNameChange)
    AccountTypeField(/* ... */)
}
```

## Detail screens

Screens pushed on top of another screen (e.g. `NewAccount` over `AccountList`) start with
`DetailScreenHeader` (`desktop/.../desktop/components/DetailScreenHeader.kt`): a back button followed by
the title, with an optional `actions` slot at the end for buttons like Save. Its back arrow lines up with
`ListScreenHeader`'s title. Wire `onBackClick` to a ViewModel
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
