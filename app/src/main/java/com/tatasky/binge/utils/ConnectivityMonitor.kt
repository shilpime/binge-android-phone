package com.tatasky.binge.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import java.util.*

/**
 * Created by amrro <amr.elghobary></amr.elghobary>@gmail.com> on 8/15/17.
 *
 *
 * This class handle connectivity and detect if available.
 */
class ConnectivityMonitor(context: Context) :
    LifecycleObserver {
    private val manager: ConnectivityManager
    private val isConnected: MutableLiveData<Boolean> = MutableLiveData()
    private var monitoring = false
    private val connectivityCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            isConnected.postValue(true)
        }

        override fun onLost(network: Network) {
            isConnected.postValue(false)
        }
    }

    private fun checkConnectivity() {
        val activeNetworkInfo = manager.activeNetworkInfo
        val connection =
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        if (connection) {
            isConnected.setValue(connection)
        } else {
            isConnected.setValue(false)
            manager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                connectivityCallback
            )
            monitoring = true
        }
    }

    @NonNull
    fun isConnected(): MutableLiveData<Boolean> {
        return isConnected
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        checkConnectivity()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (monitoring) {
            manager.unregisterNetworkCallback(connectivityCallback)
            monitoring = false
        }
    }

    init {
        Objects.requireNonNull(
            context,
            "_> context cannot be null."
        )
        manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}