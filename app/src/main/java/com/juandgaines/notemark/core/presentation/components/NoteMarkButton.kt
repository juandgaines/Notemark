package com.juandgaines.notemark.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandgaines.notemark.ui.theme.OnPrimary
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurface12
import com.juandgaines.notemark.ui.theme.Primary
import com.juandgaines.notemark.ui.theme.SpaceGrotesk

@Composable
fun NoteMarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = OnPrimary,
            disabledContainerColor = OnSurface12,
            disabledContentColor = OnSurface.copy(alpha = 0.38f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = OnPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                ),
            )
        }
    }
}
