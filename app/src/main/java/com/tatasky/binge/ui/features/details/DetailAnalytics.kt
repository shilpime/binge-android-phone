package com.tatasky.binge.ui.features.details

import android.text.TextUtils
import android.util.Log
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.utils.PROVIDER_APPLE
import com.tatasky.binge.utils.PROVIDER_GAMEZOP
import com.tatasky.binge.utils.PROVIDER_PRIME
import com.tatasky.binge.utils.e
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DetailAnalytics(
    private val mixpanelHelper: MixpanelHelper, private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper

) {

    fun trackActivateAppleTvSubscriptionClick(
        title: String,
        type: String,
        genre: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String, /*Editorial, Recommendation*/
        appleRedemptionStatus:String?
    ) {
        trackMixPanelActivateAppleSubscriptionClick(
            title,
            type,
            genre,
            language,
            origin,
            railName,
            source,
            partnerName,
            parentTitle,
            isFreeContent,
            pageName,
            railPosition,
            railType,
            railCategory,
            contentLanguagePrimary,
            contentGenrePrimary,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            releaseYear,
            deviceType,
            actors,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType,
            appleRedemptionStatus
        )

    }

    private fun trackMixPanelActivateAppleSubscriptionClick(
        title: String,
        type: String,
        genres: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String, /*Editorial, Recommendation*/
        appleRedemptionStatus:String?
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railName)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it ?: emptyList<String>()))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_PARENT_TITLE,parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
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
                put(PARA_PACK_NAME, packName)
                put(PARA_AUTO_PLAYED,autoPlayed)
                put(PARA_LIVE_CONTENT,liveContent)
                appleRedemptionStatus?.let {
                    put(PARA_APPLE_COUPON_STATUS,appleRedemptionStatus)
                }

            }
            mixpanelHelper.trackEvent(EVENT_ACTIVATE_APPLE_TV_SUBSCRIPTION_CLICK,jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackApplePlayCTAClick(
        title: String,
        type: String,
        genre: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        contentConfigType: String /*Editorial, Recommendation*/,
        appleRedemptionStatus:String?

    ) {
        trackMixPanelApplePlayCTAClick(
            title,
            type,
            genre,
            language,
            origin,
            railName,
            source,
            partnerName,
            parentTitle,
            isFreeContent,
            pageName,
            railPosition,
            railType,
            railCategory,
            contentLanguagePrimary,
            contentGenrePrimary,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            releaseYear,
            deviceType,
            actors,
            packPrice,
            packName,
            contentConfigType,
            appleRedemptionStatus
        )

    }


    private fun trackMixPanelApplePlayCTAClick(
        title: String,
        type: String,
        genres: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        contentConfigType: String, /*Editorial, Recommendation*/
        appleRedemptionStatus:String?
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railName)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it ?: emptyList<String>()))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_PARENT_TITLE,parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
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
                put(PARA_PACK_NAME, packName)
                appleRedemptionStatus?.let {
                    put(PARA_APPLE_COUPON_STATUS,appleRedemptionStatus)
                }
            }
            mixpanelHelper.trackEvent(EVENT_APPLE_PLAY_CTA_CLICK,jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackAppleActivateNowClick(
        title: String,
        type: String,
        genre: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String, /*Editorial, Recommendation*/
        appleRedemptionStatus:String?,
        durationMinute: String,
        durationSecond: String,
        noOfPauses: String,
        noOfResumes: String,
        seekBarProgress: String,
        videoQuality: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
    ) {
        trackMixPanelAppleActivateNowClick(
            title,
            type,
            genre,
            language,
            origin,
            railName,
            source,
            partnerName,
            parentTitle,
            isFreeContent,
            pageName,
            railPosition,
            railType,
            railCategory,
            contentLanguagePrimary,
            contentGenrePrimary,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            releaseYear,
            deviceType,
            actors,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType,
            appleRedemptionStatus,
            durationMinute,
            durationSecond,
            noOfPauses,
            noOfResumes,
            seekBarProgress,
            videoQuality,
            initialBufferSeconds,
            initialBufferMinutes
        )
    }




    private fun trackMixPanelAppleActivateNowClick(
        title: String,
        type: String,
        genres: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String, /*Editorial, Recommendation*/
        appleRedemptionStatus:String?,
        durationMinute: String,
        durationSecond: String,
        noOfPauses: String,
        noOfResumes: String,
        seekBarProgress: String,
        videoQuality: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railName)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it ?: emptyList<String>()))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_PARENT_TITLE,parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
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
                put(PARA_PACK_NAME, packName)
                put(PARA_AUTO_PLAYED,autoPlayed)
                put(PARA_LIVE_CONTENT,liveContent)
                put(PARA_WATCH_DURATION_SECONDS, durationSecond)
                put(PARA_WATCH_DURATION_MINUTES, durationMinute)
                put(PARA_NUMBER_OF_PAUSE, noOfPauses)
                put(PARA_NUMBER_OF_RESUME, noOfResumes)
                put(PARA_SEEK_BAR_PROGRESS, seekBarProgress)
                put(PARA_VIDEO_QUALITY, videoQuality)
                put(PARA_BUFFER_DURATION_SECONDS, initialBufferSeconds)
                put(PARA_BUFFER_DURATION_MINUTES, initialBufferMinutes)
                appleRedemptionStatus?.let {
                    put(PARA_APPLE_COUPON_STATUS,appleRedemptionStatus)
                }
            }
            mixpanelHelper.trackEvent(EVENT_APPLE_ACTIVATE_NOW_CLICK,jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackLinkAppleTvAccountClick(
        source: String,
        pageName: String,
        deviceType: String,
        packPrice: String,
        packName: String,/*Pack name*/
        appleRedemptionStatus:String?
    ) {
        trackMixPanelLinkAppleTvAccountClick(
            source,
            pageName,
            deviceType,
            packPrice,
            packName,
            appleRedemptionStatus
        )
    }


    private fun trackMixPanelLinkAppleTvAccountClick(
        source: String,
        pageName: String,
        deviceType: String,
        packPrice: String,
        packName: String,/*Pack name*/
        appleRedemptionStatus:String?
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_DEVICE_TYPE,deviceType)
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
                put(PARA_PACK_NAME, packName)
                appleRedemptionStatus?.let {
                    put(PARA_APPLE_COUPON_STATUS,appleRedemptionStatus)
                }
            }
            mixpanelHelper.trackEvent(EVENT_LINK_APPLE_TV_ACCOUNT_CLICK,jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun trackViewContentDetail(
        title: String,
        type: String,
        genre: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/,
        searchKeyword : String,
        searchType : String,
        appleRedemptionStatus: String?=null
    ) {
        trackMixPanelViewContentDetail(
            title,
            type,
            genre,
            language,
            origin,
            railName,
            source,
            partnerName,
            parentTitle,
            isFreeContent,
            pageName,
            railPosition,
            railType,
            railCategory,
            contentLanguagePrimary,
            contentGenrePrimary,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            releaseYear,
            deviceType,
            actors,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType,
            searchKeyword,
            searchType,
            appleRedemptionStatus
        )
        trackMoEngageViewContentDetail(title, type, genre, origin, railName, source,partnerName, parentTitle)
        trackAppsflyerViewContentDetail(title, type, isFreeContent, source, partnerName, language)
    }

    fun trackPlayTrailer(
        title: String,
        type: String,
        genre: List<String>?,
        isSubscribed: String,
        partnerName: String,
        language: List<String>?,
        pageName: String,
        railTitle: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentGenrePrimary: String,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        contentParentTitle: String,
        contentFreeContent: String,
        contentReleaseYear: String,
        deviceType: String,
        actors: String,
        source: String,
        packPrice: String,
        packName: String,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMixPanelPlayTrailer(
            title,
            type,
            genre,
            isSubscribed.uppercase(),
            partnerName,
            language,
            pageName,
            railTitle,
            railPosition,
            railType,
            railCategory,
            contentGenrePrimary,
            contentPartner,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            contentParentTitle,
            contentFreeContent,
            contentReleaseYear,
            deviceType,
            actors,
            source,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType
        )
        trackMoEngagePlayTrailer(title, type, genre, isSubscribed, partnerName)
    }

    fun trackWhatsAppShare(
        title: String,
        type: String,
        genre: List<String>?,
        partnerName: String,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMoEngageWhatsAppShareEvent(title, type, genre, partnerName)
        trackMixPanelWhatsAppShareEvent(
            title,
            type,
            genre,
            partnerName,
            language,
            origin,
            railName,
            source,
            parentTitle,
            isFreeContent,
            pageName,
            railPosition,
            railType,
            railCategory,
            contentLanguagePrimary,
            contentGenrePrimary,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            releaseYear,
            deviceType,
            actors,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType
        )
    }

    fun trackShare(
        title: String,
        type: String,
        genre: List<String>?,
        partnerName: String,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMoEngageShareEvent(title, type, genre, partnerName)
        trackMixPanelShareEvent(
            title,
            type,
            genre,
            partnerName,
            language,
            origin,
            railName,
            source,
            parentTitle,
            isFreeContent,
            pageName,
            railPosition,
            railType,
            railCategory,
            contentLanguagePrimary,
            contentGenrePrimary,
            contentAuth,
            contentCategory,
            contentPosition,
            contentRating,
            releaseYear,
            deviceType,
            actors,
            packPrice,
            packName,
            autoPlayed,
            liveContent,
            contentConfigType
        )
    }

    private fun trackAppsflyerViewContentDetail(
        title: String,
        type: String,
        isFreeContent: Boolean,
        source: String,
        partnerName: String,
        language: List<String>?
    ) {
        try {
            val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
            parameterAndValueMap[PARA_CONTENT_TITLE] = title
            parameterAndValueMap[PARA_CONTENT_TYPE] = type
            parameterAndValueMap[PARA_FREE_CONTENT] = if(isFreeContent) YES else NO
            // Source should be Deeplink if user came from Onelink or Deeplink
            parameterAndValueMap[PARA_SOURCE] = source
            parameterAndValueMap[PARA_PARTNER_NAME] = partnerName
            parameterAndValueMap[PARA_LANGUAGE] = TextUtils.join(", ", language ?: emptyList<String>())
            appsFlyerHelper.trackEvent(EVENT_DETAIL_SCREEN_VISIT, parameterAndValueMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelViewContentDetail(
        title: String,
        type: String,
        genres: List<String>?,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/,
        searchKeyword: String,
        searchType: String,
        appleRedemptionStatus: String?=null
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railName)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it ?: emptyList<String>()))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_PARENT_TITLE,parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
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
                put(PARA_PACK_NAME, packName)
                put(PARA_AUTO_PLAYED,autoPlayed)
                put(PARA_LIVE_CONTENT,liveContent)
                put(PARA_SEARCH_KEYWORD, searchKeyword)
                put(PARA_SEARCH_TYPE,searchType)
            }
            if(partnerName.equals(PROVIDER_APPLE,true)){
                appleRedemptionStatus?.let {
                    jsonObjectUnified.put(PARA_APPLE_COUPON_STATUS,appleRedemptionStatus)
                }
            }
            if (partnerName.equals(PROVIDER_PRIME, true)){
                mixpanelHelper.trackEvent(EVENT_CONTENT_CLICK,jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
            }else{
                mixpanelHelper.trackEvent(EVENT_CONTENT_CLICK,jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
                mixpanelHelper.trackEvent(EVENT_DETAIL_SCREEN_VISIT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelPlayTrailer(
        title: String,
        type: String,
        genres: List<String>?,
        isSubscribed: String,
        partnerName: String,
        language: List<String>?,
        pageName: String,
        railTitle: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentGenrePrimary: String,
        contentPartner: String,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        contentParentTitle: String,
        contentFreeContent: String,
        contentReleaseYear: String,
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
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railTitle)
                put(PARA_RAIL_POSITION, railPosition)
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
                put(PARA_RAIL_CATEGORY, railCategory)
                put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", language ?: emptyList<String>()))
                put(CONTENT_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, contentPartner)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
                put(PARA_FREE_CONTENT, contentFreeContent)
                put(PARA_RELEASE_YEAR, contentReleaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_ACTORS, actors)
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
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
                put(PARA_AUTO_PLAYED, autoPlayed)
                put(PARA_LIVE_CONTENT, liveContent)
            }

            mixpanelHelper.trackEvent(EVENT_PLAY_TRAILER, jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageViewContentDetail(
        title: String,
        type: String,
        genres: List<String>?,
        origin: String,
        railName: String,
        source: String,
        partnerName : String,
        parentTitle: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_CONTENT_TITLE, title)
            payloadBuilder.addAttribute(PARENT_TITLE, parentTitle)
            payloadBuilder.addAttribute(PARA_RAIL, railName)
            payloadBuilder.addAttribute(PARA_ORIGIN, origin)
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(PARA_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
            moEngageHelper.trackEvent(EVENT_DETAIL_SCREEN_VISIT, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngagePlayTrailer(title: String, type: String, genres: List<String>?, isSubscribed: String, partnerName : String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_CONTENT_TITLE, title)
            payloadBuilder.addAttribute(SUBSCRIBED, isSubscribed)
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(PARA_GENRE, TextUtils.join(", ", genres ?: emptyList<String>()))
            moEngageHelper.trackEvent(EVENT_PLAY_TRAILER, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageWhatsAppShareEvent(title: String, type: String, genre: List<String>?, partnerName : String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_CONTENT_TITLE, title)
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(PARA_GENRE, TextUtils.join(", ", genre ?: emptyList<String>()))
            moEngageHelper.trackEvent(EVENT_SHARE_WHATSAPP, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelWhatsAppShareEvent(
        title: String,
        type: String,
        genre: List<String>?,
        partnerName: String,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, railName)
                put(PARA_RAIL_POSITION, railPosition)
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
                put(PARA_RAIL_CATEGORY, railCategory)
                put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", language ?: emptyList<String>()))
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", genre ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, partnerName)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_ACTORS, TextUtils.join(", ", actors ?: emptyList<String>()))
                put(PARA_CONTENT_PARENT_TITLE, parentTitle)
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
                put(PARA_PACK_PRICE, packPrice)
                put(PARA_PACK_NAME, packName)
                put(PARA_LIVE_CONTENT, liveContent)
            }
            mixpanelHelper.trackEvent(EVENT_WHATSAPP_SHARE_CLICK, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageShareEvent(title: String, type: String, genre: List<String>?, partnerName : String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_CONTENT_TITLE, title)
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(CONTENT_GENRE, TextUtils.join(", ", genre ?: emptyList<String>()))
            moEngageHelper.trackEvent(EVENT_SHARE, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun trackMixPanelShareEvent(
        title: String,
        type: String,
        genre: List<String>?,
        partnerName: String,
        language: List<String>?,
        origin: String,
        railName: String,
        source: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
        railPosition: String,
        railType: String,
        railCategory: String,
        contentLanguagePrimary: String?,
        contentGenrePrimary: String?,
        contentAuth: String,
        contentCategory: String,
        contentPosition: String,
        contentRating: String,
        releaseYear: String,
        deviceType: String,
        actors: List<String>?,
        packPrice: String,
        packName: String/*Pack name*/,
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL,railName)
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
                put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", language ?: emptyList<String>()))
                put(PARA_CONTENT_LANGUAGE_PRIMARY,contentLanguagePrimary)
                put(PARA_CONTENT_GENRE, TextUtils.join(", ", genre ?: emptyList<String>()))
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_TITLE,title)
                put(PARA_FREE_CONTENT,if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
                put(PARA_DEVICE_TYPE,deviceType)
                put(PARA_ACTORS, TextUtils.join(", ", actors ?: emptyList<String>()))
                put(PARA_CONTENT_PARENT_TITLE, parentTitle)
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
                put(PARA_PACK_NAME, packName)
                put(PARA_LIVE_CONTENT,liveContent)
            }
            mixpanelHelper.trackEvent(EVENT_SHARE_CLICK, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun trackDetailsRailWatched(
        railTitle: String,
        railPosition: String,
        pageName: String,
        partnerName: String,
        railType: String,
        railCategory: String,
        timestamp: String,
        deviceType: String,
        packName: String,
        packPrice: String
    ) {
        trackDetailsMoEngageRailWatched(railTitle,railPosition, pageName, partnerName)
        trackDetailsMixPanelHorizontalSwipe(pageName, railTitle, railType, railPosition, railCategory,timestamp,deviceType,packName,packPrice)
    }
    private fun trackDetailsMixPanelHorizontalSwipe(pageName: String, railTitle: String, railType: String, railPosition: String, railCategory: String,timestamp: String,deviceType: String,packName: String,packPrice: String){
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

    private fun trackDetailsMoEngageRailWatched(railTitle: String, railPosition: String, pageName: String, partnerName: String){
        try {
            val properties = Properties()
            properties.addAttribute(PARA_RAIL_POSITION, railPosition)
            properties.addAttribute(PARA_TITLE_RAIL, railTitle)
            properties.addAttribute(PARA_PAGE_NAME, pageName)
            properties.addAttribute(PARA_PARTNER, partnerName)
            moEngageHelper.trackEvent(EVENT_RAIL_WATCHED, properties)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}
