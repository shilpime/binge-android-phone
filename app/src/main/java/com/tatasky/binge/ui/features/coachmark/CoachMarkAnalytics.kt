package com.tatasky.binge.ui.features.coachmark

import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import org.json.JSONObject
import javax.inject.Inject


class CoachMarkAnalytics @Inject constructor(
    private val mixpanelHelper: MixpanelHelper,
) {
    fun trackCoachMarkDisplayed(coachMarkName: String, source: String, displayCount: Int) {
        trackMixpanelCoachMarkDisplayed(coachMarkName, source, displayCount)
    }

    fun trackCoachMarkIconClick(coachMarkName: String, source: String, displayCount: Int) {
        trackMixpanelCoachMarkIconClick(coachMarkName, source, displayCount)
    }

    fun trackCoachMarkOutsideClick(coachMarkName: String, source: String, displayCount: Int) {
        trackMixpanelCoachMarkOutsideClick(coachMarkName, source, displayCount)
    }

    private fun trackMixpanelCoachMarkDisplayed(
        coachMarkName: String,
        source: String,
        displayCount: Int,
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.apply {
                put(COACH_MARK_NAME, coachMarkName)
                put(PARA_SOURCE, source)
                put(DISPLAY_COUNT, displayCount)
            }
            mixpanelHelper.trackEvent(
                COACH_MARK_DISPLAYED,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixpanelCoachMarkIconClick(
        coachMarkName: String,
        source: String,
        displayCount: Int,
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.apply {
                put(COACH_MARK_NAME, coachMarkName)
                put(PARA_SOURCE, source)
                put(DISPLAY_COUNT, displayCount)
            }
            mixpanelHelper.trackEvent(
                COACH_MARK_ICON_CLICK,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixpanelCoachMarkOutsideClick(
        coachMarkName: String,
        source: String,
        displayCount: Int,
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.apply {
                put(COACH_MARK_NAME, coachMarkName)
                put(PARA_SOURCE, source)
                put(DISPLAY_COUNT, displayCount)
            }
            mixpanelHelper.trackEvent(
                COACH_MARK_OUTSIDE_CLICK,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}