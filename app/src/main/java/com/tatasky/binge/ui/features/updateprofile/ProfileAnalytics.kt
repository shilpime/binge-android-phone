package com.tatasky.binge.ui.features.updateprofile

import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONException
import org.json.JSONObject

class ProfileAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper
) {

    fun trackTransactionHistoryInitiate() {
        trackMixPanelTransactionHistoryInitiate()
//        trackMoEngageTransactionHistoryInitiate()
    }

    fun trackSwitchAccountMaxDevice(sid: String) {
        trackMixPanelMaxDevice(sid)
        trackMoEngageMaxDevice(sid)
    }

    fun trackSwitchProfile(baId : String) {
        trackMixPanelSwitchProfile(baId)
        trackMoEngageSwitchProfile(baId)
    }

    fun trackEditProfileVisit() {
        trackMixPanelEditProfileVisit()
        trackMoEngageEditProfileVisit()
    }

    fun trackUpdateProfile() {
        trackMixPanelUpdateProfile()
        trackMoEngageUpdateProfile()
    }

    private fun trackMixPanelTransactionHistoryInitiate() {
        mixpanelHelper.trackEvent(EVENT_TRANSACTION_HISTORY_VISIT, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_TRANSACTION_HISTORY_VISIT, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMoEngageMaxDevice(sid: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SID, sid)
            moEngageHelper.trackEvent(EVENT_MAX_DEVICE, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelMaxDevice(sid: String) {
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_SID, sid)
            }
            mixpanelHelper.trackEvent(EVENT_MAX_DEVICE, jsonObject, mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SID, sid)
            }
            mixpanelHelper.trackEvent(EVENT_MAX_DEVICE, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageEditProfileVisit() {
        moEngageHelper.trackEvent(EVENT_EDIT_PROFILE_VISIT)
    }

    private fun trackMixPanelEditProfileVisit() {
        mixpanelHelper.trackEvent(EVENT_EDIT_PROFILE_VISIT,mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_EDIT_PROFILE_VISIT,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    //todo: need to track
    private fun trackMixPanelAddProfile(type: String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_TYPE, type)
            }

            mixpanelHelper.trackEvent(
                EVENT_ADD_PROFILE,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {

        }
    }

    private fun trackMoEngageUpdateProfile() {
        moEngageHelper.trackEvent(EVENT_UPDATE_PROFILE)
    }

    private fun trackMixPanelUpdateProfile() {
        mixpanelHelper.trackEvent(EVENT_UPDATE_PROFILE, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_UPDATE_PROFILE, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelSwitchProfile(baId: String) {
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_TO_BAID, baId)
            }
            mixpanelHelper.trackEvent(EVENT_SWITCH_PROFILE, jsonObject,mixpanelHelper.mMixpanelAPI)
            mixpanelHelper.trackEvent(EVENT_SWITCH_PROFILE, jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e:Exception){ }
    }



    private fun trackMoEngageSwitchProfile(baId: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_TO_BAID, baId)
            moEngageHelper.trackEvent(EVENT_SWITCH_PROFILE, payloadBuilder)
        } catch (e:Exception){ }
    }

    fun setUserIdentity(mixpanelId: String?){
        mixpanelHelper.setUserIdentity(mixpanelId, mixpanelHelper.mMixpanelUnifiedAPI)
    }
}