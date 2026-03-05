# Navigation Pattern (Android)

Jetpack Compose Navigation with type-safe `@Serializable` route objects and nested graphs per feature.

## Route Definitions

Define routes as `@Serializable` objects/data classes. Group them by feature:

```kotlin
package com.juandgaines.notemark.navigation

import kotlinx.serialization.Serializable

// --- Graph markers (for nested navigation graphs) ---
@Serializable data object AuthGraph
@Serializable data object MainGraph

// --- Screen routes ---
@Serializable data object LandingRoute
@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object HomeRoute

// Routes with arguments use data classes:
@Serializable data class NoteDetailRoute(val noteId: String)
```

## When to Use Nested Graphs

| Scenario | Approach |
|----------|----------|
| Feature has 1 screen | Flat `composable<Route>` — no graph needed |
| Feature has 2+ related screens (auth, onboarding, settings) | Nested `navigation<Graph>` |
| Need to pop entire feature on completion (e.g., logout clears auth stack) | Nested graph — `popUpTo<AuthGraph>` clears all screens in that graph |

## Nested Graph Structure

```kotlin
@Composable
fun NavigationRoot(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) MainGraph else AuthGraph
    ) {
        // Auth feature — nested graph with 3 screens
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
                        // Pop entire auth graph, navigate to main
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

        // Main feature — nested graph (or flat if single screen)
        navigation<MainGraph>(startDestination = HomeRoute) {
            composable<HomeRoute> {
                // ...
            }
            composable<NoteDetailRoute> {
                // ...
            }
        }
    }
}
```

## Key Benefits of Nested Graphs

1. **Clean back stack management** — `popUpTo<AuthGraph> { inclusive = true }` removes all auth screens at once, no risk of stale screens remaining
2. **Feature isolation** — each graph defines its own start destination and internal flow
3. **Logout safety** — when logging out, pop `MainGraph` and navigate to `AuthGraph`:
   ```kotlin
   navController.navigate(AuthGraph) {
       popUpTo<MainGraph> { inclusive = true }
   }
   ```

## Graph Marker Convention

Graph markers are `@Serializable data object` with a `Graph` suffix:

```kotlin
@Serializable data object AuthGraph      // auth feature
@Serializable data object MainGraph      // main/home feature
@Serializable data object SettingsGraph  // settings feature
@Serializable data object OnboardingGraph
```

Screen routes use a `Route` suffix:

```kotlin
@Serializable data object LoginRoute
@Serializable data object HomeRoute
@Serializable data class NoteDetailRoute(val noteId: String)
```

## Navigation Actions from ViewModels

Navigation events flow from ViewModel → Root composable via `Channel`:

```kotlin
// In Event sealed interface
sealed interface MyScreenEvent {
    data object NavigateToLogin : MyScreenEvent
    data class NavigateToDetail(val id: String) : MyScreenEvent
}

// In Root composable
ObserveAsEvents(viewModel.events) { event ->
    when (event) {
        MyScreenEvent.NavigateToLogin -> {
            navController.navigate(LoginRoute)
        }
        is MyScreenEvent.NavigateToDetail -> {
            navController.navigate(NoteDetailRoute(event.id))
        }
    }
}
```
