package com.juandgaines.notemark.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.juandgaines.notemark.auth.presentation.landing.LandingScreen
import com.juandgaines.notemark.auth.presentation.login.LoginScreenRoot
import com.juandgaines.notemark.auth.presentation.register.RegisterScreenRoot

@Composable
fun NavigationRoot(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) MainGraph else AuthGraph
    ) {
        navigation<AuthGraph>(startDestination = LandingRoute) {
            composable<LandingRoute> {
                LandingScreen(
                    onGetStarted = {
                        navController.navigate(RegisterRoute) {
                            popUpTo<LandingRoute> { inclusive = true }
                        }
                    },
                    onLogIn = {
                        navController.navigate(LoginRoute) {
                            popUpTo<LandingRoute> { inclusive = true }
                        }
                    }
                )
            }
            composable<LoginRoute> {
                LoginScreenRoot(
                    onLoginSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    },
                    onSignUp = {
                        navController.navigate(RegisterRoute) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable<RegisterRoute> {
                RegisterScreenRoot(
                    onRegisterSuccess = {
                        navController.navigate(LoginRoute) {
                            popUpTo<RegisterRoute> { inclusive = true }
                        }
                    },
                    onLogIn = {
                        navController.navigate(LoginRoute) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        navigation<MainGraph>(startDestination = MainRoute) {
            composable<MainRoute> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Main Screen")
                }
            }
        }
    }
}
