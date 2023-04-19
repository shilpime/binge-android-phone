package com.tatasky.binge.ui.features.notifications

import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONObject

class NotificationAnalytics(private val mixpanelHelper: MixpanelHelper,private val moEngageHelper: MoEngageHelper) {

    fun trackNotificationScreenVisit() {
        trackMixPanelNotificationScreenVisit()
        trackMoEngageNotificationScreenVisit()
    }
    private fun trackMixPanelNotificationScreenVisit() {
        mixpanelHelper.trackEvent(EVENT_VIEW_NOTIFICATION, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_VIEW_NOTIFICATION, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackNotificationDelete() {
        trackMixPanelNotificationDelete()
        trackMoEngageNotificationDelete()
    }
    private fun trackMixPanelNotificationDelete() {
        mixpanelHelper.trackEvent(EVENT_DELETE_NOTIFICATION)
    }

    private fun trackMoEngageNotificationScreenVisit() {
        moEngageHelper.trackEvent(EVENT_VIEW_NOTIFICATION)
    }

    private fun trackMoEngageNotificationDelete() {
        moEngageHelper.trackEvent(EVENT_DELETE_NOTIFICATION)
    }
}