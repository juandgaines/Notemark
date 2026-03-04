package com.juandgaines.notemark.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juandgaines.notemark.R
import com.juandgaines.notemark.auth.presentation.components.AuthFormLayout
import com.juandgaines.notemark.core.presentation.components.NotemarkButton
import com.juandgaines.notemark.core.presentation.components.NotemarkTextField
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreenRoot(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: () -> Unit,
    onLogIn: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RegisterEvent.RegisterSuccess -> onRegisterSuccess()
            is RegisterEvent.RegisterError -> {
                Toast.makeText(
                    context,
                    event.error.asString(context),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    RegisterScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RegisterAction.OnLogInClick -> onLogIn()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
) {
    AuthFormLayout(
        title = stringResource(R.string.create_account_title),
        subtitle = stringResource(R.string.create_account_subtitle),
    ) {
        NotemarkTextField(
            value = state.username,
            onValueChange = { onAction(RegisterAction.OnUsernameChanged(it)) },
            label = stringResource(R.string.username_label),
            error = if (state.isUsernameTouched) state.usernameError?.asString() else null,
            supportingText = stringResource(R.string.supporting_username),
            onFocusLost = { onAction(RegisterAction.OnUsernameFocusLost) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        NotemarkTextField(
            value = state.email,
            onValueChange = { onAction(RegisterAction.OnEmailChanged(it)) },
            label = stringResource(R.string.email_label),
            error = if (state.isEmailTouched) state.emailError?.asString() else null,
            onFocusLost = { onAction(RegisterAction.OnEmailFocusLost) },
            keyboardType = KeyboardType.Email,
        )
        Spacer(modifier = Modifier.height(16.dp))
        NotemarkTextField(
            value = state.password,
            onValueChange = { onAction(RegisterAction.OnPasswordChanged(it)) },
            label = stringResource(R.string.password_label),
            error = if (state.isPasswordTouched) state.passwordError?.asString() else null,
            supportingText = stringResource(R.string.supporting_password),
            isPassword = true,
            isPasswordVisible = state.isPasswordVisible,
            onTogglePasswordVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) },
            onFocusLost = { onAction(RegisterAction.OnPasswordFocusLost) },
            keyboardType = KeyboardType.Password,
        )
        Spacer(modifier = Modifier.height(16.dp))
        NotemarkTextField(
            value = state.repeatPassword,
            onValueChange = { onAction(RegisterAction.OnRepeatPasswordChanged(it)) },
            label = stringResource(R.string.repeat_password_label),
            error = if (state.isRepeatPasswordTouched) state.repeatPasswordError?.asString() else null,
            isPassword = true,
            isPasswordVisible = state.isRepeatPasswordVisible,
            onTogglePasswordVisibility = { onAction(RegisterAction.OnToggleRepeatPasswordVisibility) },
            onFocusLost = { onAction(RegisterAction.OnRepeatPasswordFocusLost) },
            keyboardType = KeyboardType.Password,
        )
        Spacer(modifier = Modifier.height(24.dp))
        NotemarkButton(
            text = stringResource(R.string.create_account),
            onClick = { onAction(RegisterAction.OnRegisterClick) },
            isLoading = state.isLoading,
            enabled = state.canRegister,
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = { onAction(RegisterAction.OnLogInClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.already_have_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
