package sk.mpage.lumen.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import sk.mpage.lumen.core.common.dispatcher.ApplicationScope
import sk.mpage.lumen.core.domain.port.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ConnectivityNetworkMonitor
    @Inject
    constructor(
        @ApplicationContext context: Context,
        @ApplicationScope appScope: CoroutineScope,
    ) : NetworkMonitor {
        override val isOnline: SharedFlow<Boolean> =
            callbackFlow {
                val manager = context.getSystemService<ConnectivityManager>()
                if (manager == null) {
                    send(false)
                    awaitClose()
                    return@callbackFlow
                }
                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            capabilities: NetworkCapabilities,
                        ) {
                            trySend(capabilities.hasInternet())
                        }

                        override fun onLost(network: Network) {
                            trySend(false)
                        }
                    }
                trySend(manager.getNetworkCapabilities(manager.activeNetwork)?.hasInternet() == true)
                manager.registerDefaultNetworkCallback(callback)
                awaitClose { manager.unregisterNetworkCallback(callback) }
            }.conflate()
                .distinctUntilChanged()
                .shareIn(appScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

        private fun NetworkCapabilities.hasInternet(): Boolean =
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
