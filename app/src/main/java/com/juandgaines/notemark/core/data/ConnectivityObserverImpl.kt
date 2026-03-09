package com.juandgaines.notemark.core.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.juandgaines.notemark.core.domain.ConnectivityObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ConnectivityObserverImpl(
    context: Context,
) : ConnectivityObserver {

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun isConnected(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Default network lost — no active internet connection
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                trySend(hasInternet)
            }
        }

        // Track the system's default (active) network — only fires onLost
        // when there's truly no connectivity, not when one of many networks drops
        connectivityManager.registerDefaultNetworkCallback(callback)

        // Emit current state
        val currentNetwork = connectivityManager.activeNetwork
        val currentCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        val isConnected = currentCapabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        ) == true
        trySend(isConnected)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
