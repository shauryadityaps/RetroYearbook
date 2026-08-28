package com.yearbook.retro.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Robust real-time network connectivity monitor for Android.
 * Integrates default network callback, connectivity broadcast receiver,
 * and continuous validation to catch instant network drops (Wi-Fi, Mobile Data, Airplane Mode).
 */
class NetworkObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(checkActiveConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // 1. Register Default Network Callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        scope.launch {
                            _isOnline.value = checkActiveConnectivity()
                        }
                    }

                    override fun onLost(network: Network) {
                        scope.launch {
                            _isOnline.value = false
                        }
                    }

                    override fun onUnavailable() {
                        scope.launch {
                            _isOnline.value = false
                        }
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        scope.launch {
                            _isOnline.value = hasInternet
                        }
                    }
                })
            } catch (e: Exception) {
                // Fallback handled by broadcast receiver below
            }
        }

        // 2. Legacy / Instant State Change BroadcastReceiver (Catches Mobile Data toggles immediately)
        try {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    scope.launch {
                        _isOnline.value = checkActiveConnectivity()
                    }
                }
            }, filter)
        } catch (e: Exception) {
            // Ignore
        }

        // 3. Background Poller (Verifies connectivity every 2 seconds to catch silent drops)
        scope.launch {
            while (isActive) {
                delay(2000)
                val current = checkActiveConnectivity()
                if (_isOnline.value != current) {
                    _isOnline.value = current
                }
            }
        }
    }

    private fun checkActiveConnectivity(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun checkConnectivityNow(): Boolean = withContext(Dispatchers.IO) {
        val active = checkActiveConnectivity()
        if (!active) {
            _isOnline.value = false
            return@withContext false
        }

        // Active socket probe to confirm live data transfer
        try {
            val url = URL("https://vqahvognmtqsojeoxacs.supabase.co/rest/v1/")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "HEAD"
            }
            val online = connection.responseCode in 200..499
            _isOnline.value = online
            online
        } catch (e: Exception) {
            val fallback = checkActiveConnectivity()
            _isOnline.value = fallback
            fallback
        }
    }
}
