# Compose Tips (Android)

Preferred patterns and conventions for Jetpack Compose UI code.

## TextFields — Prefer `TextFieldState`

Use `TextFieldState` (Compose Foundation) instead of `value`/`onValueChange`. It lives in the ViewModel state, handles input internally, and enables `snapshotFlow` for reactive validation.

### Reusable TextField Component

Design custom text fields wrapping `BasicTextField` with `TextFieldState`:

```kotlin
@Composable
fun AppTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    title: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    Column(modifier = modifier) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        val interactionSource = remember { MutableInteractionSource() }

        BasicTextField(
            state = state,
            enabled = enabled,
            lineLimits = if (singleLine) TextFieldLineLimits.SingleLine
                         else TextFieldLineLimits.Default,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            decorator = { innerBox ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (state.text.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    innerBox()
                }
            },
        )

        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```

Adapt this skeleton to your design system — add borders, backgrounds, padding, icons as needed. The key is that `TextFieldState` manages the text internally.

### TextFieldState in ViewModel State

```kotlin
data class RegisterState(
    val usernameTextState: TextFieldState = TextFieldState(),
    val emailTextState: TextFieldState = TextFieldState(),
    val passwordTextState: TextFieldState = TextFieldState(),
    val isRegistering: Boolean = false,
    val isUsernameValid: Boolean = false,
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
)
```

### Reactive Validation with snapshotFlow

Use `snapshotFlow` to observe `TextFieldState.text` changes and validate reactively:

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), RegisterState())

    private fun observeValidationStates() {
        // Username: 3-20 characters
        snapshotFlow { _state.value.usernameTextState.text.toString() }
            .map { it.length in 3..20 }
            .distinctUntilChanged()
            .onEach { isValid -> _state.update { it.copy(isUsernameValid = isValid) } }
            .launchIn(viewModelScope)

        // Email: basic pattern check
        snapshotFlow { _state.value.emailTextState.text.toString() }
            .map { it.contains("@") && it.contains(".") }
            .distinctUntilChanged()
            .onEach { isValid -> _state.update { it.copy(isEmailValid = isValid) } }
            .launchIn(viewModelScope)

        // Password: minimum 8 chars
        snapshotFlow { _state.value.passwordTextState.text.toString() }
            .map { it.length >= 8 }
            .distinctUntilChanged()
            .onEach { isValid -> _state.update { it.copy(isPasswordValid = isValid) } }
            .launchIn(viewModelScope)
    }
}
```

Key points:
- `snapshotFlow` bridges Compose snapshot state into coroutine Flow
- `distinctUntilChanged()` avoids redundant state updates
- Validation logic stays in the ViewModel, not in composables

## Password TextField Variant

For password fields, add visibility toggle and `SecureTextField` or `visualTransformation`:

```kotlin
@Composable
fun AppPasswordTextField(
    state: TextFieldState,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    title: String? = null,
    isError: Boolean = false,
) {
    // Same pattern as AppTextField but with:
    // - obscureText = !isPasswordVisible in BasicSecureTextField
    //   OR use BasicTextField with visualTransformation
    // - Trailing icon button for visibility toggle
}
```
