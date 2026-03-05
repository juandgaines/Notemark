package com.juandgaines.notemark.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.juandgaines.notemark.auth.presentation.landing.LandingScreen
import com.juandgaines.notemark.auth.presentation.login.LoginScreenRoot
import com.juandgaines.notemark.auth.presentation.register.RegisterScreenRoot
import com.juandgaines.notemark.note.presentation.note_detail.NoteDetailScreenRoot
import com.juandgaines.notemark.note.presentation.note_list.NoteListScreenRoot

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

        navigation<MainGraph>(startDestination = NoteListRoute) {
            composable<NoteListRoute> {
                NoteListScreenRoot(
                    onNavigateToNote = { noteId ->
                        navController.navigate(NoteDetailRoute(noteId = noteId))
                    },
                )
            }
            composable<NoteDetailRoute> {
                NoteDetailScreenRoot(
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}
