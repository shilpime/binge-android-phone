package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.filterProviderOnly

class ConfigResponse : BaseResponse() {

    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {
        @SerializedName("app")
        @Expose
        var app: App? = null

        @SerializedName("config")
        @Expose
        var config: Config? = null



    }

    class Image {

        @SerializedName("cloudAccountUrl")
        @Expose
        var cloudAccountUrl: String? = null

        @SerializedName("cloudSubAccountUrl")
        @Expose
        var cloudSubAccountUrl: String? = null

    }

//    class TermsCondition

    class LicenseAgreement {
        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("url")
        @Expose
        var url: String? = null

        @SerializedName("subTitle")
        @Expose
        var subTitle:String?=null
    }

    class Url {

        @SerializedName("eulaUrl")
        @Expose
        var eulaUrl: String? = null

        @SerializedName("eulaUrlHybrid")
        @Expose
        var eulaUrlHybrid: String? = null

        @SerializedName("image")
        @Expose
        var image: Image? = null

        @SerializedName("helpUrl")
        @Expose
        var helpUrl: String? = null

        @SerializedName("termsConditionsUrlHybrid")
        @Expose
        var termsConditionsUrlHybrid: String? = null

        @SerializedName("privacyPolicyUrlHybrid")
        @Expose
        var privacyPolicyUrlHybrid: String? = null
    }

    class FreemiumBackgroundPoster{
        @SerializedName("android")
        @Expose
        var androidSubscriptionBackgroundPoster: AndroidSubscriptionBackgroundPoster? = null
    }

    class AndroidSubscriptionBackgroundPoster{
        @SerializedName("otherPackPoster")
        @Expose
        var otherPackPoster: String? = null
    }

    class TermConditionPrivacy{
        @SerializedName("termConditionsUrl")
        @Expose
        var termConditionsUrl: String? = null

        @SerializedName("privacyPolicyUrl")
        @Expose
        var privacyPolicyUrl: String? = null
    }

    class SubscriberImage {
        @SerializedName("SubscriberImageBaseUrl", alternate = ["subscriberImageBaseUrl"])
        @Expose
        var imageBaseUrl: String? = null
    }

    class Nudges {
        @SerializedName("android")
        @Expose
        var androidNudge: AndroidNudge? = null
    }

    class NudgesDetails {
        @SerializedName("android")
        @Expose
        var androidNudgesDetails: AndroidNudgesDetails? = null
    }

    class AndroidNudge {
        @SerializedName("freeTrial")
        @Expose
        var freeTrialStartupNudge: FreeTrialStartupNudge? = null
    }

    class AndroidNudgesDetails {
        @SerializedName("renewNudgeActive")
        @Expose
        var renewNudgeActive: UpsellNudge? = null

        @SerializedName("renewNudgeExpired")
        @Expose
        var renewNudgeExpired: UpsellNudge? = null

        @SerializedName("renewSINudge")
        @Expose
        var renewSINudge: UpsellNudge? = null

        @SerializedName("emailNudge")
        @Expose
        var emailNudge: UpsellNudge? = null

        @SerializedName("notificationNudge")
        @Expose
        var notificationNudge: UpsellNudge? = null
    }

    class FreeTrialStartupNudge {
        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("desc")
        @Expose
        var desc: String? = null

        @SerializedName("button")
        @Expose
        var button: String? = null
    }

    /**
     * timeFrequency is used here for timeGap for showing first nudge after install
     * launchFrequency is used here for timeGap for showing nudge repeatedly after showing last time
     */
    class UpsellNudge {
        @SerializedName("timeFrequency")
        @Expose
        var timeFrequency: Int? = null

        @SerializedName("launchFrequency")
        @Expose
        var launchFrequency: Int? = null

        @SerializedName("verbiage")
        @Expose
        var verbiage: String? = null
    }

    class Android {

        @SerializedName("packageId")
        val partnerAppPackageId: String? = null

        @SerializedName("playAuthType")
        var playAuthType: String? = null


        @SerializedName("recommendedVersion")
        @Expose
        var recommendedVersion: String? = null

        @SerializedName("forceUpgradeVersion")
        @Expose
        var forceUpgradeVersion: String? = null

        @SerializedName("recommendedMessage")
        @Expose
        var recommendedMessage: String? = null

        @SerializedName("forceUpgradeMessage")
        @Expose
        var forceUpgradeMessage: String? = null

        @SerializedName("forceUpgradeHeader")
        @Expose
        var forceUpgradeTitle : String? = null

        @SerializedName("recommendedUpgradeHeader")
        @Expose
        var recommendedUpgradeTitle : String? = null

        @SerializedName("forceUpgradeImage")
        @Expose
        var forceImageUrl : String? = null

        @SerializedName("recommendedUpgradeImage")
        @Expose
        var recommendedImageUrl : String? = null

        @SerializedName("actionRedirection")
        @Expose
        var primaryBtnAction : String? = null

         @SerializedName("partnerUpdateMessage")
        @Expose
        var partnerUpdateMessage : String? = null


    }

    class App {

        @SerializedName("appUpgrade")
        @Expose
        var appUpgrade: AppUpgrade? = null

        @SerializedName("FTVPopUp")
        @Expose
        var ftvPopup: FTVPopup? = null

        @SerializedName("gameNudgePopup")
        var gameNudgePopUp : GameNudgePopup? = null

        @SerializedName("gameAnimPopup")
        var gameAnimPopup :GameAnimPopup ? = null

        @SerializedName("nudges")
        @Expose
        var nudges: Nudges? = null

        @SerializedName("nudgesDetails")
        @Expose
        var nudgesDetails: NudgesDetails? = null
    }

    class FTVPopup {

        @SerializedName("android")
        @Expose
        var ftvAndroid: FTVPopupAndroid? = null

    }

    class FTVPopupAndroid {

        @SerializedName("timeFrequency")
        @Expose
        var ftvTimeFrequency: Int = 0

        @SerializedName("launchFrequency")
        @Expose
        var ftvLaunchFrequency: Int = 0

        @SerializedName("popupType")
        @Expose
        var ftvPopupType: String? = null

    }

    class AppUpgrade {

        @SerializedName("android")
        @Expose
        var android: Android? = null

    }

    class HeroBannerRotation {

        @SerializedName("heroBannerIncrementValue")
        @Expose
        var heroBannerIncrementValue : Int? = 0

        @SerializedName("homeLaunchValue")
        @Expose
        var homeLaunchValue : Int? = 0
    }

    class PaymentGatewayInfo {
        @SerializedName("paymentBetaAssets") var paymentBetaAssets : Boolean = false
        @SerializedName("paymentClientId") var paymentClientId : String? = null
        @SerializedName("paymentServiceId") var paymentServiceId : String? = null
    }

    data class CoachMark(
        @SerializedName("androidLaunchFrequency") var androidLaunchFrequency: Int? = null
    )

    data class DigitalFeedDecryptionKey(
        @SerializedName("v1") var aesEncryptionSecretKeyV1: String? = null,
        @SerializedName("v2") var aesEncryptionSecretKeyV2: String? = null,
    )

    class Config {
        @SerializedName("channelDetailRetry")
        var channelDetailRetry : Long? = null // Always comes in seconds

        @SerializedName("dd")
        var digitalFeedDecryptionKey : DigitalFeedDecryptionKey? = null

        @SerializedName("coachMark")
        var coachMark : CoachMark? = null

        @SerializedName("freeEpisodesAvailable")
        var freeEpisodesAvailableVerbiage : String ? = null

        @SerializedName("firstEpisodeFree")
        var firstEpisodeFreeVerbiage : String ? = null

        @SerializedName("regionalAppFrequency")
        val regionalAppsNudgeFrequency: Int? = null

        @SerializedName("mxUpsellInfo")
        var mxUpsellInfo : MxUpsellInfo ? = null

        @SerializedName("appRating")
        var appRating: AppRating? = null


        @SerializedName("searchSuggestionThershold")
        var searchSuggestionThershold : Int = 2

        @SerializedName("heroBannerRotation")
        @Expose
        var heroBannerRotation : HeroBannerRotation? = null

        @SerializedName("paymentGatewayInfo")
        var paymentGatewayInfo: PaymentGatewayInfo? = null

        /*Changes for FreeTrail UI*/
        @SerializedName("android_freetrial_title")
        var freeTrialTitle : String? = null

        @SerializedName("providers")
        private var providers : List<Providers>? = null

        var filterProviders : List<Providers>? = ArrayList()
        get() = providers?.filter { filterProviderOnly(it.providerName?:"") }
        /*End of changes*/

        @SerializedName("licenseAgreement")
        @Expose
        var licenseAgreement : LicenseAgreement? = null

        @SerializedName("otpDuration")
        @Expose
        var optDuration: Int = 30//default

        @SerializedName("maxRechargeAmount")
        @Expose
        var maxRechargeAmount: Int = 49000//default

        @SerializedName("passwordRedirectionTimeInSecs")
        @Expose
        var passwordRedirectionTimeInSecs: Int = 5//default

        @SerializedName("connect_timeout", alternate = ["connectTimeout"])
        @Expose
        var connectTimout: Long = 60//default

        @SerializedName("loaderDelayTime")
        @Expose
        var loaderDelayTime: Long = 1000//default


        @SerializedName("showMarketingScreen")
        @Expose
        var showMarketingScreen: Boolean = false//default


        @SerializedName("bingeAndroidDrpEnabled")
        @Expose
        var bingeAndroidDrpEnabled: Boolean = false//default

        @SerializedName("bingeTabletDrpEnabled")
        @Expose
        val bingeTabletDrpEnabled: Boolean = false

        @SerializedName("otpResentCount")
        @Expose
        var otpResentCount: Int = 5//default

        @SerializedName("playbackRetryCount")
        @Expose
        var playbackRetryCount: Int = 2//default

        @SerializedName("url")
        @Expose
        var url: Url? = null

        @SerializedName("SubscriberImage", alternate = ["subscriberImage"])
        @Expose
        var subscriberImage: SubscriberImage? = null

        @SerializedName("taHeroBanner")
        @Expose
        var taHeroBanner: List<TaHeroBanner>? = null


        @SerializedName("availableProviders")
        var availableProviders : List <AvailableProviders>? = null

        @SerializedName("verbiages")
        @Expose
        var verbiages: List<Verbiages>? = null

        fun getLanguageVerbiage(categoryName: String): Verbiages {
            verbiages?.let{
                for (i in it) {
                    if (i.categoryName.equals(categoryName))
                        return i
                }
            }
            return Verbiages()
        }


        @SerializedName("provider_Logo", alternate = ["providerLogo"])
        @Expose
        var providerLogo: ProviderLogo? = null

        @SerializedName("rateLimit")
        @Expose
        var rateLimit : String? = null

        @SerializedName("packButtonsEligibility")
        @Expose
        var buttonsEligibility: Map<String, PackButton?>? = null

        @SerializedName ("drpPartnerPages")
        var drpPartnerPages : ArrayList<String> = ArrayList()

        @SerializedName("subscribedTitle")
        @Expose
        var amazonSubscribedTitle : String?=null

        @SerializedName("unSubscribedTitle")
        @Expose
        var amazonUnSubscribedTitle : String?=null

        @SerializedName("primeRedirection")
        @Expose
        var primeRedirection : PrimeRedirection?=null


        @SerializedName("bingeDrpCacheDuration")
        var bingeDrpCacheDuration : Int  = 0

        @SerializedName("hotstarPopUp")
        var hotstarPopUp: HotstarPopUp? = null



        @SerializedName("primePopUp")
        @Expose
        var primePopUpFrequency : PrimePopUp? = null

        @SerializedName("primeExistingPopUp")
        @Expose
        var primeExistingPopUpFrequency : PrimePopUp? = null


        /*New TnC Class*/
        @SerializedName("termConditionPrivacy")
        @Expose
        val termConditionPrivacy : TermConditionPrivacy? = null


        /*New TnC Class*/
        @SerializedName("helpCenterInfo")
        @Expose
        var helpCenterInfo : HelpCenterInfo? = null

        @SerializedName("paymentCallbackArrayInfo")
        var paymentCallbackArrayInfo: PaymentCallbackArrayInfo? = null

        @SerializedName("FreemiumBackgroundPoster")
        var freemiumBackgroundPoster:FreemiumBackgroundPoster ? =null

        @SerializedName("taRelatedRail")
        var taRelatedRail : ArrayList<TaRelatedRail> = arrayListOf()


        @SerializedName("SubscriptionDrawer",alternate = ["subscriptionDrawer"])
        var subscriptionDrawer : SubscriptionDrawer?= null //used to show Mini Subscription Drawer at the time of launch app

        @SerializedName("newUserDelay")
        var newUserDelay : Int = 3

        @SerializedName("freeToggleEnable")
        var freeToggleEnable: Boolean = false

        @SerializedName("managedAppEnabled")
        var managedAppEnabled: Boolean = true//TODO it will be false when going live

        @SerializedName("choosePlanManagedApp")
        var choosePlanManagedApp: String?=null

        @SerializedName("enableTickTickJourney")
        var enableTickTickJourney: Boolean = false

        @SerializedName("tickTickDrawerScreen")
        var tickTickDrawerScreen : ManagedAppDrawerResponse.TickTickDrawerDetail?=null

        @SerializedName("tickTickFixedPlanDrawerScreen")
        var tickTickFixedPlanDrawerScreen : ManagedAppDrawerResponse.TickTickDrawerDetail?=null

        @SerializedName("notNow")
        var notNow: String? = null
        @SerializedName("notification")
        var notification: String? = null
        @SerializedName("accountRefresh")
        var accountRefresh: String? = null
        @SerializedName("refreshCTA")
        var refreshCTA: String? = null
        @SerializedName("loginScreen")
        var loginScreen: LoginScreen? = null
        @SerializedName("logout")
        var logout: Logout? = null
        @SerializedName("device")
        var device: Device? = null
        @SerializedName("parental")
        var parental: Parental? = null
        @SerializedName("hamburger")
        var hamburger: Hamburger? = null
        @SerializedName("search")
        var search: Search? = null
        @SerializedName("switchAccount")
        var switchAccount: SwitchAccount? = null
    }

    data class LoginScreen(
        @SerializedName("logo") val logo: String? = null,
        @SerializedName("login") val login: String? = null,
        @SerializedName("getOtp") val getOtp: String? = null,
        @SerializedName("resendOtpIn") val resendOtpIn: String? = null,
        @SerializedName("resendOtp") val resendOtp: String? = null,
        @SerializedName("loginToWatch") val loginToWatch: String? = null,
        @SerializedName("incorrectOtp") val incorrectOtp: String? = null,
        @SerializedName("subHeader1") val subHeader1: String? = null,
        @SerializedName("subHeader2") val subHeader2: String? = null,
        @SerializedName("cta") val cta: String? = null
    )

    data class Logout(
        @SerializedName("header") val header: String? = null,
        @SerializedName("subHeader") val subHeader: String? = null,
        @SerializedName("logout") val logout: String? = null,
        @SerializedName("success") val success: String? = null
    )

    data class Device(
        @SerializedName("header") val header: String? = null,
        @SerializedName("subHeader") val subHeader: String? = null,
        @SerializedName("review") val review: String? = null,
        @SerializedName("notSuccess") val notSuccess: String? = null,
        @SerializedName("success") val success: String? = null
    )

    data class Parental(
        @SerializedName("setView") val setView: String? = null,
        @SerializedName("viewRest") val viewRest: String? = null,
        @SerializedName("parentalPin") val parentalPin: String? = null,
        @SerializedName("enter") val enter: String? = null,
        @SerializedName("enterDigit") val enterDigit: String? = null,
        @SerializedName("forgot") val forgot: String? = null
    )

    data class Hamburger(
        @SerializedName("loginNow") val loginNow: String? = null,
        @SerializedName("subscribe") val subscribe: String? = null,
        @SerializedName("bingeList") val bingeList: String? = null,
        @SerializedName("notifications") val notifications: String? = null,
        @SerializedName("settings") val settings: String? = null,
        @SerializedName("help") val help: String? = null,
        @SerializedName("subText") val subText: String? = null,
        @SerializedName("subTextCta") val subTextCta: String? = null,
        @SerializedName("myPlan") val myPlan: String? = null,
        @SerializedName("tataPlayBalance") val tataPlayBalance: String? = null,
        @SerializedName("login") val login: String? = null
    )

    data class Search(
        @SerializedName("voiceSearch") val voiceSearch: String? = null,
        @SerializedName("tapSpeak") val tapSpeak: String? = null,
        @SerializedName("tvShow") val tvShow: String? = null,
        @SerializedName("toggleCTA") val toggleCTA: String? = null,
        @SerializedName("filter") val filter: String? = null,
        @SerializedName("filterLang") val filterLang: String? = null,
        @SerializedName("filterGenre") val filterGenre: String? = null,
        @SerializedName("noResults") val noResults: String? = null,
        @SerializedName("recentSearch") val recentSearch: String? = null,
        @SerializedName("clearAll") val clearAll: String? = null,
        @SerializedName("trySaying") val trySaying: String? = null,
        @SerializedName("allContent") val allContent: String? = null,
        @SerializedName("free") val free: String? = null,
        @SerializedName("tapMicrophone") val tapMicrophone: String? = null
    )

    data class SwitchAccount(
        @SerializedName("successToast") val successToast: String? = null,
        @SerializedName("cta") val cta: String? = null,
        @SerializedName("header") val header: String? = null,
        @SerializedName("subText") val subText: String? = null
    )

    data class AppRating(
        @SerializedName("appRatingSkipCtaVerbiage") var appRatingSkipCtaVerbiage: String? = null,
        @SerializedName("appRatingFrequency") var appRatingFrequency: String? = null,
        @SerializedName("appRatingProceedCtaVerbiage") var appRatingProceedCtaVerbiage: String? = null,
        @SerializedName("appRatingHeaderVerbiage") var appRatingHeaderVerbiage: String? = null,
        @SerializedName("appRatingImg") var appRatingImg: String? = null
    )

    data class AvailableProviders(

        @SerializedName("logoRectangular") var logoRectangular: String? = null,
        @SerializedName("deeplink") var deeplink: Deeplink? = Deeplink(),
        @SerializedName("logoCircular") var logoCircular: String? = null,
        @SerializedName("providerName") var providerName: String? = null,
        @SerializedName("platform") var platform: ArrayList<String> = arrayListOf(),
        @SerializedName("authType") var authType: AuthType?= AuthType()

    )

    data class AuthType(
        @SerializedName("Android") var android: Android? = Android()
    )

    data class Deeplink(

        @SerializedName("deeplinkReq") var deeplinkReq: Boolean? = null,
        @SerializedName("platform") var platform: ArrayList<String> = arrayListOf()

    )



    class SubscriptionDrawer{

        @SerializedName("delayToOpenSubscriptionDrawer")
        var delayToOpenSubscriptionDrawer: Int? = null //In Seconds

        @SerializedName("openSubscriptionDrawer")
        var openSubscriptionDrawer: Boolean? = false

        @SerializedName("gapToOpenSubscriptionDrawer")
        var gapToOpenSubscriptionDrawer: Int = 0 //In Hours

        @SerializedName("guestDrawerFrequency")
        var guestDrawerFrequency : Int = 1

        @SerializedName("loggedInDrawerFrequency")
        var loggedInDrawerFrequency : Int = 1

        @SerializedName("firstLaunchGuestOpenSubscriptionDrawer")
        var firstLaunchGuestOpenSubscriptionDrawer :Int =1

        @SerializedName("firstLaunchLoginOpenSubscriptionDrawer")
        var firstLaunchLoginOpenSubscriptionDrawer :Int =1
    }


    class PaymentCallbackArrayInfo{
        @SerializedName("paymentStatusApiArray" ) var paymentStatusApiArray : ArrayList<Long>? = null
    }

    class MxUpsellInfo {
        @SerializedName("mxUpsellBannerUrl")
        var mxUpsellBannerUrl : String? = null

        @SerializedName("mxUpsellPopup")
        var mxUpsellPopup : Boolean = false
    }

    class GameNudgePopup {
        @SerializedName("android")
        @Expose
        var gameAndroid: GameNudgeAndroid? = null
    }

    class GameAnimPopup {
        @SerializedName("android")
        @Expose
        var gameAndroid: GameAnimAndroid? = null
    }

    class HotstarPopUp {
        @SerializedName("android")
        @Expose
        var hotstarAndroid: HotstarPopUpAndroid? = null
    }

    class HotstarPopUpAndroid {
        @SerializedName("launchFrequency")
        @Expose
        var hotstarLaunchFrequency: Int = 0

        @SerializedName("periodicFrequency")
        @Expose
        var hotstarPeriodicFrequency: Int = 0

        @SerializedName("primePopUp")
        @Expose
        var primePopUpFrequency : PrimePopUp? = null

        @SerializedName("primeExistingPopUp")
        @Expose
        var primeExistingPopUpFrequency : PrimePopUp? = null
    }

    class GameNudgeAndroid {

        @SerializedName("nudgeVerbiage")
        var nudgeVerbiage: String? = null

        @SerializedName("nudgeHeader")
        var nudgeHeader: String? = null

        @SerializedName("launchFrequency")
        var launchFrequency: Int = 2

        @SerializedName("periodicFrequency")
        var periodicFrequency: Int = 10

        @SerializedName("popupEnabled")
        var popupEnabled: Boolean = true
    }

    class GameAnimAndroid {
        @SerializedName("launchFrequency")
        var launchFrequency: Int = 2

        @SerializedName("periodicFrequency")
        var periodicFrequency: Int = 10

        @SerializedName("popupEnabled")
        var popupEnabled: Boolean = true
    }

    class PrimePopUp {
        @SerializedName("android")
        @Expose
        var primeAndroid: PrimePopUpAndroid? = null
    }

    class PrimePopUpAndroid {
        @SerializedName("launchFrequency")
        @Expose
        var primeLaunchFrequency: Int = 2

        @SerializedName("periodicFrequency")
        @Expose
        var primePeriodicFrequency: Int = 30
    }

    class PrimeRedirection {
        @SerializedName("redirectionDelay")
        @Expose
        var redirectionDelay : String?=null
        @SerializedName("enableRedirection")
        @Expose
        var enableRedirection : Boolean = false
    }

    class HelpCenterInfo {
        @SerializedName("helpCenterurlAnywhere")
        @Expose
        var helpCenterUrlAnywhere : String = ""
        @SerializedName("helpCenterTokenBaseUrl")
        @Expose
        var helpCenterTokenBaseUrl : String = ""
        @SerializedName("helpCenterHeading")
        @Expose
        var helpCenterHeading : String = ""
        @SerializedName("helpCenterUrl")
        @Expose
        var helpCenterUrl : String = ""
        @SerializedName("helpCenterAllowedURL")
        @Expose
        var helpCenterAllowedURL : List<String> = mutableListOf()
    }
}

