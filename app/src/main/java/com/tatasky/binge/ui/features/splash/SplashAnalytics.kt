package com.tatasky.binge.ui.features.splash

import com.tatasky.binge.utils.Properties
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.analytics.facebook.FacebookAnalyticsHelper
import com.tatasky.binge.analytics.firebase.FirebaseAnalyticsHelper
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.utils.UserDetailsUtil
import com.tatasky.binge.utils.getTimeInUTC
import org.json.JSONException
import org.json.JSONObject
import java.util.*

open class SplashAnalytics(
    private val mixpanelHelper: MixpanelHelper,
    private val moEngageHelper: MoEngageHelper,
    private val appsFlyerHelper: AppsFlyerHelper,
    private val firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
    private val facebookAnalyticsHelper: FacebookAnalyticsHelper
) {
    /*
	* Variables to keep track of Source and amount
	* from where the recharge is initiated
	* to track the Recharge success
	 */
    var rechargeSourceScreen: String = ""
    var rechargeAmount: String = ""

    /**
     * Clevertap Changes
     */
    fun updateUserDetails(
        sid: String?,
        userDetail: LoginResponse.BingeSubscription?
    ) {
        System.out.println("qwe")
        try {
            if (sid != null && userDetail != null) {
                moEngageHelper.updateSuperPropertiesClevertap(sid, userDetail)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackRechargeInitiate(fromScreen: String, amount: String) {
        trackMixPanelRechargeInitiate(fromScreen, amount)
        trackMoEngageRechargeInitiate(fromScreen, amount)
        trackAppsFlyerRechargeInitiate(fromScreen, amount)
        storeRechargeInitiateInfo(fromScreen, amount)
    }

    private fun storeRechargeInitiateInfo(fromScreen: String, amount: String) {
        rechargeSourceScreen = fromScreen
        rechargeAmount = amount
    }

    private fun trackAppsFlyerRechargeInitiate(fromScreen: String, amount: String) {
        try {
            val parameterAndValueMap = HashMap<String, Any>() // Key/Parameter name, Value/Parameter value
            parameterAndValueMap[PARA_SOURCE] = fromScreen
//			parameterAndValueMap[PARA_AMOUNT] = amount
            appsFlyerHelper.trackEvent(EVENT_RECHARGE_INITIATE, parameterAndValueMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun trackFirstInvention(){
        firebaseAnalyticsHelper.trackEvent(EVENT_FIRST_USER_INVENTION)
        facebookAnalyticsHelper.trackEvent(EVENT_FIRST_USER_INVENTION)
    }

    private fun trackMoEngageRechargeInitiate(fromScreen: String, amount: String) {
        try {
            val payloadBuilder = Properties()
            payloadBuilder.addAttribute(PARA_SOURCE, fromScreen)
            payloadBuilder.addAttribute(PARA_AMOUNT, amount)
            moEngageHelper.trackEvent(EVENT_RECHARGE_INITIATE, payloadBuilder)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun trackMixPanelRechargeInitiate(fromScreen: String, amount: String) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put(PARA_SOURCE, fromScreen)
            jsonObject.put(PARA_AMOUNT, amount)
            mixpanelHelper.trackEvent(EVENT_RECHARGE_INITIATE, mixpanelHelper.mMixpanelUnifiedAPI)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setUserIdentity(mixpanelId: String?){
        mixpanelHelper.setUserIdentity(mixpanelId, mixpanelHelper.mMixpanelUnifiedAPI)
    }

    fun trackUserDetails(
        mixpanelId : String?,
        sid: String?,
        userDetail: LoginResponse.BingeSubscription?,
        aid : String?,
        firstAppLaunchTimeInUTC: String,
        burnRateType: String
    ) {
        try {
            if (sid != null && userDetail != null) {
                mixpanelHelper.clearSuperProperties()
                /**Set the user identity first else the properties will be updated to old user profile
                 * due to not calling the reset of Mixpanel
                 */
                mixpanelHelper.setUserIdentity(mixpanelId, mixpanelHelper.mMixpanelUnifiedAPI)
                mixpanelHelper.setGroup(SID, sid, userDetail, firstAppLaunchTimeInUTC, burnRateType)
                mixpanelHelper.registerInitialSuperProperties(
                    sid,
                    userDetail,
                    mixpanelHelper.mMixpanelUnifiedAPI,
                    firstAppLaunchTimeInUTC,
                    burnRateType
                )
                mixpanelHelper.addUpdateSuperProperty(USER_TYPE, UserDetailsUtil.getUserType(userDetail.dthStatus))
                moEngageHelper.registerInitialSuperProperties(sid, userDetail,aid, burnRateType)
                /**Set AppsFlyer Customer user Id after login
                 * as Mixpanel distinctID will be changed
                 * after identity() for each new login
                 */
                appsFlyerHelper.setCUID()
                appsFlyerHelper.generateAdditionalData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addAlias(alias : String?,aliasUnified : String?, sid : String?){
        sid?.let{
            alias?.let { it ->
                mixpanelHelper.setAlias(it, sid, mixpanelHelper.mMixpanelAPI)
            }
            aliasUnified?.let { it ->
                mixpanelHelper.setAlias(it, sid, mixpanelHelper.mMixpanelUnifiedAPI)
            }
        }
    }

    fun trackEmailId(email : String) {
        trackMixPanelEmailId(email)
        trackMoEngageEmailId(email)
    }

    fun trackUserProfile(profile: SubscriberProfileListModel.Data) {
        trackMixPanelProfileId(profile)
        trackMoEngageProfileId(profile)
    }

    private fun trackMixPanelProfileId(profile: SubscriberProfileListModel.Data){
        mixpanelHelper.updateProperty(PROFILE_ID, profile.profileId?:"")
        mixpanelHelper.updateProperty(FNAME, profile.firstName?:"")
        mixpanelHelper.updateProperty(LNAME, profile.lastName?:"")

        mixpanelHelper.updateProperty(PROFILE_ID, profile.profileId?:"",mixpanelHelper.mMixpanelUnifiedAPI)
        mixpanelHelper.updateProperty(FNAME, profile.firstName?:"",mixpanelHelper.mMixpanelUnifiedAPI)
        mixpanelHelper.updateProperty(LNAME, profile.lastName?:"",mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMoEngageProfileId(profile: SubscriberProfileListModel.Data){
        moEngageHelper.updateProperty(PROFILE_ID, profile.profileId?:"")
        moEngageHelper.updateProperty(FNAME, profile.firstName?:"")
        moEngageHelper.updateProperty(LNAME, profile.lastName?:"")
    }



    fun updateUserProperty(key :String, value : String){
        mixpanelHelper.updateProperty(key, value)
        moEngageHelper.updateProperty(key, value)
    }

    fun setGroup(
        key: String,
        value: String,
        userDetails: LoginResponse.BingeSubscription,
        firstAppLaunchTimeInUTC: String,
        burnRateType: String
    ) {
        mixpanelHelper.setGroup(key,value, userDetails, firstAppLaunchTimeInUTC, burnRateType)
    }

    private fun trackMixPanelEmailId(email: String){
        mixpanelHelper.updateProperty(EMAIL, email)
        mixpanelHelper.updateProperty(EMAIL, email,mixpanelHelper.mMixpanelUnifiedAPI)
    }

    private fun trackMoEngageEmailId(email: String){
        moEngageHelper.updateProperty(EMAIL, email)
    }

    fun trackAppLaunch(firstTime: Boolean,timeStamp: String) {
        trackMixPanelAppLaunch()
        trackMixpanelBingeAppLaunch(firstTime,timeStamp)
        trackMoEngageAppLaunch()
        trackAppsFlyerAppLaunch()
        trackFacebookAppLaunch()
        trackFirebaseAppLaunch()
    }

    private fun trackFacebookAppLaunch() {
        facebookAnalyticsHelper.trackEventWithoutFormatting(EVENT_APP_LAUNCH)
    }

    private fun trackFirebaseAppLaunch() {
        firebaseAnalyticsHelper.trackEvent(EVENT_APP_LAUNCH)
    }


    private fun trackMixPanelAppLaunch() {
//        mixpanelHelper.trackEvent(EVENT_APP_LAUNCH ,mixpanelHelper.mMixpanelAPI)
//        mixpanelHelper.updateProperty(LAST_USED_AT, Date(),mixpanelHelper.mMixpanelAPI)
//        mixpanelHelper.trackEvent(EVENT_APP_LAUNCH,mixpanelHelper.mMixpanelUnifiedAPI)
        mixpanelHelper.updateProperty(
            LAST_USED_AT,
            getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT),
            mixpanelHelper.mMixpanelUnifiedAPI
        )
    }


    private fun trackMixpanelBingeAppLaunch(firstTime: Boolean, timeStamp: String) {
        try {
            val jsonObjectUnified = JSONObject().apply {
                put(FIRST_TIME, if (firstTime) YES else NO)
                put(TIMESTAMP, timeStamp)
            }
            mixpanelHelper.trackEvent(
                EVENT_BINGE_APP_LAUNCH,
                jsonObjectUnified,
                mixpanelHelper.mMixpanelUnifiedAPI
            )
        } catch (e: Exception) {

        }
    }

    private fun trackMoEngageAppLaunch() {
        moEngageHelper.trackEvent(EVENT_APP_LAUNCH)
        moEngageHelper.updateProperty(LAST_USED_AT, Date())
        moEngageHelper.updateProperty(ANDROID_APP_VERSION, BuildConfig.VERSION_CODE)
        moEngageHelper.updateProperty(ANDROID_APP_VERSION_NAME, BuildConfig.VERSION_NAME)
        moEngageHelper.generateFCMToken()
    }

    private fun trackAppsFlyerAppLaunch() {
        appsFlyerHelper.trackEvent(EVENT_APP_LAUNCH)
    }

    fun upgradeUserProperty(
        sid: String,
        it: LoginResponse.BingeSubscription,
        firstAppLaunchTimeInUTC: String,
        burnRateType: String
    ) {
        mixpanelHelper.registerInitialSuperProperties(
            sid,
            it,
            mixpanelHelper.mMixpanelUnifiedAPI,
            firstAppLaunchTimeInUTC,
            burnRateType
        )
        mixpanelHelper.addUpdateSuperProperty(USER_TYPE, UserDetailsUtil.getUserType(it.dthStatus))
    }

    fun registerGuestSuperProperty() {
        mixpanelHelper.registerGuestSuperProperties()
    }

    fun registerOrUpdateCommonSuperProperties() {
        mixpanelHelper.registerOrUpdateCommonSuperProperties()
    }
}