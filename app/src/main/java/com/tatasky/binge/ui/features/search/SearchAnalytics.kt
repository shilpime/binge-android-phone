package com.tatasky.binge.ui.features.search

import android.util.Log
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.utils.PROVIDER_GAMEZOP
import org.json.JSONException
import org.json.JSONObject

class SearchAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper
) {

    fun trackFilterToggleClick(source: String, state: String, packName: String, packPrice: String, selectedFilter: String)
    {
        trackMixPanelFilterToggleEvent(source,state,packName,packPrice,selectedFilter)
    }

    private fun trackMixPanelFilterToggleEvent(source: String, state: String, packName: String, packPrice: String, selectedFilter: String)
    {
            try {
                val jsonObject = JSONObject()
                jsonObject.put(PARA_SOURCE, source)
                jsonObject.put(STATE, state)
                jsonObject.put(PARA_PACK_NAME, packName)
                jsonObject.put(PARA_PACK_PRICE, packPrice)
                jsonObject.put(PARA_FILTER_SELECTED, selectedFilter)
                mixpanelHelper.trackEvent(EVENT_FILTER_TOGGLE, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
    }


    fun trackSearchHomeClick(section:String){
        trackMixPanelSearchHomeClick(section.uppercase())
    }

    fun trackSearchResultSwipe(keyword: String){
        trackMixPanelSearchResultSwipe(keyword)
    }

    fun trackSearchResultClicks(position: String, contentTitle: String){
        trackMixPanelSearchResultClicks(position,contentTitle)
    }


    fun trackSearchStart(selectedBottomTab: String) {
        trackMixPanelSearchStart()
        trackMoEngageSearchStart()
        trackAppsFlyerSearchView()
        trackMixPanelSearchInitiate()
        trackMixPanelSearchHome(selectedBottomTab)
    }

    private fun trackAppsFlyerSearchView() {
        appsFlyerHelper.trackEvent(EVENT_SEARCH)
    }

    private fun trackMixPanelSearchStart() {
        mixpanelHelper.trackEvent(EVENT_SEARCH_START)
    }


    private fun trackMixPanelSearchInitiate() {
        mixpanelHelper.trackEvent(EVENT_SEARCH_INITIATE, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackSearch(keyword: String, source: String, screenName: String,filterLanguage:String,filterGenre: String) {
        trackMixPanelSearch(keyword, source, screenName, filterLanguage, filterGenre)
        trackMoEngageSearch(keyword, source, screenName)
    }

    fun trackSearchMIC(screenName: String,keyWord:String, source:String) {
        trackMixPanelSearchMIC(screenName, keyWord, source)
        trackMoEngageSearchMIC(screenName)
    }

    fun trackSearchMICPermission(permissionAllowed: String) {
        trackMixPanelSearchVoicePermission(permissionAllowed)
        trackMoEngageSearchVoicePermission(permissionAllowed)
    }

    fun trackSearchReactivateMic() {
        trackMixPanelSearchReacitivateMic()
        trackMoEngageSearchReacitivateMic()
    }

    fun trackLanguageOrGenreScreenView(titleOrName: String, screenType: String, Source: String) {
        trackAppsFlyerLanguageOrGenreScreenView(titleOrName, screenType, Source)
    }

    private fun trackMoEngageSearchReacitivateMic() {
        try {
            moEngageHelper.trackEvent(EVENT_SEARCH_REACTIVATE_MIC)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelSearchReacitivateMic() {
        try {
            mixpanelHelper.trackEvent(EVENT_SEARCH_REACTIVATE_MIC,mixpanelHelper.mMixpanelAPI)
            mixpanelHelper.trackEvent(EVENT_SEARCH_REACTIVATE_MIC,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelSearch(
        keyword: String,
        source: String,
        screenName: String,
        filterLanguage: String,
        filterGenre: String
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_KEYWORD, keyword)
            jsonObject.put(
                PARA_SOURCE,
                if (source.equals(
                        VOICE_SEARCH,
                        true
                    )
                ) source.uppercase() else source
            )
            jsonObject.put(PARA_SCREEN_NAME, screenName)
            mixpanelHelper.trackEvent(EVENT_SEARCH, jsonObject, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelSearchVoicePermission(permissionAllowed: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_SEARCH_ACCESS, permissionAllowed)
            mixpanelHelper.trackEvent(EVENT_SEARCH_VOICE_PERMISSION, jsonObject, mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject()
            jsonObjectUnified.put(PARA_SEARCH_ACCESS, permissionAllowed)
            mixpanelHelper.trackEvent(EVENT_SEARCH_VOICE_PERMISSION, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelSearchMIC(
        screenName: String,
        keyWord: String,
        source: String
    )
    {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SCREEN_NAME, screenName)
                put(
                    PARA_SOURCE,
                    if (source.equals(
                            VOICE_SEARCH,
                            true
                        )
                    ) source.uppercase() else source
                )
                put(PARA_KEYWORD,keyWord)
           }
           mixpanelHelper.trackEvent(EVENT_SEARCH_MIC, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackSearchNoResult(
        keyword: String,
        searchType: String,
        filterLanguage: String,
        filterGenre: String,
        source: String,
        searchCount: String
    ) {
        trackMixPanelSearchNoResult(
            keyword,
            searchType,
            filterLanguage,
            filterGenre,
            source,
            searchCount
        )
        trackMoEngageSearchNoResult(keyword, searchType)
    }

    private fun trackMixPanelSearchNoResult(keyword: String, searchType: String , filterLanguage: String,filterGenre:String, source: String, searchCount: String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_KEYWORD, keyword)
                put(
                    PARA_SEARCH_TYPE,
                    if (searchType.equals(
                            VOICE_SEARCH,
                            true
                        )
                    ) searchType.uppercase() else searchType
                )
                put(PARA_FILTER_GENRE, filterGenre)
                put(PARA_FILTER_LANGUAGE,filterLanguage)
                put(
                    PARA_SOURCE,
                    if (source.equals(
                            VOICE_SEARCH,
                            true
                        )
                    ) source.uppercase() else source
                )
                put(PARA_SEARCH_COUNT,searchCount)
            }
            mixpanelHelper.trackEvent(EVENT_NO_SEARCH_RESULT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackSearchResult(keyword: String, count: Int, source: String, searchType: String,filterLanguage: String,filterGenre: String) {
        trackMixPanelSearchResult(
            keyword,
            count,
            source,
            searchType,
            filterLanguage,
            filterGenre
        )
        trackMoEngageSearchResult(keyword, count, source, searchType)
    }

    private fun trackMixPanelSearchResult(
        keyword: String,
        count: Int,
        source: String,
        searchType: String,
        filterLanguage: String,
        filterGenre:String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_KEYWORD, keyword)
                put(
                    PARA_SOURCE,
                    if (source.equals(
                            VOICE_SEARCH,
                            true
                        )
                    ) source.uppercase() else source
                )
                put(PARA_SEARCH_COUNT, count)
                put(
                    PARA_SEARCH_TYPE,
                    if (searchType.equals(
                            VOICE_SEARCH,
                            true
                        )
                    ) searchType.uppercase() else searchType
                )
                put(PARA_FILTER_LANGUAGE,filterLanguage)
                put(PARA_FILTER_GENRE,filterGenre)
            }
            mixpanelHelper.trackEvent(EVENT_SEARCH_RESULT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelSearchHomeClick(section: String){
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SECTION, section)
            }
            mixpanelHelper.trackEvent(EVENT_SEARCH_HOME_CLICKS, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        }catch (e:Exception){

        }
    }

    private fun trackMixPanelSearchResultClicks(position: String, contenttitle: String){
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_POSITION, position)
                put(CONTENT_TITLE, contenttitle)
            }
            mixpanelHelper.trackEvent(EVENT_SEARCH_RESULT_CLICKS, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        }catch (e:java.lang.Exception){

        }
    }

    private fun trackMixPanelSearchHome(source: String){
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE,source)
            }
            mixpanelHelper.trackEvent(EVENT_SEARCH_HOME, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)
        }catch (e:Exception){

        }
    }

    private fun trackMixPanelSearchResultSwipe(keyword: String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_KEYWORD, keyword)
            }
            mixpanelHelper.trackEvent(
                EVENT_SEARCH_RESULT_SWIPE,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {

        }
    }

    private fun trackMoEngageSearchStart() {
        moEngageHelper.trackEvent(EVENT_SEARCH_START)
    }

    private fun trackMoEngageSearch(keyword: String, source: String, screenName: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_KEYWORD, keyword)
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_SCREEN_NAME, screenName)
            moEngageHelper.trackEvent(EVENT_SEARCH, payloadBuilder)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageSearchVoicePermission(permissionAllowed: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SEARCH_ACCESS, permissionAllowed)
            moEngageHelper.trackEvent(EVENT_SEARCH_VOICE_PERMISSION, payloadBuilder)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageSearchMIC(screenName: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SCREEN_NAME, screenName)
            moEngageHelper.trackEvent(EVENT_SEARCH_MIC, payloadBuilder)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageSearchNoResult(keyword: String, searchType: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_KEYWORD, keyword)
            payloadBuilder.addAttribute(PARA_SEARCH_TYPE, searchType)
            moEngageHelper.trackEvent(EVENT_NO_SEARCH_RESULT, payloadBuilder)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageSearchResult(
        keyword: String,
        count: Int,
        source: String,
        searchType: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_KEYWORD, keyword)
            payloadBuilder.addAttribute(PARA_SEARCH_COUNT, count)
            payloadBuilder.addAttribute(
                PARA_SEARCH_TYPE,
                searchType
            )
            moEngageHelper.trackEvent(EVENT_SEARCH_RESULT, payloadBuilder)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackAppsFlyerLanguageOrGenreScreenView(
        titleOrName: String,
        screenType: String,
        Source: String
    ) {
        try {
            val parameterAndValueMap = java.util.HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
            parameterAndValueMap[PARA_SOURCE] = Source
            if (PARAM_LANGUAGE.equals(screenType, true)) {
                parameterAndValueMap[PARAM_LANGUAGE] = titleOrName
                appsFlyerHelper.trackEvent(EVENT_VIEW_LANGUAGE, parameterAndValueMap)
            }
            else if (PARAM_GENRE.equals(screenType, true)) {
                parameterAndValueMap[PARAM_GENRE] = titleOrName
                appsFlyerHelper.trackEvent(EVENT_VIEW_GENRE, parameterAndValueMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun trackSearchRailWatched(
        railTitle: String,
        railPosition: String,
        pageName: String,
        partnerHome: Boolean,
        partnerName: String,
        railType: String,
        railCategory: String,
        timestamp: String,
        deviceType: String,
        packName: String,
        packPrice: String
    ) {
        trackSearchMoEngageRailWatched(railTitle,railPosition, pageName, partnerHome, partnerName)
        trackSearchMixPanelHorizontalSwipe(pageName, railTitle, railType, railPosition, railCategory,timestamp,deviceType,packName,packPrice)
    }

    private fun trackSearchMixPanelHorizontalSwipe(pageName: String, railTitle: String, railType: String, railPosition: String, railCategory: String,timestamp: String,deviceType: String,packName: String,packPrice: String){
        try {
            val jsonObject = JSONObject().apply {
                put(
                    PARA_PAGE_NAME,
                    if (pageName.equals(PROVIDER_GAMEZOP, true)) SOURCE_GAMES else pageName
                )
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
//                put(
//                    PARA_RAIL_TYPE,
//                    if (railType.equals(EDITORIAL, true))
//                        EDITORIAL
//                    else
//                        railType.lowercase().replaceFirstChar {
//                            it.titlecase(Locale.getDefault())
//                        }
//                )
//                put(PARA_RAIL_CATEGORY, railCategory)
                put(TIMESTAMP, timestamp)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PACK_NAME, packName)
                put(PACK_PRICE, packPrice)
            }
            mixpanelHelper.trackEvent(
                EVENT_RAIL_WATCHED,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackSearchMoEngageRailWatched(railTitle: String, railPosition: String, pageName: String, partnerHome: Boolean, partnerName: String){
        try {
            val properties = Properties()
            properties.addAttribute(PARA_RAIL_POSITION, railPosition)
            properties.addAttribute(PARA_TITLE_RAIL, railTitle)
            properties.addAttribute(PARA_PAGE_NAME, pageName)
            properties.addAttribute(PARA_PARTNER_HOME, if (partnerHome) "YES" else "NO")
            properties.addAttribute(PARA_PARTNER, partnerName)
            moEngageHelper.trackEvent(EVENT_RAIL_WATCHED, properties)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}