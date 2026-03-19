package com.juandgaines.notemark.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juandgaines.notemark.auth.presentation.landing.LandingScreen
import com.juandgaines.notemark.auth.presentation.login.LoginScreenRoot
import com.juandgaines.notemark.auth.presentation.register.RegisterScreenRoot

@Composable
fun NavigationRoot(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Route.Main else Route.Landing,
    ) {
        authGraph(navController)
        composable<Route.Main> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Main Screen")
            }
        }
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable<Route.Landing> {
        LandingScreen(
            onGetStartedClick = {
                navController.navigate(Route.Register)
            },
            onLogInClick = {
                navController.navigate(Route.Login)
            },
        )
    }
    composable<Route.Login> {
        LoginScreenRoot(
            onLoginSuccess = {
                navController.navigate(Route.Main) {
                    popUpTo(Route.Landing) { inclusive = true }
                }
            },
            onRegisterClick = {
                navController.navigate(Route.Register) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            },
        )
    }
    composable<Route.Register> {
        RegisterScreenRoot(
            onRegisterSuccess = {
                navController.navigate(Route.Main) {
                    popUpTo(Route.Landing) { inclusive = true }
                }
            },
            onLoginClick = {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Register) { inclusive = true }
                }
            },
        )
    }
}
