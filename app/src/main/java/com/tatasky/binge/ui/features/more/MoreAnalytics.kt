package com.tatasky.binge.ui.features.more

import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.*
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.utils.WATCH_NOTI_SETTINGS_KEY
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MoreAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper
) {

    fun trackMoreVisit() {
        trackAppsFlyerMoreView()
    }

    fun trackLogout() {
        trackMixPanelLogout()
        trackMoEngageLogout()
    }

    fun trackLogoutFailed(reason: String) {
        trackMixPanelLogoutFailed(reason)
        trackMoEngageLogoutFailed(reason)
    }

    private fun trackAppsFlyerMoreView() {
        appsFlyerHelper.trackEvent(SOURCE_MORE.toUpperCase(Locale.getDefault()))
    }

    private fun trackMixPanelLogout() {
        mixpanelHelper.trackEvent(EVENT_LOGOUT,mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_LOGOUT,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelLogoutFailed(reason : String) {
        try {
            val jsonObject = JSONObject().apply {
            put(PARA_REASON, reason)
            }
            mixpanelHelper.trackEvent(EVENT_LOGOUT_FAILED, jsonObject,mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_REASON, reason)
            }
            mixpanelHelper.trackEvent(EVENT_LOGOUT_FAILED, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackEmail() {
        trackMixPanelEmail()
        trackMoEngageEmail()
    }

    private fun trackMixPanelEmail() {
        mixpanelHelper.trackEvent(EVENT_EMAIL_US)
    }

    fun trackRaiseRequest() {
        trackMixPanelRaiseRequest()
        trackMoEngageRaiseRequest()
    }
    private fun trackMixPanelRaiseRequest() {
        mixpanelHelper.trackEvent(EVENT_RAISE_REQUEST)
    }

    fun trackCall() {
        trackMixPanelCall()
        trackMoEngageCall()
    }
    private fun trackMixPanelCall() {
        mixpanelHelper.trackEvent(EVENT_CALL_TATASKY)
    }

    fun trackFAQVisit() {
        trackMixPanelFAQVisit()
        trackMoEngageFAQVisit()
    }

    fun trackTnCVisit() {
        trackMixPanelTnCVisit()
        trackMoEngageTnCVisit()
    }

    fun trackPrivacyPolicyVisit() {
        trackMixPanelPrivacyPolicyVisit()
        trackMoEngagePrivacyPolicyVisit()
    }

    fun trackContactUsVisit() {
        trackMixPanelContactUsVisit()
        trackMoEngageContactUsVisit()
    }

    fun trackNotificationSettingsChanged(notificationType: String, value : String) {
        trackMixPanelNotificationSettingsChanged(notificationType, value)
        trackMoEngageNotificationSettingsChanged(notificationType, value)
    }

    fun trackAutoPlaySettingsChanged(value : String) {
        trackMixPanelAutoPlaySettingsChanged(value)
        trackMoEngageAutoPlaySettingsChanged(value)
    }

    private fun trackMixPanelFAQVisit() {
        mixpanelHelper.trackEvent(EVENT_FAQ_VIEW)
    }

    fun trackChat() {
        trackMixPanelChat()
        trackMoEngageChat()
    }
    private fun trackMixPanelChat() {
        mixpanelHelper.trackEvent(EVENT_CHAT_WITH_TATASKY)
    }

    private fun trackMixPanelTnCVisit() {
        mixpanelHelper.trackEvent(EVENT_TERMS_CONDITION_VISIT)
        mixpanelHelper.trackEvent(EVENT_TERMS_CONDITION_VISIT,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelPrivacyPolicyVisit() {
        mixpanelHelper.trackEvent(EVENT_PRIVACY_POLICY_VISIT)
        mixpanelHelper.trackEvent(EVENT_PRIVACY_POLICY_VISIT,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelContactUsVisit() {
        mixpanelHelper.trackEvent(EVENT_CONTACT_US_VISIT)
    }

    private fun trackMixPanelNotificationSettingsChanged(notificationType: String ,value: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_ALLOWED, value)
            mixpanelHelper.trackEvent(EVENT_NOTIFICATION_SETTING, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelAutoPlaySettingsChanged(value: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_ENABLED, value)
            mixpanelHelper.trackEvent(EVENT_AUTO_PLAY_SETTING, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageLogout() {
        try {
            moEngageHelper.trackEvent(EVENT_MOE_LOGOUT)
        } catch (e:Exception){}
    }

    private fun trackMoEngageLogoutFailed(reason: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_REASON, reason)
            moEngageHelper.trackEvent(EVENT_LOGOUT_FAILED, payloadBuilder)
        } catch (e:JSONException){
            e.printStackTrace()
        }
    }

    private fun trackMoEngageEmail() {
        moEngageHelper.trackEvent(EVENT_EMAIL_US)
    }

    private fun trackMoEngageRaiseRequest() {
        moEngageHelper.trackEvent(EVENT_RAISE_REQUEST)
    }

    private fun trackMoEngageCall() {
        moEngageHelper.trackEvent(EVENT_CALL_TATASKY)
    }

    private fun trackMoEngageFAQVisit() {
        moEngageHelper.trackEvent(EVENT_FAQ_VIEW)
    }

    private fun trackMoEngageTnCVisit() {
        moEngageHelper.trackEvent(EVENT_TERMS_CONDITION_VISIT)
    }

    private fun trackMoEngagePrivacyPolicyVisit() {
        moEngageHelper.trackEvent(EVENT_PRIVACY_POLICY_VISIT)
    }

    private fun trackMoEngageContactUsVisit() {
        moEngageHelper.trackEvent(EVENT_CONTACT_US_VISIT)
    }

    private fun trackMoEngageNotificationSettingsChanged(notificationType: String, value : String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_ALLOWED, value)
            moEngageHelper.trackEvent(EVENT_NOTIFICATION_SETTING, payloadBuilder)
        } catch (e:JSONException){
            e.printStackTrace()
        }
    }

    private fun trackMoEngageAutoPlaySettingsChanged(value : String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_ENABLED, value)
            moEngageHelper.trackEvent(EVENT_AUTO_PLAY_SETTING, payloadBuilder)
        } catch (e:JSONException){
            e.printStackTrace()
        }
    }


    private fun trackMoEngageChat() {
        moEngageHelper.trackEvent(EVENT_CHAT_WITH_TATASKY)
    }

}