package com.tatasky.binge.ui.features.update_password

import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONException
import org.json.JSONObject

class UpdatePasswordAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper
) {

    fun trackUpdatePassword() {
        trackMixPanelUpdatePassword()
        trackMoEngageUpdatePassword()
    }

    private fun trackMixPanelUpdatePassword() {
        try {
            mixpanelHelper.trackEvent(EVENT_UPDATE_PASSWORD_INVOKED, mixpanelHelper.mMixpanelAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun trackUpdatePasswordSuccess() {
        trackMixPanelUpdatePasswordSuccess()
        trackMoEngageUpdatePasswordSuccess()
    }

    private fun trackMixPanelUpdatePasswordSuccess() {
        try {
            mixpanelHelper.trackEvent(EVENT_UPDATE_PASSWORD_SUCCESS, mixpanelHelper.mMixpanelAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun trackMoEngageUpdatePassword() {
        try {
            moEngageHelper.trackEvent(EVENT_UPDATE_PASSWORD_INVOKED)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun trackMoEngageUpdatePasswordSuccess() {
        try {
            moEngageHelper.trackEvent(EVENT_UPDATE_PASSWORD_SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun trackUpdatePasswordFailure(reason: String) {
        trackMixPanelUpdatePasswordFailure(reason)
        trackMoEngageUpdatePasswordFailure(reason)
    }

    private fun trackMixPanelUpdatePasswordFailure(reason: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_REASON, reason)
            mixpanelHelper.trackEvent(EVENT_UPDATE_PASSWORD_FAILED, jsonObject, mixpanelHelper.mMixpanelAPI)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageUpdatePasswordFailure(reason: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_REASON, reason)
            moEngageHelper.trackEvent(EVENT_UPDATE_PASSWORD_FAILED, payloadBuilder)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}