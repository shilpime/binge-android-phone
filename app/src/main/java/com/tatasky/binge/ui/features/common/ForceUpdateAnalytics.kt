package com.tatasky.binge.ui.features.common

import com.tatasky.binge.utils.Properties
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.ANDROID_APP_VERSION
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONException

class ForceUpdateAnalytics(
    private val moEngageHelper: MoEngageHelper
) {
    fun trackForceUpdate() {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(ANDROID_APP_VERSION, BuildConfig.VERSION_CODE)
            moEngageHelper.trackEvent(
                "FORCE-UPDATE-CHECK",
                payloadBuilder
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}