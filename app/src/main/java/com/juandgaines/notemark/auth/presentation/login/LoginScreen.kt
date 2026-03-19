package com.juandgaines.notemark.auth.presentation.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.components.NoteMarkButton
import com.juandgaines.notemark.core.presentation.components.NoteMarkTextButton
import com.juandgaines.notemark.core.presentation.components.NoteMarkTextField
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.ObserveAsEvents
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration
import com.juandgaines.notemark.ui.theme.Inter
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.Primary
import com.juandgaines.notemark.ui.theme.SpaceGrotesk
import com.juandgaines.notemark.ui.theme.SurfaceLowest
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreenRoot(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.LoginSuccess -> onLoginSuccess()
            is LoginEvent.LoginError -> {
                Toast.makeText(
                    context,
                    event.error.asString(context),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when (action) {
                LoginAction.OnRegisterClick -> onRegisterClick()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        DeviceConfiguration.MOBILE_LANDSCAPE -> LoginLandscape(state, onAction)
        else -> LoginPortrait(state, onAction)
    }
}

@Composable
private fun LoginPortrait(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 60.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceLowest)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            LoginHeader()
            Spacer(modifier = Modifier.height(40.dp))
            LoginForm(state, onAction)
        }
    }
}

@Composable
private fun LoginLandscape(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceLowest)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
            ) {
                LoginHeader()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                LoginForm(state, onAction)
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Text(
        text = stringResource(R.string.log_in),
        style = TextStyle(
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.32.sp,
            color = OnSurface,
        ),
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.dont_have_account),
        style = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            color = OnSurfaceVariant,
        ),
    )
}

@Composable
private fun LoginForm(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    NoteMarkTextField(
        value = state.email,
        onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
        label = stringResource(R.string.email),
        placeholder = stringResource(R.string.email_placeholder),
        errorText = stringResource(R.string.invalid_email),
        isValid = state.isEmailValid,
        hasFocusedOnce = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
    Spacer(modifier = Modifier.height(16.dp))
    NoteMarkTextField(
        value = state.password,
        onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
        label = stringResource(R.string.password),
        placeholder = stringResource(R.string.password_placeholder),
        isPassword = true,
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(LoginAction.OnTogglePasswordVisibility) },
        isValid = true,
        hasFocusedOnce = false,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    Spacer(modifier = Modifier.height(24.dp))
    NoteMarkButton(
        text = stringResource(R.string.log_in),
        onClick = { onAction(LoginAction.OnLoginClick) },
        enabled = state.canLogin,
        isLoading = state.isLoggingIn,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.dont_have_account),
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = OnSurfaceVariant,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        NoteMarkTextButton(
            text = stringResource(R.string.create_account),
            onClick = { onAction(LoginAction.OnRegisterClick) },
        )
    }
}
