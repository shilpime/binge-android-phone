
package com.tatasky.binge.utils

const val EXPIRED = "Expired"
const val SUBSCRIBED = "Subscribed"
const val CANCELLED = "Cancelled"
const val VIDEO_TRACK: Int = 0
const val AUDIO_TRACK: Int = 1
const val SUBTITLE_TRACK: Int = 2
const val PAGE_LIMIT_TA_RAIL: String = "30"

const val STATE_NORMAL = 0
const val STATE_PRESSED = 1
const val STATE_RECORDING = 2

const val INTENT_LANGUAGE = "LANGUAGE"
const val INTENT_GENRE = "GENRE"
const val INTENT_LANGUAGE_GENRE = "GENRE_LANGUAGE"

const val QUALITY_AUTO = "Auto"
const val QUALITY_VERY_LOW = "Very Low"
const val QUALITY_LOW = "Low"
const val QUALITY_MEDIUM = "Medium"
const val QUALITY_VERY_MEDIUM = "Very Medium"
const val QUALITY_HIGH = "High"
const val QUALITY_VERY_HIGH = "Very High"

// Error Codes
const val RESPONSE_CODE_SUCCESS = 200        // Success
const val CODE_SUCCESS = 0        // Success
const val RESPONSE_CODE_DUNNING_RECHARGE = 40033        // Success
const val RESPONSE_CODE_INVALID_EMAIL = 700006
const val RESPONSE_CODE_INVALID_FIRST_NAME = 20109
const val RESPONSE_CODE_UNEXPECTED = 444    // No Response
const val RESPONSE_CODE_SERVER_ERROR = 500 // Unknown Error
const val RESPONSE_CODE_UNAUTHORIZED = 401 // Unauthorized
const val RESPONSE_CODE_NETWORK_ERROR = 99
const val RESPONSE_CODE_DEACTIVATED = 403  // Forbidden
const val RESPONSE_CODE_NOT_FOUND = 404
const val RESPONSE_CODE_TIMEOUT = 408
const val RESPONSE_CODE_UN_PROCESSABLE_ENTITY = 429
const val RESPONSE_CODE_UNPROCESSABLE_ENTITY = 422 // Unprocessable Entity
const val RESPONSE_CODE_BAD_REQUEST = 400 // Bad Request
const val RESPONSE_CODE_CONFLICT = 409 // Conflict
const val RESPONSE_CODE_500 = 500
const val RESPONSE_CODE_BAD_GATEWAY = 502
const val RESPONSE_CODE_SERVICE_TEMPORARY_UNAVAILABLE = 503
const val RESPONSE_CODE_NO_DATA = 101 // Locally defined : No data
const val RESPONSE_CODE_DOWNGRADE_ERROR = 40019
const val RESPONSE_CODE_DOWNGRADE_ERROR2 = 100014
const val RESPONSE_CODE_DOWNGRADE_ERROR3 = 3007
const val RESPONSE_CODE_PASSWORD_ERROR = 6034
const val RESPONSE_CODE_LOW_BALANCE_ERROR = 100030
const val RESPONSE_CODE_OTP_ERROR = 100014
const val RESPONSE_CODE_RATE_LIMIT = 429
const val ERROR_CODE_CONCURRENCY = 130401
const val ERROR_CODE_SUBSCRIBER_NOT_FOUND = 20022

const val RESPONSE_CODE_WRONG_CURRENT_PASSWORD = 6031
const val RESPONSE_CODE_WRONG_PASSWORD = 6035
const val RESPONSE_CODE_WRONG_OTP = 60001

const val CUSTOM_RESPONSE_CODE_SERIES_ADDING = -101010101
const val CUSTOM_RESPONSE_CODE_ZEE5_ERROR = -101010102

const val DTH_ERROR_CODE = 80002
const val DTH_FRESH_IF_ERROR_CODE = 700014
const val FORCE_LOGIN_ERROR_CODE = 40016
const val FORCE_LOGIN_ERROR_CODE2 = 80001
const val LOGIN_MAX_DEVICE_ERROR_CODE = 200007
const val DTH_PENDING_ERROR_CODE = 40041

const val PRIME_NOT_SUBSCRIBED_BINGE = 900004
const val PRIME_INACTIVE_SUBSCRIBED_BINGE = 900012
const val PRIME_TS_BALANCE_LOW = 900006
const val PRIME_TS_PENDING_ACTIVATION = 900013

const val INSUFFICIENT_DTH_BALANCE = 1000106
const val PAYMENT_FAILURE = 1000107

const val CODE_LOGOUT_ALL = 100048




/**
 * Device Info Keys
 */
const val KEY_PLATFORM = "pl"
const val KEY_OS_VERSION = "os"
const val KEY_IP_ADDRESS = "ip"
const val KEY_LOCALE = "lo"
const val KEY_APP_VERSION = "app"
const val KEY_NETWORK_TYPE = "net"
const val KEY_DEVICE_NAME = "deviceName"
const val KEY_CARRIER = "car"
const val KEY_MAC_ADDRESS = "ma"
const val KEY_DEVICE_ID = "deviceId"
const val KEY_DEVICE_TYPE = "deviceType"
const val KEY_DEVICE_TYPE_OPEN = "OPEN"
const val KEY_DEVICE_TYPE_CLOSED = "CLOSED"
const val KEY_SOURCE_TYPE = "sourceApp"
const val KEY_SOURCE_STAGE = "stage"
const val KEY_SOURCE_UAT = "uat"
const val KEY_SOURCE_PROD = "prod"

/**
 * End Of Device Info Keys
 */

const val TYPE_MOVIES = "MOVIES"
const val TYPE_WEB_SHORTS = "WEB_SHORTS"//WEB_SHORTS
const val TYPE_TV_SHOWS = "TV_SHOWS"
const val TYPE_CUSTOM_TV_SHOWS_DETAIL = "CUSTOM_TV_SHOWS_DETAIL"
const val TYPE_LANGUAGE = "LANGUAGE"
const val TYPE_GENRE = "GENRE"
const val TYPE_BRAND = "BRAND"
const val TYPE_CATCH_UP = "CATCH_UP"
const val TYPE_SERIES = "SERIES"
const val TYPE_SEASONS = "SEASONS"
const val TYPE_SERIES_CHILD = "SERIES_CHILD_LOCAL"
const val TYPE_BRAND_CHILD = "BRAND_CHILD_LOCAL"
const val TYPE_CUSTOM_WEB_VIEW = "CUSTOM_WEB_VIEW"
const val TYPE_LIVE = "LIVE"
const val TYPE_LIVE_EVENT = "LIVE_EVENT"
const val TYPE_CUSTOM_PRIME = "CUSTOM_PRIME"
const val TYPE_SUB_PAGE = "SUB_PAGE"
const val TYPE_GAMES = "GAMES"

/**
 * FS PopUp Keys
 */
const val KEY_DIALOG_VISIBILITY_TYPE_TIME = "time"
const val KEY_DIALOG_VISIBILITY_TYPE_EVENT = "event"
const val KEY_DIALOG_VISIBILITY_TYPE_TIME_EVENT = "time-event"


/**
 * Header Keys
 */
const val KEY_HEADER_MOBILE_NUMBER = "mobileNumber"
const val KEY_HEADER_DTH_STATUS = "dthStatus"
const val KEY_HEADER_PLATFORM = "platform"
const val KEY_HEADER_VERSION = "apiVersion"
const val KEY_HEADER_APP_VERSION = "appVersion"
const val KEY_HEADER_DEVICE_TOKEN = "deviceToken"
const val KEY_HEADER_FREEMIUM_USER= "freemiumUser"
const val KEY_HEADER_LOCALE = "locale"
const val KEY_HEADER_PROFILE_ID = "profileId"
const val KEY_HEADER_SUBSCRIBER_ID = "subscriberId"
const val KEY_HEADER_S_ID = "sId"
const val KEY_HEADER_DSN = "dsn"
const val KEY_HEADER_BAID = "baId"
const val KEY_HEADER_DSN_EXIST = "dsnExists"

const val KEY_HEADER_AUTH = "authorization"
const val KEY_HEADER_TYPE = "header_type"
const val KEY_HEADER_AUTH_USER = "x-authenticated-userid"
const val KEY_HEADER_AUTH_APP_ID = "x-app-id"
const val KEY_HEADER_AUTH_APP_KEY = "x-app-key"
const val KEY_HEADER_AUTH_SUB_ID = "x-subscriber-id"
const val KEY_HEADER_AUTHORIZATION = "Authorization"
const val KEY_HEADER_AUTH_SUB_NAME = "x-subscriber-name"
const val KEY_HEADER_AUTH_DEVICE_ID = "x-device-id"
const val KEY_HEADER_AUTH_DEVICE_TYPE = "x-device-type"
const val KEY_HEADER_AUTH_DEVICE_PLATFORM = "x-device-platform"
const val KEY_HEADER_BINGE_PRODUCT = "bingeProduct"
const val KEY_HEADER_BEFORE_LOGIN = "beforeLogin"
const val KEY_HEADER_ANONYMOUS_ID = "anonymousId"
const val KEY_HEADER_UNIQUE_ID = "uniqueId"
const val KEY_HEADER_GAUTH_TOKEN = "g-auth-token"
const val KEY_HEADER_CALLED_FROM = "calledFrom"
const val KEY_SUBSCRIPTION_TYPE = "subscriptionType"

const val KEY_HEADER_TICK_TICK = "ticktick"
const val KEY_HEADER_PARTNERS = "partners"
/**
 * End Of Headers Key
 */

/**
 * Header Type
 */
const val HEADER_TYPE_OLD_BA = "migrateOldUser"
const val KEY_HEADER_TYPE_PARTNER_UNIQUE_ID = "partnerUniqueId"
const val HEADER_VALUE_DEVICE_TYPE = "Android"
const val HEADER_VALUE_PLATFORM_BA = "BINGE_ANYWHERE"
const val HEADER_TYPE_BA = "binge_anywhere"
const val HEADER_TYPE_BA_DEVICE_MANAGEMENT = "binge_anywhere_device_management"
const val HEADER_TYPE_PARENTAL_CONTROL = "parental_control"
const val HEADER_TYPE_FREEMIUM_ACCOUNT_DETAILS = "freemium_account_details"
const val HEADER_TYPE_CONFIG = "config"
const val HEADER_TYPE_BA_CREATE = "binge_anywhere_create"
const val HEADER_TYPE_DEVICE = "freemium_device"
const val HEADER_TYPE_ALIAS = "header_alias"
const val HEADER_VALUE_PLATFORM_TA = "binge_anywhere_android"
const val HEADER_TYPE_DONGLE = "binge_mobile"
const val HEADER_TYPE_NO_CACHE = "no_cache"
const val HEADER_TYPE_NONE = "header_none"
const val HEADER_TYPE_TVOD = "header_tvod"
const val HEADER_TYPE_RRM = "header_rrm"
const val HEADER_TYPE_VRTA = "header_vrta"
const val HEADER_TYPE_VRTARAIL = "header_vrta_rail"
const val HEADER_TYPE_VR_WITH_AUTH = "header_rrm_auth"
const val HEADER_TYPE_VR_WITH_AUTH_NO_CACHE = "header_rrm_auth_no_cache"
const val HEADER_TYPE_NO_HEADER = "header_no_value"
const val HEADER_TYPE_SA = "header_switch_account"
const val HEADER_TYPE_BA_LOGIN = "ba_login"
const val HEADER_TYPE_SID_AUTH = "sid_auth"
const val HEADER_TYPE_DSN = "header_dsn"
const val HEADER_TYPE_BAID = "header_baid"
const val HEADER_TYPE_VOOT_KIDS_UNIQUE_ID = "header_vootkids_uniqueid"
const val HEADER_TYPE_VOOT_SELECT_UNIQUE_ID = "header_vootselect_uniqueid"
const val HEADER_TYPE_ZEE5_UNIQUE_ID = "header_zee5_uniqueid"
const val HEADER_TYPE_HELP_CENTER_URL = "header_help_center"
const val HEADER_TYPE_CW = "header_type_cw"
const val HEADER_TYPE_CONTROL_CHANGE = "control_changes"
const val HEADER_TYPE_HOICHOI_TOKEN = "Bearer I71hIsFWI8fqn2CPLdZPGq3bq1fYkBth"
const val HEADER_FROM_NUDGE = "nudge"
const val HEADER_FROM_SETTINGS = "settings"

val HEADER_TYPE_VR_GENERATE_OTP = "header_vr_generateOtp"
const val HEADER_TYPE_VR_VALIDATE_OTP = "header_vr_validateOtp"
val HEADER_TYPE_VR_LOOKUP = "header_vr_lookup"
val HEADER_TYPE_VR_WITH_AUTH_DSN = "header_rrm_auth_dsn"
val HEADER_TYPE_VR_WITH_AUTH_SALES_TYPE = "header_rrm_auth_sales_type"
val HEADER_VALUE_PLATFORM = "dongle"
val HEADER_VALUE_NETWORK_TYPE = "Wifi"
val HEADER_VALUE_CARRIER = ""
val HEADER_VALUE_AUTH = ""
val HEADER_VALUE_KEY_HEADER_X_API_LOGIN_GENERATE = "ylFS29nVnD1A59ucq6NpcM0QiNIZI2Nd"
val HEADER_VALUE_KEY_HEADER_X_API_LOGIN_VALIDATE = "88k8Wwdz1NtqykinxmBbxXfazcbhOphf"
val HEADER_VALUE_LOOKUP = "cHwH9oW4zAcxVTkD2ZxaivxljEEXhLG2"

/**
 * End Of Header Type
 */
const val PROVIDER = "PROVIDER"


val UNAUTHORISED_MESSAGE = "401 Unauthorized"
val PROVIDER_TATA_SKY = "tatasky"
val PROVIDER_HUNGAMA = "hungama"
val PROVIDER_SUN_NEXT = "sunnxt"
val PROVIDER_HOTSTAR = "hotstar"
val PROVIDER_EROSNOW = "erosnow"
val PROVIDER_ZEE5 = "zee5"
val PROVIDER_PRIME = "prime"
val PROVIDER_SHEMAROO = "Shemaroome"
val PROVIDER_VOOTSELECT = "VootSelect"
val PROVIDER_VOOTKIDS = "VootKids"
val PROVIDER_CURIOSITY_STREAM = "CuriosityStream"
val PROVIDER_SONYLIV = "SonyLiv"
val PROVIDER_EPIC_ON     = "EpicOn"
val PROVIDER_DOCU_BAY    = "DocuBay"
val PROVIDER_HOICHOI    = "HOICHOI"
val PROVIDER_MXPLAYER    = "MXPLAYER"
val PROVIDER_GAMEZOP = "GameZop"
val PROVIDER_CHAUPAL    = "CHAUPAL"
val PROVIDER_PLANET_MARATHI = "PLANETMARATHI"
val PROVIDER_NAMMAFLIX = "sjfsd"
val PROVIDER_LIONSGATE = "LIONSGATE"


val subscriptionTypeAtv = "atv"
val subscriptionTypeFtv = "ANDROID_STICK"
val subscriptionTypeBingeMobile = "ANYWHERE"
val subscriptionTypeFreemium = "FREEMIUM"

val UNREACHABLE_ERROR_MSG =
    "There seems to be a problem accessing details on this screen. Please try again."
val GENERIC_ERROR_MSG =
    "Oops! An error has occurred on our server. Please check internet connection and try to playback again!"
const val NETWORK_ERROR_MSG = "Make sure that Wi-Fi or mobile data is turned on, then try again."
const val COMMON_ERROR_MSG = "The operation couldn’t be completed."
const val COMMON_ERROR_TITLE = "Something Went Wrong"
const val TIMEOUT_ERROR_MSG = "Your request timed out. Please try again in some time."

const val RECOMMENDATION = "Recommendation"
const val CONTINUE_WATCHING = "CONTINUE_WATCHING"
const val TVOD = "TVOD"
const val EDITORIAL = "Editorial"
const val HB_SEE_ALL = "HB_SEE_ALL"
const val GAMES_FAV = "GAMES_FAV"
const val WATCHLIST = "WATCHLIST"

const val TRANSACTIONAL_CHANNEL_ID = "1001"
const val WATCH_CHANNEL_ID = "1002"
const val OFFERS_CHANNEL_ID = "1003"

const val TRANSACTIONAL_NOTI_SETTINGS_KEY = "TRANSACTIONAL_NOTIFICATION"
const val WATCH_NOTI_SETTINGS_KEY = "WATCH_NOTIFICATION"
const val AUTO_PLAY_TRAILER_SETTINGS_KEY = "AUTO_PLAY_TRAILER"

const val HERO_BANNER = "HERO_BANNER"
const val FREE = "FREE"
const val CLEAR = "CLEAR"
const val SUBSCRIPTION = "SUBSCRIPTION"
const val RENTAL = "RENTAL"
const val RENTAL_PURCHASED_NOTEXPIRED = "ACTIVE"
const val RENTAL_PURCHASED_EXPIRED = "EXPIRED"
const val PREMIUM = "PREMIUM"
const val FREE_AVOD = "free_advertisement"

const val PRIME_ACTIVATED = "ACTIVATED"
const val PRIME_SUSPENDED = "SUSPENDED"
const val PRIME_EXPIRED = "EXPIRED"
const val PRIME_CANCELLED = "CANCELLED"
const val PRIME_CANCELLATION_INITIATED = "CANCELLED_INITIATED"

const val SERVER_DATE_TIME_FORMAT = "dd/MM/yyyy"
const val DEVICE_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss"
const val DIALOG_TAG = "DialogTag"
const val LOGOUT_DIALOG_TAG = "LogoutDialogTag"
const val APP_UPDATE_DIALOG_TAG = "UpdateDialogTag"
const val ATV_DIALOG_TAG = "AtvDialogTag"

const val CURRENT_EPISODE = "CurrentEpisode"


enum class AccountStatusEnum(val status: String) {
    ACTIVE("Active"),
    DEACTIVATED("Deactivated"),
    TEMP_SUSPENSION("Temp_Suspension"),
    PENDING("Pending"),
    WRITTEN_OFF("Written_Off"),
    CANCELLED("Cancelled"),
    BLACKLISTED("Blacklisted"),
    PARTIAL_DUNNED("Partial_Dunned"),
    PARTIALLY_DUNNED("Partially Dunned");
}

enum class SubscriptionPackStatusEnum(val status: String) {
    ACTIVE("Active"),
    DEACTIVE("Deactive"),
    WRITTEN_OFF("Written_Off"),
}
/*
* Notification constants
* */
const val KEY_NOTIFICATION_HOME="HOME"
const val KEY_NOTIFICATION_HOME_ERROR = "HOME_ERROR"
const val KEY_NOTIFICATION_DETAIL="DETAIL_SCREEN"
const val KEY_NOTIFICATION_WATCHLIST="WATCHLIST"
const val KEY_NOTIFICATION_MY_ACCOUNT="MY_ACCOUNT"
const val KEY_NOTIFICATION_SEE_ALL="SEE_ALL"
const val KEY_NOTIFICATION_LOGIN="LOGIN"
const val KEY_NOTIFICATION_MANAGE_PACK="MANAGE_PACK"
const val KEY_NOTIFICATION_MANAGED_APP_USE_CASE="MANAGED_APP_USE_CASE"
const val KEY_NOTIFICATION_SELFCARE="SELFCARE_RECHARGE"
const val KEY_NOTIFICATION_HELP="HELP_FAQ"
const val KEY_NOTIFICATION_PARTNER = "PARTNER_HOME"
const val KEY_NOTIFICATION_GAMES = "GAMES"
const val KEY_NOTIFICATION_GAMES_HOME = "HOME-GAMEZOP"
const val KEY_SCREEN_NAME = "screenName"
const val KEY_SCREEN_DATA = "screenData"
const val KEY_FROM_SCREEN = "fromScreen"

/*
* Deeplink constants
* */
const val KEY_MY_SUBSCRIPTION = "my-subscription"
const val KEY_PACK_SELECTION = "pack-selection"
const val KEY_RECHARGE = "recharge"
const val KEY_MY_ACCOUNT = "my-account"
const val KEY_DETAIL = "detail"
const val KEY_SEE_ALL = "see-all"
const val KEY_LANGUAGE_GENRE = "language-genre"
const val KEY_SEARCH = "search"
const val KEY_WATCHLIST = "watchlist"
const val KEY_MORE = "more"
const val KEY_HOME_PAGE = "home-page" //For Home Menu Item/Tab //TODO: Remove BA UseCases
const val KEY_PRIME = "prime" //Amazon Prime
const val KEY_BINGE_LIST = "bingelist"
const val KEY_PARTNER = "partner" // App/Partner Page (Shows content of a partner)
const val KEY_FAQ = "faq"
const val KEY_APP_SEE_ALL = "app-see-all" // All available partners
const val KEY_HOME = "home"
const val KEY_MOVIES = "movies"
const val KEY_SHOWS = "shows"
const val KEY_KIDS = "kids"
const val KEY_CATEGORIES = "categories"
const val KEY_PARENTAL_CONTROL = "parental-control"
const val KEY_CONTENT_LANGUAGE = "content-language"
const val KEY_TRANSACTION_HISTORY = "transaction-history"
const val KEY_DEVICE_MANAGEMENT = "device-management"
const val KEY_EDIT_PROFILE = "profile"
const val KEY_SETTING = "setting"
const val KEY_SUBSCRIPTION = "subscription"
const val KEY_LANGUAGE = "language"
const val KEY_LOGIN = "login"
const val KEY_NOTIFICATION = "notification"
const val KEY_GAMES = "games"
const val KEY_SPORTS = "sports"

/*
* Mid scroll screen names
* */
const val MID_SCROLL_MOVIES = "MOVIES"
const val MID_SCROLL_SHOWS = "SHOWS"
const val MID_SCROLL_GAMEZOP = "HOME-GAMEZOP"
const val MID_SCROLL_SPORTS = "SPORTS"
const val MID_SCROLL_DETAIL_SCREEN = "DETAIL_SCREEN"



/*
* Payment status constants
* */

const val SUCCESS = "SUCCESS"
const val INPROGRESS  = "INPROGRESS"
const val FAILURE = "FAILURE"
const val PENDING = "PENDING"
const val NOT_ATTEMPTED = "NOT-ATTEMPTED"
const val NOT_SELECTED = "NOT-SELECTED"

/*
* ShemarooMe Analytics Constants
* */
const val APPLICATION = "tata-sky"
const val REGION = "IN"


const val REFRESH_HOME = "REFRESH_HOME"

internal interface LAErrorCode {
    companion object {
        const val STATUS_OK = 200
        const val BAD_REQUEST = 400
        const val UNAUTHORISED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val NOT_ALLOWED = 405
        const val CONFLICT = 409

        //4104
        const val REQUEST_TOO_LONG = 414
        const val TOO_MANY_REQUESTS = 429
        const val INTERNAL_SERVER_ERROR = 500 // NOT GETTING USED IN SDK
        const val SERVICE_UNAVAILABLE = 503
    }
}
internal interface InterruptedBottomTabConstants {
    companion object {
//        const val SOURCE_PARENTAL_PIN = "SOURCE_PARENTAL_PIN"
        const val SOURCE_CATEGORIES = "SOURCE_CATEGORIES"
    }
}
internal interface ControlErrorCodes {
    companion object {
        const val SESSION_FAILURE = 100202
        const val CONCURRENT_STREAM_ERROR = 130401
        const val REGION_BLOCKED = 130301
        const val PROXY_ERROR = 130302
        const val CUSTOM_ERROR = 101010 // Custom error code when URL and EPIds are null
    }
}

/*
* Freemium DTH status keys*/
//DTH With Binge Old Stack
const val DTH_W_BINGE_OLD_USER ="DTH With Binge Old Stack"
const val DTH_W_BINGE_NEW_USER ="DTH With Binge New Stack"
//const val DTH_W_BINGE_USER ="DTH With Binge"//""DTH_WITH_BINGE"//DTH With Binge
const val DTH_WO_BINGE_USER ="DTH Without Binge"//""DTH_WITHOUT_BINGE"//DTH Without Binge
const val NON_DTH_USER ="Non DTH User"//"NON_DTH_USER"//Non DTH User
const val GUEST_USER ="Guest"//"NON_DTH_USER"//Non DTH User
const val FIBER_USER ="TPF"//"Fiber User"


// Learn Action types

const val FAVOURITE_LEARN_ACTION = "FAVOURITE"
const val CLICK_LEARN_ACTION = "CLICK"
const val SEARCH_LEARN_ACTION = "SEARCH"



// TA CONSTANTS
const val USER_PREFERRED_LANGUAGE_TYPE = "language"
const val USER_PREFERRED_GENRE_TYPE = "genre"
const val USER_PREFERRED_GENRE_USE_CASE = "UC_GET_GENRE_PROFILE_1"
const val USER_PREFERRED_LANGUAGE_USE_CASE = "UC_PL"

//AppsFlyer/Deeplink/Other constants
const val ACTION = "action"
const val KEY_IS_RENEW = "isRenew"
const val KEY_ACTUAL_PRORATED_AMOUNT_FROM_API = "actualProratedAmountResponse"
const val KEY_MODIFICATION_TYPE = "modificationType"
const val KEY_PACK_PRICE = "packPrice"
const val KEY_MODIFY_PACK_CALLED = "modifyPackCalled"
const val KEY_ADD_PACK_CALLED = "addPackCalled"
const val KEY_DTH = "dth"
const val KEY_APPSFLYER_SOURCE = "sourceForAppsFlyer"
const val KEY_PACK_NAME = "packName"
const val KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX = "selectedTenureDurationInDaysWithDSuffix" /*Eg. 30D, 180D*/
const val KEY_SELECTED_TENURE_PACK_PRICE = "selectedTenurePackPrice"
const val KEY_PACK_ID = "packId"
const val KEY_SELECTED_TENURE_TYPE = "selectedTenureType"
const val KEY_MANAGED_APP_JOURNEY_SOURCE_REFID = "managedAppJourneySourceRefId"
const val KEY_JOURNEY_SOURCE_REFID = "journeySourceRefId"
const val KEY_TITLE = "title"
const val KEY_PRODUCT_TYPE = "productType"


//Tick Tick Model constants
const val CART_ID = "cartId"
/*
const val KEY_PROMO_CODE = "promoCode"
const val KEY_PAYMENT_MODE = "paymentMode"*/
const val MINI_DRAWER = "MINI_DRAWER"
const val UPDATE_IN_PACK = "UPDATE_IN_PACK"

const val CLEVERTAP_USER_ID = "$" + "CleverTap_user_id"

const val VTR_PERCENTAGE_50 = "50%"
const val VTR_PERCENTAGE_75 = "75%"
const val KEY_REFRESH_GAME_FAV = "refreshGameFav"

///CT Constants
const val DEEP_LINK_KEY = "wzrk_dl"
const val WZRK_FROM_KEY = "wzrk_from"
const val WZRK_FROM = "CTPushNotificationReceiver"
const val EMPTY_NOTIFICATION_ID = -1000
const val NOTIF_MSG = "nm"
const val NOTIF_TITLE = "nt"
const val NOTIF_PRIORITY = "pr"
const val PRIORITY_HIGH_CT = "high"
const val PRIORITY_MAX_CT = "max"
const val WZRK_COLLAPSE = "wzrk_ck"
const val WZRK_CHANNEL_ID = "wzrk_cid"
const val WZRK_SOUND = "wzrk_sound"
const val WHITE = "#FFFFFF"
const val CLOSE_SYSTEM_DIALOGS = "close_system_dialogs"
const val CATEGORY_LANGUAGE_SETTING = "language-setting"
const val CATEGORY_LANGUAGE_DRAWER = "language-drawer"
//CT New Constant
const val NOTIF_ICON = "ico"
const val WZRK_ACTIONS = "wzrk_acts"
const val WZRK_BIG_PICTURE = "wzrk_bp"
const val WZRK_MSG_SUMMARY = "wzrk_nms"
const val WZRK_SUBTITLE = "wzrk_st"
const val WZRK_COLOR = "wzrk_clr"
const val KEY_CT_TYPE = "ct_type"
const val LABEL_INTENT_SERVICE = "CLEVERTAP_INTENT_SERVICE"
