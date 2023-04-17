package com.tatasky.binge.analytics.appsflyer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.utils.*
import java.util.*
import javax.inject.Singleton
import kotlin.collections.HashMap

@Singleton
class AppsFlyerHelper(
    private val context: Context,
    private val sharedPrefs: PrefsRepo,
    private val mixpanelHelper: MixpanelHelper
) {
    var appsFlyerId: String
    private var additionalDataMap: HashMap<String, String>? = null
    private val appsFlyer: AppsFlyerLib = AppsFlyerLib.getInstance()
    var deepLinkDestination: String? = null
    private var deepLinkDestinationParam: String? = null

    init {

        /**
         * If you don't use conversion data for purposes other than testing, make sure to remove
         * AppsFlyerConversionListener when releasing your app to production.
         */
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                d(this@AppsFlyerHelper.javaClass.simpleName, "onConversionDataSuccess: $data")
            }

            override fun onConversionDataFail(error: String?) {
                d(this@AppsFlyerHelper.javaClass.simpleName, "onConversionDataFail: $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                // Must be overriden to satisfy the AppsFlyerConversionListener interface.
                // Business logic goes here when UDL is not implemented.
                data?.map {
                }
                d(this@AppsFlyerHelper.javaClass.simpleName, "onAppOpenAttribution: $data")
            }

            override fun onAttributionFailure(error: String?) {
                // Must be overriden to satisfy the AppsFlyerConversionListener interface.
                // Business logic goes here when UDL is not implemented.
                d(this@AppsFlyerHelper.javaClass.simpleName, "onAttributionFailure: $error")
            }
        }
        appsFlyer.setOneLinkCustomDomain(BuildConfig.AF_BRANDED_DOMAIN)
        /* To test the AppsFlyer use conversionDataListener */
//        appsFlyer.init(BuildConfig.AF_DEV_KEY, conversionDataListener, context)
        appsFlyer.init(BuildConfig.AF_DEV_KEY, null, context)
        setCUID()
        generateAdditionalData()
        appsFlyer.start(context)
        appsFlyerId = appsFlyer.getAppsFlyerUID(context)
        // Debug Mode, Disable this in Live app
        if (BuildConfig.DEBUG)
            appsFlyer.setDebugLog(true)
        createUDL()
    }

    fun setCUID() {
        appsFlyer.setCustomerUserId(mixpanelHelper.getMixPanelDistinctId())
        d(this.javaClass.simpleName, "AppsFlyer CUID: ${mixpanelHelper.getMixPanelDistinctId()}")
    }

    fun generateAdditionalData() {
        additionalDataMap = if (sharedPrefs.getLoginStatus())
            HashMap<String, String>().apply {
                when (sharedPrefs.getDthStatusFreemium()) {
                    DTH_W_BINGE_OLD_USER -> put(SID, sharedPrefs.getOriginalSubscriberId())
                    else -> sharedPrefs.getBingeSid()?.let { put(COMVIVA_ID, it) }
                }
                put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
                put(AFInAppEventParameterName.CURRENCY, CURRENCY_INR)
            }
        else
            HashMap<String, String>().apply {
                put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
                put(AFInAppEventParameterName.CURRENCY, CURRENCY_INR)
            }
    }

    /**
     * Create and return URI and extras as needed to redirect to
     * destination or any activity
     */
    private fun createAndGetIntent( //TODO: Remove BA UseCases
        context: Context,
        deepLinkDestination: String?,
        deepLinkDestinationParam: String?
    ): Pair<Uri?, Bundle?>? {
        return when (deepLinkDestination?.toLowerCase(Locale.getDefault())) {
            KEY_BINGE_LIST -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_watchlist,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_MY_SUBSCRIPTION -> {
                //See https://docs.google.com/spreadsheets/d/1ln7BCFbmMC_OH1_l2dB2fnXHSYfW29ceDPwK7jKCUUk/edit#gid=820089498&range=D23
                val subscriptionUri: String = if (sharedPrefs.isManagedAppEnabled()) {
                    "${
                        context.getString(
                            R.string.deeplink_subscription_generic_placeholder,
                            BuildConfig.hostName
                        )
                    }?$deepLinkDestinationParam"
                } else {
                    val splittedParams = deepLinkDestinationParam?.split("/")
                    val action = splittedParams?.getOrNull(0) ?: ""
                    val packName = splittedParams?.getOrNull(1)?.replace("_", " ") ?: ""
                    context.getString(
                        R.string.deeplink_subscription,
                        BuildConfig.hostName,
                        action,
                        packName
                    )
                }
                Pair(
                    Uri.parse(subscriptionUri), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_RECHARGE -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_recharge,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_MY_ACCOUNT -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_account,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_DETAIL -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_detail_for_onelink,
                            BuildConfig.hostName,
                            deepLinkDestinationParam /* Required {contentType}/{id}/{partner-name} */
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_LANGUAGE_GENRE -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_language_genre_for_onelink,
                            BuildConfig.hostName,
                            deepLinkDestinationParam /* Required {title}/{type} Eg. Hindi/Language */
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_SEARCH -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_search,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_WATCHLIST -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_watchlist,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_MORE -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_more,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_HOME_PAGE -> { /*Lands to home screen and navigate to a specified page/tab/left menu*/
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_homepage,
                            BuildConfig.hostName,
                            deepLinkDestinationParam /* Required: {pageType} */
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_PRIME -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_prime,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_PARTNER -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_app_page_for_onelink,
                            BuildConfig.hostName,
                            deepLinkDestinationParam /* Required {partnerName}/{partnerID}?pageType={pagetype} */
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_SEE_ALL -> { // Rail see all
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_see_all_for_onelink,
                            BuildConfig.hostName,
                            deepLinkDestinationParam /* Required {railId}?title={title}&sectionType={sectionType}&placeHolder={placeHolder} (title eg. Bollywood_Movies will be Bollywood Movies on FE) */
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_FAQ -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_faq,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_APP_SEE_ALL -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_app_see_all_for_onelink,
                            BuildConfig.hostName,
                            deepLinkDestinationParam /* Required {railID}?title={title without spacing} */
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_HOME -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_home,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_MOVIES -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_movies,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_SHOWS -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_shows,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_KIDS -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_kids,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_CATEGORIES -> { // Bottom tab
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_categories,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_PARENTAL_CONTROL -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_parental_control,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_CONTENT_LANGUAGE -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_content_language,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_LOGIN -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_login,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_NOTIFICATION -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_notification,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_SETTING -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_settings,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_EDIT_PROFILE -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_edit_profile,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_DEVICE_MANAGEMENT -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_device_management,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_TRANSACTION_HISTORY -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_transaction_history,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_GAMES -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_games,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            KEY_SPORTS -> {
                Pair(
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_sports,
                            BuildConfig.hostName
                        )
                    ), Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_DEEPLINK)
                    }
                )
            }
            else -> null
        }
    }

    fun setupOnelinkRedirection(context: Context): Pair<Uri?, Bundle?>? {
        deepLinkDestination?.let {
            return createAndGetIntent(
                context,
                it,
                deepLinkDestinationParam
            )
        }
        return null
    }

    /** Create Unified Deep Linking for deep and deferred deep linking using OneLink
     * Visit below link to know about how to create Onelink in AppsFlyer
     */
    //https://docs.google.com/spreadsheets/d/1ln7BCFbmMC_OH1_l2dB2fnXHSYfW29ceDPwK7jKCUUk/edit?usp=sharing
    private fun createUDL() {
        appsFlyer.subscribeForDeepLink(DeepLinkListener { deepLinkResult ->
            when (deepLinkResult.status) {
                DeepLinkResult.Status.FOUND -> {
                    val deepLinkObj = deepLinkResult.deepLink
                    try {
                        if (deepLinkObj.deepLinkValue != null) {
                            deepLinkDestination = deepLinkObj.deepLinkValue
                            deepLinkDestinationParam =
                                deepLinkObj.getStringValue("deep_link_sub1")
                            d(
                                this.javaClass.simpleName,
                                "The DeepLink will route to: $deepLinkDestination, $deepLinkDestinationParam"
                            )
                        }
                    } catch (e: java.lang.Exception) {
                        d(
                            this.javaClass.simpleName,
                            e.localizedMessage ?: "Error in Deeplink"
                        )
                        return@DeepLinkListener
                    }
                }
                DeepLinkResult.Status.NOT_FOUND -> {
                    d(this.javaClass.simpleName, "Deep link not found")
                    return@DeepLinkListener
                }
                else -> {
                    // dlStatus == DeepLinkResult.Status.ERROR
                    val dlError = deepLinkResult.error
                    d(
                        this.javaClass.simpleName,
                        "There was an error getting Deep Link data: $dlError"
                    )
                    return@DeepLinkListener
                }
            }
        })
    }

    fun trackEvent(eventName: String, parametersAndValues: HashMap<String, Any>) {
        try {
            appsFlyer.logEvent(
                context,
                eventName,
                parametersAndValues.apply { additionalDataMap?.let { putAll(it) } },
                null
            )
            d(this.javaClass.simpleName, "Event: $eventName, Param: $parametersAndValues") //TODO: Will be commented
        } catch (e: Exception) {
            e(this.javaClass.simpleName, e.localizedMessage)
        }
    }

    fun trackEvent(eventName: String) {
        try {
            appsFlyer.logEvent(
                context,
                eventName,
                additionalDataMap as Map<String, Any>?,
                null
            )
            d(this.javaClass.simpleName, "Event: $eventName, Param: $additionalDataMap") //TODO: Will be commented
        } catch (e: Exception) {
            e(this.javaClass.simpleName, e.localizedMessage)
        }
    }


    /*TSF-8332 Sign-up event handling for comviva id with RMN*/
    fun trackEventWithoutComvivaID(eventName: String, parametersAndValues: HashMap<String, Any>) {
        try {
            additionalDataMap?.remove(COMVIVA_ID)//remove only for sign-up event
            appsFlyer.logEvent(
                context,
                eventName,
                parametersAndValues.apply { additionalDataMap?.let { putAll(it) } },
                null
            )
            d(this.javaClass.simpleName, "Event: $eventName, Param: $parametersAndValues") //TODO: Will be commented
        } catch (e: Exception) {
            e(this.javaClass.simpleName, e.localizedMessage)
        }
    }
}
