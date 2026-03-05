package com.juandgaines.notemark.navigation

import kotlinx.serialization.Serializable

// Graph markers — for nested navigation graphs
@Serializable
data object AuthGraph

@Serializable
data object MainGraph

// Screen routes
@Serializable
data object LandingRoute

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object MainRoute
