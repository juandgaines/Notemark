package com.juandgaines.notemark.core.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

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
            val width = windowSizeClass.windowWidthSizeClass
            val height = windowSizeClass.windowHeightSizeClass
            return when {
                width == WindowWidthSizeClass.COMPACT &&
                        height != WindowHeightSizeClass.COMPACT -> MOBILE_PORTRAIT
                width == WindowWidthSizeClass.EXPANDED &&
                        height == WindowHeightSizeClass.COMPACT -> MOBILE_LANDSCAPE
                width == WindowWidthSizeClass.MEDIUM &&
                        height == WindowHeightSizeClass.EXPANDED -> TABLET_PORTRAIT
                width == WindowWidthSizeClass.EXPANDED &&
                        height == WindowHeightSizeClass.MEDIUM -> TABLET_LANDSCAPE
                else -> DESKTOP
            }
        }
    }
}
