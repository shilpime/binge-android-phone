package com.tatasky.binge.data.service

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.KEY_SCREEN_DATA
import java.util.HashMap

class CustomNotificationRedirectionHandlerClevertap : Application.ActivityLifecycleCallbacks {

    fun initialize(myApp: MyApp) {
        myApp.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        handle(activity)

//        CleverTapAPI.setAppForeground(true)
//        try {
//            CleverTapAPI.getDefaultInstance(activity)
//                ?.pushNotificationClickedEvent(activity.intent.extras)
//        } catch (t: Throwable) {
//            // Ignore
//        }
//        try {
//            val intent = activity.intent
//            val data = intent.data
//            CleverTapAPI.getDefaultInstance(activity)?.pushDeepLink(data)
//        } catch (t: Throwable) {
//            // Ignore
//        }

    }

    override fun onActivityStarted(activity: Activity) {
        handle(activity)
    }


    override fun onActivityResumed(activity: Activity) {
        CleverTapAPI.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        CleverTapAPI.onActivityPaused()
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    private fun handle(activity: Activity) {
        Log.d("djfkdfds", activity.toString())
        CleverTapAPI.getDefaultInstance(activity)?.setCTPushNotificationListener(object :
            CTPushNotificationListener {
            override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>?) {
                System.out.print("djfkd")
                val extras = Bundle()
                if (payload == null) {
                    return
                }
                for ((key, value) in payload) {
                    extras.putString(key, value.toString())
                }
                NotificationNavigator().navigate(activity, extras)

            }

        });

        CleverTapAPI.getDefaultInstance(activity)?.setCTPushAmpListener(object : CTPushAmpListener {
            override fun onPushAmpPayloadReceived(extras: Bundle?) {

            }
        });
    }
}
