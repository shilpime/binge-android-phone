package com.tatasky.binge.analytics.mixpanel

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.SUBSCRIBED
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.utils.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Singleton

@Singleton
class MixpanelHelper(val context: Context) {
    private val TAG = this.javaClass.simpleName
    var mMixpanelAPI: MixpanelAPI = MixpanelAPI.getInstance(context, "")
        private set
    var mMixpanelUnifiedAPI: MixpanelAPI = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_UNIFIED_KEY)
        private set

    val propertyArray = arrayOf<String>(STACK, FREE_TRIAL, FREE_TRIAL_TAKEN, FREE_TRIAL_ELIGIBLE,
        DATE_OF_SUBSCRIPTION, PACK_RENEWAL_DATE, PACK_START_DATE, PACK_END_DATE, PACK_NAME,
        PACK_TYPE, PACK_PRICE, BURN_RATE_TYPE, LAST_PACK_PRICE, LAST_PACK_TYPE, LAST_PACK_NAME,
        LAST_BILLING_TYPE, RENEWAL_DUE_DATE,TOTAL_PAID_PACK_RENEWALS, FIRST_PACK_SUBSCRIPTION_DATE,
        FIRST_PAID_PACK_SUBSCRIPTION_DATE, FIRE_TV,ATV, SUBSCRIBED, SI_PAYMENT_MODE,
        RECURRING_PAYMENT, FTV_ELIGIBLE, SUBSCRIPTION_TYPE, RENEWAL_DUE)

    fun registerInitialSuperProperties(
        sid: String,
        userDetails: LoginResponse.BingeSubscription,
        mixpanelAPI: MixpanelAPI = mMixpanelUnifiedAPI,
        firstAppLaunchTimeInUTC: String,
        burnRateType: String
    ): JSONObject {

        mixpanelAPI.people.unset(TS_SID)
        mixpanelAPI.people.unset(C_ID)
        mixpanelAPI.unregisterSuperProperty(TS_SID)
        mixpanelAPI.unregisterSuperProperty(C_ID)
        val initialSuperProperties = getInitialSuperProperties(userDetails.rmn, userDetails.dthStatus, userDetails.subscriberId, userDetails.bingeSubscriberId,
            if(isTablet(context)) PLATFORM_ANDROID_TABLET else PLATFORM_ANDROID)
        try {
            userDetails.let { it ->
                initialSuperProperties.put(PROFILE_ID, it.profileId)
                initialSuperProperties.put(TP_RMN, YES)
                initialSuperProperties.put(MIXPANEL_ID, it.mixpanelid)
//                initialSuperProperties.put(TS_SID, "")
//                initialSuperProperties.put(C_ID, "")
                when (it.dthStatus) {
                    DTH_W_BINGE_OLD_USER -> {
                        initialSuperProperties.put(TS_SID,it.subscriberId)
                    }
                    else -> {
                        initialSuperProperties.put(C_ID,it.bingeSubscriberId)
                    }
                }
                initialSuperProperties.put(RMN, it.rmn)
                initialSuperProperties.put(EMAIL, it.emailId)
                initialSuperProperties.put(FNAME, it.firstName)
                initialSuperProperties.put(LNAME, it.lastName)
                initialSuperProperties.put("\$name", (it.firstName?:"") + " " + (it.lastName?:""))
//                initialSuperProperties.put(FIRE_TV, if(it.subscriptionType.equals(subscriptionTypeFtv, true)) YES else NO)
//                initialSuperProperties.put(ATV, if(it.subscriptionType.equals(subscriptionTypeAtv, true)) YES else NO)
                initialSuperProperties.put(BINGE_ACCOUNT_COUNT, it.numberOfBingeAccounts)
                //current subsc
//                initialSuperProperties.put(SUBSCRIBED, if (!it.partnerSubscriptions?.subscriptionStatus.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)) NO else YES)
                it.firstTimeLoginDate?.let { initialSuperProperties.put(FIRST_TIME_LOGIN, it) }

                //current subs
//                it.partnerSubscriptions?.packCreationDate?.let {
//                    initialSuperProperties.put(DATE_OF_SUBSCRIPTION,  it)
//                }
//                it.partnerSubscriptions?.packCreationDate?.let {
//                    initialSuperProperties.put(PACK_START_DATE,  getDateObject(it, START_DATE_FORMAT))
//                }
//                it.partnerSubscriptions?.expirationDate?.let{
//                    initialSuperProperties.put(PACK_END_DATE,  getDateObject(it, END_DATE_FORMAT))
//                }
//                it.partnerSubscriptions?.rechargeDue?.let{
//                    initialSuperProperties.put(PACK_RENEWAL_DATE,  getDateObject(it, END_DATE_FORMAT))
//                }
                initialSuperProperties.put(LAST_USED_AT, getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT))
                initialSuperProperties.put(LOGGED_IN_DEVICE_COUNT, it.numberOfDevicesLoggedIn)
//                initialSuperProperties.put(FREE_TRIAL, it.partnerSubscriptions?.freeTrialStatus?.let{if(it) YES else NO}?:"")
//                initialSuperProperties.put(PACK_NAME,it.partnerSubscriptions?.productName?: FREEMIUM)
//                initialSuperProperties.put(PACK_TYPE, if(it.partnerSubscriptions?.subscribedBingeProduct != null) "PAID" else "FREE")
//                initialSuperProperties.put(PACK_PRICE,it.partnerSubscriptions?.packPrice?: FREEMIUM)
//                initialSuperProperties.put(SUBSCRIPTION_TYPE,it.subscriptionDetailInfo?.subscriptionType?:"UNSUBSCRIBED")
//                initialSuperProperties.put(BURN_RATE_TYPE, burnRateType)
//                initialSuperProperties.put(FREE_TRIAL_ELIGIBLE, if(it.freeTrialAvailed) "NO" else "YES")
//                it.partnerSubscriptions?.let { initialSuperProperties.put(RENEWAL_DUE, if(true == it.subscriptionStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true) && !it.isCancelled) "YES" else "NO") } ?: initialSuperProperties.put(RENEWAL_DUE,  "NO")
//                initialSuperProperties.put(SI_PAYMENT_MODE, it.partnerSubscriptions?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else "YES" } ?: "")
//                initialSuperProperties.put(RECURRING_PAYMENT, it.partnerSubscriptions?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else "YES" } ?: "")
//                initialSuperProperties.put(FTV_ELIGIBLE, if(it.partnerSubscriptions?.eligibleFirestick == true) "YES" else "NO")
//                initialSuperProperties.put(
//                    STACK,
//                    if (it.dthStatus == DTH_W_BINGE_OLD_USER) "Tata Play" else "Comviva"
//                )
                initialSuperProperties.put(DEVICE_1, it.deviceDTOList?.getOrNull(0)?.deviceType?:"")
                initialSuperProperties.put(DEVICE_2, it.deviceDTOList?.getOrNull(1)?.deviceType?:"")
                initialSuperProperties.put(DEVICE_3, it.deviceDTOList?.getOrNull(2)?.deviceType?:"")
                initialSuperProperties.put(DEVICE_4, it.deviceDTOList?.getOrNull(3)?.deviceType?:"")
                initialSuperProperties.put(LAST_USED_DEVICE, "")  //current date - ios
//                initialSuperProperties.put(LAST_PACK_TYPE, it.lastPackType)
//                initialSuperProperties.put(LAST_PACK_PRICE, it.lastPackPrice)
//                initialSuperProperties.put(LAST_PACK_NAME, it.lastPackName)
//                initialSuperProperties.put(LAST_BILLING_TYPE, it.lastbillingType)
//                initialSuperProperties.put(RENEWAL_DUE_DATE, it.rechargeDueDate)
                initialSuperProperties.put(LAST_APP_USAGE_DATE, "") // current date - ios
//                initialSuperProperties.put(TOTAL_PAID_PACK_RENEWALS, it.totalPaidPackRenewal)
                initialSuperProperties.put(FIRST_LOGIN_DATE, it.firstTimeLoginDate)
//                initialSuperProperties.put(FIRST_PACK_SUBSCRIPTION_DATE,
//                    it.firstPaidPackSubscriptionDate
//                        ?: "" /*TSF-8151 Due to Free trial disable from BE, Moving it to firstPaidPackSubscriptionDate*/
//                )
//                initialSuperProperties.put(FIRST_PAID_PACK_SUBSCRIPTION_DATE, it.firstPaidPackSubscriptionDate?:"")
                initialSuperProperties.put(DEVICE_USED_IN_LAST_DAYS, "")
                initialSuperProperties.put(TOTAL_WATCH_HOURS_LAST_DAYS, "") // Not available from BE
                initialSuperProperties.put(TOTAL_WATCH_HOURS_LIFETIME, "")  // Not available from BE
//                initialSuperProperties.put(FREE_TRIAL_TAKEN, YES)
                initialSuperProperties.put(PROFILE_NAME, it.profileName?:"")
                initialSuperProperties.put(TYPE_OF_SUBSCRIBER, it.subscriberType?:"")
                initialSuperProperties.put(
                    FIRST_APP_DOWNLOAD_DATE,
                    firstAppLaunchTimeInUTC
                )
                initialSuperProperties.put(DOWNLOAD_SOURCE, "Play Store")
//                initialSuperProperties.put(SID, sid)
                initialSuperProperties.put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        mixpanelAPI.registerSuperProperties(
            getInitialSuperProperties(
                userDetails.rmn,
                userDetails.dthStatus,
                userDetails.subscriberId,
                userDetails.bingeSubscriberId,
                if(isTablet(context)) PLATFORM_ANDROID_TABLET else PLATFORM_ANDROID
            )
        )
        mixpanelAPI.people.set(CLEVERTAP_USER_ID, userDetails.subscriberId)
        mixpanelAPI.people.set(initialSuperProperties)
        return initialSuperProperties
    }

    fun addUpdateSuperProperty(key: String, value: String) {
        val props = JSONObject()
        props.put(key, value)
        mMixpanelUnifiedAPI.registerSuperProperties(props)
    }

    fun updateProperty(propertyName : String, propertyValue : String, mixpanelAPI: MixpanelAPI = mMixpanelAPI){
        val props = JSONObject()
        if(!propertyArray.contains(propertyName))
            props.put(propertyName, propertyValue)
        mixpanelAPI.people.set(props)
        mMixpanelUnifiedAPI.people.set(props)
    }

    fun setGroup(
        key: String,
        value: String,
        userDetails: LoginResponse.BingeSubscription,
        firstAppLaunchTimeInUTC: String,
        burnRateType: String
    ) {
        mMixpanelUnifiedAPI.setGroup(key,value)
        userDetails.let {
            val group = mMixpanelUnifiedAPI.getGroup(key, value)
            group.unset(TS_SID)
            group.unset(C_ID)
            group.setMap(HashMap<String, String>().apply {
                it.profileId?.let { it1 -> put(PROFILE_ID, it1) }
                put(TP_RMN, YES)
//                put(TS_SID, "")
//                put(C_ID, "")
                when (it.dthStatus) {
                    DTH_W_BINGE_OLD_USER -> {
                        it.subscriberId?.let { it1 -> put(TS_SID, it1) }
                    }
                    else -> {
                        it.bingeSubscriberId?.let { it1 -> put(C_ID, it1) }
                    }
                }
                it.rmn?.let { it1 -> put(RMN, it1) }
                it.emailId?.let { it1 -> put(EMAIL, it1) }
                it.firstName?.let { it1 -> put(FNAME, it1) }
                it.lastName?.let { it1 -> put(LNAME, it1) }
                put("\$name", (it.firstName?:"") + " " + (it.lastName?:""))
//                put(FIRE_TV, if(it.subscriptionType.equals(subscriptionTypeFtv, true)) YES else NO)
//                put(ATV, if(it.subscriptionType.equals(subscriptionTypeAtv, true)) YES else NO)
                put(BINGE_ACCOUNT_COUNT, it.numberOfBingeAccounts.toString())
                //current subsc
//                put(SUBSCRIBED, if (!it.partnerSubscriptions?.subscriptionStatus.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)) NO else YES)
                it.firstTimeLoginDate?.let { put(FIRST_TIME_LOGIN, it) }

                //current subs
//                it.partnerSubscriptions?.packCreationDate?.let {
//                    put(DATE_OF_SUBSCRIPTION,  it)
//                }
//                it.partnerSubscriptions?.packCreationDate?.let {
//                    getDateObject(it, START_DATE_FORMAT)?.let { it1 ->
//                        put(PACK_START_DATE,
//                            it1.toString()
//                        )
//                    }
//                }
//                it.partnerSubscriptions?.expirationDate?.let{
//                    getDateObject(it, END_DATE_FORMAT)?.let { it1 ->
//                        put(PACK_END_DATE,
//                            it1.toString()
//                        )
//                    }
//                }
//                it.partnerSubscriptions?.rechargeDue?.let{
//                    getDateObject(it, END_DATE_FORMAT)?.let { it1 ->
//                        put(PACK_RENEWAL_DATE,
//                            it1.toString()
//                        )
//                    }
//                }
                put(LAST_USED_AT, getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT))
                put(LOGGED_IN_DEVICE_COUNT, it.numberOfDevicesLoggedIn.toString())
//                put(FREE_TRIAL, it.partnerSubscriptions?.freeTrialStatus?.let{if(it) YES else NO}?:"")
//                put(PACK_NAME,it.partnerSubscriptions?.productName?:FREEMIUM)
//                put(PACK_TYPE, if(it.partnerSubscriptions?.subscribedBingeProduct != null) "PAID" else "FREE")
//                put(PACK_PRICE,it.partnerSubscriptions?.packPrice?:"")
//                put(SUBSCRIPTION_TYPE,it.subscriptionDetailInfo?.subscriptionType?:"UNSUBSCRIBED")
//                put(BURN_RATE_TYPE, burnRateType)
//                put(FREE_TRIAL_ELIGIBLE, if(it.freeTrialAvailed) "NO" else "YES")
//                it.partnerSubscriptions?.let { put(RENEWAL_DUE, if(true == it.subscriptionStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true) && !it.isCancelled) "YES" else "NO") } ?: put(RENEWAL_DUE,  "NO")
//                put(SI_PAYMENT_MODE, it.partnerSubscriptions?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else "YES" } ?: "")
//                put(RECURRING_PAYMENT, it.partnerSubscriptions?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else "YES" } ?: "")
//                put(FTV_ELIGIBLE, if(it.partnerSubscriptions?.eligibleFirestick == true) "YES" else "NO")
//                put(
//                    STACK,
//                    if (it.dthStatus == DTH_W_BINGE_OLD_USER) "Tata Play" else "Comviva"
//                )
                put(DEVICE_1, it.deviceDTOList?.getOrNull(0)?.deviceType?:"")
                put(DEVICE_2, it.deviceDTOList?.getOrNull(1)?.deviceType?:"")
                put(DEVICE_3, it.deviceDTOList?.getOrNull(2)?.deviceType?:"")
                put(DEVICE_4, it.deviceDTOList?.getOrNull(3)?.deviceType?:"")
                put(LAST_USED_DEVICE, "")  //current date - ios
//                it.lastPackType?.let { it1 -> put(LAST_PACK_TYPE, it1) }
//                it.lastPackPrice?.let { it1 -> put(LAST_PACK_PRICE, it1) }
//                it.lastPackName?.let { it1 -> put(LAST_PACK_NAME, it1) }
//                it.lastbillingType?.let { it1 -> put(LAST_BILLING_TYPE, it1) }
//                it.rechargeDueDate?.let { it1 -> put(RENEWAL_DUE_DATE, it1) }
                put(LAST_APP_USAGE_DATE, "") // current date - ios
//                it.totalPaidPackRenewal?.let { it1 -> put(TOTAL_PAID_PACK_RENEWALS, it1) }
                it.firstTimeLoginDate?.let { it1 -> put(FIRST_LOGIN_DATE, it1) }
//                it.firstPaidPackSubscriptionDate?.let{it1 -> put(FIRST_PACK_SUBSCRIPTION_DATE,it1)}
//                it.firstPaidPackSubscriptionDate?.let{it1 -> put(FIRST_PAID_PACK_SUBSCRIPTION_DATE,it1)}
                put(DEVICE_USED_IN_LAST_DAYS, "")
                put(TOTAL_WATCH_HOURS_LAST_DAYS, "") // Not available from BE
                put(TOTAL_WATCH_HOURS_LIFETIME, "")  // Not available from BE
//                put(FREE_TRIAL_TAKEN, YES)
                it.profileName?.let { it1 -> put(PROFILE_NAME, it1) }
                it.subscriberType?.let { it1 -> put(TYPE_OF_SUBSCRIBER, it1) }
                put(
                    FIRST_APP_DOWNLOAD_DATE,
                    firstAppLaunchTimeInUTC
                )
                put(DOWNLOAD_SOURCE, "Play Store")
                put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
//                put(SID, value)
            } as Map<String, Any>?)
        }
    }

    fun updateGroupProfile(key :String, value : String,partnerPacks: PartnerPacks?){
        partnerPacks.let {
            mMixpanelUnifiedAPI.getGroup(key, value).setMap(HashMap<String, String>().apply {
//                put(FIRE_TV, if(subscriptionTypeFtv.equals(it?.subscriptionType, true)) YES else NO)
//                put(ATV, if(subscriptionTypeAtv.equals(it?.subscriptionType, true)) YES else NO)
//                put(SUBSCRIBED, if (!SubscriptionPackStatusEnum.ACTIVE.status.equals(it?.subscriptionStatus, true)) NO else YES)
//                put(DATE_OF_SUBSCRIPTION,  it?.packCreationDate?:"")
//                val startDate = it?.packCreationDate?.let {
//                    getDateObject(it, START_DATE_FORMAT)
//                }
//                put(PACK_START_DATE,
//                    (startDate?:"").toString()
//                )
//                val endDate = it?.expirationDate?.let {
//                    getDateObject(it, END_DATE_FORMAT)
//                }
//                put(PACK_END_DATE,
//                    (endDate?:"").toString()
//                )
//                val rechargeDate = it?.rechargeDue?.let {
//                    getDateObject(it, END_DATE_FORMAT)
//                }
//                put(PACK_RENEWAL_DATE,
//                    (rechargeDate?:"").toString()
//                )

                put(LAST_USED_AT, getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT))
//                put(FREE_TRIAL, it?.freeTrialStatus?.let{if(it) YES else NO}?:"")
//                put(PACK_NAME,it?.productName?: FREEMIUM)
//                put(PACK_TYPE, if(it?.subscribedBingeProduct != null) "PAID" else "FREE")
//                put(PACK_PRICE,it?.packPrice?:"")
//                put(SUBSCRIPTION_TYPE,it?.subscriptionDetailInfo?.subscriptionType?:"UNSUBSCRIBED")
//                put(BURN_RATE_TYPE, partnerPacks?.burnRateType ?: "")
//                it.let { put(RENEWAL_DUE, if(true == it?.subscriptionStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true) && !it.isCancelled) "YES" else "NO") } ?: put(RENEWAL_DUE,  "NO")
//                put(SI_PAYMENT_MODE, it?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else "YES" } ?: "")
//                put(RECURRING_PAYMENT, it?.planCTADetails?.renewPlanOption?.let { if (it) "NO" else "YES" } ?: "")
//                put(FTV_ELIGIBLE, if(it?.eligibleFirestick == true) "YES" else "NO")
//                put(
//                    STACK,
//                    if (it?.dthStatus == DTH_W_BINGE_OLD_USER) "Tata Play" else "Comviva"
//                )
//                put(
//                    FIRST_PACK_SUBSCRIPTION_DATE,
//                    it?.firstPaidPackSubscriptionDate
//                        ?: "" /*TSF-8151 Due to Free trial disable from BE, Moving it to firstPaidPackSubscriptionDate*/
//                )
//                put(
//                    FIRST_PAID_PACK_SUBSCRIPTION_DATE,
//                    it?.firstPaidPackSubscriptionDate ?: ""
//                )
            } as Map<String, Any>?)
        }
    }

    fun updateProperty(propertyName : String, propertyValue : Int, mixpanelAPI: MixpanelAPI = mMixpanelAPI){
        val props = JSONObject()
        if(!propertyArray.contains(propertyName))
            props.put(propertyName, propertyValue)
        mixpanelAPI.people.set(props)
    }

    fun updateProperty(propertyName : String, propertyValue : Date?, mixpanelAPI: MixpanelAPI = mMixpanelAPI){
        val props = JSONObject()
        if(!propertyArray.contains(propertyName))
            props.put(propertyName, propertyValue)
        mixpanelAPI.people.set(props)
        mMixpanelUnifiedAPI.people.set(props)
    }

    private fun getInitialSuperProperties(
        rmn: String?,
        dthStatus: String,
        subscriberId: String?,
        bingeSubscriberId: String?,
        platform: String?,
    ): JSONObject {
        val props = JSONObject()
        try {
            //props.put(SID, sid)
            props.put(RMN, rmn)
            props.put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
            when (dthStatus) {
                DTH_W_BINGE_OLD_USER -> {
                    props.put(TS_SID,subscriberId)
                    //props.put(C_ID,"")
                }
                else -> {
                    props.put(C_ID,bingeSubscriberId)
                    //props.put(TS_SID,"")
                }
            }
            props.put(PLATFORM, platform)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return props
    }

    fun trackEvent(eventName: String, mixpanelAPI: MixpanelAPI = mMixpanelAPI) {
        try {
            if (mixpanelAPI == mMixpanelUnifiedAPI)
                d(this.javaClass.simpleName, "Event: $eventName") // TODO: Remove it
            validateAndEvent(checkForValidEventName(eventName), mixpanelAPI)
        } catch (e: Exception) {
            d("exception in mixpanel event", e.toString())
        }
    }

    fun trackEvent(eventName: String, parameter: JSONObject, mixpanelAPI: MixpanelAPI = mMixpanelAPI) {
        try {
            if (mixpanelAPI == mMixpanelUnifiedAPI)
                d(this.javaClass.simpleName, "Event: $eventName, Param: $parameter") // TODO: Remove it
            validateAndEvent(checkForValidEventName(eventName), parameter, mixpanelAPI)
        } catch (e: Exception) {
            d("exception in mixpanel event with parameter", e.toString())
        }
    }

    private fun validateAndEvent(eventName: String, mixpanelAPI: MixpanelAPI = mMixpanelAPI) {
        mixpanelAPI.track(eventName)
    }

    private fun validateAndEvent(eventName: String, params: JSONObject, mixpanelAPI: MixpanelAPI = mMixpanelAPI) {
        mixpanelAPI.track(eventName, params)
    }

    fun registerOrUpdateCommonSuperProperties() {
        try {
            val props = JSONObject()
            props.put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
            if(isTablet(context))
                props.put(PLATFORM, PLATFORM_ANDROID_TABLET)
            else
                props.put(PLATFORM, PLATFORM_ANDROID)
            mMixpanelUnifiedAPI.registerSuperProperties(props)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun registerGuestSuperProperties() {
        val props = JSONObject()
        try {
            props.put(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
            if(isTablet(context))
                props.put(PLATFORM, PLATFORM_ANDROID_TABLET)
            else props.put(PLATFORM, PLATFORM_ANDROID)
            props.put(USER_TYPE, UserDetailsUtil.getUserType(null))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mMixpanelUnifiedAPI.registerSuperProperties(props)
    }

    fun setUserIdentity(userIdentity: String?, mixpanelAPI: MixpanelAPI = mMixpanelAPI) {
        //Setting generic super properties before calling identify()
        if(isTablet(context))
            addUpdateSuperProperty(PLATFORM, PLATFORM_ANDROID_TABLET)
         else
            addUpdateSuperProperty(PLATFORM, PLATFORM_ANDROID)
        addUpdateSuperProperty(DEVICE_ID, DeviceInfoUtils.getDeviceId(context))
        if (userIdentity?.isNotBlank() == true) {
            mixpanelAPI.identify(userIdentity)
            mixpanelAPI.people.identify(userIdentity)
        }
    }

    fun setAlias(alias : String , sid: String, mixpanelAPI: MixpanelAPI = mMixpanelAPI) {
        mixpanelAPI.alias(alias, sid)
    }

    fun logout(mixpanelAPI: MixpanelAPI = mMixpanelAPI){
        mixpanelAPI.reset()
    }

    fun getMixPanelDistinctId(mixpanelAPI: MixpanelAPI = mMixpanelUnifiedAPI) : String = mixpanelAPI.distinctId
    fun clearSuperProperties() {
        mMixpanelUnifiedAPI.clearSuperProperties()
    }


}
