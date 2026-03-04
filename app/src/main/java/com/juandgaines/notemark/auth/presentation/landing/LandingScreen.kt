package com.juandgaines.notemark.auth.presentation.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.components.NotemarkButton
import com.juandgaines.notemark.core.presentation.components.NotemarkOutlinedButton
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            LandingPortrait(
                onGetStarted = onGetStarted,
                onLogIn = onLogIn,
            )
        }
        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            LandingLandscape(
                onGetStarted = onGetStarted,
                onLogIn = onLogIn,
            )
        }
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            LandingTablet(
                onGetStarted = onGetStarted,
                onLogIn = onLogIn,
            )
        }
    }
}

@Composable
private fun LandingPortrait(
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_landing_notes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.6f),
                            Color.White,
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.landing_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.landing_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(40.dp))
            NotemarkButton(
                text = stringResource(R.string.get_started),
                onClick = onGetStarted,
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotemarkOutlinedButton(
                text = stringResource(R.string.log_in),
                onClick = onLogIn,
            )
        }
    }
}

@Composable
private fun LandingLandscape(
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0EAFF)),
    ) {
        Image(
            painter = painterResource(R.drawable.bg_landing_notes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterStart)
        )
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 60.dp, end = 40.dp, top = 40.dp, bottom = 40.dp),
        ) {
            Text(
                text = stringResource(R.string.landing_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.landing_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(40.dp))
            NotemarkButton(
                text = stringResource(R.string.get_started),
                onClick = onGetStarted,
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotemarkOutlinedButton(
                text = stringResource(R.string.log_in),
                onClick = onLogIn,
            )
        }
    }
}

@Composable
private fun LandingTablet(
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_landing_notes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.6f),
                            Color.White,
                        ),
                    )
                )
        )
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.landing_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.landing_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(40.dp))
            NotemarkButton(
                text = stringResource(R.string.get_started),
                onClick = onGetStarted,
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotemarkOutlinedButton(
                text = stringResource(R.string.log_in),
                onClick = onLogIn,
            )
        }
    }
}
