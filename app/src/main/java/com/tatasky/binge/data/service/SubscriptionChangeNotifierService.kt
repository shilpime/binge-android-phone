package com.tatasky.binge.data.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import dagger.android.AndroidInjection
import javax.inject.Inject

class SubscriptionChangeNotifierService : Service() {
    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Handler(Looper.getMainLooper()).postDelayed({
            localBroadcastHelper.sendBroadcast(
                this,
                Intent(localBroadcastHelper.ACTION_PLAN_CHANGED).apply {
                    putExtra("title", intent?.getStringExtra("title"))
                    putExtra("message", intent?.getStringExtra("message"))
                })
            stopSelf()
        }, 1000)
        return super.onStartCommand(intent, flags, startId)
    }
}