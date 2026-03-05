# ViewModel Tips (Android)

Preferred patterns for ViewModel state management, initial data loading, SavedStateHandle, and navigation argument extraction.

## State Loading with onStart

### With initial data loading or validation setup

Use `onStart` to load data or set up observers exactly once:

```kotlin
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeValidationStates()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterState()
        )
}
```

The `hasLoadedInitialData` guard prevents re-running setup when the subscriber reattaches (e.g., after configuration change with `WhileSubscribed`).

### Simple state — no initial loading needed

When the screen has no initial data to load and no complex validation:

```kotlin
class ForgotPasswordViewModel : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()
}
```

No `onStart`, no `stateIn` — just expose the flow directly.

### Choosing the right pattern

| Scenario | Pattern |
|----------|---------|
| Need to load data from repository on screen open | `onStart` + `stateIn` |
| Need to set up reactive validation flows | `onStart` + `stateIn` |
| Simple form with no initial data | `asStateFlow()` |
| Need to observe a Room/DB Flow continuously | `onStart` + `stateIn` |

## SavedStateHandle — Process Death Survival

**Only use for critical paths** where losing state on process death is unacceptable (e.g., login form mid-typing, checkout flow). Don't apply to every ViewModel by default.

```kotlin
class LoginViewModel(
    private val repository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LoginState(
            email = TextFieldState(
                initialText = savedStateHandle[KEY_EMAIL] ?: ""
            ),
            password = TextFieldState(
                initialText = savedStateHandle[KEY_PASSWORD] ?: ""
            ),
            isPasswordVisible = savedStateHandle[KEY_PASSWORD_VISIBLE] ?: false,
        )
    )
    val state = _state.asStateFlow()

    // Persist text changes to SavedStateHandle
    init {
        snapshotFlow { _state.value.email.text.toString() }
            .onEach { savedStateHandle[KEY_EMAIL] = it }
            .launchIn(viewModelScope)

        snapshotFlow { _state.value.password.text.toString() }
            .onEach { savedStateHandle[KEY_PASSWORD] = it }
            .launchIn(viewModelScope)
    }

    private fun togglePasswordVisibility() {
        val newVisibility = !_state.value.isPasswordVisible
        savedStateHandle[KEY_PASSWORD_VISIBLE] = newVisibility
        _state.update { it.copy(isPasswordVisible = newVisibility) }
    }

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_PASSWORD_VISIBLE = "passwordVisible"
    }
}
```

### When to use SavedStateHandle

| Use it | Don't use it |
|--------|-------------|
| Login/register forms | Read-only detail screens |
| Checkout/payment flow | Lists that reload from DB |
| Multi-step wizards | Screens with no user input |

## Navigation Argument Extraction

When a route has arguments, extract them in the ViewModel via `SavedStateHandle`. The keys match the property names of the `@Serializable` route data class.

### Route definition

```kotlin
@Serializable
data class NoteDetailRoute(
    val noteId: String,
    val isNewNote: Boolean = false,
)
```

### ViewModel extraction

```kotlin
class NoteDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val noteId = requireNotNull(savedStateHandle.get<String>("noteId")) {
        "noteId argument is required for NoteDetailViewModel"
    }
    private val isNewNote = savedStateHandle.get<Boolean>("isNewNote") ?: false

    private val _state = MutableStateFlow(NoteDetailState())
    val state = _state
        .onStart {
            if (!isNewNote) {
                loadNote(noteId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), NoteDetailState())
}
```

### Key conventions

- Use `requireNotNull` for mandatory arguments — fail fast with a clear message
- Use `?: defaultValue` for optional arguments
- Property names in `savedStateHandle.get<Type>("key")` must exactly match the `@Serializable` data class property names
- Extract arguments as `val` properties at the top of the ViewModel — don't access `savedStateHandle` throughout the class
