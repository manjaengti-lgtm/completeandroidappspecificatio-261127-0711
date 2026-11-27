package com.arin.tube18.data.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkMonitor(context: Context) {
    private val cm: ConnectivityManager? = context.getSystemService(ConnectivityManager::class.java)

    fun isOnline(): Boolean {
        val manager = cm ?: return true
        return try {
            val network = manager.activeNetwork ?: return false
            val caps = manager.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: SecurityException) {
            true
        }
    }
}
