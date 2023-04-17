package com.tatasky.binge.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatasky.binge.analytics.ATV
import com.tatasky.binge.analytics.FIRE_TV
import com.tatasky.binge.analytics.NO
import com.tatasky.binge.analytics.YES
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.data.utils.StrictModePermitter.permitDiskReads
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.Languages
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import java.util.ArrayList
import java.util.HashMap
import javax.inject.Singleton


@Singleton
class SharedPrefs(private val ctx: Context) : PrefsRepo {

    companion object {
        const val PREF_KEY_CONFIG = "PREF_KEY_CONFIG"
        const val PREF_KEY_CONFIG_ANDROID = "PREF_KEY_CONFIG_ANDROID"
        const val PREF_KEY_CLOUDINARY_URL = "PREF_KEY_CLOUDINARY_URL"
        const val PREFS_NAME = "secured_binge_prefs"
        const val PREFS_PROFILE_ID = "PREFS_PROFILE_ID"
        const val PREF_USER_DETAILS = "PREFS_USER_DETAILS"
        const val PREF_SEARCH_KEYWORDS = "PREFS_RECENT_SEARCHES"
        const val SID = "SID"
        const val OSID = "ORIGINAL_SID"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val DEVICE_TOKEN = "DEVICE_TOKEN"
        const val IS_LOGIN = "isLogin"
        const val USER_NAME = "userName"
        const val IS_KIDS = "isKids"
        const val PREF_KEY_IS_MOE_USER_TRACKED = "PREF_KEY_IS_MOE_USER_TRACKED"
        const val PREF_KEY_TRAILER_SOUND = "PREF_KEY_IS_TRAILER_SOUND_ON"
        const val PREF_KEY_TRAILER_AUTOPLAY = "PREF_KEY_IS_TRAILER_AUTOPLAY"
        const val PREF_KEY_TRANSACTIONAL_NOTIFICATION = "PREFS_TRANSACTIONAL_NOTIFICATION"
        const val PREF_KEY_OFFERS_NOTIFICATION = "PREFS_OFFERS_NOTIFICATION"
        const val PREF_KEY_WATCH_NOTIFICATION = "PREFS_WATCH_NOTIFICATION"
        const val PREF_OTP_RESENT_COUNT = "PREF_OTP_RESENT_COUNT"
        const val PREF_OTP_DURATION = "PREF_OTP_DURATION"
        const val PREF_KEY_PASSWORD_CREATED = "PREF_KEY_PASSWORD_CREATED"
        const val PREF_KEY_PDF_DOWNLOADED = "PREF_KEY_PDF_DOWNLOADED"
        const val PREF_KEY_SUBSCRIPTIONS = "PREF_KEY_SUBSCRIPTIONS"
        const val PREF_KEY_BUNDLE = "PREF_KEY_PACK_SELECTION_SKIPPED_OR_COMPLETED"
        const val PREF_KEY_SUBSCRIBED_PACK = "PREF_KEY_PACK_SUBSCRIBED"
        const val PREF_KEY_PRIME_PACK = "PREF_KEY_PRIME_PACK"
        const val PREF_KEY_PRIVACY_POLICY_URL = "PREF_KEY_PRIVACY_POLICY_URL"
        const val PREF_KEY_TNC_URL = "PREF_KEY_TNC_URL"
        const val PREF_KEY_EULA_URL = "PREF_KEY_EULA_URL"

        const val PREF_KEY_SELECTED_PROFILE = "PREF_KEY_SELECTED_PROFILE"
        const val PREF_KEY_SELECTED_BINGE_ID = "PREF_KEY_SELECTED_BINGE_ID"

        const val PREF_KEY_CONFIG_RESPONSE = "PREF_KEY_CONFIG_RESPONSE"
        const val PREF_KEY_ORIENTATION = "PREF_KEY_ORIENTATION"
        const val PREF_KEY_BINGE_SURVEY_NOTIFICATION = "PREF_KEY_BINGE_SURVEY_NOTIFICATION"
        const val PREF_KEY_BINGE_OFFERS_NOTIFICATION = "PREF_KEY_BINGE_OFFERS_NOTIFICATION"
        const val PREF_KEY_BINGE_UPDATES_NOTIFICATION = "PREF_KEY_BINGE_UPDATES_NOTIFICATION"
        const val PREF_KEY_USER_AUTHENTICATE_TOKEN = "PREF_KEY_USER_AUTHENTICATE_TOKEN"
        const val PREF_KEY_DEVICE_AUTHENTICATE_TOKEN = "PREF_KEY_DEVICE_AUTHENTICATE_TOKEN"
        const val PREF_PARTNER_SET = "PREF_PARTNER_SET"
        const val PREF_KEY_DTH_ACCOUNT_STATUS = "PREF_KEY_DTH_ACCOUNT_STATUS"
        const val PREF_KEY_DSN = "PREF_KEY_DSN"
        const val PREF_KEY_FIRESTICK_TAKEN_STATUS = "PREF_KEY_FIRESTICK_TAKEN_STATUS"
        const val PREF_KEY_FIRESTICK_DIALOG_SHOWN = "PREF_KEY_FIRESTICK_DIALOG_SHOWN_STATUS"
        const val PREF_KEY_FIRESTICK_DIALOG_VISIBILITY_TYPE = "PREF_KEY_FIRESTICK_DIALOG_VISIBILITY_TYPE"
        const val PREF_KEY_FIRESTICK_DIALOG_TIME_FREQUENCY = "PREF_KEY_FIRESTICK_DIALOG_TIME_FREQUENCY"
        const val PREF_KEY_FIRESTICK_DIALOG_LAUNCH_FREQUENCY = "PREF_KEY_FIRESTICK_DIALOG_LAUNCH_FREQUENCY"
        const val PREF_KEY_FIRESTICK_DIALOG_LAST_VISIBLE_TIME = "PREF_KEY_FIRESTICK_DIALOG_LAST_VISIBLE_TIME"
        const val PREF_KEY_FIRESTICK_DIALOG_LAST_LAUNCH_VALUE = "PREF_KEY_FIRESTICK_DIALOG_LAST_LAUNCH_VALUE"
        const val PREF_KEY_APP_LAUNCH_COUNT_LOGGED_IN = "PREF_KEY_APP_LAUNCH_COUNT_LOGGED_IN"
        const val PREF_KEY_START_LAUNCH_COUNT = "PREF_KEY_START_LAUNCH_COUNT"
        const val PREF_KEY_APP_LAUNCH_COUNT_GUEST = "PREF_KEY_APP_LAUNCH_COUNT_GUEST"
        const val PREF_KEY_FIRESTICK_DIALOG_FIRST_VISIBLE_TIME = "PREF_KEY_FIRESTICK_DIALOG_FIRST_VISIBLE_TIME"
        const val PREF_KEY_HOTSTAR_DIALOG_LAUNCH_FREQUENCY = "PREF_KEY_HOTSTAR_DIALOG_LAUNCH_FREQUENCY"
        const val PREF_KEY_HOTSTAR_DIALOG_PERIODIC_FREQUENCY = "PREF_KEY_HOTSTAR_DIALOG_PERIODIC_FREQUENCY"
        const val PREF_KEY_HOTSTAR_DIALOG_LAST_FINAL_POPUP_SHOWN_TIME = "PREF_KEY_HOTSTAR_DIALOG_LAST_FINAL_POPUP_SHOWN_TIME"
        const val PREF_KEY_HOTSTAR_DIALOG_IS_FIRST_CYCLE_COMPLETED = "PREF_KEY_HOTSTAR_DIALOG_IS_FIRST_CYCLE_COMPLETED"
        const val PREF_KEY_FREE_TRIAL_STARTUP_NUDGE_DATA = "PREF_KEY_FREE_TRIAL_STARTUP_NUDGE_DATA"
        const val PREF_KEY_DUNNING_TIME = "PREF_KEY_DUNNING_TIME"
        const val PREF_KEY_LOGIN_TYPE_PASSWORD = "PREF_KEY_LOGIN_TYPE_PASSWORD"
        const val PREF_KEY_MAX_RECHARGE_AMOUNT = "MAX_RECHARGE_AMOUNT"
        const val PREF_KEY_PASSWORD_CHANGE_REDIRECTION_TIME = "PASSWORD_CHANGE_REDIRECTION_TIME"
        const val PREF_KEY_CLEAR_RMN = "PREF_KEY_CLEAR_RMN"
        const val PREF_LANGAUAGES = "PREF_LANGAUAGES"
        const val PREF_GENRES = "PREF_GENRES"
        const val PREF_GENREAPI_TIME = "PREF_GENREAPI_TIME"
        const val PREF_CONNECT_TIMEOUT = "PREF_CONNECT_TIMEOUT"
        const val PREF_LOADER_TIMEOUT = "PREF_LOADER_TIMEOUT"
        const val PREF_SHOW_MARKETING = "PREF_SHOW_MARKETING"
        const val PREF_BINGE_BUTTONS = "PREF_BINGE_BUTTONS"
        const val PREF_KEY_SUBSCRIPTION_TYPE = "PREF_SUBSCRIPTION_TYPE"
        const val PREF_KEY_CONTENT_PLAYBACK_ALLOWED = "PREF_CONTENT_PLAYBACK_ALLOWED"
        const val PREF_KEY_RMN = "PREF_RMN"
        const val PREF_KEY_PARTNER_UNIQUE_ID = "PREF_KEY_PARTNER_UNIQUE_ID"
        const val PREF_RATE_LIMIT  = "PREF_RATE_LIMIT"
        const val PREF_HOTSTAR_POPUP_COUNT  = "PREF_HOTSTAR_POPUP_COUNT"
        const val PREF_PRIME_POPUP_COUNT  = "PREF_PRIME_POPUP_COUNT"
        const val PREF_ACCOUNT_SUB_STATUS  = "PREF_ACCOUNT_SUB_STATUS"
        const val PREF_MIX_PANEL_ID  = "PREF_MIX_PANEL_ID"
        const val PREF_REF_PANEL_ID = "PREF_REF_PANEL_ID"
        const val PREF_KEY_SONY_LOGIN_AGAIN = "PREF_KEY_SONY_LOGIN_AGAIN"
        const val PREF_KEY_PRIME_REDIRECTION_ENABLED = "PREF_KEY_PRIME_REDIRECTION_ENABLED"
        const val PREF_KEY_PRIME_REDIRECTION_DELAY = "PREF_KEY_PRIME_REDIRECTION_DELAY"
        const val PREF_KEY_DEVICE_CANCELLATION_FLAG = "PREF_KEY_DEVICE_CANCELLATION_FLAG"
        const val PREF_KEY_PARTNER_UNIQUE_ID_INFO = "PREF_KEY_PARTNER_UNIQUE_ID_INFO"
        const val PREF_PRIME_EXISTING_CLICK_COUNT  = "PREF_PRIME_EXISTING_CLICK_COUNT"
        const val PREF_PRIME_POPUP_FREQUENCY  = "PREF_PRIME_POPUP_FREQUENCY"
        const val PREF_PRIME_POPUP_PERIOD  = "PREF_PRIME_POPUP_PERIOD"
        const val PREF_PRIME_EXISTING_CLICK_FREQUENCY  = "PREF_PRIME_EXISTING_CLICK_FREQUENCY"
        const val PREF_PRIME_EXISTING_CLICK_PERIOD  = "PREF_PRIME_EXISTING_CLICK_PERIOD"
        const val PREF_PRIME_POPUP_LAST_SHOWN = "PREF_PRIME_POPUP_LAST_SHOWN_TIME"
        const val PREF_PRIME_EXISTING_LAST_CLICK = "PREF_PRIME_EXISTING_LAST_CLICK_TIME"
        const val PREF_LICENCE_TITLE = "PREF_LICENCE_TITLE"
        const val PREF_LICENCE_SUBTITLE = "PREF_LICENCE_SUBTITLE"
        const val PREF_KEY_NUMBER_BA_ACCOUNT = "PREF_KEY_NUMBER_BA_ACCOUNT"
        const val PREF_KEY_LANGUAGE_WIDGET_VISIBILITY = "PREF_KEY_LANGUAGE_WIDGET_VISIBILITY"
        /*Freemium Pref Keys*/
        const val PREF_ANONYMOUS_ID = "PREF_ANONYMOUS_ID"
        const val PREF_GUEST_PROFILE_ID = "PREF_GUEST_PROFILE_ID"
        const val PREF_GUEST_PREFERRED_LANGUAGES = "PREF_GUEST_PREFERRED_LANGUAGES"
        const val IS_FIRST_TIME_LANGUAGE_POP_UP = "IS_FIRST_TIME_LANGUAGE_POP_UP"
        const val IS_PARENTAL_CONTROL_ENABLED = "IS_PARENTAL_CONTROL_ENABLED"
        const val PARENTAL_PIN_EXISTS_VALUE = "PARENTAL_PIN_EXISTS_VALUE"
        const val PREF_KEY_DTH_STATUS_FREEMIUM = "PREF_KEY_DTH_STATUS_FREEMIUM"
        const val PREF_PARENTAL_RATING_VALUE = "PREF_PARENTAL_RATING_VALUE"
        const val PREF_FETCHED_PROFILE_DATA = "PREF_FETCHED_PROFILE_DATA"
        const val PREF_FETCHED_BALANCE_DATA = "PREF_FETCHED_BALANCE_DATA"
        const val PREF_ELIGIBLE_FOR_FREE_TRIAL = "PREF_ELIGIBLE_FOR_FREE_TRIAL"
        const val PREF_BINGE_SID = "PREF_BINGE_SID"
        const val PREF_KEY_INTERRUPT_CATEGORIES_PAGE = "PREF_KEY_INTERRUPT_CATEGORIES_PAGE"
        const val PREF_KEY_LOGIN_TIMESTAMP = "PREF_KEY_LOGIN_TIMESTAMP"
        const val PREF_KEY_LAST_NUDGE_TIME_UPDATE_EMAIL = "PREF_KEY_LAST_NUDGE_TIME_UPDATE_EMAIL"
        const val PREF_KEY_LAST_NUDGE_TIME_SHOW_NOTIFICATION = "PREF_KEY_LAST_NUDGE_TIME_SHOW_NOTIFICATION"
        const val PREF_KEY_LAST_NUDGE_TIME_NEVER_MISS_PLAN_RENEWAL = "PREF_KEY_LAST_NUDGE_TIME_NEVER_MISS_PLAN_RENEWAL"
        const val PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_BEFORE_EXPIRY = "PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_BEFORE_EXPIRY"
        const val PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_AFTER_EXPIRY = "PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_AFTER_EXPIRY"
        // Temporarily saving SID to perform device management prior to login
        const val PREF_KEY_TEMP_SID = "PREF_KEY_TEMP_SID"
        const val PREF_KEY_TEMP_DTH_STATUS = "PREF_KEY_TEMP_DTH_STATUS"
        const val PREF_KEY_GAUTH_TOKEN = "PREF_KEY_GAUTH_TOKEN"
        const val PREF_KEY_LOGOUT_CALLED = "PREF_KEY_LOGOUT_CALLED"
        const val PREF_KEY_APP_LAUNCH_COUNT = "PREF_KEY_APP_LAUNCH_COUNT"
        const val PREF_KEY_HB_STARTING_POSITION = "PREF_KEY_HB_STARTING_POSITION"
        const val PREF_ADD_MODIFY_RESPONSE = "PREF_ADD_MODIFY_RESPONSE"
        const val PREF_PREVIOUS_PACK = "PREF_PREVIOUS_PACK"
        const val PREF_FIRST_USER_INVENTION = "FIRST-USER-INVENTION"
        const val PREF_FIRST_CONTENT_CLICK = "FIRST-CONTENT-CLICK"
        const val PREF_FIRST_CONTENT_FREE_PLAY = "FIRST-CONTENT-FREE-PLAY"
        const val PREF_FIRST_CONTENT_PREMIUM_PLAY = "FIRST-CONTENT-PREMIUM-PLAY"
        const val FIRST_TIME_LANDING_HOME_AFTER_LAUNCH = "FIRST_TIME_LANDING_HOME_AFTER_LAUNCH"
        const val PREF_KEY_SUBSCROPTION_DRAWER_LAST_SEEN = "PREF_KEY_SUBSCROPTION_DRAWER_LAST_SEEN"
        const val PREF_KEY_PAYMENT_STATUS_PENDING = "PREF_KEY_PAYMENT_STATUS_PENDING"
        const val PREF_KEY_HELP_CENTER_URL = "PREF_KEY_HELP_CENTER_URL"
        const val PREF_KEY_FIRST_APP_LAUNCH_TIME_IN_UTC = "PREF_KEY_FIRST_APP_LAUNCH_TIME_IN_UTC"
        const val PREF_KEY_NUMBER_OF_CONTENT_PLAYBACK = "PREF_KEY_NUMBER_OF_CONTENT_PLAYBACK"
        const val PREF_KEY_IS_APP_RATING_SUBMITTED = "PREF_KEY_IS_APP_RATING_SUBMITTED"
        const val PREF_KEY_MANAGED_APPS = "PREF_KEY_MANAGEDAPP"
        const val PREF_KEY_IS_ELIGIBLE_FOR_FREE_TRIAL = "PREF_KEY_IS_ELIGIBLE_FOR_FREE_TRIAL"
        const val PREF_KEY_LAUNCH_FREQUENCY_FOR_GAME_NUDGE = "PREF_KEY_LAUNCH_FREQUENCY_FOR_GAME_NUDGE"
        const val PREF_KEY_GAME_NUDGE_LAST_TIME_SHOWN = "PREF_KEY_GAME_NUDGE_LAST_TIME_SHOWN"
        const val PREF_KEY_NEXT_GAME_NUDGE_TIME = "PREF_KEY_NEXT_GAME_NUDGE_TIME"
        const val PREF_KEY_NEXT_GAME_NUDGE_COUNT = "PREF_KEY_NEXT_GAME_NUDGE_COUNT"
        const val PREF_KEY_NEXT_GAME_ANIM_COUNT = "PREF_KEY_NEXT_GAME_ANIM_COUNT"
        const val PREF_KEY_NEXT_GAME_ANIM_TIME = "PREF_KEY_NEXT_GAME_ANIM_TIME"
        const val PREF_KEY_HELP_CENTER_DATA = "PREF_KEY_HELP_CENTER_DATA"
        const val PREF_KEY_GAME_NUDGE_SHOWN = "PREF_KEY_GAME_NUDGE_SHOWN"
        const val PREF_KEY_SILENT_LOGIN_TIMESTAMP = "PREF_KEY_SILENT_LOGIN_TIMESTAMP"
        const val PREF_KEY_SILENT_LOGIN = "PREF_KEY_SILENT_LOGIN"
        const val PREF_KEY_MX_UPSELL_CLOSED = "PREF_KEY_MX_UPSELL_CLOSED"
        const val PREF_KEY_GENERIC_APP_LAUNCH_COUNT_FOR_LOGGED_IN_USER =
            "PREF_KEY_GENERIC_APP_LAUNCH_COUNT_FOR_LOGGED_IN_USER"
        const val PREF_KEY_HIERARCHY_DATA = "PREF_KEY_HIERARCHY_DATA"

        const val PREF_KEY_LAST_PG_PROCESS_STATUS = "PREF_KEY_LAST_PG_PROCESS_STATUS"
        const val PREF_KEY_ALLOWED_PROVIDERS = "PREF_KEY_ALLOWED_PROVIDERS"
        const val PREF_KEY_AVAILABLE_PROVIDERS= "PREF_KEY_AVAILABLE_PROVIDERS"
        const val PREF_KEY_MANAGED_APP_ENABLED = "PREF_KEY_MANGED_APP_ENABLED"
        const val PREF_KEY_GAME_VIBRATION_ENABLED = "PREF_KEY_GAME_VIBRATION_ENABLED"
        const val PREF_KEY_COACH_MARK_FREQUENCY = "PREF_KEY_COACH_MARK_FREQUENCY"
        const val PREF_KEY_GENERIC_APP_LAUNCH_COUNT = "PREF_KEY_GENERIC_APP_LAUNCH_COUNT"
        const val PREF_KEY_HOME_SCREEN_SEARCH_COACH_MARK_ENABLED = "PREF_KEY_HOME_SCREEN_SEARCH_COACH_MARK_ENABLED"
        const val PREF_KEY_SEARCH_SCREEN_MIC_COACH_MARK_ENABLED = "PREF_KEY_SEARCH_SCREEN_MIC_COACH_MARK_ENABLED"
        const val PREF_KEY_WELCOME_DIALOG_STATUS = "PREF_KEY_WELCOME_DIALOG_STATUS"
    }

    /**
     * @EncryptedSharedPreferences to block the super user from hacking into
     * the shared preferences file and reading sensitive user data
     */
    private val prefs: SharedPreferences by lazy {
        permitDiskReads {
            ctx.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

//    override fun <T> getLiveValue(key: String, defaultValue: T): LiveData<T> {
//        return object : SharedPreferenceLiveData<T>(prefs, key = key, defValue = defaultValue) {
//            override fun getValueFromPreferences(key: String, defValue: T): T {
//                var returnValue: Any? = null
//                returnValue = when (defValue) {
//                    is String -> prefs.getString(key, defValue)
//                    is Int -> prefs.getInt(key, defValue)
//                    is Boolean -> prefs.getBoolean(key, defValue)
//                    is Float -> prefs.getFloat(key, defValue)
//                    is Long -> prefs.getLong(key, defValue)
//                    else -> defValue
//                }
//                return try {
//                    returnValue as T
//                } catch (e: ClassCastException) {
//                    defValue
//                }
//            }
//        }
//    }
//
//    override fun getLiveStringValue(key: String, defValue: String): LiveData<String> {
//        return prefs.stringLiveData(key, defValue)
//    }
//
//    override fun getLiveBooleanValue(key: String, defValue: Boolean): LiveData<Boolean> {
//        return prefs.booleanLiveData(key, defValue)
//    }

    override fun getCloudenieryUrl(): String? {
        return prefs.getString(PREF_KEY_CLOUDINARY_URL, null)
    }

    override fun saveCloudenieryUrl(url: String?) {
        prefs.edit().putString(PREF_KEY_CLOUDINARY_URL, url).apply()
    }


    override fun isConfigured(): Boolean {
        return prefs.getBoolean(PREF_KEY_CONFIG, false)
    }

    override fun setConfigured(b: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_CONFIG, b).apply()
    }

    override fun getLoginStatus(): Boolean {
        return !prefs.getString(ACCESS_TOKEN, null).isNullOrBlank()
    }

    override fun getProfileId(): String? {
//        return "584c81e8-3326-4f6b-a08a-9ba6db8b4e6c"
        return prefs.getString(PREFS_PROFILE_ID, "") ?: ""
    }

    override fun setProfileId(pId: String) {
        prefs.edit().putString(PREFS_PROFILE_ID, pId).apply()
    }

    override fun getBaId(): String {
//        return "5000000075"
        return prefs.getString(SID, "") ?: ""
    }

    override fun setBaId(sId: String) {
        prefs.edit().putString(SID, sId).apply()
    }

    override fun getOriginalSubscriberId(): String {
//        return "5000000075"
        return prefs.getString(OSID, "") ?: ""
    }

    override fun setOriginalSubscriberId(sId: String) {
        prefs.edit().putString(OSID, sId).apply()
    }

    override fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN, "")
    }

    override fun setAccessToken(token: String) {
        prefs.edit().putString(ACCESS_TOKEN, token).apply()
    }

    override fun getDeviceToken(): String? {
        return prefs.getString(DEVICE_TOKEN, "")
    }

    override fun setDeviceToken(token: String) {
        prefs.edit().putString(DEVICE_TOKEN, token).apply()
    }

    override fun saveAccountDetails(accountJson: String) {
        prefs.edit().putString(PREF_USER_DETAILS, accountJson).apply()
    }

    override fun getAccountDetails(): String? {
        return prefs.getString(PREF_USER_DETAILS, null)
    }

    override fun getLoginResponse(): LoginResponse.BingeSubscription? {
        val instance = Gson().fromJson<LoginResponse.BingeSubscription>(
            getAccountDetails(),
            LoginResponse.BingeSubscription::class.java
        ) ?: return null

        return instance
    }

    override fun logoutUser() {
        clear()
    }

    override fun getEntitlements(): Set<String>? {
//        val accountDetails = Gson().fromJson<LoginResponse.UserData>(
//            getAccountDetails(),
//            LoginResponse.UserData::class.java
//        )
//        return accountDetails?.userDetails?.entitlements
        return prefs.getStringSet(PREF_KEY_SUBSCRIPTIONS, null)
    }

    override fun getMOEUserTracked(): Boolean {
        return prefs.getBoolean(PREF_KEY_IS_MOE_USER_TRACKED, false)
    }

    override fun setMOEUserTracked(isTracked: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_IS_MOE_USER_TRACKED, isTracked).apply()
    }


    override fun getRrmSessionInfo(): RrmSessionInfo? {
        val instance = Gson().fromJson<LoginResponse.BingeSubscription>(
            getAccountDetails(),
            LoginResponse.BingeSubscription::class.java
        ) ?: return null

        return instance.rrmInfo
    }

    override fun setConfigAppVersion(configAppVersion: ConfigResponse.Android) {
        prefs.edit().putString(PREF_KEY_CONFIG_ANDROID, Gson().toJson(configAppVersion)).apply()
    }

    override fun getConfigAppVersion(): ConfigResponse.Android? {

        return Gson().fromJson(
            prefs.getString(PREF_KEY_CONFIG_ANDROID, null),
            ConfigResponse.Android::class.java
        ) ?: return null
    }


    override fun setManagedAppResponse(configAppVersion: ManagedAppResponse.Data) {
        prefs.edit().putString(PREF_KEY_MANAGED_APPS, Gson().toJson(configAppVersion)).apply()
    }

    override fun getManagedAppResponse(): ManagedAppResponse.Data? {

        return Gson().fromJson(
            prefs.getString(PREF_KEY_MANAGED_APPS, null),
            ManagedAppResponse.Data::class.java
        ) ?: return null
    }


    override fun setTrailerSound(soundOn: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_TRAILER_SOUND, soundOn).apply()
    }

    override fun getTrailerSound(): Boolean {
        return prefs.getBoolean(PREF_KEY_TRAILER_SOUND, true)
    }

    override fun setAutoPlayTrailerOn(autoPlay: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_TRAILER_AUTOPLAY, autoPlay).apply()
    }

    override fun getAutoPlayTrailerOn(): Boolean {
        return prefs.getBoolean(PREF_KEY_TRAILER_AUTOPLAY, true)
    }

    override fun saveSearchKeyword(keyWord: String) {
        val stringSet = prefs.getString(PREF_SEARCH_KEYWORDS, null)
        if (stringSet == null) {
            prefs.edit().putString(PREF_SEARCH_KEYWORDS, keyWord).apply()
        } else {
            val list = stringSet.split("_|_").toMutableList()
            if (!list.contains(keyWord)) {
                list.add(0, keyWord)
            } else {
                list.remove(keyWord)
                list.add(0, keyWord)
            }
            val listToSave = if (list.size > 10) {
                list.subList(0, 10)
            } else list
            prefs.edit().putString(PREF_SEARCH_KEYWORDS, listToSave.joinToString("_|_")).apply()
        }
    }

    override fun clearAllSearchKeyword() {
        prefs.edit().remove(PREF_SEARCH_KEYWORDS).apply()
    }

    override fun clearRecentSearchItem(keyWord: String) {
        val stringSet = prefs.getString(PREF_SEARCH_KEYWORDS, null)
        val list = stringSet?.split("_|_")?.toMutableList()
        list?.remove(keyWord)
        prefs.edit().putString(PREF_SEARCH_KEYWORDS, list?.joinToString("_|_")).apply()
    }

    override fun getSearchKeywords(): List<String> {
        val stringSet = prefs.getString(PREF_SEARCH_KEYWORDS, null) ?: return emptyList()
        return stringSet.split("_|_").toMutableList()
    }

    override fun setAllowTransactionalNotification(allow: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_TRANSACTIONAL_NOTIFICATION, allow).apply()
    }

    override fun setAllowWatchNotification(allow: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_WATCH_NOTIFICATION, allow).apply()
    }

    override fun setAllowOffersNotification(allow: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_OFFERS_NOTIFICATION, allow).apply()
    }

    override fun getTransactionalNotificationAllowed(): Boolean {
        return prefs.getBoolean(PREF_KEY_TRANSACTIONAL_NOTIFICATION, true)
    }

    override fun getWatchNotificationAllowed(): Boolean {
        return prefs.getBoolean(PREF_KEY_WATCH_NOTIFICATION, true)
    }

    override fun getOffersNotificationAllowed(): Boolean {
        return prefs.getBoolean(PREF_KEY_OFFERS_NOTIFICATION, false)
    }

    override fun saveOtpResentCount(count: Int) {
        prefs.edit().putInt(PREF_OTP_RESENT_COUNT, count).apply()
    }

    override fun getOtpResentCount(): Int {
        return prefs.getInt(PREF_OTP_RESENT_COUNT, 5)
    }

    override fun saveOtpDuration(count: Int) {
        prefs.edit().putInt(PREF_OTP_DURATION, count).apply()
    }

    override fun getOtpDuration(): Int {
        return prefs.getInt(PREF_OTP_DURATION, 30)
    }


    override fun clear() {
        prefs.edit().remove(ACCESS_TOKEN)
            .remove(DEVICE_TOKEN)
            .remove(PREF_USER_DETAILS)
            .remove(SID)
            .remove(PREFS_PROFILE_ID)
            .remove(PREF_KEY_PASSWORD_CREATED)
            .remove(PREF_KEY_SUBSCRIPTIONS)
            .remove(PREF_KEY_SUBSCRIPTION_TYPE)
            .remove(PREF_KEY_PARTNER_UNIQUE_ID)
            .remove(PREF_KEY_BUNDLE)
            .remove(PREF_KEY_SUBSCRIBED_PACK)
            .remove(PREF_KEY_SELECTED_PROFILE)
            .remove(PREF_KEY_TRAILER_AUTOPLAY)
            .remove(PREF_PARTNER_SET)
            .remove(PREF_KEY_FIRESTICK_TAKEN_STATUS)
            .remove(PREF_KEY_FIRESTICK_DIALOG_SHOWN)
            .remove(PREF_KEY_FIRESTICK_DIALOG_FIRST_VISIBLE_TIME)
            .remove(PREF_KEY_FIRESTICK_DIALOG_LAST_VISIBLE_TIME)
            .remove(PREF_KEY_FIRESTICK_DIALOG_LAST_LAUNCH_VALUE)
            .remove(PREF_KEY_HOTSTAR_DIALOG_LAST_FINAL_POPUP_SHOWN_TIME)
            .remove(PREF_KEY_HOTSTAR_DIALOG_IS_FIRST_CYCLE_COMPLETED)
            .remove(PREF_KEY_DTH_ACCOUNT_STATUS)
            .remove(PREF_ACCOUNT_SUB_STATUS)
            .remove(PREF_KEY_DSN)
            .remove(OSID)
            .remove(PREF_KEY_DUNNING_TIME)
            .remove(PREF_LANGAUAGES)
            .remove(PREF_GENRES)
            .remove(PREF_GENREAPI_TIME)
            .remove(PREF_KEY_CONTENT_PLAYBACK_ALLOWED)
            .remove(PREF_KEY_SONY_LOGIN_AGAIN)
            .remove(PREF_KEY_RMN)
            .remove(PREF_KEY_CLEAR_RMN)
            .remove(PREF_HOTSTAR_POPUP_COUNT)
            .remove(PREF_PRIME_POPUP_COUNT)
            .remove(PREF_KEY_PARTNER_UNIQUE_ID_INFO)
            .remove(PREF_KEY_DEVICE_CANCELLATION_FLAG)
            .remove(PREF_KEY_WATCH_NOTIFICATION)
            .remove(PREF_PRIME_POPUP_LAST_SHOWN)
            .remove(PREF_PRIME_EXISTING_LAST_CLICK)
            .remove(PREF_PRIME_EXISTING_CLICK_COUNT)
            .remove(PREF_KEY_NUMBER_BA_ACCOUNT)
            .remove(PARENTAL_PIN_EXISTS_VALUE)
            .remove(IS_PARENTAL_CONTROL_ENABLED)
            .remove(PREF_PARENTAL_RATING_VALUE)
            .remove(PREF_KEY_DTH_STATUS_FREEMIUM)
            .remove(PREF_FETCHED_PROFILE_DATA)
            .remove(PREF_FETCHED_BALANCE_DATA)
            .remove(PREF_BINGE_SID)
            .remove(PREF_KEY_LOGIN_TIMESTAMP)
            .remove(PREF_KEY_LAST_NUDGE_TIME_UPDATE_EMAIL)
            .remove(PREF_KEY_LAST_NUDGE_TIME_SHOW_NOTIFICATION)
            .remove(PREF_KEY_LAST_NUDGE_TIME_NEVER_MISS_PLAN_RENEWAL)
            .remove(PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_BEFORE_EXPIRY)
            .remove(PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_AFTER_EXPIRY)
            .remove(PREF_KEY_LOGOUT_CALLED)
            .remove(PREF_KEY_TEMP_SID)
            .remove(PREF_KEY_APP_LAUNCH_COUNT)
            .remove(PREF_KEY_HB_STARTING_POSITION)
            .remove(PREF_PREVIOUS_PACK)
            .remove(PREF_MIX_PANEL_ID)
            .remove(PREF_REF_PANEL_ID)
            .remove(PREF_KEY_SUBSCROPTION_DRAWER_LAST_SEEN)
            .remove(PREF_KEY_PAYMENT_STATUS_PENDING)
            .remove(PREF_KEY_PRIME_PACK)
            .remove(PREF_KEY_HELP_CENTER_URL)
            .remove(PREF_KEY_APP_LAUNCH_COUNT_LOGGED_IN)
            .remove(PREF_KEY_APP_LAUNCH_COUNT_GUEST)
            .remove(PREF_KEY_START_LAUNCH_COUNT)
            .remove(PREF_KEY_IS_ELIGIBLE_FOR_FREE_TRIAL)
            .remove(PREF_KEY_GAME_NUDGE_LAST_TIME_SHOWN)
            .remove(PREF_KEY_NEXT_GAME_NUDGE_TIME)
            .remove(PREF_KEY_LAUNCH_FREQUENCY_FOR_GAME_NUDGE)
            .remove(PREF_KEY_NEXT_GAME_NUDGE_COUNT)
            .remove(PREF_KEY_NEXT_GAME_ANIM_COUNT)
            .remove(PREF_KEY_NEXT_GAME_ANIM_TIME)
            .remove(PREF_KEY_SILENT_LOGIN_TIMESTAMP)
            .remove(PREF_KEY_SILENT_LOGIN)
            .remove(PREF_KEY_MX_UPSELL_CLOSED)
            .remove(PREF_KEY_GENERIC_APP_LAUNCH_COUNT_FOR_LOGGED_IN_USER)
            .apply()
    }


    override fun getUserDetails(): LoginResponse.BingeSubscription? {
        val accountDetails = Gson().fromJson<LoginResponse.BingeSubscription>(
            getAccountDetails(),
            LoginResponse.BingeSubscription::class.java
        )
        return accountDetails
    }

    override fun setPasswordCreated(isPassword: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_PASSWORD_CREATED, isPassword).apply()
    }

    override fun isPasswordCreated(): Boolean {
        return prefs.getBoolean(PREF_KEY_PASSWORD_CREATED, false)

    }

    override fun setPDFDownloaded(data: String) {

        var downloadedList = getPDFDownloaded() as HashSet<String>
        downloadedList.add(data)
        prefs.edit().putStringSet(PREF_KEY_PDF_DOWNLOADED, downloadedList).apply()
    }

    override fun getPDFDownloaded(): Set<String>? {
        return prefs.getStringSet(PREF_KEY_PDF_DOWNLOADED, HashSet<String>())
    }


    override fun setSelectedProfile(data: LoginResponse.BingeSubscription) {
        prefs.edit().putString(PREF_KEY_SELECTED_PROFILE, Gson().toJson(data)).apply()
    }

    override fun getSelectedProfile(): LoginResponse.BingeSubscription? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_SELECTED_PROFILE, null),
            LoginResponse.BingeSubscription::class.java
        ) ?: return null//prefs.getStringSet(PREF_KEY_PDF_DOWNLOADED, HashSet<String>())
    }

    override fun setEntitlements(listOfIds: Set<String>) {
        prefs.edit().putStringSet(PREF_KEY_SUBSCRIPTIONS, listOfIds).apply()
    }


    override fun setPackSelectionJourneyCompleted() {
        prefs.edit().putBoolean(PREF_KEY_BUNDLE, true).apply()
    }

    override fun getPrivacyPolicyUrl(): String? {
        return prefs.getString(PREF_KEY_PRIVACY_POLICY_URL, null)
    }

    override fun getTnCUrl(): String? {
        return prefs.getString(PREF_KEY_TNC_URL, null)
    }

    override fun saveEulaTitle(title: String) {
        prefs.edit().putString(PREF_LICENCE_TITLE,title).apply()
    }

    override fun getEulaTitle(): String {
        return prefs.getString(PREF_LICENCE_TITLE,null)?:""
    }

    override fun saveEulaSubTitle(subTitle: String) {
        prefs.edit().putString(PREF_LICENCE_SUBTITLE,subTitle).apply()
    }

    override fun getEulaSubTitle(): String {
        return prefs.getString(PREF_LICENCE_SUBTITLE, null)?:""
    }

    override fun getEulaUrl(): String? {
        return prefs.getString(PREF_KEY_EULA_URL, null)
    }

    override fun saveEulaUrl(url: String) {
        prefs.edit().putString(PREF_KEY_EULA_URL, url).apply()
    }

    override fun savePrivacyPolicyUrl(url: String) {
        prefs.edit().putString(PREF_KEY_PRIVACY_POLICY_URL, url).apply()
    }

    override fun saveTnCUrl(url: String) {
        prefs.edit().putString(PREF_KEY_TNC_URL, url).apply()
    }

    override fun setSelectedBAID(data: LoginResponse.BingeSubscription) {
        prefs.edit().putString(PREF_KEY_SELECTED_BINGE_ID, Gson().toJson(data)).apply()
    }

    override fun getSelectedBAID(): LoginResponse.BingeSubscription? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_SELECTED_BINGE_ID, null),
            LoginResponse.BingeSubscription::class.java
        ) ?: return null
    }

    override fun addConfigResponse(configResponse: ConfigResponse) {
        prefs.edit().putString(PREF_KEY_CONFIG_RESPONSE, Gson().toJson(configResponse)).apply()
    }

    override fun getConfigResponse(): ConfigResponse? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_CONFIG_RESPONSE, null),
            ConfigResponse::class.java
        ) ?: return null
    }

    override fun orientationEnabled(): Boolean {
        return prefs.getBoolean(PREF_KEY_ORIENTATION, true)
    }

    override fun setOrientationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_ORIENTATION, enabled).apply()
    }


    override fun getTAHeroBanner(): List<TaHeroBanner>? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_CONFIG_RESPONSE, null),
            ConfigResponse::class.java
        )?.data?.config?.taHeroBanner ?: return null
    }

    override fun getTARelatedRail(): List<TaRelatedRail>? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_CONFIG_RESPONSE, null),
            ConfigResponse::class.java
        )?.data?.config?.taRelatedRail ?: return null
    }

    override fun getProviderLogo(): ProviderLogo {
        return getConfigResponse()?.data?.config?.providerLogo ?: return ProviderLogo()
    }

    override fun setBingeUpdateNotification(allow: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_BINGE_UPDATES_NOTIFICATION, allow).apply()
    }

    override fun setBingeOffersNotification(allow: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_BINGE_OFFERS_NOTIFICATION, allow).apply()
    }

    override fun setBingeSurveyNotification(allow: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_BINGE_SURVEY_NOTIFICATION, allow).apply()
    }

    override fun getBingeUpdateNotification(): Boolean {
        return prefs.getBoolean(PREF_KEY_BINGE_UPDATES_NOTIFICATION, true)
    }

    override fun getBingeOffersNotification(): Boolean {
        return prefs.getBoolean(PREF_KEY_BINGE_OFFERS_NOTIFICATION, true)
    }

    override fun getBingeSurveyNotification(): Boolean {
        return prefs.getBoolean(PREF_KEY_BINGE_SURVEY_NOTIFICATION, false)
    }

    override fun saveLoginAccessToken(userAuthenticateToken: String?) {
        prefs.edit().putString(PREF_KEY_USER_AUTHENTICATE_TOKEN, userAuthenticateToken).apply()
    }

    override fun saveLoginDeviceToken(deviceAuthenticateToken: String?) {
        prefs.edit().putString(PREF_KEY_DEVICE_AUTHENTICATE_TOKEN, deviceAuthenticateToken).apply()
    }

    override fun getLoginAccessToken(): String {
        return prefs.getString(PREF_KEY_USER_AUTHENTICATE_TOKEN, "") ?: ""
    }

    override fun getLoginDeviceToken(): String {
        return prefs.getString(PREF_KEY_DEVICE_AUTHENTICATE_TOKEN, "") ?: ""
    }

    override fun removeTempToken() {
        prefs.edit()
            .remove(PREF_KEY_DEVICE_AUTHENTICATE_TOKEN)
            .remove(PREF_KEY_USER_AUTHENTICATE_TOKEN)
            .remove(PREF_KEY_TEMP_SID)
            .remove(PREF_KEY_TEMP_DTH_STATUS)
            .apply()
    }

    override fun savePrimePackDetails(primePack: PrimePack?){
        primePack?.let{
            prefs.edit().putString(PREF_KEY_PRIME_PACK, Gson().toJson(primePack)).apply()
        }
    }

    override fun getPrimePackDetails(): PrimePack? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_PRIME_PACK, null),
            PrimePack::class.java
        )
    }



    override fun saveSubscribedPack(pack: PartnerPacks?,subscriptionAnalytics:SubscriptionAnalytics) {
        if(pack == null){
            removeSubscribedPack()
            subscriptionAnalytics.trackUpdatedPackDetails(null)
            subscriptionAnalytics.updateGroupProfileWithSubscription(SID, getOriginalSubscriberId(),null)
        }
        else
            pack.takeIf {
                !(it.subscriptionStatus.equals("REQUESTED", true) || it.subscriptionStatus.isNullOrEmpty())
            }?.let {
//            pack?.productName = "VootSelect Starter Plan"
                prefs.edit().putString(PREF_KEY_SUBSCRIBED_PACK, Gson().toJson(pack)).apply()
                subscriptionAnalytics.trackUpdatedPackDetails(pack)
                subscriptionAnalytics.updateGroupProfileWithSubscription(SID, getOriginalSubscriberId(),pack)
                saveAccountDetails(Gson().toJson(
                    Gson().fromJson<LoginResponse.BingeSubscription>(
                        getAccountDetails(),
                        LoginResponse.BingeSubscription::class.java
                    )?.apply { freeTrialAvailed = true }
                ))
                val appList = pack?.getSelectedComponentAppList ?: emptyList()
                val setofPartnerIds = hashSetOf<String>()
                for (app in appList) app.partnerId?.let { it1 -> setofPartnerIds.add(it1) }
                savePartnerIdsList(setofPartnerIds)
//        } ?: prefs.edit().putString(PREF_KEY_SUBSCRIBED_PACK, null).apply()
                saveFirestickTaken(pack?.fsTaken ?: false)
                saveContentPlaybackAllowed(pack?.contentPlayBackHybrid ?: false)
                saveDeviceCancellationFlag(pack?.deviceCancellationFlag ?: false)
                pack.subscriptionType?.let {it ->
                    subscriptionAnalytics.updateProperty(FIRE_TV, if(it.equals(subscriptionTypeFtv, true)) YES else NO)
                    subscriptionAnalytics.updateProperty(ATV,if(it.equals(subscriptionTypeAtv, true)) YES else NO)
                    saveSubscriptionType(it)
                }
//            pack?.subscriptionDetailInfo?.subscriptionType?.let { saveSubscriptionType(it) }
                savePartnerUniqueId(pack?.partnerUniqueId)
//            pack?.rmn?.let { saveRMN(it) }
                pack?.dthStatus?.let { saveDTHAccountStatus(it) }
                pack?.accountSubStatus?.let { saveDTHAccountSubStatus(it) }
            }
    }
    override fun getPartnerDetail(partnerName: String, partnerUniqueIdInfo: ProviderInfo?) : PartnerUniqueInfo?{
        return partnerUniqueIdInfo?.let {
            getProviderInfo(
                partnerName,
                it
            )
        }
    }

    override fun getPartnerUniqueIdInfo(partnerName : String) : String {
        val list = getPartnerDetail(partnerName, getSubscribedPack()?.partnerUniqueIdInfo)
        if(list != null) {
            e("getSubscribedPack","partnerName : $partnerName, list.partnerUniqueId : ${list.partnerUniqueId}")
            return list.partnerUniqueId ?: getPartnerUniqueId()
        }
        return getPartnerUniqueId()
    }

    override fun getSubscribedPack(): PartnerPacks? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_SUBSCRIBED_PACK, null),
            PartnerPacks::class.java
        )
    }

    override fun removeSubscribedPack(){
        prefs.edit().remove(PREF_KEY_SUBSCRIBED_PACK).apply()
    }

    override fun getFaqData(): HelpCenterResponse.Data? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_HELP_CENTER_DATA, null),
            HelpCenterResponse.Data::class.java
        )
    }

    override fun setFaqData(data: HelpCenterResponse.Data) {

        prefs.edit().putString(PREF_KEY_HELP_CENTER_DATA, Gson().toJson(data)).apply()
    }


    fun savePartnerIdsList(list: Set<String>) {
        prefs.edit().putStringSet(PREF_PARTNER_SET, list).apply()
    }

    override fun getPartnerIdsList(): Set<String>? {
        //list: Set<String>
        return prefs.getStringSet(PREF_PARTNER_SET, null)
    }


    override fun isActivePack(): Boolean {
        return SubscriptionPackStatusEnum.ACTIVE.status.equals((getSubscribedPack()?.subscriptionStatus ?: ""), true)
    }

    override fun saveFirestickTaken(isTaken: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_FIRESTICK_TAKEN_STATUS, isTaken).apply()
    }

    override fun isFirestickTaken(): Boolean {
        return prefs.getBoolean(PREF_KEY_FIRESTICK_TAKEN_STATUS, false)
    }

    override fun setFirestickDialogShown(isShown: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_FIRESTICK_DIALOG_SHOWN, isShown).apply()
    }

    override fun setFirestickDialogVisibilityType(ftvDialogVisibilityType: String?) {
        prefs.edit().putString(PREF_KEY_FIRESTICK_DIALOG_VISIBILITY_TYPE, ftvDialogVisibilityType).apply()
    }

    override fun setFirestickDialogTimeFrequency(ftvDialogTimeFrequency: Int) {
        prefs.edit().putInt(PREF_KEY_FIRESTICK_DIALOG_TIME_FREQUENCY, ftvDialogTimeFrequency).apply()
    }

    override fun setFirestickDialogLaunchFrequency(ftvDialogLaunchFrequency: Int) {
        prefs.edit().putInt(PREF_KEY_FIRESTICK_DIALOG_LAUNCH_FREQUENCY, ftvDialogLaunchFrequency).apply()
    }

    override fun getFirestickDialogVisibilityType(): String? {
        return prefs.getString(PREF_KEY_FIRESTICK_DIALOG_VISIBILITY_TYPE, null)
    }

    override fun getFirestickDialogTimeFrequency(): Int {
        return prefs.getInt(PREF_KEY_FIRESTICK_DIALOG_TIME_FREQUENCY, 0)
    }

    override fun getFirestickDialogLaunchFrequency(): Int {
        return prefs.getInt(PREF_KEY_FIRESTICK_DIALOG_LAUNCH_FREQUENCY, 0)
    }

    override fun setFirestickDialogLastVisibleTime(lastVisibleTime: Long) {
        prefs.edit().putLong(PREF_KEY_FIRESTICK_DIALOG_LAST_VISIBLE_TIME, lastVisibleTime).apply()
    }

    override fun setAppLaunchValue(lastLaunchValue: Int) {
        prefs.edit().putInt(PREF_KEY_FIRESTICK_DIALOG_LAST_LAUNCH_VALUE, lastLaunchValue).apply()
    }

    override fun getFirestickDialogLastVisibleTime(): Long {
        return prefs.getLong(PREF_KEY_FIRESTICK_DIALOG_LAST_VISIBLE_TIME, 0)
    }

    override fun getAppLaunchValue(): Int {
        return prefs.getInt(PREF_KEY_FIRESTICK_DIALOG_LAST_LAUNCH_VALUE, 0)
    }

    override fun setFirestickDialogFirstVisbileTime(firstVisibleTime: Long) {
        prefs.edit().putLong(PREF_KEY_FIRESTICK_DIALOG_FIRST_VISIBLE_TIME, firstVisibleTime).apply()
    }

    override fun setAppLaunchValueGuest(count: Int) {
        prefs.edit().putInt(PREF_KEY_APP_LAUNCH_COUNT_GUEST, count).apply()
    }

    override fun getAppLaunchValueGuest(): Int {
        return prefs.getInt(PREF_KEY_APP_LAUNCH_COUNT_GUEST, 0)
    }

    override fun setAppLaunchValueLoggedIn(count: Int) {
        prefs.edit().putInt(PREF_KEY_APP_LAUNCH_COUNT_LOGGED_IN, count).apply()
    }

    override fun getAppLaunchValueLoggedIn(): Int {
        return prefs.getInt(PREF_KEY_APP_LAUNCH_COUNT_LOGGED_IN, 0)
    }


    override fun getStartLaunchCount(): Boolean {
        return prefs.getBoolean(PREF_KEY_START_LAUNCH_COUNT,false)
    }
    override fun setStartLaunchCount(b: Boolean){
        prefs.edit().putBoolean(PREF_KEY_START_LAUNCH_COUNT,b).apply()
    }


    override fun getFirestickDialogFirstVisbileTime(): Long {
        return prefs.getLong(PREF_KEY_FIRESTICK_DIALOG_FIRST_VISIBLE_TIME, 0)
    }

    override fun setHotstarDialogLaunchFrequency(hotstarDialogLaunchFrequency: Int) {
        prefs.edit().putInt(PREF_KEY_HOTSTAR_DIALOG_LAUNCH_FREQUENCY, hotstarDialogLaunchFrequency).apply()
    }

    override fun getHotstarDialogLaunchFrequency(): Int {
        return prefs.getInt(PREF_KEY_HOTSTAR_DIALOG_LAUNCH_FREQUENCY, 0)
    }

    override fun setHotstarDialogPeriodicFrequency(hotstarDialogPeriodicFrequency: Int) {
        prefs.edit().putInt(PREF_KEY_HOTSTAR_DIALOG_PERIODIC_FREQUENCY, hotstarDialogPeriodicFrequency).apply()
    }

    override fun getHotstarDialogPeriodicFrequency(): Int {
        return prefs.getInt(PREF_KEY_HOTSTAR_DIALOG_PERIODIC_FREQUENCY, 0)
    }

    override fun setHotstarLastFinalPopupShownTime(hotstarLastFinalPopupShownTime: Long) {
        prefs.edit().putLong(PREF_KEY_HOTSTAR_DIALOG_LAST_FINAL_POPUP_SHOWN_TIME, hotstarLastFinalPopupShownTime).apply()
    }

    override fun getHotstarLastFinalPopupShownTime(): Long {
        return prefs.getLong(PREF_KEY_HOTSTAR_DIALOG_LAST_FINAL_POPUP_SHOWN_TIME, 0)
    }

    override fun setHotstarPopupFirstCycleCompleted(isCompleted: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_HOTSTAR_DIALOG_IS_FIRST_CYCLE_COMPLETED, isCompleted).apply()
    }

    override fun getHotstarPopupFirstCycleCompleted(): Boolean {
        return prefs.getBoolean(PREF_KEY_HOTSTAR_DIALOG_IS_FIRST_CYCLE_COMPLETED, false)
    }

    override fun saveFreeTrialStartupNudgeData(onBannerNudge: ConfigResponse.FreeTrialStartupNudge) {
        prefs.edit().putString(PREF_KEY_FREE_TRIAL_STARTUP_NUDGE_DATA, Gson().toJson(onBannerNudge)).apply()
    }

    override fun getFreeTrialStartupData(): ConfigResponse.FreeTrialStartupNudge? {
        return Gson().fromJson(
            prefs.getString(PREF_KEY_FREE_TRIAL_STARTUP_NUDGE_DATA, null),
            ConfigResponse.FreeTrialStartupNudge::class.java
        ) ?: return null
    }


    override fun increaseGameLaunchFrequency() {
        prefs.edit().putInt(PREF_KEY_LAUNCH_FREQUENCY_FOR_GAME_NUDGE,getLaunchFrequencyForGameNudge()+1).apply()
    }

    override fun getLaunchFrequencyForGameNudge():Int{
        return prefs.getInt(PREF_KEY_LAUNCH_FREQUENCY_FOR_GAME_NUDGE,0)
    }

    override fun setGameNudgeLastTimeShown(timeInMillis: Long){
        prefs.edit().putLong(PREF_KEY_GAME_NUDGE_LAST_TIME_SHOWN, timeInMillis).apply()
    }

    override fun getGameNudgeLastTimeShown(): Long{
        return prefs.getLong(PREF_KEY_GAME_NUDGE_LAST_TIME_SHOWN,0L)
    }


    override fun isFirestickDialogShown(): Boolean {
        return prefs.getBoolean(PREF_KEY_FIRESTICK_DIALOG_SHOWN, false)
    }

    override fun saveDTHStatusFreemium(status: String){
        prefs.edit().putString(PREF_KEY_DTH_STATUS_FREEMIUM, status).apply()
    }

    override fun saveDTHAccountStatus(status: String) {
        prefs.edit().putString(PREF_KEY_DTH_ACCOUNT_STATUS, status).apply()
    }

    override fun lastDunningRechargeShownTime(): Long {
        return prefs.getLong(PREF_KEY_DUNNING_TIME, 0L)
    }

    override fun setLastDunningRechargeShownTime(time: Long) {
        prefs.edit().putLong(PREF_KEY_DUNNING_TIME, time).apply()
    }

    override fun isLoggedInWithPassword(): Boolean {
        return prefs.getBoolean(PREF_KEY_LOGIN_TYPE_PASSWORD, false)
    }

    override fun setLoggedInWithPassword(t: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_LOGIN_TYPE_PASSWORD, t).apply()
    }

    override fun getDthStatusFreemium(): String {
        return prefs.getString(PREF_KEY_DTH_STATUS_FREEMIUM, "") ?: ""
    }

    override fun setInterruptCategoryTabStatus(status:Boolean) {
        prefs.edit().putBoolean(PREF_KEY_INTERRUPT_CATEGORIES_PAGE, status).apply()
    }

    override fun getInterruptCategoryTabStatus(): Boolean =
        prefs.getBoolean(PREF_KEY_INTERRUPT_CATEGORIES_PAGE, true)

    override fun saveGAuthToken(it: String) {
        prefs.edit().putString(PREF_KEY_GAUTH_TOKEN, it).apply()
    }

    override fun getGAuthToken(): String? {
        return prefs.getString(PREF_KEY_GAUTH_TOKEN, "")
    }

    override fun setLogoutCalled(b: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_LOGOUT_CALLED, b).apply()
    }

    override fun isLogoutCalled(): Boolean  =
        prefs.getBoolean(PREF_KEY_LOGOUT_CALLED, false)


    override fun getDthStatus(): String {
        return try {
            prefs.getString(PREF_KEY_DTH_ACCOUNT_STATUS, null) ?: AccountStatusEnum.ACTIVE.status
        } catch (e: Exception) {
            if (prefs.getBoolean(PREF_KEY_DTH_ACCOUNT_STATUS, true)) AccountStatusEnum.ACTIVE.status
            else {
                AccountStatusEnum.DEACTIVATED.status
            }
        }
    }

    override fun setMaxRechargeAmount(amount: Int) {
        prefs.edit().putInt(PREF_KEY_MAX_RECHARGE_AMOUNT, amount).apply()
    }

    override fun setPasswordRedirectionTime(time: Int) {
        prefs.edit().putInt(PREF_KEY_PASSWORD_CHANGE_REDIRECTION_TIME, time).apply()
    }

    override fun getMaxRechargeAmount(): Int {
        return prefs.getInt(PREF_KEY_MAX_RECHARGE_AMOUNT, 49000)
    }

    override fun getPasswordRedirectionTime(): Int {
        return prefs.getInt(PREF_KEY_PASSWORD_CHANGE_REDIRECTION_TIME, 5)
    }

    override fun getPrefLanguages(): List<String> {
        val stringSet = prefs.getStringSet(PREF_LANGAUAGES, null) ?: return emptyList()
        return stringSet.toList()
    }

    override fun setPrefLanguage(list: List<String>) {
        prefs.edit().putStringSet(PREF_LANGAUAGES, list.toSet()).apply()
    }

    override fun getPrefGenres(): List<String> {
        val stringSet = prefs.getStringSet(PREF_GENRES, null) ?: return emptyList()
        return stringSet.toList()
    }

    override fun getGenreAPITime(): String? {
        return prefs.getString(PREF_GENREAPI_TIME, null)
    }

    override fun saveGenreAPITime(time: String) {
        prefs.edit().putString(PREF_GENREAPI_TIME, time).apply()
    }

    override fun setPrefGenre(list: List<String>?) {
        prefs.edit().putStringSet(PREF_GENRES, list?.toSet()).apply()
    }

    override fun getConnectionTimeout(): Long {
        return prefs.getLong(PREF_CONNECT_TIMEOUT, 60)
    }

    override fun saveConnectionTimeout(time: Long) {
        prefs.edit().putLong(PREF_CONNECT_TIMEOUT, time).apply()
    }

    override fun getLoaderDelayTime(): Long {
        return prefs.getLong(PREF_LOADER_TIMEOUT, 1000)
    }

    override fun saveLoaderDelayTime(time: Long) {
        prefs.edit().putLong(PREF_LOADER_TIMEOUT, time).apply()
    }

    override fun getShowMarketingScreen(): Boolean {
        return prefs.getBoolean(PREF_SHOW_MARKETING, false)
    }

    override fun saveShowMarketingScreen(showMarketing: Boolean) {
        prefs.edit().putBoolean(PREF_SHOW_MARKETING, showMarketing).apply()
    }

    override fun saveDsn(dsn: String) {
        prefs.edit().putString(PREF_KEY_DSN, dsn).apply()
    }

    override fun getDsn(): String {
        return prefs.getString(PREF_KEY_DSN, "") ?: ""
    }


    override fun saveHierarchyData(key: String, hierarchyResponse: HierarchyResponse) {

        hierarchyResponse.apply {
            cacheTimeStamp =  System.currentTimeMillis()
        }

        var outputMap: HashMap<String, HierarchyResponse?> = HashMap()
        val map =  Gson().fromJson<HashMap<String, HierarchyResponse?>>(
            prefs.getString(
                PREF_KEY_HIERARCHY_DATA,
                ""
            ), object : TypeToken<HashMap<String, HierarchyResponse?>>() {}.type
        )
        if (map != null) outputMap = map
        outputMap[key] = hierarchyResponse
        prefs.edit().putString(PREF_KEY_HIERARCHY_DATA, Gson().toJson(outputMap)).apply()
    }

    override fun getHierarchyData(pageNameDrp: String): HierarchyResponse? {
        val map = Gson().fromJson<HashMap<String, HierarchyResponse?>>(
            prefs.getString(
                PREF_KEY_HIERARCHY_DATA,
                ""
            ), object : TypeToken<HashMap<String, HierarchyResponse?>>() {}.type
        )
        try {
            return map.get(pageNameDrp)
        } catch (e: Exception) {
            return null
        }
    }


    override fun saveBingeButtonsEligibility(packButton: Map<String, PackButton?>?) {
        prefs.edit().putString(PREF_BINGE_BUTTONS, Gson().toJson(packButton)).apply()
    }

    override fun getBingeButtonsEligibility(): Map<String, PackButton?> {
        val r = Gson().fromJson<Map<String, PackButton?>>(prefs.getString(PREF_BINGE_BUTTONS, null), object : TypeToken<Map<String, PackButton?>>() {} .type) ?: emptyMap()
        return r
    }

    override fun saveSubscriptionType(type: String) {
        prefs.edit().putString(PREF_KEY_SUBSCRIPTION_TYPE, type).apply()
    }

    override fun getSubscriptionType(): String {
        return prefs.getString(PREF_KEY_SUBSCRIPTION_TYPE, "") ?: ""
    }

    override fun contentPlaybackAllowed(): Boolean = prefs.getBoolean(PREF_KEY_CONTENT_PLAYBACK_ALLOWED, false)

    override fun saveContentPlaybackAllowed(allowed: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_CONTENT_PLAYBACK_ALLOWED, true).apply()
//        prefs.edit().putBoolean(PREF_KEY_CONTENT_PLAYBACK_ALLOWED, allowed).apply()
    }

    override fun saveRMN(rmn: String) {
        prefs.edit().putString(PREF_KEY_RMN, rmn).apply()
    }

    override fun getRMN(): String = prefs.getString(PREF_KEY_RMN, "")?:""

    override fun setClearRMN(clearRmn: String) {
        prefs.edit().putString(PREF_KEY_CLEAR_RMN, clearRmn).apply()
    }

    override fun getClearRMN(): String = prefs.getString(PREF_KEY_CLEAR_RMN, "") ?: ""

    override fun getPartnerUniqueId(): String {
        return prefs.getString(PREF_KEY_PARTNER_UNIQUE_ID, "") ?: ""
    }

    override fun savePartnerUniqueId(partnerUniqueId: String?) {
        prefs.edit().putString(PREF_KEY_PARTNER_UNIQUE_ID, partnerUniqueId).apply()
    }

    override fun saveRateLimit(rateLimit:String) {
        prefs.edit().putString(PREF_RATE_LIMIT, rateLimit).apply()
    }

    override fun getRateLimit(): String? {
        return prefs.getString(PREF_RATE_LIMIT, null)
    }

    override fun getHotStarPopupCount(): Int {
        return prefs.getInt(PREF_HOTSTAR_POPUP_COUNT, 0)
    }

    override fun increaseHotStarPopupCount() {
        prefs.edit().putInt(PREF_HOTSTAR_POPUP_COUNT, getHotStarPopupCount()+1).apply()
    }

    override fun resetHotstarPopupCount() {
        prefs.edit().putInt(PREF_HOTSTAR_POPUP_COUNT, 0).apply()
    }

    override fun clearHotstarPopupData() {
        prefs.edit()
            .remove(PREF_KEY_HOTSTAR_DIALOG_LAST_FINAL_POPUP_SHOWN_TIME)
            .remove(PREF_KEY_HOTSTAR_DIALOG_IS_FIRST_CYCLE_COMPLETED)
            .apply()
    }

    override fun getPrimePopupCount(): Int {
        return prefs.getInt(PREF_PRIME_POPUP_COUNT, 0)
    }

    override fun setPrimePopupShownCount(count: Int) {
        prefs.edit().putInt(PREF_PRIME_POPUP_COUNT, count).apply()
    }

    override fun saveMixPanelId(distinctId: String) {
        prefs.edit().putString(PREF_MIX_PANEL_ID, distinctId).apply()
    }

    override fun getMixPanelId(): String? {
        return prefs.getString(PREF_MIX_PANEL_ID, null)
    }
    override fun saveRefrenceId(distinctId: String) {
        prefs.edit().putString(PREF_REF_PANEL_ID, distinctId).apply()
    }

    override fun getReferenceId(): String? {
        return prefs.getString(PREF_REF_PANEL_ID, null)
    }

    override fun getDTHSubStatus(): String? {
        return prefs.getString(PREF_ACCOUNT_SUB_STATUS, null)
    }

    override fun saveDTHAccountSubStatus(status: String) {
        prefs.edit().putString(PREF_ACCOUNT_SUB_STATUS, status).apply()
    }


    override fun isLoginAgain(): Boolean = prefs.getBoolean(PREF_KEY_SONY_LOGIN_AGAIN, true)

    override fun setLoginAgain(b: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_SONY_LOGIN_AGAIN, b).apply()
    }

    override fun isPrimeRedirectionEnabled() : Boolean {
        return prefs.getBoolean(PREF_KEY_PRIME_REDIRECTION_ENABLED, false)
    }

    override fun setPrimeRedirectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_PRIME_REDIRECTION_ENABLED, enabled).apply()
    }

    override fun getPrimeRedirectionDelay() : Int {
        return prefs.getInt(PREF_KEY_PRIME_REDIRECTION_DELAY, 0)
    }

    override fun setPrimeRedirectionDelay(delayInSec: Int) {
        prefs.edit().putInt(PREF_KEY_PRIME_REDIRECTION_DELAY, delayInSec).apply()
    }

    override fun saveDeviceCancellationFlag(status:Boolean) {
        prefs.edit().putBoolean(PREF_KEY_DEVICE_CANCELLATION_FLAG,false).apply()
    }

    override fun getDeviceCancellationFlag(): Boolean {
        return prefs.getBoolean(PREF_KEY_DEVICE_CANCELLATION_FLAG,false)
    }

    override fun saveNumberOfBingeAccount(nosBaids: Int) {
        prefs.edit().putInt(PREF_KEY_NUMBER_BA_ACCOUNT, nosBaids).apply()
    }

    override fun getNumberOfBingeAccount(): Int {
        return prefs.getInt(PREF_KEY_NUMBER_BA_ACCOUNT, 1)
    }

    override fun setLanguageWidgetVisibility(status: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_LANGUAGE_WIDGET_VISIBILITY,status).apply()
    }

    override fun getLanguageWidgetVisibility(): Boolean {
        return prefs.getBoolean(PREF_KEY_LANGUAGE_WIDGET_VISIBILITY,true)
    }


    override fun clearSwitchAccountInfo() {
        prefs.edit()
            .remove(PREF_FETCHED_PROFILE_DATA)
            .remove(PREF_FETCHED_BALANCE_DATA)
            .remove(PREF_KEY_SUBSCRIBED_PACK)
            .remove(PREF_SEARCH_KEYWORDS)
            .remove(PREF_KEY_TRAILER_AUTOPLAY)
            .remove(PREF_PARTNER_SET)
            .remove(PREF_KEY_FIRESTICK_TAKEN_STATUS)
            .remove(PREF_KEY_DUNNING_TIME)
            .remove(PREF_GENREAPI_TIME)
            .remove(PREF_KEY_CONTENT_PLAYBACK_ALLOWED)
            .remove(PREF_KEY_SONY_LOGIN_AGAIN)
            .remove(PREF_HOTSTAR_POPUP_COUNT)
            .remove(PREF_PRIME_POPUP_COUNT)
            .remove(PREF_KEY_DEVICE_CANCELLATION_FLAG)
            .remove(PREF_KEY_WATCH_NOTIFICATION)
            .remove(PREF_KEY_APP_LAUNCH_COUNT)
            .remove(PREF_KEY_HB_STARTING_POSITION)
            .remove(PREF_KEY_TEMP_SID)
            .remove(PREF_PREVIOUS_PACK)
            .remove(PREF_MIX_PANEL_ID)
            .remove(PREF_REF_PANEL_ID)
            .remove(PREF_KEY_SUBSCROPTION_DRAWER_LAST_SEEN)
            .remove(PREF_KEY_PAYMENT_STATUS_PENDING)
            .remove(PREF_KEY_PRIME_PACK)
            .remove(PREF_KEY_HELP_CENTER_URL)
            .remove(PREF_KEY_APP_LAUNCH_COUNT_LOGGED_IN)
            .remove(PREF_KEY_APP_LAUNCH_COUNT_GUEST)
            .remove(PREF_KEY_START_LAUNCH_COUNT)
            .remove(PREF_KEY_IS_ELIGIBLE_FOR_FREE_TRIAL)
            .remove(PREF_KEY_NEXT_GAME_NUDGE_TIME)
            .remove(PREF_KEY_GAME_NUDGE_LAST_TIME_SHOWN)
            .remove(PREF_KEY_LAUNCH_FREQUENCY_FOR_GAME_NUDGE)
            .remove(PREF_KEY_NEXT_GAME_NUDGE_COUNT)
            .remove(PREF_KEY_NEXT_GAME_ANIM_COUNT)
            .remove(PREF_KEY_NEXT_GAME_ANIM_TIME)
            .remove(PREF_KEY_MX_UPSELL_CLOSED)
            .remove(PREF_KEY_GENERIC_APP_LAUNCH_COUNT_FOR_LOGGED_IN_USER)
            .apply()
    }


    override fun setPrimePopupFrequency(frequency: Int) {
        prefs.edit().putInt(PREF_PRIME_POPUP_FREQUENCY, frequency).apply()
    }

    override fun setAppLaunchCount(launchCount:Int) {
        prefs.edit().putInt(PREF_KEY_APP_LAUNCH_COUNT,launchCount).apply()
    }

    override fun getAppLaunchCount(): Int {
        return prefs.getInt(PREF_KEY_APP_LAUNCH_COUNT,0)
    }

    override fun getCurrentStartingPosition():Int{
        return prefs.getInt(PREF_KEY_HB_STARTING_POSITION, 1)
    }

    override fun setCurrentStartingPosition(x:Int){
        prefs.edit().putInt(PREF_KEY_HB_STARTING_POSITION,x).apply()
    }

    override fun getPrimePopupFrequency(): Int {
        return prefs.getInt(PREF_PRIME_POPUP_FREQUENCY, 2)
    }

    override fun setExistingPrimeInterstitialFrequency(frequency: Int) {
        prefs.edit().putInt(PREF_PRIME_EXISTING_CLICK_FREQUENCY, frequency).apply()
    }

    override fun getExistingPrimeInterstitialFrequency(): Int {
        return prefs.getInt(PREF_PRIME_EXISTING_CLICK_FREQUENCY, 2)
    }

    override fun setPrimePopupInterval(interval: Int) {
        prefs.edit().putInt(PREF_PRIME_POPUP_PERIOD, interval).apply()
    }

    override fun getPrimePopupInterval(): Int {
        return prefs.getInt(PREF_PRIME_POPUP_PERIOD, 30)
    }

    override fun setExistingPrimeInterstitialInterval(interval: Int) {
        prefs.edit().putInt(PREF_PRIME_EXISTING_CLICK_PERIOD, interval).apply()
    }

    override fun getExistingPrimeInterstitialInterval(): Int {
        return prefs.getInt(PREF_PRIME_EXISTING_CLICK_PERIOD, 30)
    }

    override fun getExistingPrimeInterstitialClickCount(): Int {
        return prefs.getInt(PREF_PRIME_EXISTING_CLICK_COUNT,0)
    }

    override fun setExistingPrimeInterstitialClickCount(clickCount: Int) {
        prefs.edit().putInt(PREF_PRIME_EXISTING_CLICK_COUNT, clickCount).apply()
    }

    override fun setLastExistingPrimeButtonClick(timeStamp: Long) {
        prefs.edit().putLong(PREF_PRIME_EXISTING_LAST_CLICK, timeStamp).apply()
    }

    override fun getLastExistingPrimeButtonClick(): Long {
        return prefs.getLong(PREF_PRIME_EXISTING_LAST_CLICK, 0)
    }

    override fun setLastPrimePopupShownTimeStamp(timestamp: Long) {
        prefs.edit().putLong(PREF_PRIME_POPUP_LAST_SHOWN, timestamp).apply()
    }

    override fun getLastPrimePopupShownTimeStamp(): Long {
        return prefs.getLong(PREF_PRIME_POPUP_LAST_SHOWN, 0)
    }

    override fun saveAnonymousId(anonymousId: String) {
        prefs.edit().putString(PREF_ANONYMOUS_ID, anonymousId).apply()
    }

    override fun getAnonymousId(): String? {
        return prefs.getString(PREF_ANONYMOUS_ID, null)
    }

    override fun saveGuestProfileId(ProfileId: String) {
        prefs.edit().putString(PREF_GUEST_PROFILE_ID, ProfileId).apply()
    }

    override fun getGuestProfileId(): String? {
        return prefs.getString(PREF_GUEST_PROFILE_ID, "")
    }

    override fun saveGuestPreferredLanguages(preferredLanguages: List<Languages>) {
        prefs.edit().putString(PREF_GUEST_PREFERRED_LANGUAGES, Gson().toJson(preferredLanguages)).apply()
    }

    override fun getGuestPreferredLanguages(): List<Languages>? {
        return Gson().fromJson(
            prefs.getString(PREF_GUEST_PREFERRED_LANGUAGES, null),
            AnonymousResponse::class.java
        ) ?.data?.preferredLanguages
    }

    override fun setFirstTimeLanguagePopUpShown(firstTimeUser: Boolean) {
        prefs.edit().putBoolean(IS_FIRST_TIME_LANGUAGE_POP_UP, firstTimeUser).apply()
    }

    override fun isFirstTimeLanguagePopUpShown(): Boolean {
        return prefs.getBoolean(IS_FIRST_TIME_LANGUAGE_POP_UP, true)
    }

    override fun setParentalControlEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(IS_PARENTAL_CONTROL_ENABLED, enabled) }
    }

    override fun isParentalControlEnabled(): Boolean {
        return prefs.getBoolean(IS_PARENTAL_CONTROL_ENABLED, false)
    }

    override fun setParentalPinExists(parentalPinExistsValue: Boolean) {
        prefs.edit { putBoolean(PARENTAL_PIN_EXISTS_VALUE, parentalPinExistsValue) }
    }

    override fun isParentalPinExists(): Boolean {
        return prefs.getBoolean(PARENTAL_PIN_EXISTS_VALUE, false)
    }

    override fun setParentalRating(parentalRating: AgeRatingsResponse.AgeRatings) {
        prefs.edit { putString(PREF_PARENTAL_RATING_VALUE, Gson().toJson(parentalRating)) }
    }

    override fun getParentalRating(): AgeRatingsResponse.AgeRatings? {
        return Gson().fromJson<AgeRatingsResponse.AgeRatings>(
            prefs.getString(PREF_PARENTAL_RATING_VALUE, null),
            AgeRatingsResponse.AgeRatings::class.java
        )
    }

    override fun setFetchedProfileData(jsonData: String) {
        prefs.edit().putString(PREF_FETCHED_PROFILE_DATA, jsonData).apply()
    }

    override fun getFetchedProfileData(): String? {
        return prefs.getString(PREF_FETCHED_PROFILE_DATA, null)
    }

    override fun setFetchedBalanceData(jsonData: String) {
        prefs.edit().putString(PREF_FETCHED_BALANCE_DATA, jsonData).apply()
    }

    override fun saveAddModifyResponse(jsonData: String){
        prefs.edit().putString(PREF_ADD_MODIFY_RESPONSE,jsonData).apply()
    }

    override fun getAddModifyResponse(): AddPackResponse? {
        return Gson().fromJson(
            prefs.getString(PREF_ADD_MODIFY_RESPONSE, null),
            AddPackResponse::class.java
        )
    }

    override fun getFetchedBalanceData(): String? {
        return prefs.getString(PREF_FETCHED_BALANCE_DATA, null)
    }

    override fun saveBingeSid(bingeSubscriberId: String?) {
        prefs.edit().putString(PREF_BINGE_SID, bingeSubscriberId).apply()
    }

    override fun getBingeSid(): String? {
        return prefs.getString(PREF_BINGE_SID, null)
    }

    override fun saveLoginTimeStamp(timeMillis: Long) {
        prefs.edit { putLong(PREF_KEY_LOGIN_TIMESTAMP, timeMillis) }
    }

    override fun saveNudgeTimeUpdateEmail(timeMillis: Long) {
        prefs.edit { putLong(PREF_KEY_LAST_NUDGE_TIME_UPDATE_EMAIL, timeMillis) }
    }

    override fun saveNudgeTimeShowNotification(timeMills: Long) {
        prefs.edit { putLong(PREF_KEY_LAST_NUDGE_TIME_SHOW_NOTIFICATION, timeMills) }
    }

    override fun saveNudgeTimeNeverMissPlanRenewal(timeMills: Long) {
        prefs.edit { putLong(PREF_KEY_LAST_NUDGE_TIME_NEVER_MISS_PLAN_RENEWAL, timeMills) }
    }

    override fun saveNudgeTimeRenewalBeforeExpiry(timeMills: Long) {
        prefs.edit { putLong(PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_BEFORE_EXPIRY, timeMills) }
    }

    override fun saveNudgeTimeRenewalAfterExpiry(timeMills: Long) {
        prefs.edit { putLong(PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_AFTER_EXPIRY, timeMills) }
    }

    override fun getLoginTimeStamp(): Long {
        return prefs.getLong(PREF_KEY_LOGIN_TIMESTAMP, 0L)
    }

    override fun getNudgeTimeUpdateEmail(): Long {
        return prefs.getLong(PREF_KEY_LAST_NUDGE_TIME_UPDATE_EMAIL, 0L)
    }

    override fun getNudgeTimeShowNotification(): Long {
        return prefs.getLong(PREF_KEY_LAST_NUDGE_TIME_SHOW_NOTIFICATION, 0L)
    }

    override fun getNudgeTimeNeverMissPlanRenewal(): Long {
        return prefs.getLong(PREF_KEY_LAST_NUDGE_TIME_NEVER_MISS_PLAN_RENEWAL, 0L)
    }

    override fun getNudgeTimeRenewalBeforeExpiry(): Long {
        return prefs.getLong(PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_BEFORE_EXPIRY, 0L)
    }

    override fun getNudgeTimeRenewalAfterExpiry(): Long {
        return prefs.getLong(PREF_KEY_LAST_NUDGE_TIME_RENEW_PLAN_AFTER_EXPIRY, 0L)
    }

    override fun tempSaveSid(sId: String, dthStatus: String) {
        prefs.edit().putString(PREF_KEY_TEMP_SID, sId)
            .putString(PREF_KEY_TEMP_DTH_STATUS, dthStatus).apply()
    }

    override fun getTempSavedSid(): String? {
        return prefs.getString(PREF_KEY_TEMP_SID, null)
    }

    override fun resetHeroBannerCounts() {
        return prefs.edit().remove(PREF_KEY_HB_STARTING_POSITION)
            .remove(PREF_KEY_APP_LAUNCH_COUNT)
            .apply()
    }

    override fun setFirstTimeLandingOpen(b: Boolean) {
        prefs.edit().putBoolean(FIRST_TIME_LANDING_HOME_AFTER_LAUNCH, b).apply()
    }

    override fun isFirstTimeLandingOpen(): Boolean {
        return prefs.getBoolean(FIRST_TIME_LANDING_HOME_AFTER_LAUNCH, true)
    }

    override fun getSubscriptionDrawerLastSeen(): Long {
        return prefs.getLong(PREF_KEY_SUBSCROPTION_DRAWER_LAST_SEEN, 0L)
    }

    override fun setSubscriptionDrawerLastSeen(currentTime: Long) {
        prefs.edit().putLong(PREF_KEY_SUBSCROPTION_DRAWER_LAST_SEEN, currentTime).apply()
    }

    override fun saveUpdateInPackStatus(b: Boolean) {
        prefs.edit().putBoolean(UPDATE_IN_PACK, b).apply()
    }

    override fun isUpdateInPackStatus(): Boolean {
        return prefs.getBoolean(UPDATE_IN_PACK, false)
    }

    override fun getTempDthStatus(): String? {
        return prefs.getString(PREF_KEY_TEMP_DTH_STATUS, null)
    }

    override fun getPreviousSubscribedPack(): PartnerPacks? {
        return Gson().fromJson(
            prefs.getString(PREF_PREVIOUS_PACK, null),
            PartnerPacks::class.java
        )
    }

    override fun savePreviousSubscribedPack(pack: PartnerPacks?) {
        pack?.let {
            prefs.edit().putString(PREF_PREVIOUS_PACK, Gson().toJson(pack)).apply()
        }
    }

    override fun saveFirstInvention() {
        prefs.edit().putBoolean(PREF_FIRST_USER_INVENTION, true).apply()
    }

    override fun saveFirstContentClick() {
        prefs.edit().putBoolean(PREF_FIRST_CONTENT_CLICK, true).apply()
    }

    override fun saveFirstFreeContentPlay() {
        prefs.edit().putBoolean(PREF_FIRST_CONTENT_FREE_PLAY, true).apply()
    }

    override fun saveFirstPremiumContentPlay() {
        prefs.edit().putBoolean(PREF_FIRST_CONTENT_PREMIUM_PLAY, true).apply()
    }

    override fun getFirstInvention(): Boolean {
        return prefs.getBoolean(PREF_FIRST_USER_INVENTION, false)
    }

    override fun getFirstContentClick(): Boolean {
        return prefs.getBoolean(PREF_FIRST_CONTENT_CLICK, false)
    }

    override fun getFirstFreeContentPlay(): Boolean {
        return prefs.getBoolean(PREF_FIRST_CONTENT_FREE_PLAY, false)
    }

    override fun getFirstPremiumContentPlay(): Boolean {
        return prefs.getBoolean(PREF_FIRST_CONTENT_PREMIUM_PLAY, false)
    }

    override fun getPaymentPendingStatus(): Boolean {
        return prefs.getBoolean(PREF_KEY_PAYMENT_STATUS_PENDING, false)
    }

    override fun setPaymentPendingStatus(paymentPendingStatus: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_PAYMENT_STATUS_PENDING, paymentPendingStatus).apply()
    }

    override fun setFirstAppLaunchTimeInUTC(currentTimeInUTC: String) {
        prefs.edit().putString(PREF_KEY_FIRST_APP_LAUNCH_TIME_IN_UTC, currentTimeInUTC).apply()
    }

    override fun getFirstAppLaunchTimeInUTC() =
        prefs.getString(PREF_KEY_FIRST_APP_LAUNCH_TIME_IN_UTC, null)

    override fun saveNumberOfContentPlaybackForAppRating(value: Int) {
        prefs.edit().putInt(PREF_KEY_NUMBER_OF_CONTENT_PLAYBACK, value).apply()
    }

    override fun getNumberOfContentPlaybackForAppRating(): Int =
        prefs.getInt(PREF_KEY_NUMBER_OF_CONTENT_PLAYBACK, 0)

    override fun setIsEligibleForAppRating(value: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_IS_APP_RATING_SUBMITTED, value).apply()
    }

    override fun isEligibleForAppRating(): Boolean =
        prefs.getBoolean(PREF_KEY_IS_APP_RATING_SUBMITTED, true /*By default everyone eligible until BE checks come*/)

    override fun setIsEligibleForFreeTrial(value: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_IS_ELIGIBLE_FOR_FREE_TRIAL, value).apply()
    }

    override fun getEligibleForFreeTrial(): Boolean {
        return prefs.getBoolean(PREF_KEY_IS_ELIGIBLE_FOR_FREE_TRIAL, false)
    }

    override fun getNextGameNudgeTime() : Long =
        prefs.getLong(PREF_KEY_NEXT_GAME_NUDGE_TIME,0L)

    override fun setNextGameNudgeTime(time:Long) {
        prefs.edit().putLong(PREF_KEY_NEXT_GAME_NUDGE_TIME,time).apply()
    }

    // For Game nudge

    override fun getGameNudgeOpenCount():Int =
        prefs.getInt(PREF_KEY_NEXT_GAME_NUDGE_COUNT, 0)

    override fun setGameNudgeOpenCount(count: Int) {
        prefs.edit().putInt(PREF_KEY_NEXT_GAME_NUDGE_COUNT, count).apply()
    }

    override fun getLastGameNudgeTime() : Long =
        prefs.getLong(PREF_KEY_NEXT_GAME_NUDGE_TIME,0L)

    override fun setLastGameNudgeTime(time:Long) {
        prefs.edit().putLong(PREF_KEY_NEXT_GAME_NUDGE_TIME,time).apply()
    }


    // For Game animation

    override fun getGameAnimOpenCount(): Int =
        prefs.getInt(PREF_KEY_NEXT_GAME_ANIM_COUNT, 0)

    override fun setGameAnimOpenCount(count: Int) {
        prefs.edit().putInt(PREF_KEY_NEXT_GAME_ANIM_COUNT, count).apply()
    }

    override fun getLastGameAnimTime(): Long =
        prefs.getLong(PREF_KEY_NEXT_GAME_ANIM_TIME, 0L)

    override fun setLastGameAnimTime(time: Long) {
        prefs.edit().putLong(PREF_KEY_NEXT_GAME_ANIM_TIME, time).apply()
    }

    override fun resetFSDialog() {
        setFirestickDialogLastVisibleTime(0)
        setAppLaunchValue(0)
        setFirestickDialogFirstVisbileTime(0)
    }

    override fun getGameNudgeShown(): Boolean =
        prefs.getBoolean(PREF_KEY_GAME_NUDGE_SHOWN,false)

    override fun setGameNudgeShown(b: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_GAME_NUDGE_SHOWN,b).apply()
    }

    override fun setSilentLoginCalled(b: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_SILENT_LOGIN,b).apply()
    }

    override fun isSilentLoginCalled(): Boolean =
        prefs.getBoolean(PREF_KEY_SILENT_LOGIN, false)

    override fun saveSilentLoginTimestamp(timeStamp: String) {
        prefs.edit().putString(PREF_KEY_SILENT_LOGIN_TIMESTAMP, timeStamp).apply()
    }

    override fun getSilentLoginTimestamp(): String? {
        return prefs.getString(PREF_KEY_SILENT_LOGIN_TIMESTAMP , null)
    }

    override fun setMxUpsellClosed(shown : Boolean ){
        prefs.edit().putBoolean(PREF_KEY_MX_UPSELL_CLOSED,shown).apply()
    }

    override fun getMxUpsellClosed() : Boolean {
        return prefs.getBoolean(PREF_KEY_MX_UPSELL_CLOSED,false)
    }

    override fun saveLoggedInAppLaunchCountForRegionalAppNudge(launchCount: Int) {
        prefs.edit {
            putInt(PREF_KEY_GENERIC_APP_LAUNCH_COUNT_FOR_LOGGED_IN_USER, launchCount)
        }
    }

    override fun getLoggedInAppLaunchCountForRegionalAppNudge(): Int =
        prefs.getInt(PREF_KEY_GENERIC_APP_LAUNCH_COUNT_FOR_LOGGED_IN_USER, 0)

    override fun saveLastPgSdkProcessStatus(status: String?) {
        prefs.edit {
            putString(PREF_KEY_LAST_PG_PROCESS_STATUS, status)
        }
    }

    override fun getLastPgSdkProcessStatus(): String? =
        prefs.getString(PREF_KEY_LAST_PG_PROCESS_STATUS, null)

    /*Save New Provider Info and names for Genereic Partner Integrations*/
    override fun saveAllowedProviders(list: ArrayList<String>) {
        prefs.edit().putStringSet(PREF_KEY_ALLOWED_PROVIDERS, list.toSet()).apply()
    }

    override fun saveAvailableProviders(map: HashMap<String, ConfigResponse.AvailableProviders>) {
        prefs.edit().putString(PREF_KEY_AVAILABLE_PROVIDERS, Gson().toJson(map)).apply()
    }

    override fun getAllowedProviderList(): List<String>? {
        return (prefs.getStringSet(PREF_KEY_ALLOWED_PROVIDERS, null) ?: return emptyList()).toList()
    }

    override fun getAllowedProviderInfo(): HashMap<String, ConfigResponse.AvailableProviders>? {
        return Gson().fromJson<HashMap<String, ConfigResponse.AvailableProviders>>(
            prefs.getString(PREF_KEY_AVAILABLE_PROVIDERS, null),
            object : TypeToken<HashMap<String, ConfigResponse.AvailableProviders>>() {} .type)
    }
    override fun setManagedAppEnabled(isManageAppEnable: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_MANAGED_APP_ENABLED, isManageAppEnable).apply()
    }

    override fun isManagedAppEnabled(): Boolean {
        return prefs.getBoolean(PREF_KEY_MANAGED_APP_ENABLED, true)//getConfigResponse()?.data?.config?.managedAppEnabled == true
//        return true
    }

    override fun isGameHapticFeedbackEnabled() =
        prefs.getBoolean(PREF_KEY_GAME_VIBRATION_ENABLED, true)

    override fun enableGameHapticFeedback(status: Boolean) {
        prefs.edit {
            putBoolean(PREF_KEY_GAME_VIBRATION_ENABLED, status)
        }
    }

    override fun saveGenericAppLaunchCount(count: Int) {
        prefs.edit {
            putInt(PREF_KEY_GENERIC_APP_LAUNCH_COUNT, count)
        }
    }

    override fun getGenericAppLaunchCount(): Int =
        prefs.getInt(PREF_KEY_GENERIC_APP_LAUNCH_COUNT, 0)

    override fun saveCoachMarkLaunchFrequency(count: Int?) {
        count?.let {
            prefs.edit {
                putInt(PREF_KEY_COACH_MARK_FREQUENCY, it)
            }
        }
    }

    override fun getCoachMarkLaunchFrequency(): Int =
        prefs.getInt(PREF_KEY_COACH_MARK_FREQUENCY, -1)

    override fun isHomeScreenSearchCoachMarkEnabled() =
        prefs.getBoolean(PREF_KEY_HOME_SCREEN_SEARCH_COACH_MARK_ENABLED, true)

    override fun enableHomeScreenSearchCoachMark(status: Boolean) {
        prefs.edit {
            putBoolean(PREF_KEY_HOME_SCREEN_SEARCH_COACH_MARK_ENABLED, status)
        }
    }

    override fun isSearchScreenMicCoachMarkEnabled() =
        prefs.getBoolean(PREF_KEY_SEARCH_SCREEN_MIC_COACH_MARK_ENABLED, true)

    override fun enableSearchScreenMicCoachMark(status: Boolean) {
        prefs.edit {
            putBoolean(PREF_KEY_SEARCH_SCREEN_MIC_COACH_MARK_ENABLED, status)
        }
    }

    override fun removeHierarchyData() {
        prefs.edit().remove(PREF_KEY_HIERARCHY_DATA).apply()
    }

    // To save inbox message click flag CT
    override fun saveNotificationClickedId(ctId:String){
        prefs.edit {
            putBoolean(ctId,true)
        }
    }
    // To get inbox message click flag for CT
    override fun isNotificationClickedIdExit(ctId: String):Boolean{
        return  prefs.getBoolean(ctId,false)
    }


    override fun setWelcomeDialogStatus(status: Boolean) {
        prefs.edit {
            putBoolean(PREF_KEY_WELCOME_DIALOG_STATUS, status)
        }
    }

    override fun getWelcomeDialogStatus() =  prefs.getBoolean(PREF_KEY_WELCOME_DIALOG_STATUS, false)
}