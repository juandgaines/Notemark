# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build
./gradlew assembleDebug

# Run tests
./gradlew test                                                                          # All unit tests
./gradlew testDebugUnitTest --tests "com.juandgaines.notemark.ExampleUnitTest"           # Single test class
./gradlew connectedAndroidTest                                                          # Instrumented tests (requires device/emulator)

# Lint
./gradlew lint

# Clean
./gradlew clean assembleDebug
```

## Architecture

Notemark is an Android app built with **Kotlin + Jetpack Compose + Material Design 3**.

Single module (`:app`) with package-based organization:

```
app/src/main/java/com/juandgaines/notemark/
  ui/theme/          → Material3 theme, colors, typography
  MainActivity.kt    → App entry point
```

### Key Patterns

- **DI:** Koin — register dependencies in `di/` package with `*Module.kt` files. Initialize in the `Application` class.
- **Screen pattern (MVVM):** Each screen has `*Screen.kt`, `*ViewModel.kt`, `*State.kt`, `*Event.kt` (one-time UI events), `*Action.kt` (user actions). ViewModels use `StateFlow` for state and `Channel` for events.
- **Navigation:** Jetpack Compose Navigation with typed `@Serializable` route objects.

## Feature Development Guide

### Creating a New Screen

Each screen consists of 5 files, plus DI registration:

```
<feature_package>/
  my_screen/
    MyScreenAction.kt     ← User interactions (sealed interface)
    MyScreenEvent.kt      ← One-time UI events like navigation (sealed interface)
    MyScreenState.kt      ← Immutable UI state (data class with defaults)
    MyScreenViewModel.kt  ← State management + business logic
    MyScreen.kt           ← Two composables: MyScreenRoot + MyScreen
```

**Screen composable pattern** — always split into Root (stateful) and Screen (stateless):

```kotlin
@Composable
fun MyScreenRoot(
    viewModel: MyScreenViewModel = koinViewModel(),
    onNavigateAway: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MyScreenEvent.Success -> onNavigateAway()
        }
    }
    MyScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun MyScreen(state: MyScreenState, onAction: (MyScreenAction) -> Unit) { ... }
```

**ViewModel pattern:**

```kotlin
class MyScreenViewModel(
    private val someRepository: SomeRepository   // Constructor-injected via Koin
) : ViewModel() {

    private val eventChannel = Channel<MyScreenEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(MyScreenState())
    val state = _state
        .onStart { /* load initial data */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MyScreenState())

    fun onAction(action: MyScreenAction) {
        when (action) {
            MyScreenAction.OnSomeClick -> doSomething()
            else -> Unit  // Navigation actions handled in Root
        }
    }
}
```

**Action vs Event:**
- `Action` = user input flowing **into** the ViewModel (button clicks, text input). Some actions (navigation-only) are handled directly in the Root composable with `else -> Unit` in the ViewModel.
- `Event` = one-time signals flowing **out** of the ViewModel to the UI (navigate, show snackbar). Sent via `Channel` and collected with `ObserveAsEvents`.

### Error Handling with Result Type

All domain/data operations return `Result<T, E>` instead of throwing exceptions:

```kotlin
sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Failure<out E: Error>(val error: E): Result<Nothing, E>
}
typealias EmptyResult<E> = Result<Unit, E>
```

**DataError** categorizes all errors:
- `DataError.Remote` — HTTP errors (BAD_REQUEST, UNAUTHORIZED, NO_INTERNET, etc.)
- `DataError.Local` — Database errors (DISK_FULL, NOT_FOUND)

### UiText — Displaying User-Facing Strings

`UiText` wraps both dynamic strings and localized resources:

```kotlin
UiText.Resource(R.string.error_no_internet)
UiText.Resource(R.string.greeting, arrayOf(username))
UiText.DynamicString("Something went wrong")

// In a composable:
text.asString()
```

**Converting DataError to UiText:** Use `DataError.toUiText()` extension. Every `DataError` variant has a mapped string resource.

## Build Configuration

- **Compile SDK:** 36 | **Min SDK:** 29 | **Target SDK:** 36
- **Java compatibility:** 11
- **Gradle:** 9.2.1 with Kotlin DSL
- **AGP:** 9.0.1 | **Kotlin:** 2.0.21
- **Dependency versions:** centralized in `gradle/libs.versions.toml`

## Implementation Patterns

Detailed implementation patterns are in `docs/patterns/`. **Read the relevant pattern doc before implementing** — don't guess conventions:

| When you are... | Read first |
|-----------------|------------|
| Implementing a ViewModel | `docs/patterns/viewmodel-tips.md` |
| Building Compose UI (especially TextFields) | `docs/patterns/compose-tips.md` |
| Adding/modifying navigation routes or graphs | `docs/patterns/navigation.md` |
| Writing a repository with local+remote sync | `docs/patterns/offline-first.md` |
| Setting up Koin modules or DI wiring | `docs/patterns/koin-setup.md` |
| Working with encrypted session/token storage | `docs/patterns/encrypted-storage.md` |
| Implementing token refresh or auth flows | `docs/patterns/jwt-refresh.md` |
| Handling responsive layouts | `docs/patterns/device-configuration.md` |

Other patterns:
- `splash.md` — Android 12+ Splash API with auth check
- `koin-setup.md` — Koin DI module organization and wiring
- `jwt-refresh.md` — Ktor bearer token refresh, session expiration handling
- `typography.md` — Font resources and M3 type scale (if present)
## Design & Spec References

### For planning and requirements:
- Read `milestones/milestone-{N}/requirements.md` — pre-extracted text from PDF specs (token-efficient)
- **NEVER read PDF files if `requirements.md` exists** — PDFs cost ~5x more tokens and waste context window
- If no `requirements.md` exists, read the PDF directly with the Read tool as a last resort (use `pages` parameter for large PDFs)

### For UI implementation (priority order):
1. **Figma MCP** — use for precise design tokens, spacing, colors (rate-limited: 6/month free, 10-20/min paid)
2. **Exported designs** — `milestones/milestone-{N}/designs/{phone,tablet}/*.png` — prefer over Figma MCP to save calls
3. If neither is available, ask the user for design references before guessing

### Milestone folders:
- `milestones/milestone-1/`
- `milestones/milestone-2/`
- `milestones/milestone-3/`
- `milestones/milestone-4/`
