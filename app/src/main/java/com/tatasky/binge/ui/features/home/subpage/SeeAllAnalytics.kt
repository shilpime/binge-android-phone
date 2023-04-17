package com.tatasky.binge.ui.features.home.subpage

import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.utils.PROVIDER_GAMEZOP
import org.json.JSONException
import org.json.JSONObject

class SeeAllAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper
) {

    fun trackGameClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        gameGenre: String,
        gamePartner: String,
        gamePosition: String,
        gameRating: Float?,
        gameTitle: String,
        freeGame: String,
        releaseYear: String,
        deviceType: String,
        source: String,
        packPrice: String,
        packName: String
    ) {
        trackMixPanelGameClick(
            pageName,
            railTitle,
            railPosition,
            railType,
            railCategory,
            gameGenre,
            gamePartner,
            gamePosition,
            gameRating,
            gameTitle,
            freeGame,
            releaseYear,
            deviceType,
            source,
            packPrice,
            packName
        )
        trackMoEngageGameClick(
            pageName,
            railTitle,
            railPosition,
            railType,
            railCategory,
            gameGenre,
            gamePartner,
            gamePosition,
            gameRating,
            gameTitle,
            freeGame,
            releaseYear,
            deviceType,
            source,
            packPrice,
            packName
        )
    }

    private fun trackMixPanelGameClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        gameGenre: String,
        gamePartner: String,
        gamePosition: String,
        gameRating: Float?,
        gameTitle: String,
        freeGame: String,
        releaseYear: String,
        deviceType: String,
        source: String,
        packPrice: String,
        packName: String
    ) {
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_PAGE_NAME, if(pageName.equals(PROVIDER_GAMEZOP,true)) SOURCE_GAMES else pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(PARA_RAIL_TYPE, railType)
                put(PARA_RAIL_CATEGORY, railCategory)
                put(PARA_GAME_GENRE, gameGenre)
                put(PARA_GAME_PARTNER, gamePartner)
                put(PARA_GAME_POSITION, gamePosition)
                put(PARA_GAME_RATING, gameRating)
                put(PARA_GAME_TITLE, gameTitle)
                put(PARA_FREE_GAME, freeGame)
//                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_SOURCE, if(source.equals(PROVIDER_GAMEZOP,true)) SOURCE_GAMES else source)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
            }

            mixpanelHelper.trackEvent(
                EVENT_GAME_CLICK,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageGameClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        gameGenre: String,
        gamePartner: String,
        gamePosition: String,
        gameRating: Float?,
        gameTitle: String,
        freeGame: String,
        releaseYear: String,
        deviceType: String,
        source: String,
        packPrice: String,
        packName: String
    ) {

        try {
            moEngageHelper.trackEvent(EVENT_GAME_CLICK)
        } catch (e: JSONException){
            e.printStackTrace()
        }
    }


    fun trackSeeAll(
        railTitle: String,
        source: String
    ) {
        trackAppsFlyerSeeAll(railTitle, source)
    }

    private fun trackAppsFlyerSeeAll(railTitle: String, source: String) {
        try {
            val parameterAndValueMap = java.util.HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
            parameterAndValueMap[PARA_TITLE_RAIL] = railTitle
            parameterAndValueMap[PARA_SOURCE] = source
            appsFlyerHelper.trackEvent(EVENT_SEE_ALL, parameterAndValueMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}