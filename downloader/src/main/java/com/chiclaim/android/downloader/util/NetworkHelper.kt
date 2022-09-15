package com.chiclaim.android.downloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest


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

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    // 会被调用多次
                    if (networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            e("wifi网络已连接" + Thread.currentThread().name)
                        } else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                            e("移动网络已连接" + Thread.currentThread().name)
                        }else{
                            e("其他链接 $networkCapabilities in thread ${Thread.currentThread().name}")
                        }
                    }
                }
            })
    }
}