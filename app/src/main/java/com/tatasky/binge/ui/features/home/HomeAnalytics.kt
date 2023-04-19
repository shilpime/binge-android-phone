package com.tatasky.binge.ui.features.home

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.utils.ContentUtil.isLiveContent
import com.tatasky.binge.utils.EventConstants
import com.tatasky.binge.utils.FREE
import com.tatasky.binge.utils.PROVIDER_PRIME
import com.tatasky.binge.utils.TYPE_GENRE
import com.tatasky.binge.utils.TYPE_LANGUAGE
import com.tatasky.binge.utils.PROVIDER_GAMEZOP
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HomeAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper,
    private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
    private val facebookAnalyticsHelper: FacebookAnalyticsHelper
) {


    fun trackGameNotificationNudgeClick() {
        trackMixpanelGameNotificationNudgeClick()
    }

    private fun trackMixpanelGameNotificationNudgeClick() {
        try {
            mixpanelHelper.trackEvent(
                EVENT_NOTIFICATION_NUDGE_CLICK,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackMenuCategoryClick(categoryName: String) {
        trackMixPanelMenuCategoryClick(categoryName)
    }

    private fun trackMixPanelMenuCategoryClick(categoryName: String) {
        try {
            val jsonObjectUnified = JSONObject()
            jsonObjectUnified.put(CATEGORY_NAME, categoryName)
            mixpanelHelper.trackEvent(
                MENU_CATEGORY,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackContentLanguageBottomSheetOpen(source: String) {
        trackMixPanelContentLanguageBottomSheetOpen(source)
    }

    private fun trackMixPanelContentLanguageBottomSheetOpen(source: String) {
        try {
            val jsonObjectUnified = JSONObject()
            jsonObjectUnified.put(PARA_SOURCE, source)
            mixpanelHelper.trackEvent(
                EVENT_CONTENT_LANG_OPEN,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackContentLanguageBottomSheetSkip() {
        trackMixPanelContentLanguageSkip()
        trackMoEngageContentLanguageSkip()

    }

    private fun trackMoEngageContentLanguageSkip() {
        moEngageHelper.trackEvent(EVENT_CONTENT_LANG_SKIP)
    }

    fun trackContentLanguageBottomSheetExpand() {
        trackMixPanelContentLanguageExpand()
    }

    fun trackContentLanguageSelected(
        source: String,
        lang1: String?,
        lang2: String?,
        lang3: String?,
        lang4: String?
    ) {
        trackMixPanelBottomSheetContentLanguageSelect(source, lang1, lang2, lang3, lang4)
        trackMixPanelContentLanguageBottomSheetProceed()
    }

    private fun trackMixPanelContentLanguageBottomSheetProceed() {
        try {
            mixpanelHelper.trackEvent(
                EVENT_CONTENT_LANG_PROCEED,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackShuffleClick(
        railType: String,
        pageName: String,
        railTitle: String,
        railPosition: String
    ) {
        trackMixPanelShuffleClick(
            railType,
            pageName,
            railTitle,
            railPosition
        )
    }


    fun trackAddWatchList(
        pageName: String,
        railTitle: String,
        railPosition: String,
        contentType: String,
        railType: String,
        railCategory: String,
        contentLanguage: String,
        contentLanguagePrimary: String?,
        contentGenre: String,
        contentGenrePrimary: String?,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        contentTitle: String,
        freeContent: String,
        releaseYear: String,
        deviceType: String,
        actors: String,
        contentParentTitle: String,
        source: String,
        packPrice: String,
        packName: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ){
        trackMixPanelAddWatchList(
            pageName,
            railTitle,
            railPosition,
            contentType,
            railType,
            railCategory,
            contentLanguage,
            contentLanguagePrimary,
            contentGenre,
            contentGenrePrimary,
            contentPartner,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            contentTitle,
            freeContent,
            releaseYear,
            deviceType,
            actors,
            contentParentTitle,
            source,
            packPrice,
            packName,
            liveContent,
            contentConfigType
        )
    }




    fun trackRemoveWatchList(
        pageName: String,
        railTitle: String,
        railPosition: String,
        contentType: String,
        railType: String,
        railCategory: String,
        contentLanguage: String,
        contentLanguagePrimary: String?,
        contentGenre: String,
        contentGenrePrimary: String?,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        contentTitle: String,
        freeContent: String,
        releaseYear: String,
        deviceType: String,
        actors: String,
        contentParentTitle: String,
        source: String,
        packPrice: String,
        packName: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ){
        trackMixPanelRemoveWatchList(
            pageName,
            railTitle,
            railPosition,
            contentType,
            railType,
            railCategory,
            contentLanguage,
            contentLanguagePrimary,
            contentGenre,
            contentGenrePrimary,
            contentPartner,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            contentTitle,
            freeContent,
            releaseYear,
            deviceType,
            actors,
            contentParentTitle,
            source,
            packPrice,
            packName,
            liveContent,
            contentConfigType
        )
    }

    /**
     * Tracks opened tab item
     */
    fun trackBottomTabItemSelected(tabName: String?) {
        tabName?.let {
            trackAppsFlyerBottomTabItemSelected(it.uppercase(Locale.getDefault()))
            trackMixPanelPageClick(it)
            trackMoengagePageClick(it)
        }
    }

    private fun trackAppsFlyerBottomTabItemSelected(tabName: String) {
        appsFlyerHelper.trackEvent(tabName)
    }

    fun trackHomeInitiate(pageName:String, subscribed: Boolean) {
        trackMixPanelHomeInitiate(pageName, subscribed)
        trackMoEngageHomeInitiate(pageName, subscribed)
        trackAppsFlyerHomePageView(pageName.toUpperCase(Locale.getDefault()))
    }

    /**
     * Tracks opened tab/page/menu item
     */
    private fun trackAppsFlyerHomePageView(pageName: String) {
        appsFlyerHelper.trackEvent(pageName)
    }


    fun trackAccountInitiate() {
        trackMixPanelAccountInitiate()
        trackMoEngageAccountInitiate()
        trackAppsFlyerMyAccountView()
    }

    private fun trackAppsFlyerMyAccountView() {
        appsFlyerHelper.trackEvent(EVENT_ACCOUNT_SCREEN_VISIT)
    }

    fun trackRailWatched(
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
        trackMoEngageRailWatched(railTitle,railPosition, pageName, partnerHome, partnerName)
        trackMixPanelHorizontalSwipe(pageName, railTitle, railType, railPosition, railCategory,timestamp,deviceType,packName,packPrice)
    }

    private fun trackMixPanelHorizontalSwipe(pageName: String, railTitle: String, railType: String, railPosition: String, railCategory: String,timestamp: String,deviceType: String,packName: String,packPrice: String){
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

    private fun trackMoEngageRailWatched(railTitle: String, railPosition: String, pageName: String, partnerHome: Boolean, partnerName: String){
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


    fun trackWatchlistInitiate() {
        trackMixPanelWatchlistInitiate()
        trackMoEngageWatchlistInitiate()
    }

    fun trackTransactionHistoryInitiate() {
        trackMixPanelTransactionHistoryInitiate()
        trackMoEngageTransactionHistoryInitiate()
    }

    fun trackHBView(
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
        contentConfigType: String,
        configType: String
    ) {
        trackMixPanelHBView(
            timestamp,
            contentTitle,
            bannerPosition,
            contentType,
            pageName,
            partnerHome,
            partner,
            railType,
            railCategory,
            contentLanguage,
            contentLanguagePrimary,
            contentGenre,
            contentGenrePrimary,
            contentPartner,
            contentAuth,
            contentCategory,
            contentRating,
            contentParentTitle,
            freeContent,
            releaseYear,
            deviceType,
            actors,
            source,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType.uppercase()
        )
       trackMoEngageHBView(bannerPosition, configType, contentTitle, contentType, partnerHome, pageName, partner)
    }

    fun trackContentClick(
        section: String,
        itemPosition: String,
        sectionPosition: String,
        iListItem: ContentItem,
        railTitle: String,
        partnerHomePage: Boolean,
        pageName: String,
        configType: String,
        cardConfigType: String,
        sharedPrefs: PrefsRepo,
        sectionType: String/*To track use case of rail type, Language, Genre, Apps rail etc.*/,
        contentAuth: String,
        pageOffSet:Int
    ) {
        when {
            sectionType.equals(
                ItemViewType.LANGUAGE.name,
                true
            ) -> trackMixPanelBrowseByLanguageRailClick(
                pageName,
                railTitle,
                sectionPosition,
                iListItem.title,
                itemPosition,
                sharedPrefs.getDeviceType() ?: "",
                sharedPrefs.getSubscribedPack()?.amountValue,
                sharedPrefs.getSubscribedPack()?.productName,
                (pageOffSet/10)+1,
                iListItem.title
            )
            sectionType.equals(
                ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                true
            ) -> trackGameBBGRailClick(
                pageName,
                railTitle,
                sectionPosition,
                iListItem.title,
                itemPosition,
                sharedPrefs.getDeviceType()?.uppercase()?:"",
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                "${(pageOffSet/10)+1}"
            )
            sectionType.equals(
                ItemViewType.GENRE.name,
                true
            ) -> trackMixPanelBrowseByGenreRailClick(
                pageName,
                railTitle,
                sectionPosition,
                iListItem.title,
                itemPosition,
                sharedPrefs.getDeviceType() ?: "",
                sharedPrefs.getSubscribedPack()?.amountValue,
                sharedPrefs.getSubscribedPack()?.productName,
                (pageOffSet/10)+1,
                iListItem.title
            )
            sectionType.equals(EventConstants.TYPE_APPS, true) -> trackMixPanelAppsRailClick(
                pageName,
                railTitle,
                sectionPosition,
                configType,
                iListItem.title,
                sharedPrefs.getDeviceType() ?: "",
                sharedPrefs.getSubscribedPack()?.amountValue,
                itemPosition,
                sharedPrefs.getSubscribedPack()?.productName
            )
            else -> {
                trackMixPanelContentClick(
                    section,
                    itemPosition,
                    sectionPosition,
                    iListItem,
                    railTitle,
                    partnerHomePage,
                    pageName,
                    configType,
                    cardConfigType,
                    sharedPrefs,
                    contentAuth
                )
            }
        }
        trackMoEngageHomeClick(
            section,
            itemPosition,
            sectionPosition,
            iListItem,
            railTitle,
            partnerHomePage,
            pageName,
            configType,
            cardConfigType
        )
    }

    fun trackGameSeeAll(
        pageName: String,
        railTitle: String,
        railPosition: String,
        gamePartner: String,
        railType: String,
        railCategory: String
    ) {
        trackMixpanelGameSeeAll(
            pageName,
            railTitle,
            railPosition,
            gamePartner,
            railType,
            railCategory
        )
    }

    private fun trackMixpanelGameSeeAll(
        pageName: String,
        railTitle: String,
        railPosition: String,
        gamePartner: String,
        railType: String,
        railCategory: String
    ) {
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(PARA_RAIL_TYPE, railType)
                put(PARA_RAIL_CATEGORY, railCategory)
                put(PARA_GAME_PARTNER, gamePartner)
            }

            mixpanelHelper.trackEvent(
                EVENT_SEE_ALL,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun trackHomeSeeAll(pageName:String,
                        configType: String,
                        partner:String,
                        railTitle: String,
                        railPosition:String,
                        partnerHome: Boolean,
                        railType: String,
                        railCategory: String,
                        partnerHomePage: Boolean) {
        trackMixPanelHomeSeeAll(pageName, configType, partner, railTitle, railPosition, partnerHome,railType,railCategory,partnerHomePage)
        trackMoEngageHomeSeeAll(pageName, configType, partner, railTitle, railPosition, partnerHomePage)
    }

    private fun trackMixPanelBottomSheetContentLanguageSelect(
        source: String,
        lang1: String?,
        lang2: String?,
        lang3: String?,
        lang4: String?
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_SOURCE, source)
                put(PARA_LANGUAGE1, lang1)
                put(PARA_LANGUAGE2, lang2)
                put(PARA_LANGUAGE3, lang3)
                put(PARA_LANGUAGE4, lang4)
            }
            mixpanelHelper.trackEvent(
                EVENT_CONTENT_LANG_SELECT,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {

        }
    }

    private fun trackMixPanelContentLanguageExpand() {
        mixpanelHelper.trackEvent(EVENT_CONTENT_LANG_EXPAND, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelContentLanguageSkip() {
        mixpanelHelper.trackEvent(EVENT_CONTENT_LANG_SKIP, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMixPanelHomeInitiate(pageName: String, subscribed : Boolean) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(NAME, pageName)
            jsonObject.put(PARA_VIEW_TYPE, if(subscribed) EVENT_SUBSCRIBED else EVENT_UNSUBSCRIBED)
            mixpanelHelper.trackEvent(EVENT_HOME_SCREEN_VISIT, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelAccountInitiate() {
        try {
            mixpanelHelper.trackEvent(EVENT_ACCOUNT_SCREEN_VISIT)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelWatchlistInitiate() {
        try {
            mixpanelHelper.trackEvent(EVENT_WATCHLIST_SCREEN_VISIT)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    //EVENT_CONTENT_CLICK - Disabled here Now it is getting tracked from DetailsAnalytics
    private fun trackMixPanelContentClick(
        section: String /*Rail, Hero banner*/,
        itemPosition: String,
        sectionPosition: String /*Rail position*/,
        iListItem: ContentItem,
        railTitle: String,
        partnerHomePage: Boolean,
        pageName: String,
        configType: String /*Rail config type, Editorial, Recommendation, Language*/,
        cardConfigType: String /*Content config type, Editorial, Recommendation*/,
        sharedPrefs: PrefsRepo,
        contentAuth: String
    ) {
        try {
            val jsonUnifiedObject = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, sectionPosition)
                put(
                    PARA_CONTENT_TYPE,
                    if (cardConfigType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        cardConfigType
                )
                put(
                    PARA_RAIL_TYPE,
                    if (configType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        configType
                )
                put(PARA_RAIL_CATEGORY, iListItem.railCategory)
                put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", iListItem.language))
                put(PARA_CONTENT_LANGUAGE_PRIMARY, iListItem.language.getOrNull(0))
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", iListItem.genres))
                put(PARA_CONTENT_GENRE_PRIMARY, iListItem.genres.getOrNull(0))
                put(PARA_CONTENT_PARTNER, iListItem.provider)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, iListItem.contentType)
                put(PARA_CONTENT_POSITION, itemPosition)
                put(PARA_CONTENT_RATING, iListItem.masterRating)
                put(PARA_CONTENT_PARENT_TITLE, iListItem.seriesTitle?:iListItem.title)
                put(PARA_CONTENT_TITLE, iListItem.title)
                put(
                    PARA_FREE_CONTENT, if (iListItem.partnerSubscriptionType?.contains(
                            FREE,
                            true
                        ) == true
                    ) YES else NO
                )
                put(PARA_RELEASE_YEAR, iListItem.releaseYear?:"")
                put(PARA_DEVICE_TYPE, sharedPrefs.getDeviceType() ?: "")
                put(PARA_ACTORS, iListItem.actor?:"")
                put(PARA_SOURCE, iListItem.source)
                put(PARA_PACK_PRICE, sharedPrefs.getSubscribedPack()?.amountValue)
                put(PARA_PACK_NAME, sharedPrefs.getSubscribedPack()?.productName)
                put(PARA_AUTO_PLAYED, NO /*For Prime there is not PI Screen, So making it by default No*/)
                put(
                    PARA_LIVE_CONTENT,
                    if (isLiveContent(iListItem.contentType, iListItem.liveContent)) YES else NO
                )
                put(PARA_SEARCH_KEYWORD, iListItem.searchKeyword)
                put(PARA_SEARCH_TYPE,iListItem.refId)
            }
            if (iListItem.provider.equals(PROVIDER_PRIME, true))
                mixpanelHelper.trackEvent(EVENT_CONTENT_CLICK, jsonUnifiedObject, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelBrowseByLanguageRailClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        langugeSelected: String,
        contentPosition: String,
        deviceType: String,
        packPrice: String?,
        packName:String?,
        numberOfTimeSwiped: Int,
        filterSelected: String
    ){
        try{
            val jsonObject = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(LANGUAGE_SELECTED, langugeSelected)
                put(RAIL_LANGUAGE_POSITION, contentPosition)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_PACK_PRICE, packPrice?: FREEMIUM)
                put(PARA_PACK_NAME,packName ?: FREEMIUM)
                put(PAGE_RESULT_SWIPE, numberOfTimeSwiped)
                put(PARA_FILTER_SELECTED,filterSelected)
            }
            mixpanelHelper.trackEvent(EVENT_BROWSE_BY_LANGUAGE_RAIL_CLICK,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    fun trackGameBBGRailClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        genreSelected: String,
        railGenrePosition: String,
        deviceType: String,
        packPrice: String,
        packName: String,
        pageResultSwipe: String
    ) {
        trackMixPanelGameBBGRailClick(
            pageName,
            railTitle,
            railPosition,
            genreSelected,
            railGenrePosition,
            deviceType,
            packPrice,
            packName,
            pageResultSwipe
        )
    }

    private fun trackMixPanelGameBBGRailClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        genreSelected: String,
        railGenrePosition: String,
        deviceType: String,
        packPrice: String,
        packName: String,
        pageResultSwipe: String
    ) {
        try {
            var newPageName = pageName
            if(pageName.equals(PROVIDER_GAMEZOP,true)){
                newPageName = SOURCE_GAMES
            }
            val jsonObject = JSONObject().apply {
                put(PARA_PAGE_NAME, newPageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
                put(PARA_GENRE_SELECTED, genreSelected)
                put(PARA_GAME_RAIL_GENERE_POSITION, railGenrePosition)
                put(PARA_PAGE_RESULT_SWIPE, pageResultSwipe)
            }
            mixpanelHelper.trackEvent(
                EVENT_GAME_BROWSE_BY_GENRE_RAIL_CLICK,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun trackMixPanelBrowseByGenreRailClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        genreSelected: String,
        contentPosition: String,
        deviceType: String,
        packPrice: String?,
        packName:String?,
        numberOfTimeSwiped: Int,
        filterSelected: String
    ){
        try{
            val jsonObject = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(GENRE_SELECTED, genreSelected)
                put(RAIL_GENRE_POSITION, contentPosition)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME,packName)
                put(PAGE_RESULT_SWIPE, numberOfTimeSwiped)
                put(PARA_FILTER_SELECTED,filterSelected)
            }
            mixpanelHelper.trackEvent(EVENT_BROWSE_BY_GENRE_RAIL_CLICK,jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    private fun trackMixPanelAppsRailClick(
        pageName: String,
        railTitle: String,
        railPosition: String,
        railType: String,
        contentPartner: String,
        deviceType: String,
        packPrice: String?,
        contentPartnerPosition: String,
        packName: String?
    ) {
        try {
            val jsonObject = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(
                    PARA_RAIL_TYPE,
                    if (railType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        railType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(PARA_CONTENT_PARTNER, contentPartner)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_PACK_PRICE, packPrice ?: FREEMIUM)
                put(PARA_PACK_NAME,packName ?: FREEMIUM)
                put(PARA_CONTENT_PARTNER_POSITION, contentPartnerPosition)
            }
            mixpanelHelper.trackEvent(
                EVENT_APPS_RAIL_CLICK,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelHomeSeeAll(
        pageName:String,
        configType: String,
        partner:String,
        railTitle: String,
        railPosition:String,
        partnerHome: Boolean,
        railType: String,
        railCategory: String,
        partnerHomePage: Boolean
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_PAGE_NAME, pageName)
            jsonObject.put(PARA_CONFIG_TYPE, configType)
            jsonObject.put(PARA_PARTNER, partner)
            jsonObject.put(PARA_TITLE_RAIL, railTitle)
            jsonObject.put(PARA_RAIL_POSITION, railPosition)
            jsonObject.put(PARA_PARTNER_HOME, if(partnerHomePage) YES else NO)
            mixpanelHelper.trackEvent(EVENT_VALUE_SEE_ALL, jsonObject,mixpanelHelper.mMixpanelAPI)

            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_CONFIG_TYPE, configType)
                put(PARA_PARTNER, partner)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                //put(PARA_PARTNER_HOME,if (partnerHome) YES else NO)
                put(
                    PARA_RAIL_TYPE,
                    if (railType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        railType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(PARA_RAIL_CATEGORY,railCategory)
                put(PARA_PARTNER_HOME, if (partnerHomePage) YES else NO)
            }
            mixpanelHelper.trackEvent(EVENT_VALUE_SEE_ALL, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageHomeInitiate(pageName: String, subscribed: Boolean) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(NAME, pageName)
            payloadBuilder.addAttribute(PARA_VIEW_TYPE, if(subscribed) EVENT_SUBSCRIBED else EVENT_UNSUBSCRIBED)
            moEngageHelper.trackEvent(EVENT_HOME_SCREEN_VISIT, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageAccountInitiate() {
        moEngageHelper.trackEvent(EVENT_ACCOUNT_SCREEN_VISIT)
    }

    private fun trackMoEngageWatchlistInitiate() {
        moEngageHelper.trackEvent(EVENT_WATCHLIST_SCREEN_VISIT)
    }

    private fun trackMoEngageHomeClick(
        section: String,
        itemPosition: String,
        sectionPosition: String,
        iListItem: ContentItem,
        railTitle: String,
        partnerHomePage: Boolean,
        pageName : String,
        configType: String,
        cardConfigType: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SECTION, section)
            payloadBuilder.addAttribute(
                PARA_HERO_BANNER_NUMBER,
                if (section.equals(EVENT_VALUE_RAIL_HB)) itemPosition else ""
            )
            payloadBuilder.addAttribute(PARA_CONFIG_TYPE, configType)
            payloadBuilder.addAttribute(PARA_CARD_CONFIG_TYPE, cardConfigType)
            payloadBuilder.addAttribute(
                PARA_CONTENT_TYPE, if (iListItem.contentType.contains(TYPE_LANGUAGE, true))
                    TYPE_LANGUAGE
                else if (iListItem.contentType.contains(TYPE_GENRE, true))
                    TYPE_GENRE
                else
                    iListItem.contentType
            )
            payloadBuilder.addAttribute(PARA_PARTNER, iListItem.provider)
            payloadBuilder.addAttribute(PARA_PAGE_NAME, pageName)
            payloadBuilder.addAttribute(PARA_CONTENT_TITLE, iListItem.title)
            payloadBuilder.addAttribute(PARA_TITLE_RAIL, railTitle)
            payloadBuilder.addAttribute(PARA_GENRE, TextUtils.join(", ", iListItem.genres))
            payloadBuilder.addAttribute(PARA_RAIL_POSITION, sectionPosition)
            payloadBuilder.addAttribute(
                PARA_CONTENT_POSITION,
                if (!section.equals(EVENT_VALUE_RAIL_HB)) itemPosition else ""
            )
            payloadBuilder.addAttribute(PARA_PARTNER_HOME, if (partnerHomePage) "YES" else "NO")
            moEngageHelper.trackEvent(EVENT_HOME_CLICK, payloadBuilder)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageHomeSeeAll(pageName:String, configType: String, partner:String, railTitle: String, railPosition:String, partnerHomePage: Boolean) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_PAGE_NAME, pageName)
            payloadBuilder.addAttribute(PARA_CONFIG_TYPE, configType)
            payloadBuilder.addAttribute(PARA_PARTNER, partner)
            payloadBuilder.addAttribute(PARA_TITLE_RAIL, railTitle)
            payloadBuilder.addAttribute(PARA_RAIL_POSITION, railPosition)
            payloadBuilder.addAttribute(PARA_PARTNER_HOME, if(partnerHomePage) "YES" else "NO")
            moEngageHelper.trackEvent(EVENT_SEE_ALL, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageTransactionHistoryInitiate() {
        moEngageHelper.trackEvent(EVENT_TRANSACTION_HISTORY_VISIT)
    }

    private fun trackMixPanelTransactionHistoryInitiate() {
        mixpanelHelper.trackEvent(EVENT_TRANSACTION_HISTORY_VISIT)
    }

    fun trackNudgeClick(dthStatus : String, source : String, counterValue : String, displayedOn : String, freeTrialAvailed : Boolean, nudgeType:String){
        trackMixPanelNudgeClick(dthStatus, source, counterValue, displayedOn, freeTrialAvailed, nudgeType)
        trackMoEngageNudgeClick(dthStatus, source, counterValue, displayedOn, freeTrialAvailed, nudgeType)
    }

    private fun trackMoEngageNudgeClick(dthStatus : String, source : String, counterValue : String, displayedOn : String, freeTrialAvailed : Boolean, nudgeType: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_COUNTER_VALUE, counterValue)
            payloadBuilder.addAttribute(PARA_DTH_STATUS, dthStatus)
            payloadBuilder.addAttribute(PARA_DISPLAYED_ON_DAY, displayedOn)
            payloadBuilder.addAttribute(PARA_FREE_TRIAL_AVAILED, if(freeTrialAvailed) "YES" else "NO")
            payloadBuilder.addAttribute(PARA_NUDGE_TYPE,nudgeType)
            moEngageHelper.trackEvent(
                EVENT_NUDGE_CLICk,
                payloadBuilder
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelNudgeClick(dthStatus : String, source : String, counterValue : String, displayedOn : String, freeTrialAvailed : Boolean, nudgeType: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_SOURCE, source)
            jsonObject.put(PARA_COUNTER_VALUE, counterValue)
            jsonObject.put(PARA_DTH_STATUS, dthStatus)
            jsonObject.put(PARA_DISPLAYED_ON_DAY, displayedOn)
            jsonObject.put(PARA_FREE_TRIAL_AVAILED, if(freeTrialAvailed) YES else NO)
            jsonObject.put(PARA_NUDGE_TYPE,nudgeType)
            mixpanelHelper.trackEvent(EVENT_NUDGE_CLICk, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelHBView(
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
                EVENT_BANNER_VIEW,
                jsonObject,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageHBView(hbNumber : String, configType : String, contentTitle : String, contentType : String, partnerHome : Boolean, pageName: String, partner: String){
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_HERO_BANNER_NUMBER, hbNumber)
            payloadBuilder.addAttribute(PARA_CONTENT_TITLE, contentTitle)
            payloadBuilder.addAttribute(PARA_CONFIG_TYPE, configType)
            payloadBuilder.addAttribute(PARA_CONTENT_TYPE, contentType)
            payloadBuilder.addAttribute(PARA_PAGE_NAME, pageName)
            payloadBuilder.addAttribute(PARA_PARTNER_HOME, if(partnerHome) "YES" else "NO")
            payloadBuilder.addAttribute(PARA_PARTNER, partner)
            moEngageHelper.trackEvent(
                EVENT_BANNER_VIEW,
                payloadBuilder
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackRecommendedUpdateCheck(){
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(ANDROID_APP_VERSION, BuildConfig.VERSION_CODE)
            moEngageHelper.trackEvent(
                "RECOMMENDED-UPDATE-CHECK",
                payloadBuilder
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackFirstClick(iListItem: ContentItem){
        val bundle = Bundle()
        bundle.putString(PARA_CONTENT_TITLE, iListItem.title)
        bundle.putString(PARA_CONTENT_TYPE, iListItem.contentType)
        bundle.putString(PARA_PARTNER_NAME, iListItem.provider)
        facebookAnalyticsHelper.trackEvent(EVENT_FIRST_CONTENT_CLICK, bundle)
        firebaseAnalyticsHelper.trackEvent(EVENT_FIRST_CONTENT_CLICK, bundle)
    }

    private fun trackMixPanelShuffleClick(
        railType: String,
        pageName: String,
        railTitle: String,
        railPosition: String
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(
                    PARA_RAIL_TYPE,
                    if (railType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        railType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
            }

            mixpanelHelper.trackEvent(
                EVENT_SHUFFLE_CLICK,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        }catch (e:Exception){

        }
    }

    private fun trackMoengagePageClick(pageName: String) {
        try {
            if(pageName.equals(SOURCE_GAMES,true)){
                moEngageHelper.trackEvent(GAME_PAGE_VISITED)
            }
            val properties = Properties()
            properties.addAttribute(OPTION, pageName)
            moEngageHelper.trackEvent(MENU_BOTTOM, properties)
        } catch (e: Exception) {

        }
    }


    private fun trackMixPanelPageClick(pageName: String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
            }
            mixpanelHelper.trackEvent(
                EVENT_PAGE_CLICK,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )

            val jsonObj = JSONObject()
            jsonObj.put(
                OPTION,
                if (pageName.contains(SOURCE_CATEGORY, true)) CATEGORIES.lowercase() else pageName
            )
            mixpanelHelper.trackEvent(
                MENU_BOTTOM,
                jsonObj,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {

        }
    }

    private fun trackMixPanelAddWatchList(
        pageName: String,
        railTitle: String,
        railPosition: String,
        contentType: String,
        railType: String,
        railCategory: String,
        contentLanguage: String,
        contentLanguagePrimary: String?,
        contentGenre: String,
        contentGenrePrimary: String?,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        contentTitle: String,
        freeContent: String,
        releaseYear: String,
        deviceType: String,
        actors: String,
        contentParentTitle: String,
        source: String,
        packPrice: String,
        packName: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ){
        try{
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(
                    PARA_CONTENT_TYPE,
                    if (contentConfigType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        contentConfigType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(
                    PARA_RAIL_TYPE,
                    if (railType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        railType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(PARA_RAIL_CATEGORY, railCategory)
                put(PARA_CONTENT_LANGUAGE, contentLanguage)
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, contentGenre)
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, contentPartner)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, contentType)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_TITLE, contentTitle)
                put(PARA_FREE_CONTENT, freeContent)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_ACTORS, actors)
                put(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
                put(PARA_SOURCE, source)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
                put(PARA_LIVE_CONTENT, liveContent)
            }

            mixpanelHelper.trackEvent(
                EVENT_ADD_WATCHLIST,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        }catch (e: Exception){

        }
    }

    private fun trackMixPanelRemoveWatchList(
        pageName: String,
        railTitle: String,
        railPosition: String,
        contentType: String,
        railType: String,
        railCategory: String,
        contentLanguage: String,
        contentLanguagePrimary: String?,
        contentGenre: String,
        contentGenrePrimary: String?,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        contentTitle: String,
        freeContent: String,
        releaseYear: String,
        deviceType: String,
        actors: String,
        contentParentTitle: String,
        source: String,
        packPrice: String,
        packName: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ){
        try{
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
                put(
                    PARA_CONTENT_TYPE,
                    if (contentConfigType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        contentConfigType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(
                    PARA_RAIL_TYPE,
                    if (railType.equals(EDITORIAL, true))
                        EDITORIAL
                    else
                        railType.lowercase().replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                )
                put(PARA_RAIL_CATEGORY, railCategory)
                put(PARA_CONTENT_LANGUAGE, contentLanguage)
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, contentGenre)
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, contentPartner)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, contentType)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_TITLE, contentTitle)
                put(PARA_FREE_CONTENT, freeContent)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_ACTORS, actors)
                put(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
                put(PARA_SOURCE, source)
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
                put(PARA_LIVE_CONTENT, liveContent)
            }

            mixpanelHelper.trackEvent(
                EVENT_REMOVE_WATCHLIST,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        }catch (e: Exception){

        }
    }
}