package com.juandgaines.notemark.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandgaines.notemark.core.presentation.util.DeviceConfiguration
import com.juandgaines.notemark.core.presentation.util.currentDeviceConfiguration
import com.juandgaines.notemark.ui.theme.BrandPrimary

@Composable
fun AuthFormLayout(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    formContent: @Composable ColumnScope.() -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BrandPrimary),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 52.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 32.dp, bottom = 40.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    formContent()
                }
            }
        }
        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BrandPrimary),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 52.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 60.dp, end = 40.dp, top = 32.dp, bottom = 40.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        if (subtitle != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        formContent()
                    }
                }
            }
        }
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BrandPrimary),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 52.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 120.dp, vertical = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 36.sp,
                            lineHeight = 40.sp,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                    ) {
                        formContent()
                    }
                }
            }
        }
    }
}
