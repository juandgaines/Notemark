package com.juandgaines.notemark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.juandgaines.notemark.navigation.NavigationRoot
import com.juandgaines.notemark.ui.theme.NotemarkTheme
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
            NotemarkTheme {
                if (!viewModel.state.isCheckingAuth) {
                    NavigationRoot(
                        isLoggedIn = viewModel.state.isLoggedIn
                    )
                }
            }
        }
    }
}
