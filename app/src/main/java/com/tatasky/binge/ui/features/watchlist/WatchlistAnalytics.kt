package com.tatasky.binge.ui.features.watchlist

import android.text.TextUtils
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class WatchlistAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper
) {

    fun trackFavoriteVisit() {
        trackMoEngageFavoriteVisit()
        trackMixPanelFavoriteVisit()
        trackAppsFlyerFavoriteVisit()
    }

    private fun trackAppsFlyerFavoriteVisit() {
        appsFlyerHelper.trackEvent(EVENT_SCREEN_VIEW_BINGELIST)
    }

    fun trackAddFavorite(title: String, type: String, genre: List<String>?, partnerName : String, source : String, railItem : String) {
        trackMoEngageAddFavorite(title, type, genre, partnerName, source, railItem)
    }

    fun trackFavoriteDelete(
        title: String,
        type: String,
        genre: List<String>?,
        partnerName: String,
        contentConfigType: String
    ) {
        trackMoEngageFavoriteDelete(title, type, genre, partnerName)
        trackMixPanelFavoriteDelete(title, type, genre, partnerName, contentConfigType)
    }

    private fun trackMoEngageFavoriteVisit() {
        moEngageHelper.trackEvent(EVENT_VIEW_WATCHLIST)
    }

    private fun trackMoEngageAddFavorite(title: String, type: String, genre: List<String>?, partnerName : String, source: String, railName: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            genre?.let {
                payloadBuilder.addAttribute(PARA_GENRE, TextUtils.join(", ", it))
            }
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_TITLE_RAIL, railName)
            moEngageHelper.trackEvent(EVENT_ADD_FAVORITE, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageFavoriteDelete(title: String, type: String, genre: List<String>?, partnerName : String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            genre?.let {
                payloadBuilder.addAttribute(PARA_GENRE, TextUtils.join(", ", it))
            }
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            moEngageHelper.trackEvent(EVENT_DELETE_FAVORITE, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelFavoriteVisit() {
        mixpanelHelper.trackEvent(EVENT_VIEW_WATCHLIST, mixpanelHelper.mMixpanelAPI)
        mixpanelHelper.trackEvent(EVENT_VIEW_WATCHLIST, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelFavoriteDelete(
        title: String,
        type: String,
        genre: List<String>?,
        partnerName: String,
        contentConfigType: String
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(CONTENT_TITLE, title)
            genre?.let {
                jsonObject.put(PARA_GENRE, TextUtils.join(", ", it))
            }
            jsonObject.put(PARA_PARTNER_NAME, partnerName)
            jsonObject.put(
                PARA_CONTENT_TYPE,
                if (contentConfigType.equals(EDITORIAL, true))
                    EDITORIAL
                else
                    contentConfigType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
            )
            mixpanelHelper.trackEvent(EVENT_DELETE_FAVORITE, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}