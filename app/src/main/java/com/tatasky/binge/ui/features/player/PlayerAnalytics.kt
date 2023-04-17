package com.tatasky.binge.ui.features.player

import android.os.Bundle
import android.text.TextUtils
import com.tatasky.binge.utils.Properties
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.utils.VTR_PERCENTAGE_50
import com.tatasky.binge.utils.VTR_PERCENTAGE_75
import com.tatasky.binge.utils.RENTAL
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class PlayerAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper,
    private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
    private val facebookAnalyticsHelper: FacebookAnalyticsHelper
) {

    fun trackViewThroughRateContentPlayEvent(
        contentTitle: String,
        contentParentTitle: String,
        contentLanguage: String,
        contentType: String,
        durationSecond: String,
        durationMinute: String,
        contentCategory: String,
        partnerName: String,
        vtrPercentage: String
    ) {
        trackAppsFlyerViewThroughRateContentPlayEvent(
            contentTitle,
            contentParentTitle,
            contentLanguage,
            contentType,
            durationSecond,
            durationMinute,
            contentCategory,
            partnerName,
            vtrPercentage
        )
        trackFacebookViewThroughRateContentPlayEvent(
            contentTitle,
            contentParentTitle,
            contentLanguage,
            contentType,
            durationSecond,
            durationMinute,
            contentCategory,
            partnerName,
            vtrPercentage
        )
        trackFirebaseViewThroughRateContentPlayEvent(
            contentTitle,
            contentParentTitle,
            contentLanguage,
            contentType,
            durationSecond,
            durationMinute,
            contentCategory,
            partnerName,
            vtrPercentage
        )
    }

    private fun trackFacebookViewThroughRateContentPlayEvent(
        contentTitle: String,
        contentParentTitle: String,
        contentLanguage: String,
        contentType: String,
        durationSecond: String,
        durationMinute: String,
        contentCategory: String,
        partnerName: String,
        vtrPercentage: String
    ) {
        try {
            val bundle = Bundle()
            bundle.putString(PARA_CONTENT_TITLE, contentTitle)
            bundle.putString(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
            bundle.putString(PARA_CONTENT_LANGUAGE, contentLanguage)
            bundle.putString(PARA_CONTENT_TYPE, contentType)
            bundle.putString(PARA_DURATION_SECONDS, durationSecond)
            bundle.putString(PARA_DURATION_MINUTES, durationMinute)
            bundle.putString(PARA_CONTENT_CATEGORY, contentCategory)
            bundle.putString(PARA_PARTNER_NAME, partnerName)
            when (vtrPercentage) {
                VTR_PERCENTAGE_50 -> facebookAnalyticsHelper.trackEvent(
                    EVENT_PLAY_CONTENT_PREMIUM_VTR50,
                    bundle
                )
                VTR_PERCENTAGE_75 -> facebookAnalyticsHelper.trackEvent(
                    EVENT_PLAY_CONTENT_PREMIUM_VTR75,
                    bundle
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackFirebaseViewThroughRateContentPlayEvent(
        contentTitle: String,
        contentParentTitle: String,
        contentLanguage: String,
        contentType: String,
        durationSecond: String,
        durationMinute: String,
        contentCategory: String,
        partnerName: String,
        vtrPercentage: String
    ) {
        try {
            val bundle = Bundle()
            bundle.putString(PARA_CONTENT_TITLE, contentTitle)
            bundle.putString(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
            bundle.putString(PARA_CONTENT_LANGUAGE, contentLanguage)
            bundle.putString(PARA_CONTENT_TYPE, contentType)
            bundle.putString(PARA_DURATION_SECONDS, durationSecond)
            bundle.putString(PARA_DURATION_MINUTES, durationMinute)
            bundle.putString(PARA_CONTENT_CATEGORY, contentCategory)
            bundle.putString(PARA_PARTNER_NAME, partnerName)
            when (vtrPercentage) {
                VTR_PERCENTAGE_50 -> firebaseAnalyticsHelper.trackEvent(
                    EVENT_PLAY_CONTENT_PREMIUM_VTR50,
                    bundle
                )
                VTR_PERCENTAGE_75 -> firebaseAnalyticsHelper.trackEvent(
                    EVENT_PLAY_CONTENT_PREMIUM_VTR75,
                    bundle
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackAppsFlyerViewThroughRateContentPlayEvent(
        contentTitle: String,
        contentParentTitle: String,
        contentLanguage: String,
        contentType: String,
        durationSecond: String,
        durationMinute: String,
        contentCategory: String,
        partnerName: String,
        vtrPercentage: String
    ) {
        try {
            val parameterAndValueMap =
                HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
            parameterAndValueMap[CONTENT_TITLE] = contentTitle
            parameterAndValueMap[PARA_CONTENT_PARENT_TITLE] = contentParentTitle
            parameterAndValueMap[PARA_CONTENT_LANGUAGE] = contentLanguage
            parameterAndValueMap[CONTENT_TYPE] = contentType
            parameterAndValueMap[PARA_DURATION_SECONDS] = durationSecond
            parameterAndValueMap[PARA_DURATION_MINUTES] = durationMinute
            parameterAndValueMap[PARA_CONTENT_CATEGORY] = contentCategory
            parameterAndValueMap[PARA_PARTNER_NAME] = partnerName
            when (vtrPercentage) {
                VTR_PERCENTAGE_50 -> appsFlyerHelper.trackEvent(
                    EVENT_PLAY_CONTENT_PREMIUM_VTR50,
                    parameterAndValueMap
                )
                VTR_PERCENTAGE_75 -> appsFlyerHelper.trackEvent(
                    EVENT_PLAY_CONTENT_PREMIUM_VTR75,
                    parameterAndValueMap
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackContentPlayEnd(
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        partnerName: String,
        rail: String/*Rail name/title*/,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        contractName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMixPanelContentPlayEnd(
            title,
            genre,
            type,
            startTime,
            stopTime,
            durationMinute,
            durationSecond,
            initialBufferSeconds,
            initialBufferMinutes,
            noOfPauses,
            noOfResumes,
            partnerName,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            parentTitle,
            isFreeContent,
            pageName,
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
            autoPlayed,
            liveContent,
            seekBarProgress,
            videoQuality,
            contentConfigType
        )
        if (!isFreeContent && !(contractName.equals(RENTAL, true))) {
            trackFacebookContentPlay(
                title,
                parentTitle,
                language,
                type,
                durationSecond,
                durationMinute,
                genre,
                contentConfigType,
                partnerName
            )
            trackFirebaseContentPlay(
                title,
                parentTitle,
                language,
                type,
                durationSecond,
                durationMinute,
                genre,
                contentConfigType,
                partnerName
            )
        }
    }

    private fun trackFacebookContentPlay(
        contentTitle: String,
        contentParentTitle: String,
        contentLanguage: List<String>?,
        contentType: String,
        durationSecond: String,
        durationMinute: String,
        contentGenre: List<String>?,
        contentCategory: String,
        partnerName: String
    ) {
        try {
            val bundle = Bundle()
            bundle.putString(PARA_CONTENT_TITLE, contentTitle)
            bundle.putString(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
            bundle.putString(PARA_CONTENT_LANGUAGE, contentLanguage?.let { TextUtils.join(", ", it) })
            bundle.putString(PARA_CONTENT_TYPE, contentType)
            bundle.putString(PARA_DURATION_SECONDS, durationSecond)
            bundle.putString(PARA_DURATION_MINUTES, durationMinute)
            bundle.putString(CONTENT_GENRE, contentGenre?.let { TextUtils.join(", ", it) })
            bundle.putString(PARA_CONTENT_CATEGORY, contentCategory)
            bundle.putString(PARA_PARTNER_NAME, partnerName)
            facebookAnalyticsHelper.trackEvent(EVENT_PLAY_CONTENT_PREMIUM, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackFirebaseContentPlay(
        contentTitle: String,
        contentParentTitle: String,
        contentLanguage: List<String>?,
        contentType: String,
        durationSecond: String,
        durationMinute: String,
        contentGenre: List<String>?,
        contentCategory: String,
        partnerName: String
    ) {
        try {
            val bundle = Bundle()
            bundle.putString(PARA_CONTENT_TITLE, contentTitle)
            bundle.putString(PARA_CONTENT_PARENT_TITLE, contentParentTitle)
            bundle.putString(PARA_CONTENT_LANGUAGE, contentLanguage?.let { TextUtils.join(", ", it) })
            bundle.putString(PARA_CONTENT_TYPE, contentType)
            bundle.putString(PARA_DURATION_SECONDS, durationSecond)
            bundle.putString(PARA_DURATION_MINUTES, durationMinute)
            bundle.putString(CONTENT_GENRE, contentGenre?.let { TextUtils.join(", ", it) })
            bundle.putString(PARA_CONTENT_CATEGORY, contentCategory)
            bundle.putString(PARA_PARTNER_NAME, partnerName)
            firebaseAnalyticsHelper.trackEvent(EVENT_PLAY_CONTENT_PREMIUM, bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelContentPlayEnd(
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        partnerName: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            /*Unified Mixpanel*/
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, rail)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                genre?.let {
                    put(PARA_CONTENT_GENRE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, partnerName)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_PARENT_TITLE, parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                actors?.let {
                    put(PARA_ACTORS, TextUtils.join(", ", it))
                }
                put(PARA_SOURCE, source)
                put(PARA_BUFFER_DURATION_SECONDS, initialBufferSeconds)
                put(PARA_BUFFER_DURATION_MINUTES, initialBufferMinutes)
                put(PARA_START_TIME, startTime)
                put(PARA_STOP_TIME, stopTime)
                put(PARA_NUMBER_OF_PAUSE, noOfPauses)
                put(PARA_NUMBER_OF_RESUME, noOfResumes)
                put(PARA_PACK_PRICE, pack?.amountValue?: FREEMIUM)
                put(PACK_NAME, pack?.productName?: FREEMIUM)
                put(PARA_AUTO_PLAYED, autoPlayed)
                put(PARA_LIVE_CONTENT, liveContent)
                put(PARA_SEEK_BAR_PROGRESS, seekBarProgress)
                put(PARA_VIDEO_QUALITY, videoQuality)
                put(PARA_WATCH_DURATION_SECONDS, durationSecond)
                put(PARA_WATCH_DURATION_MINUTES, durationMinute)
            }
            mixpanelHelper.trackEvent(
                EVENT_CONTENT_PLAY_END,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**This will be fired when content starts playing after Player starts loading and till time the content starts playing*/
    fun trackInitialBufferTime(
        durationSecond: String,
        durationMinute: String,
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: Int,
        noOfResumes: Int,
        partnerName: String,
        rail: String/*Rail name/title*/,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        contractName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMixPanelInitialBufferTime(
            durationSecond,
            durationMinute,
            title,
            genre,
            type,
            startTime,
            stopTime,
            initialBufferSeconds,
            initialBufferMinutes,
            noOfPauses,
            noOfResumes,
            partnerName,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            contractName,
            parentTitle,
            isFreeContent,
            pageName,
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
            autoPlayed,
            liveContent,
            seekBarProgress,
            videoQuality,
            contentConfigType
        )
        trackMoEngageInitialBufferTime(durationSecond, durationMinute, title, genre, type)
    }

    private fun trackMixPanelInitialBufferTime(
        durationSecond: String,
        durationMinute: String,
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: Int,
        noOfResumes: Int,
        partnerName: String,
        rail: String/*Rail name/title*/,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        contractName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, rail)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                genre?.let {
                    put(PARA_CONTENT_GENRE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, partnerName)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_PARENT_TITLE, parentTitle)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_ACTORS, actors)
                put(PARA_SOURCE, source)
                put(PARA_BUFFER_DURATION_SECONDS, initialBufferSeconds)
                put(PARA_BUFFER_DURATION_MINUTES, initialBufferMinutes)
                put(PARA_START_TIME, startTime)
                put(PARA_STOP_TIME, stopTime)
                put(PARA_NUMBER_OF_PAUSE, noOfPauses)
                put(PARA_NUMBER_OF_RESUME, noOfResumes)
                put(PARA_PACK_PRICE, pack?.amountValue?: FREEMIUM)
                put(PACK_NAME, pack?.productName?: FREEMIUM)
                put(PARA_AUTO_PLAYED, autoPlayed)
                put(PARA_LIVE_CONTENT, liveContent)
                put(PARA_SEEK_BAR_PROGRESS, seekBarProgress)
                put(PARA_VIDEO_QUALITY, videoQuality)
//                put(PARA_WATCH_DURATION_SECONDS, durationSecond)
//                put(PARA_WATCH_DURATION_MINUTES, durationMinute)
                put(PARA_CONTENT_TITLE, title)
            }
            mixpanelHelper.trackEvent(EVENT_INITIAL_BUFFER_TIME, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /*For Prime we are not tracking parentTitle*/
    fun trackPlayContent(
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        partnerName: String,
        rail: String/*Rail name/title*/,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        contractName: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMoEngagePlayContent(
            title,
            genre,
            type,
            startTime,
            stopTime,
            durationMinute,
            durationSecond,
            initialBufferSeconds,
            initialBufferMinutes,
            noOfPauses,
            noOfResumes,
            partnerName,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            parentTitle
        )
        trackMixPanelPlayContent(
            title,
            genre,
            type,
            startTime,
            stopTime,
            durationMinute,
            durationSecond,
            initialBufferSeconds,
            initialBufferMinutes,
            noOfPauses,
            noOfResumes,
            partnerName,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            parentTitle,
            isFreeContent,
            pageName,
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
            autoPlayed,
            liveContent,
            seekBarProgress,
            videoQuality,
            contentConfigType
        )
        trackAppsFlyerPlayContent(
            type,
            title,
            durationSecond,
            durationMinute,
            partnerName,
            contractName,
            isFreeContent
        )
    }

    private fun trackAppsFlyerPlayContent(
        contentType: String,
        contentTitle: String,
        durationSecond: String,
        durationMinute: String,
        partnerName: String,
        contractName: String,
        isFreeContent: Boolean
    ) {
        try {
            val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
            parameterAndValueMap[CONTENT_TITLE] = contentTitle
            parameterAndValueMap[CONTENT_TYPE] = contentType
            parameterAndValueMap[PARA_DURATION_SECONDS] = durationSecond
            parameterAndValueMap[PARA_DURATION_MINUTES] = durationMinute
            parameterAndValueMap[PARA_PARTNER_NAME] = partnerName
            when {
                contractName == RENTAL -> {
                    // TVoD Content
                    appsFlyerHelper.trackEvent(EVENT_PLAY_CONTENT_TVOD, parameterAndValueMap)
                }
                isFreeContent -> appsFlyerHelper.trackEvent(EVENT_PLAY_CONTENT_FREEMIUM, parameterAndValueMap) //Free content
                else -> appsFlyerHelper.trackEvent(EVENT_PLAY_CONTENT_PREMIUM, parameterAndValueMap) //Paid content
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelPlayContent(
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        partnerName: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            /*Unified Mixpanel*/
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, rail)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                genre?.let {
                    put(PARA_CONTENT_GENRE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, partnerName)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_PARENT_TITLE, parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                actors?.let {
                    put(PARA_ACTORS, TextUtils.join(", ", it))
                }
                put(PARA_SOURCE, source)
                put(PARA_BUFFER_DURATION_SECONDS, initialBufferSeconds)
                put(PARA_BUFFER_DURATION_MINUTES, initialBufferMinutes)
                put(PARA_START_TIME, startTime)
                put(PARA_STOP_TIME, stopTime)
                put(PARA_NUMBER_OF_PAUSE, noOfPauses)
                put(PARA_NUMBER_OF_RESUME, noOfResumes)
                put(PARA_PACK_PRICE, pack?.amountValue ?: FREEMIUM)
                put(PACK_NAME, pack?.productName ?: FREEMIUM)
                put(PARA_AUTO_PLAYED, autoPlayed)
                put(PARA_LIVE_CONTENT, liveContent)
                put(PARA_SEEK_BAR_PROGRESS, seekBarProgress)
                put(PARA_VIDEO_QUALITY, videoQuality)
//                put(PARA_WATCH_DURATION_SECONDS, durationSecond)
//                put(PARA_WATCH_DURATION_MINUTES, durationMinute)
            }
            mixpanelHelper.trackEvent(
                EVENT_CONTENT_PLAY,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackPauseContent(
        title: String,
        genre: List<String>?,
        type: String,
        partnerName: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMixPanelPauseContent(
            title,
            genre,
            type,
            partnerName,
            startTime,
            stopTime,
            durationMinute,
            durationSecond,
            initialBufferSeconds,
            initialBufferMinutes,
            noOfPauses,
            noOfResumes,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            parentTitle,
            isFreeContent,
            pageName,
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
            autoPlayed,
            liveContent,
            seekBarProgress,
            videoQuality,
            contentConfigType
        )
        trackMoEngagePauseContent(title, genre, type, partnerName)
    }

    private fun trackMixPanelPauseContent(
        title: String,
        genre: List<String>?,
        type: String,
        partnerName: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, rail)
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
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY,contentLanguagePrimary)
                genre?.let {
                    put(PARA_CONTENT_GENRE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_PARENT_TITLE,parentTitle)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
                put(PARA_DEVICE_TYPE,deviceType)
                actors?.let {
                    put(PARA_ACTORS, TextUtils.join(", ", it))
                }
                put(PARA_SOURCE,source)
                put(PARA_BUFFER_DURATION_SECONDS,initialBufferSeconds)
                put(PARA_BUFFER_DURATION_MINUTES,initialBufferMinutes)
                put(PARA_START_TIME,startTime)
                put(PARA_STOP_TIME,stopTime)
                put(PARA_NUMBER_OF_PAUSE, noOfPauses)
                put(PARA_NUMBER_OF_RESUME, noOfResumes)
                put(PARA_PACK_PRICE,pack?.amountValue?: FREEMIUM)
                put(PACK_NAME, pack?.productName?: FREEMIUM)
                put(PARA_AUTO_PLAYED,autoPlayed)
                put(PARA_LIVE_CONTENT,liveContent)
                put(PARA_SEEK_BAR_PROGRESS,seekBarProgress)
                put(PARA_VIDEO_QUALITY,videoQuality)
//                put(PARA_WATCH_DURATION_SECONDS, durationSecond)
//                put(PARA_WATCH_DURATION_MINUTES,durationMinute)
                put(PARA_CONTENT_TITLE, title)
            }
            mixpanelHelper.trackEvent(EVENT_PAUSE_CONTENT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackResumeContent(
        title: String,
        genre: List<String>?,
        type: String,
        partnerName: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMixPanelResumeContent(
            title,
            genre,
            type,
            partnerName,
            startTime,
            stopTime,
            durationMinute,
            durationSecond,
            initialBufferSeconds,
            initialBufferMinutes,
            noOfPauses,
            noOfResumes,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            parentTitle,
            isFreeContent,
            pageName,
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
            autoPlayed,
            liveContent,
            seekBarProgress,
            videoQuality,
            contentConfigType
        )
        trackMoEngageResumeContent(title, genre, type, partnerName)
    }

    private fun trackMixPanelResumeContent(
        title: String,
        genre: List<String>?,
        type: String,
        partnerName: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        seekBarProgress: String,
        videoQuality: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, rail)
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
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY,contentLanguagePrimary)
                genre?.let {
                    put(PARA_CONTENT_GENRE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_GENRE_PRIMARY,contentGenrePrimary)
                put(PARA_CONTENT_PARTNER,partnerName)
                put(PARA_CONTENT_AUTH,contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION,contentPosition)
                put(PARA_CONTENT_RATING,contentRating)
                put(PARA_CONTENT_PARENT_TITLE,parentTitle)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR,releaseYear)
                put(PARA_DEVICE_TYPE,deviceType)
                actors?.let {
                    put(PARA_ACTORS, TextUtils.join(", ", it))
                }
                put(PARA_SOURCE,source)
                put(PARA_BUFFER_DURATION_SECONDS,initialBufferSeconds)
                put(PARA_BUFFER_DURATION_MINUTES,initialBufferMinutes)
                put(PARA_START_TIME,startTime)
                put(PARA_STOP_TIME,stopTime)
                put(PARA_NUMBER_OF_PAUSE, noOfPauses)
                put(PARA_NUMBER_OF_RESUME, noOfResumes)
                put(PARA_PACK_PRICE,pack?.amountValue?: FREEMIUM)
                put(PACK_NAME, pack?.productName?: FREEMIUM)
                put(PARA_AUTO_PLAYED,autoPlayed)
                put(PARA_LIVE_CONTENT,liveContent)
                put(PARA_SEEK_BAR_PROGRESS,seekBarProgress)
                put(PARA_VIDEO_QUALITY,videoQuality)
//                put(PARA_WATCH_DURATION_SECONDS, durationSecond)
//                put(PARA_WATCH_DURATION_MINUTES,durationMinute)
                put(PARA_CONTENT_TITLE, title)
            }
            mixpanelHelper.trackEvent(EVENT_RESUME_CONTENT, jsonObjectUnified,mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun trackPlaybackFailure(
        title: String,
        genre: List<String>?,
        type: String,
        errorMsg: String,
        partnerName: String,
        rail: String/*Rail name/title*/,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        trackMixPanelPlaybackFailure(
            title,
            genre,
            type,
            errorMsg,
            partnerName,
            rail,
            origin,
            source,
            language,
            pack,
            railPosition,
            parentTitle,
            isFreeContent,
            pageName,
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
            autoPlayed,
            liveContent,
            contentConfigType
        )
        trackMoEngagePlaybackFailure(title, genre, type, errorMsg)
    }

    /**
     * Track player playback errors with error details
     */
    fun trackPlaybackError(
        errorCode: String,
        errorMsg: String,
        origin: String,
        type: String,
        partnerName: String
    ) {
        trackMixPanelPlaybackError(errorCode, errorMsg, origin, type, partnerName)
        trackMoEngagePlaybackError(errorCode, errorMsg, origin, type, partnerName)
    }

    private fun trackMixPanelPlaybackError(
        errorCode: String,
        errorMsg: String,
        origin: String,
        type: String,
        partnerName: String
    ) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_ERROR_CODE, errorCode)
            jsonObject.put(PARA_ERROR_MESSAGE, errorMsg)
            jsonObject.put(PARA_ORIGIN, origin)
            jsonObject.put(PARA_TYPE, type)
            jsonObject.put(PARA_PARTNER, partnerName)
            mixpanelHelper.trackEvent(EVENT_ERROR, jsonObject,mixpanelHelper.mMixpanelUnifiedAPI)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngagePlaybackError(
        errorCode: String,
        errorMsg: String,
        origin: String,
        type: String,
        partnerName: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_ERROR_CODE, errorCode)
            payloadBuilder.addAttribute(PARA_ERROR_MESSAGE, errorMsg)
            payloadBuilder.addAttribute(PARA_ORIGIN, origin)
            payloadBuilder.addAttribute(PARA_TYPE, type)
            payloadBuilder.addAttribute(PARA_PARTNER, partnerName)
            moEngageHelper.trackEvent(
                (EVENT_ERROR),
                payloadBuilder
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelPlaybackFailure(
        title: String,
        genre: List<String>?,
        type: String,
        errorMsg: String,
        partnerName: String,
        rail: String/*Rail name/title*/,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String,
        isFreeContent: Boolean,
        pageName: String,
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
        autoPlayed: String,
        liveContent: String,
        contentConfigType: String /*Editorial, Recommendation*/
    ) {
        try {
            /*Mixpanel Unified*/
            val jsonObjectUnified = JSONObject().apply {
                put(PARA_PAGE_NAME, pageName)
                put(PARA_TITLE_RAIL, rail)
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
                language?.let {
                    put(PARA_CONTENT_LANGUAGE, TextUtils.join(", ", it))
                }
                actors?.let {
                    put(PARA_ACTORS, TextUtils.join(", ", it))
                }
                genre?.let {
                    put(PARA_CONTENT_GENRE, TextUtils.join(", ", it))
                }
                put(PARA_CONTENT_LANGUAGE_PRIMARY, contentLanguagePrimary)
                put(PARA_CONTENT_GENRE_PRIMARY, contentGenrePrimary)
                put(PARA_CONTENT_PARTNER, partnerName)
                put(PARA_CONTENT_AUTH, contentAuth)
                put(PARA_CONTENT_CATEGORY, type)
                put(PARA_CONTENT_POSITION, contentPosition)
                put(PARA_CONTENT_RATING, contentRating)
                put(PARA_CONTENT_PARENT_TITLE, parentTitle)
                put(PARA_CONTENT_TITLE, title)
                put(PARA_FREE_CONTENT, if (isFreeContent) YES else NO)
                put(PARA_RELEASE_YEAR, releaseYear)
                put(PARA_DEVICE_TYPE, deviceType)
                put(PARA_SOURCE, source)
                put(PARA_PACK_PRICE, pack?.amountValue?: FREEMIUM)
                put(PACK_NAME, pack?.productName?: FREEMIUM)
                put(PARA_AUTO_PLAYED, autoPlayed)
                put(PARA_LIVE_CONTENT, liveContent)
                put(PARA_REASON, errorMsg)
            }
            mixpanelHelper.trackEvent(EVENT_CONTENT_PLAY_FAIL, jsonObjectUnified, mixpanelHelper.mMixpanelUnifiedAPI)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageInitialBufferTime(
        durationSecond: String,
        durationMinute: String,
        title: String,
        genre: List<String>?,
        type: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_DURATION_SECONDS, durationSecond)
            payloadBuilder.addAttribute(PARA_DURATION_MINUTES, durationMinute)
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            genre?.let {
                payloadBuilder.addAttribute(CONTENT_GENRE, TextUtils.join(", ", it))
            }
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            moEngageHelper.trackEvent(
                (EVENT_INITIAL_BUFFER_TIME),
                payloadBuilder
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngagePlayContent(
        title: String,
        genre: List<String>?,
        type: String,
        startTime: String,
        stopTime: String,
        durationMinute: String,
        durationSecond: String,
        initialBufferSeconds: String,
        initialBufferMinutes: String,
        noOfPauses: String,
        noOfResumes: String,
        partnerName: String,
        rail: String,
        origin: String,
        source: String,
        language: List<String>?,
        pack: PartnerPacks?,
        railPosition: String,
        parentTitle: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            payloadBuilder.addAttribute(PARENT_TITLE, parentTitle)
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_START_TIME, startTime)
            payloadBuilder.addAttribute(PARA_STOP_TIME, stopTime)
            payloadBuilder.addAttribute(PARA_DURATION_MINUTES, durationMinute)
            payloadBuilder.addAttribute(PARA_DURATION_SECONDS, durationSecond)
            payloadBuilder.addAttribute(PARA_INITIAL_BUFFER_DURATION_SECONDS, initialBufferSeconds)
            payloadBuilder.addAttribute(PARA_INITIAL_BUFFER_DURATION_MINUTES, initialBufferMinutes)
            payloadBuilder.addAttribute(PARA_NO_OF_PAUSE, noOfPauses)
            payloadBuilder.addAttribute(PARA_NO_OF_RESUME, noOfResumes)
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            payloadBuilder.addAttribute(PARA_PACK_NAME, pack?.packName ?: FREEMIUM)
            payloadBuilder.addAttribute(PARA_PACK_PRICE, pack?.packPrice ?: FREEMIUM)
            payloadBuilder.addAttribute(PARA_PACK_TYPE, pack?.packType ?: "")
            payloadBuilder.addAttribute(PARA_RAIL, rail)
            payloadBuilder.addAttribute(PARA_ORIGIN, origin)
            payloadBuilder.addAttribute(PARA_SOURCE, source)
            payloadBuilder.addAttribute(PARA_RAIL_POSITION, railPosition)
            language?.let {
                payloadBuilder.addAttribute(PARA_LANGUAGE, TextUtils.join(", ", it))
            }
            genre?.let {
                payloadBuilder.addAttribute(CONTENT_GENRE, TextUtils.join(", ", it))
            }
            moEngageHelper.trackEvent(EVENT_PLAY_CONTENT, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngagePauseContent(
        title: String,
        genre: List<String>?,
        type: String,
        partnerName: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            genre?.let {
                payloadBuilder.addAttribute(CONTENT_GENRE, TextUtils.join(", ", it))
            }
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            moEngageHelper.trackEvent(EVENT_PAUSE_CONTENT, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngageResumeContent(
        title: String,
        genre: List<String>?,
        type: String,
        partnerName: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            genre?.let {
                payloadBuilder.addAttribute(CONTENT_GENRE, TextUtils.join(", ", it))
            }
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_PARTNER_NAME, partnerName)
            moEngageHelper.trackEvent(EVENT_RESUME_CONTENT, payloadBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trackMoEngagePlaybackFailure(
        title: String,
        genre: List<String>?,
        type: String,
        errorMsg: String
    ) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(CONTENT_TITLE, title)
            genre?.let {
                payloadBuilder.addAttribute(CONTENT_GENRE, TextUtils.join(", ", it))
            }
            payloadBuilder.addAttribute(CONTENT_TYPE, type)
            payloadBuilder.addAttribute(PARA_REASON, errorMsg)
            moEngageHelper.trackEvent(
                (EVENT_PLAYBACK_FAILURE),
                payloadBuilder
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackFirstFreeContentPlay(title: String, type: String, partnerName: String){
        val bundle = Bundle()
        bundle.putString(PARA_CONTENT_TITLE, title)
        bundle.putString(PARA_CONTENT_TYPE, type)
        bundle.putString(PARA_PARTNER_NAME, partnerName)
        facebookAnalyticsHelper.trackEvent(EVENT_FIRST_CONTENT_FREE_PLAY, bundle)
        firebaseAnalyticsHelper.trackEvent(EVENT_FIRST_CONTENT_FREE_PLAY, bundle)
    }

    fun trackFirstPremiumContentPlay(title: String, type: String, partnerName: String){
        val bundle = Bundle()
        bundle.putString(PARA_CONTENT_TITLE, title)
        bundle.putString(PARA_CONTENT_TYPE, type)
        bundle.putString(PARA_PARTNER_NAME, partnerName)
        facebookAnalyticsHelper.trackEvent(EVENT_FIRST_CONTENT_PREMIUM_PLAY, bundle)
        firebaseAnalyticsHelper.trackEvent(EVENT_FIRST_CONTENT_PREMIUM_PLAY, bundle)
    }
}