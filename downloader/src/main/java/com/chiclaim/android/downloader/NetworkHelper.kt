package com.chiclaim.android.downloader

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import com.chiclaim.android.downloader.util.e

/**
 *
 * @author by chiclaim@google.com
 */
object NetworkHelper {

    fun registerNetworkCallback(context: Context) {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    e("onAvailable:" + Thread.currentThread().name)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    e("onLost:" + Thread.currentThread().name)
                }

            })
    }
}