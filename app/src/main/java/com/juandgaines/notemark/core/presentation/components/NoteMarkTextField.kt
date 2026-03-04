package com.juandgaines.notemark.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.juandgaines.notemark.R
import com.juandgaines.notemark.ui.theme.Error
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.Primary
import com.juandgaines.notemark.ui.theme.Surface
import com.juandgaines.notemark.ui.theme.SurfaceLowest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.juandgaines.notemark.ui.theme.Inter

@Composable
fun NoteMarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    supportingText: String? = null,
    errorText: String? = null,
    isValid: Boolean = true,
    hasFocusedOnce: Boolean = false,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var isFocused by remember { mutableStateOf(false) }
    val showError = hasFocusedOnce && !isFocused && !isValid && value.isNotEmpty()
    val showSupport = isFocused && supportingText != null && !showError

    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            ),
            color = OnSurface,
        )
        Spacer(modifier = Modifier.height(7.dp))

        val shape = RoundedCornerShape(12.dp)
        val borderModifier = when {
            showError -> Modifier.border(1.dp, Error, shape)
            isFocused -> Modifier.border(1.dp, Primary, shape)
            else -> Modifier
        }
        val bgColor = if (isFocused || showError) SurfaceLowest else Surface
        val cursorColor = if (showError) Error else Primary

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 17.sp,
                color = OnSurface,
            ),
            cursorBrush = SolidColor(cursorColor),
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(shape)
                .then(borderModifier)
                .background(bgColor, shape)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    onFocusChanged?.invoke(focusState.isFocused)
                },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 17.sp,
                                    color = OnSurfaceVariant,
                                ),
                            )
                        }
                        innerTextField()
                    }
                    if (isPassword && onTogglePasswordVisibility != null) {
                        IconButton(
                            onClick = onTogglePasswordVisibility,
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isPasswordVisible) R.drawable.ic_eye_off
                                    else R.drawable.ic_eye
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = OnSurfaceVariant,
                            )
                        }
                    }
                }
            },
        )

        if (showError && errorText != null) {
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = errorText,
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Error,
                ),
            )
        } else if (showSupport) {
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = supportingText!!,
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = OnSurfaceVariant,
                ),
            )
        }
    }
}
