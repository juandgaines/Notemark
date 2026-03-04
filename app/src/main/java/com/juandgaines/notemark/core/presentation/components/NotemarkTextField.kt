package com.juandgaines.notemark.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.Surface

@Composable
fun NotemarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supportingText: String? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    onFocusLost: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var isFocused by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(12.dp)
    val showError = error != null && !isFocused
    val showSupporting = isFocused && supportingText != null

    val borderModifier = when {
        showError -> Modifier.border(1.dp, MaterialTheme.colorScheme.error, shape)
        isFocused -> Modifier.border(1.dp, MaterialTheme.colorScheme.primary, shape)
        else -> Modifier
    }

    val backgroundColor = when {
        showError -> MaterialTheme.colorScheme.background
        isFocused -> MaterialTheme.colorScheme.background
        else -> Surface
    }

    val cursorColor = if (showError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(7.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            cursorBrush = SolidColor(cursorColor),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(shape)
                .background(backgroundColor, shape)
                .then(borderModifier)
                .padding(horizontal = 16.dp)
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused) {
                        onFocusLost?.invoke()
                    }
                    isFocused = focusState.isFocused
                },
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                    if (isPassword && onTogglePasswordVisibility != null) {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                painter = painterResource(
                                    if (isPasswordVisible) R.drawable.ic_visibility_off
                                    else R.drawable.ic_visibility
                                ),
                                contentDescription = if (isPasswordVisible) {
                                    stringResource(R.string.hide_password)
                                } else {
                                    stringResource(R.string.show_password)
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        )
        if (showError) {
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp),
            )
        } else if (showSupporting) {
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = supportingText!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
