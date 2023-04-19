package com.tatasky.binge.pubnub

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tatasky.binge.utils.e
import javax.inject.Singleton

@Singleton
class LocalBroadcastHelper {
    val ACTION_LOGOUT = "FORCE_LOGOUT"
    val ACTION_DATA_POSTED = "DATA_POSTED"
    val ACTION_DEVICE_STATUS_LOGOUT = "FORCE_DEVICE_LOGOUT"
    val ACTION_ATV_CANCELLED_SWITCH = "ATV_CANCELLED_SWITCH"
    val ACTION_ACCOUNT_DEACTIVE = "ACCOUNT_DEACTIVE"
    val ACTION_PRIME = "PRIME_CALLBACK"
    val ACTION_NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
    val ACTION_PLAN_CHANGED = "PLAN_CHANGED"
    val ACTION_MAKE_PAYMENT = "MAKE_PAYMENT"
    val ACTION_PAYMENT_ERROR = "PAYMENT_ERROR"
    val ACTION_PAYMENT_CHARGED = "PAYMENT_CHARGED" // Payment successful
    val ACTION_PAYMENT_UPDATED = "ACTION_PAYMENT_UPDATED"
    val ACTION_SUBSCRIPTION_UPDATED = "ACTION_SUBSCRIPTION_UPDATED"
    val ACTION_DEVICE_STATUS_LOGOUT_ALL = "FORCE_DEVICE_LOGOUT_ALL"
    val ACTION_SILENT_LOGIN = "SILENT_LOGIN"
    val ACTION_SUBSCRIPTION_UPDATED_DO_REFRESH = "ACTION_SUBSCRIPTION_UPDATED_DO_REFRESH"
    init {
        e("LocalBroadcastHelper","inside local Broadcast")
    }
    fun registerBroadcast(
        context: Context,
        broadcastReceiver: BroadcastReceiver,
        action: String?
    ) {
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, IntentFilter(action))
    }

    fun unregisterBroadcast(
        context: Context,
        broadcastReceiver: BroadcastReceiver
    ) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    fun sendBroadcast(context: Context, action: String) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(action))
    }

    fun sendBroadcast(context: Context, intent: Intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}