package com.tatasky.binge.ui.features

import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.utils.PROVIDER_GAMEZOP
import com.tatasky.binge.utils.e
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MiscAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper,
    private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
    private val facebookAnalyticsHelper: FacebookAnalyticsHelper
){
    fun trackAppRattingPopupInitiate(source: String?,packName: String?,packPrice: String?) {
        trackMixPanelAppRattingPopupInitiate(source,packName,packPrice)
    }

    private fun trackMixPanelAppRattingPopupInitiate(
        source: String?,
        packName: String?,
        packPrice: String?
    ) {

        val jsonObject=JSONObject().apply {
            put(PARA_SOURCE,source)
            put(PARA_PACK_NAME,packName)
            put(PARA_PACK_PRICE,packPrice)
        }

        mixpanelHelper.trackEvent(
            APP_RATING_POP_UP_INITIATE,jsonObject,
            mixpanelHelper.mMixpanelUnifiedAPI)

    }


    fun trackAppRattingPopupYes(source: String?,packName: String?,packPrice: String?) {
        trackMixPanelAppRattingPopupYes(source,packName,packPrice)
    }

    private fun trackMixPanelAppRattingPopupYes(
        source: String?,
        packName: String?,
        packPrice: String?
    ) {

        val jsonObject=JSONObject().apply {
            put(PARA_SOURCE,source)
            put(PARA_PACK_NAME,packName)
            put(PARA_PACK_PRICE,packPrice)
        }

        mixpanelHelper.trackEvent(
            APP_RATING_POP_UP_YES,jsonObject,
            mixpanelHelper.mMixpanelUnifiedAPI)

    }



    fun trackAppRattingPopupNotReally(source: String?,packName: String?,packPrice: String?) {
        trackMixPanelAppRattingPopupNotReally(source,packName,packPrice)
    }

    private fun trackMixPanelAppRattingPopupNotReally(
        source: String?,
        packName: String?,
        packPrice: String?
    ) {

        val jsonObject=JSONObject().apply {
            put(PARA_SOURCE,source)
            put(PARA_PACK_NAME,packName)
            put(PARA_PACK_PRICE,packPrice)
        }

        mixpanelHelper.trackEvent(
            APP_RATING_POP_UP_NOT_REALLY,jsonObject,
            mixpanelHelper.mMixpanelUnifiedAPI)

    }




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


    fun trackHelpCenterClick() {
        trackMixpanelHelpCenterClick()
    }

    private fun trackMixpanelHelpCenterClick() {
        mixpanelHelper.trackEvent(
            HC_HAMBURGER_CLICK,
            mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelMidScrollClick() {
        mixpanelHelper.trackEvent(EVENT_MIDSCROLL_CLICK, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelRenewalNudgeClick() {
        mixpanelHelper.trackEvent(EVENT_RENEWAL_NUDGE_CLICK, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSubscriberFreeTrialNudgeClick(day:String) {
        val jsonObject=JSONObject().apply {
            put(PARA_DAY,day)
        }
        mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_FREETRIAL_NUDGE_CLICK,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSubscriberFreemiumNudgeClick() {
        mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_FREEMIUM_NUDGE_CLICK, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelNotificationNudgeClick() {
        mixpanelHelper.trackEvent(EVENT_NOTIFICATION_NUDGE_CLICK, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelEmailNudgeClick() {
        mixpanelHelper.trackEvent(EVENT_EMAIL_NUDGE_CLICK, mixpanelHelper.mMixpanelUnifiedAPI)
    }
    fun trackMixPanelStartFreeTrial(source:String) {
        val jsonObject=JSONObject().apply {
        put(PARA_SOURCE,source)
        }
        mixpanelHelper.trackEvent(EVENT_START_FREE_TRIAL,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSusbscribePopup() {
        mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_POPUP,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSusbscribePopupSubscribe(contentTitle:String) {
        val jsonObject=JSONObject().apply {
            put(PARA_CONTENT_TITLE,contentTitle)
        }
        mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_POPUP_SUBSCRIBE,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSusbscribePopupCancel() {
        mixpanelHelper.trackEvent(EVENT_SUBSCRIBE_POPUP_CANCEL,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelUpgradePopup() {
        mixpanelHelper.trackEvent(EVENT_UPGRADE_POPUP,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelUpgradePopupUpgrade(contentTitle:String,contentPartner:String) {
        val jsonObject=JSONObject().apply {
            put(PARA_CONTENT_TITLE,contentTitle)
            put(PARA_CONTENT_PARTNER,contentPartner)
        }
        mixpanelHelper.trackEvent(EVENT_UPGRADE_POPUP_UPGRADE,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelUpgradePopupCancel() {
        mixpanelHelper.trackEvent(EVENT_UPGRADE_POPUP_CANCEL,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelTrailerAutoplay(
        pageName:String,
        railTitle:String,
        railPosition:String,
        contentType:String,
        railType:String,
        railCategory:String,
        contentLanguage:String,
        contentLanguagePrimary: String,
        contentGenre:String,
        contentGenrePrimary: String?,
        contentPartner:String,
        contentAuth:String,
        contentCategory:String,
        contentPosition:String,
        contentRating:String,
        contentParentTitle:String,
        contentTitle: String,
        contentFreeContent:String,
        contentReleaseYear:String,
        deviceType:String,
        actors:String,
        source: String,
        packPrice:String,
        packName:String,
        autoPlayed:String,
        liveContent:String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        val jsonObjects = JSONObject().apply {
            put(PARA_PAGE_NAME,pageName)
            put(PARA_TITLE_RAIL,railTitle)
            put(PARA_RAIL_POSITION,railPosition)
            put(
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
            put(
                PARA_RAIL_TYPE,
                if (railType.equals(EDITORIAL, true))
                    EDITORIAL
                else
                    railType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
            )
            put(PARA_RAIL_CATEGORY,railCategory)
            put(PARA_CONTENT_LANGUAGE,contentLanguage)
            put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
            put(PARA_CONTENT_GENRE,contentGenre)
            put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
            put(PARA_CONTENT_PARTNER,contentPartner)
            put(PARA_CONTENT_AUTH,contentAuth)
            put(PARA_CONTENT_CATEGORY, contentType)
            put(PARA_CONTENT_POSITION,contentPosition)
            put(PARA_CONTENT_RATING,contentRating)
            put(PARA_CONTENT_PARENT_TITLE,contentParentTitle)
            put(PARA_CONTENT_TITLE,contentTitle)
            put(PARA_FREE_CONTENT,contentFreeContent)
            put(PARA_RELEASE_YEAR,contentReleaseYear)
            put(PARA_DEVICE_TYPE,deviceType)
            put(PARA_ACTORS,actors)
            put(
                PARA_SOURCE,
                if (EVENT_VALUE_SOURCE_GENRE.equals(
                        source,
                        true
                    )
                )
                    source.uppercase()
                else
                    source
            )
            put(PARA_PACK_PRICE,packPrice)
            put(PARA_PACK_NAME,packName)
            put(PARA_AUTO_PLAYED,autoPlayed)
            put(PARA_LIVE_CONTENT,liveContent)
        }
        mixpanelHelper.trackEvent(EVENT_TRAILER_AUTOPLAY,jsonObjects,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSynopsisMoreClick(
        pageName:String,
        railTitle:String,
        railPosition:String,
        contentType:String,
        railType:String,
        railCategory:String,
        contentLanguage:String,
        contentLanguagePrimary: String,
        contentGenre:String,
        contentGenrePrimary: String?,
        contentPartner:String,
        contentAuth:String,
        contentCategory:String,
        contentPosition:String,
        contentRating:String,
        contentParentTitle:String,
        contentTitle: String,
        contentFreeContent:String,
        contentReleaseYear:String,
        deviceType:String,
        actors:String,
        source: String,
        packPrice:String,
        packName:String,
        autoPlayed:String,
        liveContent:String,
        contentConfigType: String /*Editorial, Recommendation*/
        ) {
        val jsonObject=JSONObject().apply {
            put(PARA_PAGE_NAME,pageName)
            put(PARA_TITLE_RAIL,railTitle)
            put(PARA_RAIL_POSITION,railPosition)
            put(
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
            put(
                PARA_RAIL_TYPE,
                if (railType.equals(EDITORIAL, true))
                    EDITORIAL
                else
                    railType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
            )
            put(PARA_RAIL_CATEGORY,railCategory)
            put(PARA_CONTENT_LANGUAGE,contentLanguage)
            put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
            put(PARA_CONTENT_GENRE,contentGenre)
            put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
            put(PARA_CONTENT_PARTNER,contentPartner)
            put(PARA_CONTENT_AUTH,contentAuth)
            put(PARA_CONTENT_CATEGORY, contentType)
            put(PARA_CONTENT_POSITION,contentPosition)
            put(PARA_CONTENT_RATING,contentRating)
            put(PARA_CONTENT_PARENT_TITLE,contentParentTitle)
            put(PARA_CONTENT_TITLE,contentTitle)
            put(PARA_FREE_CONTENT,contentFreeContent)
            put(PARA_RELEASE_YEAR,contentReleaseYear)
            put(PARA_DEVICE_TYPE,deviceType)
            put(PARA_ACTORS,actors)
            put(
                PARA_SOURCE,
                if (EVENT_VALUE_SOURCE_GENRE.equals(
                        source,
                        true
                    )
                )
                    source.uppercase()
                else
                    source
            )
            put(PARA_PACK_PRICE,packPrice)
            put(PARA_PACK_NAME,packName)
            put(PARA_AUTO_PLAYED,autoPlayed)
            put(PARA_LIVE_CONTENT,liveContent)
        }
        mixpanelHelper.trackEvent(EVENT_SYNOPSIS_MORE_CLICK,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)

    }

    fun trackMixPanelSeeAllEpisodes(
        pageName:String,
        railTitle:String,
        railPosition:String,
        contentType:String,
        railType:String,
        railCategory:String,
        contentLanguage:String,
        contentLanguagePrimary: String,
        contentGenre:String,
        contentGenrePrimary: String?,
        contentPartner:String,
        contentAuth:String,
        contentCategory:String,
        contentPosition:String,
        contentRating:String,
        contentParentTitle:String,
        contentTitle: String,
        contentFreeContent:String,
        contentReleaseYear:String,
        deviceType:String,
        actors:String,
        source: String,
        packPrice:String,
        packName:String,
        autoPlayed:String,
        liveContent:String,
        contentConfigType: String /*Editorial, Recommendation*/
    ){
        val jsonObject=JSONObject().apply {
            put(PARA_PAGE_NAME,pageName)
            put(PARA_TITLE_RAIL,railTitle)
            put(PARA_RAIL_POSITION,railPosition)
            put(
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
            put(
                PARA_RAIL_TYPE,
                if (railType.equals(EDITORIAL, true))
                    EDITORIAL
                else
                    railType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
            )
            put(PARA_RAIL_CATEGORY,railCategory)
            put(PARA_CONTENT_LANGUAGE,contentLanguage)
            put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
            put(PARA_CONTENT_GENRE,contentGenre)
            put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
            put(PARA_CONTENT_PARTNER,contentPartner)
            put(PARA_CONTENT_AUTH,contentAuth)
            put(PARA_CONTENT_CATEGORY, contentType)
            put(PARA_CONTENT_POSITION,contentPosition)
            put(PARA_CONTENT_RATING,contentRating)
            put(PARA_CONTENT_PARENT_TITLE,contentParentTitle)
            put(PARA_CONTENT_TITLE,contentTitle)
            put(PARA_FREE_CONTENT,contentFreeContent)
            put(PARA_RELEASE_YEAR,contentReleaseYear)
            put(PARA_DEVICE_TYPE,deviceType)
            put(PARA_ACTORS,actors)
            put(
                PARA_SOURCE,
                if (EVENT_VALUE_SOURCE_GENRE.equals(
                        source,
                        true
                    )
                )
                    source.uppercase()
                else
                    source
            )
            put(PARA_PACK_PRICE,packPrice)
            put(PARA_PACK_NAME,packName)
            put(PARA_AUTO_PLAYED,autoPlayed)
            put(PARA_LIVE_CONTENT,liveContent)
        }
        mixpanelHelper.trackEvent(EVENT_SEE_ALL_EPISODES,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)

    }

    fun trackMixPanelHomePageView(name: String, source: String = "", drpEnabled: String) {
        val jsonObject = JSONObject().apply {
            put(NAME, name)
            if (!source.isEmpty()) put(PARA_SOURCE, source)
            put(PARA_DRP_ENABLE, drpEnabled)
        }

        mixpanelHelper.trackEvent(
            EVENT_HOME_PAGE_VIEW,
            jsonObject,
            mixpanelHelper.mMixpanelUnifiedAPI
        )
    }

    fun trackMixPanelMenuClick(){
        mixpanelHelper.trackEvent(EVENT_MENU_CLICK,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelMenuOption(){
        mixpanelHelper.trackEvent(EVENT_MENU_OPTION,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSettingsVisit(){
        mixpanelHelper.trackEvent(EVENT_SETTINGS_VISITS,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelSettingsMenuOption(){
        mixpanelHelper.trackEvent(EVENT_SETTINGS_MENU_OPTION,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackMixPanelHeroBannerClicks(
        timestamp: String,
        contentTitle: String,
        bannerPosition: String,
        contentType: String,
        pageName: String,
        partnerHome: Boolean,
        partner: String,
        railType: String,
        railCategory: String,
        contentLanguage: String,
        contentLanguagePrimary: String,
        contentGenre: String,
        contentGenrePrimary: String?,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentRating: String,
        contentParentTitle: String,
        freeContent: String,
        releaseYear: String,
        deviceType: String,
        actors: String,
        source: String,
        packPrice: String,
        packName: String,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String
    ) {
        try {
            val jsonObject = JSONObject().apply {
                put(TIMESTAMP, timestamp)
                put(PARA_HERO_BANNER_NUMBER, bannerPosition)
                put(PARA_PAGE_NAME, pageName)
                put(
                    PARA_CONTENT_TYPE,
                    if (contentConfigType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        contentConfigType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(PARA_CONTENT_LANGUAGE, contentLanguage)
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, contentGenre)
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, partner)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, contentType)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
                put(PARA_CONTENT_TITLE, contentTitle)
                put(PARA_FREE_CONTENT, freeContent)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_ACTORS, actors)
                put(PARA_SOURCE, source)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
                put(PARA_AUTO_PLAYED, autoPlayed)
                put(PARA_LIVE_CONTENT, liveContent)
            }
            mixpanelHelper.trackEvent(
                EVENT_HERO_BANNER_CLICKS,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun trackGameHeroBannerClick(
        timestamp: String,
        heroBannerNumber: String,
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
        packName: String,
        packPrice: String
    ) {
        trackMixpanelGameHeroBannerClick(
            timestamp,
            heroBannerNumber,
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
            packName,
            packPrice
        )
    }

    private fun trackMixpanelGameHeroBannerClick(
        timestamp: String,
        heroBannerNumber: String,
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
        packName: String,
        packPrice: String
    ) {
        try {
            val jsonObject = JSONObject().apply {
                put(TIMESTAMP,timestamp)
                put(PARA_HERO_BANNER_NUMBER,heroBannerNumber)
                put(PARA_PAGE_NAME, pageName)
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
                put(PARA_SOURCE, source)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
            }

            mixpanelHelper.trackEvent(
                EVENT_GAMES_VIEW_HERO_BANNER,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }






}