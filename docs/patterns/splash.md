# Splash Screen (Android 12+ Splash API)

Pattern for implementing the Android 12+ Splash API with an auth check gate.

## Dependencies

Add to `libs.versions.toml`:
```toml
[versions]
splashscreen = "1.0.1"

[libraries]
androidx-splashscreen = { module = "androidx.core:core-splashscreen", version.ref = "splashscreen" }
```

Add to `app/build.gradle.kts`:
```kotlin
implementation(libs.androidx.splashscreen)
```

## Splash Theme (res/values/splash.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.App.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher_foreground</item>
        <item name="postSplashScreenTheme">@style/Theme.App</item>
    </style>
</resources>
```

Add splash background color to `res/values/colors.xml`:
```xml
<!-- TODO: Replace with your brand color -->
<color name="splash_background">#5977F7</color>
```

## AndroidManifest.xml

Set the splash theme on the launcher activity:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.App.Splash">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

## MainActivity

```kotlin
package com.juandgaines.notemark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.juandgaines.notemark.navigation.NavigationRoot
import com.juandgaines.notemark.ui.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.state.isCheckingAuth
        }

        enableEdgeToEdge()

        setContent {
            AppTheme {
                if (!viewModel.state.isCheckingAuth) {
                    NavigationRoot(
                        isLoggedIn = viewModel.state.isLoggedIn
                    )
                }
            }
        }
    }
}
```

## MainViewModel + MainState

```kotlin
package com.juandgaines.notemark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.notemark.core.domain.SessionStorage
import kotlinx.coroutines.launch

data class MainState(
    val isCheckingAuth: Boolean = true,
    val isLoggedIn: Boolean = false,
)

class MainViewModel(
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            val authInfo = sessionStorage.get()
            state = state.copy(
                isLoggedIn = authInfo != null,
                isCheckingAuth = false,
            )
        }
    }
}
```

## NavigationRoot

```kotlin
package com.juandgaines.notemark.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationRoot(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        // TODO: Replace with your app's route objects
        startDestination = if (isLoggedIn) Route.Main else Route.Landing
    ) {
        // Define composable destinations here
    }
}
```

## Notes

- `installSplashScreen()` must be called BEFORE `super.onCreate()`
- `setKeepOnScreenCondition` keeps the splash visible while auth is being checked
- The `setContent` block waits for `isCheckingAuth = false` before rendering navigation
- `MainViewModel.state` uses `mutableStateOf` (not StateFlow) since it's read directly in the Activity
- The splash background color should match your app's brand color
- `@style/Theme.App` in `postSplashScreenTheme` should reference your app's main Material3 theme
