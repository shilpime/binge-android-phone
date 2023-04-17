package com.tatasky.binge.ui.features.sidemenunavdrawer

import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import org.json.JSONException
import org.json.JSONObject

class SideMenuDrawerAnalytics(
    private val appsFlyerHelper: AppsFlyerHelper,
    private val mixpanelHelper: MixpanelHelper
) {

    /**---Method declaration starts here---*/

    fun trackFAQVisit() {
        trackMixPanelFAQVisit()
//        trackMoEngageFAQVisit()
    }

    fun trackAutoPlaySettingsChanged(value : String) {
        trackMixPanelAutoPlaySettingsChanged(value)
//        trackMoEngageAutoPlaySettingsChanged(value)
    }

    fun trackNotificationSettingsChanged(notificationType: String, value : String) {
        trackMixPanelNotificationSettingsChanged(notificationType, value)
//        trackMoEngageNotificationSettingsChanged(notificationType, value)
    }

    //Help and Support
    fun trackContactUsVisit() {
        trackMixPanelContactUsVisit()
//        trackMoEngageFAQVisit()
    }

    fun trackPrivacyPolicyVisit() {
        trackMixPanelPrivacyPolicyVisit()
//        trackMoEngagePrivacyPolicyVisit()
    }

    fun trackTnCVisit() {
        trackMixPanelTnCVisit()
//        trackMoEngageTnCVisit()
    }

    fun trackParentalControlSettingView() {
        trackAppsFlyerParentalControlSettingView()
    }

    fun trackSelectContentLanguageView() {
        trackAppsFlyerSelectContentLanguageView()
    }

    /**---Method declaration ends here---*/

    /**---Method body implementation starts here---*/

    private fun trackMixPanelFAQVisit() {
        mixpanelHelper.trackEvent(EVENT_FAQ_VIEW, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_FAQ_VIEW, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelAutoPlaySettingsChanged(value: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_ENABLED, value)
            mixpanelHelper.trackEvent(EVENT_AUTO_PLAY_SETTING, jsonObject, mixpanelHelper.mMixpanelAPI)
            jsonObject.put(PARA_DISABLED, if (value.equals(YES, true)) NO else YES)
            mixpanelHelper.trackEvent(EVENT_AUTO_PLAY_SETTING, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelNotificationSettingsChanged(notificationType: String ,value: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_ALLOWED, value)
            mixpanelHelper.trackEvent(EVENT_NOTIFICATION_SETTING, jsonObject, mixpanelHelper.mMixpanelAPI)
            mixpanelHelper.trackEvent(EVENT_NOTIFICATION_SETTING, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelContactUsVisit() {
        mixpanelHelper.trackEvent(EVENT_CONTACT_US_VISIT, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_CONTACT_US_VISIT, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelPrivacyPolicyVisit() {
        mixpanelHelper.trackEvent(EVENT_PRIVACY_POLICY_VISIT, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_PRIVACY_POLICY_VISIT, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelTnCVisit() {
        mixpanelHelper.trackEvent(EVENT_TERMS_CONDITION_VISIT, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_TERMS_CONDITION_VISIT, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackAppsFlyerParentalControlSettingView() {
        appsFlyerHelper.trackEvent(EVENT_PARENTAL_CONTROL)
    }

    private fun trackAppsFlyerSelectContentLanguageView() {
        appsFlyerHelper.trackEvent(CONTENT_LANGUAGE)
    }

    /**---Method body implementation ends here---*/

}