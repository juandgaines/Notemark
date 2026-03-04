package com.juandgaines.notemark.auth.presentation.login

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
fun LoginScreenRoot(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUp: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.LoginSuccess -> onLoginSuccess()
            is LoginEvent.LoginError -> {
                Toast.makeText(
                    context,
                    event.error.asString(context),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when (action) {
                LoginAction.OnSignUpClick -> onSignUp()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    AuthFormLayout(
        title = stringResource(R.string.log_in_title),
        subtitle = stringResource(R.string.log_in_subtitle),
    ) {
        NotemarkTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.OnEmailChanged(it)) },
            label = stringResource(R.string.email_label),
            keyboardType = KeyboardType.Email,
        )
        Spacer(modifier = Modifier.height(16.dp))
        NotemarkTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.OnPasswordChanged(it)) },
            label = stringResource(R.string.password_label),
            isPassword = true,
            isPasswordVisible = state.isPasswordVisible,
            onTogglePasswordVisibility = { onAction(LoginAction.OnTogglePasswordVisibility) },
            keyboardType = KeyboardType.Password,
        )
        Spacer(modifier = Modifier.height(24.dp))
        NotemarkButton(
            text = stringResource(R.string.log_in),
            onClick = { onAction(LoginAction.OnLoginClick) },
            isLoading = state.isLoading,
            enabled = state.canLogin,
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = { onAction(LoginAction.OnSignUpClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.dont_have_account),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
