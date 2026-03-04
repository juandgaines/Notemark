package com.juandgaines.notemark.auth.presentation.register

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
fun RegisterScreenRoot(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RegisterEvent.RegisterSuccess -> onRegisterSuccess()
            is RegisterEvent.RegisterError -> {
                Toast.makeText(
                    context,
                    event.error.asString(context),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    RegisterScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RegisterAction.OnLoginClick -> onLoginClick()
                else -> viewModel.onAction(action)
            }
        },
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        DeviceConfiguration.MOBILE_LANDSCAPE -> RegisterLandscape(state, onAction)
        else -> RegisterPortrait(state, onAction)
    }
}

@Composable
private fun RegisterPortrait(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
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
            RegisterHeader()
            Spacer(modifier = Modifier.height(40.dp))
            RegisterForm(state, onAction)
        }
    }
}

@Composable
private fun RegisterLandscape(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
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
                RegisterHeader()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                RegisterForm(state, onAction)
            }
        }
    }
}

@Composable
private fun RegisterHeader() {
    Text(
        text = stringResource(R.string.create_account),
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
        text = stringResource(R.string.already_have_account),
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
private fun RegisterForm(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
) {
    NoteMarkTextField(
        value = state.username,
        onValueChange = { onAction(RegisterAction.OnUsernameChange(it)) },
        label = stringResource(R.string.username),
        placeholder = stringResource(R.string.username_placeholder),
        supportingText = stringResource(R.string.username_support),
        errorText = if (state.username.length < 3) {
            stringResource(R.string.username_error_min)
        } else {
            stringResource(R.string.username_error_max)
        },
        isValid = state.isUsernameValid,
        hasFocusedOnce = state.hasUsernameFocusedOnce,
        onFocusChanged = { onAction(RegisterAction.OnUsernameFocusChange(it)) },
    )
    Spacer(modifier = Modifier.height(16.dp))
    NoteMarkTextField(
        value = state.email,
        onValueChange = { onAction(RegisterAction.OnEmailChange(it)) },
        label = stringResource(R.string.email),
        placeholder = stringResource(R.string.email_placeholder),
        errorText = stringResource(R.string.invalid_email),
        isValid = state.isEmailValid,
        hasFocusedOnce = state.hasEmailFocusedOnce,
        onFocusChanged = { onAction(RegisterAction.OnEmailFocusChange(it)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
    Spacer(modifier = Modifier.height(16.dp))
    NoteMarkTextField(
        value = state.password,
        onValueChange = { onAction(RegisterAction.OnPasswordChange(it)) },
        label = stringResource(R.string.password),
        placeholder = stringResource(R.string.password_placeholder),
        isPassword = true,
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) },
        supportingText = stringResource(R.string.password_support),
        errorText = stringResource(R.string.password_error),
        isValid = state.isPasswordValid,
        hasFocusedOnce = state.hasPasswordFocusedOnce,
        onFocusChanged = { onAction(RegisterAction.OnPasswordFocusChange(it)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    Spacer(modifier = Modifier.height(16.dp))
    NoteMarkTextField(
        value = state.repeatPassword,
        onValueChange = { onAction(RegisterAction.OnRepeatPasswordChange(it)) },
        label = stringResource(R.string.repeat_password),
        placeholder = stringResource(R.string.password_placeholder),
        isPassword = true,
        isPasswordVisible = state.isRepeatPasswordVisible,
        onTogglePasswordVisibility = { onAction(RegisterAction.OnToggleRepeatPasswordVisibility) },
        errorText = stringResource(R.string.passwords_dont_match),
        isValid = state.passwordsMatch,
        hasFocusedOnce = state.hasRepeatPasswordFocusedOnce,
        onFocusChanged = { onAction(RegisterAction.OnRepeatPasswordFocusChange(it)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
    Spacer(modifier = Modifier.height(24.dp))
    NoteMarkButton(
        text = stringResource(R.string.create_account),
        onClick = { onAction(RegisterAction.OnRegisterClick) },
        enabled = state.canRegister,
        isLoading = state.isRegistering,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.already_have_account),
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = OnSurfaceVariant,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        NoteMarkTextButton(
            text = stringResource(R.string.log_in),
            onClick = { onAction(RegisterAction.OnLoginClick) },
        )
    }
}
