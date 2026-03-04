package com.juandgaines.notemark.core.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

@Composable
fun currentDeviceConfiguration(): DeviceConfiguration {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
}

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    val isMobile: Boolean
        get() = this in listOf(MOBILE_PORTRAIT, MOBILE_LANDSCAPE)

    val isWideScreen: Boolean
        get() = this in listOf(TABLET_LANDSCAPE, DESKTOP)

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            val width = windowSizeClass.minWidthDp
            val height = windowSizeClass.minHeightDp
            return when {
                width < WIDTH_DP_MEDIUM_LOWER_BOUND &&
                        height >= HEIGHT_DP_MEDIUM_LOWER_BOUND -> MOBILE_PORTRAIT
                width >= WIDTH_DP_EXPANDED_LOWER_BOUND &&
                        height < HEIGHT_DP_MEDIUM_LOWER_BOUND -> MOBILE_LANDSCAPE
                width in WIDTH_DP_MEDIUM_LOWER_BOUND until WIDTH_DP_EXPANDED_LOWER_BOUND &&
                        height >= HEIGHT_DP_EXPANDED_LOWER_BOUND -> TABLET_PORTRAIT
                width >= WIDTH_DP_EXPANDED_LOWER_BOUND &&
                        height in HEIGHT_DP_MEDIUM_LOWER_BOUND until HEIGHT_DP_EXPANDED_LOWER_BOUND -> TABLET_LANDSCAPE
                else -> DESKTOP
            }
        }
    }
}
