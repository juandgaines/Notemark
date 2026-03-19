package com.juandgaines.notemark.auth.presentation.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandgaines.notemark.R
import com.juandgaines.notemark.core.presentation.components.NoteMarkButton
import com.juandgaines.notemark.core.presentation.components.NoteMarkOutlinedButton
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration
import com.juandgaines.notemark.ui.theme.Inter
import com.juandgaines.notemark.ui.theme.LightBlueBg
import com.juandgaines.notemark.ui.theme.OnSurface
import com.juandgaines.notemark.ui.theme.OnSurfaceVariant
import com.juandgaines.notemark.ui.theme.SpaceGrotesk
import com.juandgaines.notemark.ui.theme.SurfaceLowest

@Composable
fun LandingScreen(
    onGetStartedClick: () -> Unit,
    onLogInClick: () -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> LandingPortrait(onGetStartedClick, onLogInClick)
        DeviceConfiguration.MOBILE_LANDSCAPE -> LandingLandscape(onGetStartedClick, onLogInClick)
        else -> LandingTablet(onGetStartedClick, onLogInClick)
    }
}

@Composable
private fun LandingPortrait(
    onGetStartedClick: () -> Unit,
    onLogInClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg),
    ) {
        Image(
            painter = painterResource(R.drawable.landing_illustration),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        )

        // Gradient fade on sides
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(LightBlueBg, Color.Transparent, Color.Transparent, LightBlueBg),
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceLowest)
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .padding(bottom = 8.dp),
        ) {
            LandingContent(
                onGetStartedClick = onGetStartedClick,
                onLogInClick = onLogInClick,
                titleFontSize = 32,
            )
        }
    }
}

@Composable
private fun LandingLandscape(
    onGetStartedClick: () -> Unit,
    onLogInClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg),
    ) {
        Image(
            painter = painterResource(R.drawable.landing_illustration),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            bottomStart = 20.dp,
                        )
                    )
                    .background(SurfaceLowest)
                    .padding(horizontal = 16.dp, vertical = 32.dp),
            ) {
                LandingContent(
                    onGetStartedClick = onGetStartedClick,
                    onLogInClick = onLogInClick,
                    titleFontSize = 32,
                )
            }
        }
    }
}

@Composable
private fun LandingTablet(
    onGetStartedClick: () -> Unit,
    onLogInClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg),
    ) {
        Image(
            painter = painterResource(R.drawable.landing_illustration),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
        )

        // Bottom gradient fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, LightBlueBg),
                    )
                ),
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(SurfaceLowest)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LandingContent(
                onGetStartedClick = onGetStartedClick,
                onLogInClick = onLogInClick,
                titleFontSize = 36,
            )
        }
    }
}

@Composable
private fun LandingContent(
    onGetStartedClick: () -> Unit,
    onLogInClick: () -> Unit,
    titleFontSize: Int,
) {
    Text(
        text = stringResource(R.string.your_own_collection_of_notes),
        style = TextStyle(
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize.sp,
            lineHeight = (titleFontSize + 4).sp,
            letterSpacing = (titleFontSize * 0.01f).sp,
            color = OnSurface,
        ),
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = stringResource(R.string.capture_your_thoughts),
        style = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            color = OnSurfaceVariant,
        ),
    )
    Spacer(modifier = Modifier.height(40.dp))
    NoteMarkButton(
        text = stringResource(R.string.get_started),
        onClick = onGetStartedClick,
    )
    Spacer(modifier = Modifier.height(12.dp))
    NoteMarkOutlinedButton(
        text = stringResource(R.string.log_in),
        onClick = onLogInClick,
    )
}
