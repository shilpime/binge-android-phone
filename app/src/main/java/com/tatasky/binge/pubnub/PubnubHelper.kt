package com.tatasky.binge.pubnub

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNLogVerbosity
import com.pubnub.api.enums.PNReconnectionPolicy
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.history.PNHistoryResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.mixpanel.MixpanelHelper
import com.tatasky.binge.data.networking.ApiCallback
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.FetchProfileRequest
import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
import com.tatasky.binge.data.networking.models.requests.WalletBalanceRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Singleton


@Singleton
class PubnubHelper(private val mContext: Context,
                   private val sharedPrefs: PrefsRepo,
                   private val mixpanelHelper: MixpanelHelper,
                   private val subscriptionAnalytics: SubscriptionAnalytics,
                   private val localBroadcastHelper: LocalBroadcastHelper,
                   private val commonUseCase: CommonUseCase) {


    private var retryCount: Int = 0
    private val PUBLISH_KEY = BuildConfig.PUBNUB_PUBLISH_KEY
    private val SUBSCRIBE_KEY = BuildConfig.PUBNUB_SUBSCRIBE_KEY
    private var channelName: String? = null
    private var mPubNub: PubNub? = null
    private val pnConfiguration = PNConfiguration()

    init {
        //initiatePubnub()
        pnConfiguration.origin = BuildConfig.PUBNUB_ORIGIN_URL
        pnConfiguration.publishKey = PUBLISH_KEY
        pnConfiguration.subscribeKey = SUBSCRIBE_KEY
        pnConfiguration.reconnectionPolicy = PNReconnectionPolicy.NONE
        if(BuildConfig.DEBUG)
            pnConfiguration.logVerbosity = PNLogVerbosity.BODY
        pnConfiguration.setPresenceTimeoutWithCustomInterval(300, 0)
    }

    fun initiatePubnub(channelName: String) {
        pnConfiguration.uuid = sharedPrefs.getOriginalSubscriberId()
        mPubNub = PubNub(pnConfiguration)
        this.channelName = channelName
        subscribePubnub()
        e("pubnub", "initialized $channelName")
    }

    private fun subscribePubnub() {
        mPubNub?.run {
            addListener(object : SubscribeCallback() {
                override fun status(pubnub: PubNub, status: PNStatus) {

//                    e("pubnub", " status : ${status}")
                    if (status.category === PNStatusCategory.PNTimeoutCategory
                        || status.category === PNStatusCategory.PNNetworkIssuesCategory
                        || status.category === PNStatusCategory.PNDisconnectedCategory
                        || status.category === PNStatusCategory.PNUnexpectedDisconnectCategory
                        || status.category === PNStatusCategory.PNTLSConnectionFailedCategory
                    ) {
                        val channelName = channelName//"sub_" + sharedPrefs.getOriginalSubscriberId()
                        mPubNub?.let { it.subscribe().channels(Arrays.asList(channelName)).execute()}
                        // This event happens when radio / connectivity is lost
                    } else if (status.category === PNStatusCategory.PNConnectedCategory) {

                    } else if (status.category === PNStatusCategory.PNBadRequestCategory) {

                    }
                }

                override fun message(pubnub: PubNub, message: PNMessageResult) {
                    e("pubnub", " message.message.toString() : ${message.message}")
                    if (sharedPrefs.getLoginStatus() && !sharedPrefs.isLogoutCalled()
                        && !sharedPrefs.isSilentLoginCalled()) {
                        try {
                            val deviceId = DeviceInfoUtils.getDeviceId(mContext)
                            val userDetails = Gson().fromJson(
                                message.message.toString(),
                                PubnubResponse::class.java
                            )

                            if(userDetails.bingeList != null){
                                e("pubnub", "BingeList push ignored")
                                return
                            }
                            /*Silent Login Feature for user state change*/
                            if (userDetails.silentLoginTimestamp.isNullOrEmpty() ||
                                userDetails.silentLoginTimestamp.equals(
                                    sharedPrefs.getSilentLoginTimestamp(),
                                    ignoreCase = true
                                )
                            )
                            {
                                if (userDetails.rmn?.isNullOrEmpty() == false && userDetails.rmn != sharedPrefs.getClearRMN()) {
                                    sharedPrefs.resetHotstarPopupCount()
                                    sharedPrefs.clearHotstarPopupData()
                                    sharedPrefs.setClearRMN(userDetails.rmn.toString())
                                }
                                /*update DthStatus*/
                                /*if(!sharedPrefs.getDthStatusFreemium().equals(userDetails.dthStatus, true)){
                                    sharedPrefs.saveDTHStatusFreemium(userDetails.dthStatus ?: sharedPrefs.getDthStatusFreemium())
                                }*/ //Commented because ashima told me to revert it
                                sharedPrefs.saveDTHAccountStatus(
                                    userDetails.acStatus ?: AccountStatusEnum.ACTIVE.status
                                )
                                if (userDetails.paymentStatus.equals("SUCCESS", true)) {
                                    localBroadcastHelper.sendBroadcast(
                                        mContext,
                                        localBroadcastHelper.ACTION_PAYMENT_UPDATED
                                    )
                                }

                                if (true == userDetails.prime?.primeStatus?.equals("Activated", true)) {
                                    localBroadcastHelper.sendBroadcast(
                                        mContext,
                                        localBroadcastHelper.ACTION_PRIME
                                    )
                                }
                                /*Need to discuss on this*/
                                userDetails.cancelledDeviceInfo?.let {
                                    for (binge in it) {
                                        if (sharedPrefs.getBaId().equals(binge.baId)) {
                                            sharedPrefs.saveDeviceCancellationFlag(binge.deviceCancellationFlag)
                                            if (binge.deviceCancellationFlag) {
                                                fetchCurrentSubscription()
                                            }
                                        }
                                    }
                                }

                                userDetails.profilePubNubList?.find { it.isDefaultProfile }
                                    .let { profile ->
                                        fetchProfileInfo()
                                        sharedPrefs.setParentalPinExists(profile?.parentalPinExists == true)
                                        if (profile?.ageRatingName != null && profile?.ageRatingMasterMapping != null) {
                                            sharedPrefs.setParentalRating(
                                                AgeRatingsResponse().AgeRatings()
                                                    .also { rating ->
                                                        rating.ageRatingName =
                                                            profile?.ageRatingName
                                                        rating.ageRatingMasterMapping =
                                                            profile?.ageRatingMasterMapping
                                                    }
                                            )
                                        }
                                    }

                                var isDeviceLogout = true
                                if (DTH_W_BINGE_NEW_USER.equals(sharedPrefs.getDthStatusFreemium()) ||
                                    NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium())
                                ) {
                                    userDetails.devices?.let {
                                        if (sharedPrefs.getBaId().equals(userDetails.baId)) {
                                            for (binge in it) {
                                                e(
                                                    "pubnub",
                                                    "enter deviceId: ${binge.deviceSerialNumber}"
                                                )
                                                if (binge.deviceSerialNumber == deviceId) {
                                                    isDeviceLogout = false
                                                    fetchCurrentSubscription()
                                                }
                                            }
                                            e("pubnub", "enter device : $isDeviceLogout")
                                            if (isDeviceLogout) {
                                                e("pubnub", "enter device : Logout1")

                                                localBroadcastHelper.sendBroadcast(
                                                    mContext,
                                                    localBroadcastHelper.ACTION_LOGOUT
                                                )
                                                return
                                            }
                                        }
                                    }
                                }
                                if (!DTH_W_BINGE_OLD_USER.equals(sharedPrefs.getDthStatusFreemium()))
                                    userDetails.accountId?.let {
                                        sharedPrefs.saveBingeSid(it)
                                        subscriptionAnalytics.updateProperty(C_ID, it)
                                        subscriptionAnalytics.addUpdateSuperProperty(C_ID, it)
                                        subscriptionAnalytics.updateAdditionalData()
                                        //Clevertap Identity update at Pubnub push update for CID from Binge BE
                                        subscriptionAnalytics.updateIdentity(it)
                                    }
                                userDetails.deviceInfo?.let {
                                    subscriptionAnalytics.updateProperty(
                                        LOGGED_IN_DEVICE_COUNT,
                                        it.size
                                    )
                                    for (binge in it) {
                                        if (DTH_W_BINGE_NEW_USER.equals(sharedPrefs.getDthStatusFreemium()) ||
                                            NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium())
                                        ) {
                                            val deviceIdAPI = binge.deviceId
                                            if (deviceIdAPI == deviceId) {
                                                isDeviceLogout = false
                                                fetchCurrentSubscription()
                                                if (binge.atvCancelled) {
                                                    localBroadcastHelper.sendBroadcast(
                                                        mContext,
                                                        localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH
                                                    )
                                                }
                                            }
                                        } else {
                                            isDeviceLogout = false
                                            if (sharedPrefs.getBaId().equals(binge.baId)) {
                                                sharedPrefs.saveContentPlaybackAllowed(binge.contentPlayBackHybrid)
                                                if (binge.forceLogoutHybrid) {
                                                    localBroadcastHelper.sendBroadcast(
                                                        mContext,
                                                        localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT
                                                    )
                                                    return
                                                    // logout immediate when key value is true
                                                } else {
                                                    e("pubnub", "enter deviceId: $deviceId")
                                                    if (binge.deviceList?.contains(deviceId) == true) {
                                                        fetchCurrentSubscription()
                                                    } else {
                                                        e("pubnub", "enter device : Logout2")
                                                        localBroadcastHelper.sendBroadcast(
                                                            mContext,
                                                            localBroadcastHelper.ACTION_LOGOUT
                                                        )
                                                        return
                                                    }
                                                }
                                                for (x in binge.deviceDetailsList ?: emptyList()) {
                                                    if (x.deviceId == deviceId && true == x.atvCancelled) {
                                                        localBroadcastHelper.sendBroadcast(
                                                            mContext,
                                                            localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH
                                                        )
                                                    }
                                                }
//                                        sharedPrefs.saveDeviceCancellationFlag(binge.deviceCancellationFlag)
                                                return
                                            }
                                        }
                                    }
                                }?: kotlin.run { e("pubnub", "device info empty") }
                                if (isDeviceLogout) {
                                    e("pubnub", "enter device : Logout3")
                                    localBroadcastHelper.sendBroadcast(
                                        mContext,
                                        localBroadcastHelper.ACTION_LOGOUT
                                    )
                                    return
                                }
                            }
                            else{
                                sharedPrefs.setSilentLoginCalled(true)
                                //Do Silent Login for this rmn and launch home
                                doSilentLogin(userDetails)
                            }
                        }
                        catch (e: Exception) {
                            e("Pubnub Error", "Parsing Error")
                        }
                    }
//                    logoutApplication(mContext)
                }

                override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {
                }
            })

            subscribe()?.channels(Arrays.asList(channelName))?.execute()

        }
    }

    /**
     * This method need to fulfill user silent login for changes made in his system
     * Need to clear all info then save pubnubTimeStamp to sharedPrefs after login
     * After successful login take user to home page with a dialog
     * if login fails then retry it 2 times
     * */

    fun doSilentLogin(userDetails: PubnubResponse?) {
        sharedPrefs.saveLoginAccessToken(sharedPrefs.getAccessToken())
        sharedPrefs.saveLoginDeviceToken(sharedPrefs.getDeviceToken())
        val request = NewBingeUserRequest(
            login = SOURCE_OTP,
            subscriberId = userDetails?.dthSubscriberId ?: sharedPrefs.getOriginalSubscriberId(),
            mobileNumber = sharedPrefs.getClearRMN(),
            baId = userDetails?.baId ?: sharedPrefs.getBaId(),
            isCreate = false,
            dthStatus = userDetails?.dthStatus ?: sharedPrefs.getDthStatusFreemium(),
            isPastBingeUser = false,
            referenceId = sharedPrefs.getReferenceId(),
            bingeSubscriberId = userDetails?.accountId,
            silentLoginEvent = userDetails?.silentLoginEvent
        )
        val subscribeWith = commonUseCase.createNewBingeMobileUser(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<NewBingeUserResponse>() {
                override fun onError(error: ErrorModel?) {
                    //retry 2 times
                    if(retryCount <=3) {
                        retryCount++
                        doSilentLogin(userDetails)
                    }
                    else{
                        sharedPrefs.setSilentLoginCalled(false)
                    }
                }

                override fun onSuccessResponse(t: NewBingeUserResponse) {
                    if (t.code == CODE_SUCCESS) {

                        val channelNameSub = "sub_" + sharedPrefs.getOriginalSubscriberId()
                        val channelNameRmn = "rmn_" + sharedPrefs.getClearRMN()
                        when (sharedPrefs.getDthStatusFreemium()) {
                            DTH_W_BINGE_OLD_USER -> unsubscribe(channelNameSub)
                            else -> unsubscribe(channelNameRmn)
                        }

                        sharedPrefs.saveSilentLoginTimestamp(userDetails?.silentLoginTimestamp?:"")
                        if (t.bingeUserData?.primePackDetails != null)
                            sharedPrefs.savePrimePackDetails(t.bingeUserData?.primePackDetails)
                        if (t.bingeUserData != null) {
                            saveLoggedInDetails(t)
                        }
                        sharedPrefs.setSilentLoginCalled(false)
                        localBroadcastHelper.sendBroadcast(mContext, localBroadcastHelper.ACTION_SILENT_LOGIN)


                    }
                }

                override fun onSubscribe(d: Disposable) {
                }
            })
    }

    private fun saveLoggedInDetails(loginResponse: NewBingeUserResponse) {
        loginResponse.bingeUserData?.dthStatus?.let { sharedPrefs.saveDTHStatusFreemium(it) }
//        loginResponse.bingeUserData?.userAuthenticateToken?.let { sharedPrefs.setAccessToken(it) }
//        loginResponse.bingeUserData?.deviceAuthenticateToken?.let { sharedPrefs.setDeviceToken(it) }
        sharedPrefs.saveDTHAccountStatus(
            loginResponse.bingeUserData?.dthAccountStatus ?: AccountStatusEnum.ACTIVE.status
        )
        loginResponse.bingeUserData?.accountSubStatus?.let{
            sharedPrefs.saveDTHAccountSubStatus(it)
        }
        sharedPrefs.saveBingeSid(loginResponse.bingeUserData?.bingeSubscriberId)
        sharedPrefs.saveFirestickTaken(loginResponse.bingeUserData?.fsTaken ?: false)
        sharedPrefs.saveDsn(loginResponse.bingeUserData?.deviceSerialNumber ?: "")
        sharedPrefs.setFirestickDialogShown(true)

        if (loginResponse.bingeUserData?.userAuthenticateToken != null)
            sharedPrefs.setAccessToken(loginResponse.bingeUserData?.userAuthenticateToken!!)

        if (loginResponse.bingeUserData?.deviceAuthenticateToken != null)
            sharedPrefs.setDeviceToken(loginResponse.bingeUserData?.deviceAuthenticateToken!!)

        sharedPrefs.saveSubscriptionType(loginResponse.bingeUserData?.subscriptionType?:"")
        if (loginResponse.bingeUserData?.baId != null) {
            loginResponse.bingeUserData?.baId?.let {
                sharedPrefs.setBaId(it)
            }
            sharedPrefs.setOriginalSubscriberId(loginResponse.bingeUserData!!.subscriberId!!)
            loginResponse.bingeUserData?.profileId?.let {
                sharedPrefs.setProfileId(it)
            }
            loginResponse.bingeUserData?.let {
                sharedPrefs.setSelectedProfile(it)
            }
            sharedPrefs.saveAccountDetails(
                Gson().toJson(
                    loginResponse.bingeUserData
                )
            )
        }
        loginResponse.bingeUserData?.mixpanelid?.let { sharedPrefs.saveMixPanelId(it) }
        loginResponse.bingeUserData?.referenceId?.let { sharedPrefs.saveRefrenceId(it) }
        loginResponse.bingeUserData?.rmn = sharedPrefs.getClearRMN()
        subscriptionAnalytics.trackUserDetails(
            loginResponse.bingeUserData?.mixpanelid,
            sharedPrefs.getOriginalSubscriberId(),
            loginResponse.bingeUserData,
            sharedPrefs.getAnonymousId(),
            sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
            sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
        )
        subscriptionAnalytics.updateProperty(RMN,sharedPrefs.getClearRMN())
//        trackEventLoginSuccess()
        val channelNameSub = "sub_" + sharedPrefs.getOriginalSubscriberId()
        val channelNameRmn = "rmn_" + sharedPrefs.getClearRMN()
        when (sharedPrefs.getDthStatusFreemium()) {
            DTH_W_BINGE_OLD_USER -> initiatePubnub(channelNameSub)
            else -> initiatePubnub(channelNameRmn)
        }
        sharedPrefs.setParentalPinExists(loginResponse.bingeUserData?.parentalPinExist == true)
        if (loginResponse.bingeUserData?.ageRatingName != null && loginResponse.bingeUserData?.ageRatingMasterMapping != null) {
            sharedPrefs.setParentalRating(
                AgeRatingsResponse().AgeRatings().also { rating ->
                    rating.ageRatingName = loginResponse.bingeUserData?.ageRatingName
                    rating.ageRatingMasterMapping =
                        loginResponse.bingeUserData?.ageRatingMasterMapping
                }
            )
        }
    }

    fun getLastStatus(apiCallback: ApiCallback<PubnubResponse>?) {
        mPubNub?.history()
            ?.channel(channelName) // where to fetch history from
            ?.count(1) // how many items to fetch
            ?.async(object : PNCallback<PNHistoryResult>() {
                override fun onResponse(result: PNHistoryResult?, status: PNStatus) {
                    e("pubnub", "getLastStatus message.message.toString() : ${result?.toString()}")
                    result?.let {
                        val deviceId = DeviceInfoUtils.getDeviceId(mContext)
                        if (status.category === PNStatusCategory.PNAcknowledgmentCategory) {
                            val gson = Gson()
                            if (result.messages.size > 0) {
                                val userDetails = gson.fromJson(
                                    result.messages.get(0).entry.toString(),
                                    PubnubResponse::class.java
                                )

                                if (sharedPrefs.getLoginStatus() && !sharedPrefs.isLogoutCalled()
                                    && !sharedPrefs.isSilentLoginCalled()) {
                                    /*End of changes*/
                                    if (userDetails.silentLoginTimestamp.isNullOrEmpty() ||
                                        userDetails.silentLoginTimestamp.equals(
                                            sharedPrefs.getSilentLoginTimestamp(),
                                            ignoreCase = true
                                        )
                                    ) {
                                        /*update DthStatus*/
                                        /*if(!sharedPrefs.getDthStatusFreemium().equals(userDetails.dthStatus, true)){
                                        sharedPrefs.saveDTHStatusFreemium(userDetails.dthStatus ?: sharedPrefs.getDthStatusFreemium())
                                    }*/ //Commented because ashima told me to revert it
                                        if (!DTH_W_BINGE_OLD_USER.equals(sharedPrefs.getDthStatusFreemium()))
                                            userDetails.accountId?.let {
                                                sharedPrefs.saveBingeSid(it)
                                                subscriptionAnalytics.updateProperty(C_ID, it)
                                                subscriptionAnalytics.addUpdateSuperProperty(
                                                    C_ID,
                                                    it
                                                )
                                                subscriptionAnalytics.updateAdditionalData()
                                                //Clevertap Identity update at Pubnub push update for CID from Binge BE
                                                subscriptionAnalytics.updateIdentity(it)
                                            }
                                        userDetails.deviceInfo?.let {
                                            userDetails.cancelledDeviceInfo?.let {
                                                for (binge in it) {
                                                    if (sharedPrefs.getBaId() == binge.baId) {
                                                        sharedPrefs.saveDeviceCancellationFlag(binge.deviceCancellationFlag)
                                                    }
                                                }
                                            }
                                            var referenceId: String? = null
                                            for (binge in it) {
                                                if (sharedPrefs.getBaId().equals(binge.baId)) {
                                                    referenceId = binge.referenceId
                                                    sharedPrefs.saveContentPlaybackAllowed(binge.contentPlayBackHybrid)
                                                    if (binge.forceLogoutHybrid) {
                                                        localBroadcastHelper.sendBroadcast(
                                                            mContext,
                                                            localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT
                                                        )
                                                        return@let
                                                    }
                                                    if (binge.atvCancelled) {
                                                        localBroadcastHelper.sendBroadcast(
                                                            mContext,
                                                            localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH
                                                        )
                                                    }
                                                    for (x in binge.deviceDetailsList
                                                        ?: emptyList()) {
                                                        if (x.deviceId == deviceId && true == x.atvCancelled) {
                                                            localBroadcastHelper.sendBroadcast(
                                                                mContext,
                                                                localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            /*This is done for Already Live Binge User who will upgrade to Freemium*/
                                            if (sharedPrefs.getLoginStatus() && referenceId != null) {
                                                if (sharedPrefs.getMixPanelId() == null)
                                                    fetchMixpanelUniqueId(referenceId)
                                            }
                                            fetchCurrentSubscription()
                                        }
                                        apiCallback?.onSuccessFullyCallback(userDetails)
                                    } else {
                                        sharedPrefs.setSilentLoginCalled(true)
                                        doSilentLogin(userDetails)
                                    }
                                }
                                else{

                                }
                            }
                            else {
                                apiCallback?.onFailure()
                            }
                        } else {
                            apiCallback?.onFailure()
                        }
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    private fun fetchMixpanelUniqueId(referenceId: String) {
        commonUseCase.fetchMixpanelUniqueId(referenceId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<MixpanelUniqueResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                }
                override fun onError(error: ErrorModel?) {
                }

                override fun onSuccessResponse(t: MixpanelUniqueResponse) {
                    if (t.data != null) {
                        t.data?.mixpanelId?.let {
                            sharedPrefs.saveMixPanelId(it)
                            mixpanelHelper.setUserIdentity(it, mixpanelHelper.mMixpanelUnifiedAPI)
                            mixpanelHelper.updateProperty(MIXPANEL_ID, it)
                        }
                    }
                }
            })
    }

    @Synchronized
    fun unsubscribe(channelName: String) {
        this.channelName = channelName
        if (mPubNub != null) {
            mPubNub!!.unsubscribe()
                .channels(Arrays.asList(channelName))
                .execute()
            mPubNub = null
        }
    }

    var retrySubject = PublishSubject.create<Any>()
    private lateinit var currentSubscriptionCompositeDisposable: Disposable

    @SuppressLint("CheckResult")
    fun fetchCurrentSubscription() {
        fetchProfileInfo()
        if(!NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(),true))
            fetchBalance()
        var count = 0
        val sid = sharedPrefs.getOriginalSubscriberId() ?: ""
        val baId = sharedPrefs.getBaId() ?: ""
        if (::currentSubscriptionCompositeDisposable.isInitialized) {
            currentSubscriptionCompositeDisposable.dispose()
        }
        commonUseCase.getFreemiumCurrentPack(
            baId = baId,
            accountId = sharedPrefs.getOriginalSubscriberId(),
            freemiumUserType = sharedPrefs.getDthStatusFreemium(),
            userIsOnTickTick = sharedPrefs.getSubscribedPack()?.flexiPlan
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                count++
                if (count < 3)
                    retrySubject.onNext(Any())
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
                override fun onSubscribe(d: Disposable) {
                    currentSubscriptionCompositeDisposable = d
                    super.onSubscribe(d)
                }
                override fun onError(error: ErrorModel?) {
                }

                override fun onSuccessResponse(t: PurchasePackResponse) {
                    when (t.code) {
                        CODE_SUCCESS -> {
                            if (t.data != null) {
                                t.data?.let {
                                    if(it.forceLogoutHybrid){
                                        localBroadcastHelper.sendBroadcast(mContext, localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT)
                                        return
                                    }
                                    if (true == it.atvCancelled) {
                                        localBroadcastHelper.sendBroadcast(mContext, localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH)
                                    }
                                    sharedPrefs.saveSubscribedPack(t.data!!,subscriptionAnalytics)
                                }
                            } else {
                                sharedPrefs.saveSubscribedPack(null,subscriptionAnalytics)
                            }
                            localBroadcastHelper.sendBroadcast(mContext, localBroadcastHelper.ACTION_SUBSCRIPTION_UPDATED)
                        }
                        CODE_LOGOUT_ALL -> {
                            //logout all users for this sid
                            localBroadcastHelper.sendBroadcast(mContext, localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT_ALL)
                        }
                        else -> {
                        }
                    }
//                    localBroadcastHelper.sendBroadcast(mContext, localBroadcastHelper.ACTION_DATA_POSTED)
                }
            })
    }

    fun fetchBalance() {
        val subscribeWith = commonUseCase.fetchBalance(WalletBalanceRequest(sharedPrefs.getBaId()!!)).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<WalletBalanceResponse>() {
                override fun onError(error: ErrorModel?) {

                }

                override fun onSuccessResponse(t: WalletBalanceResponse) {
                    when (t.code) {
                        CODE_SUCCESS -> {
                            sharedPrefs.setFetchedBalanceData(
                                Gson().toJson(
                                    t
                                )
                            )
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                }
            })
    }

    fun fetchProfileInfo() {
        val rmn = sharedPrefs.getUserDetails()?.rmn ?: ""
        val baId = sharedPrefs.getBaId()

        val disposable = commonUseCase.getProfileInfo(FetchProfileRequest(baId, rmn))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<SubscriberProfileListModel>() {
                override fun onError(error: ErrorModel?) {

                }

                override fun onSuccessResponse(t: SubscriberProfileListModel) {
                    sharedPrefs.setFetchedProfileData(
                        Gson().toJson(
                            t
                        )
                    )
                    if(t.code== CODE_SUCCESS) {
                        val list = t.userData?.languageList?: emptyList()
                        val listLang = ArrayList<String>()
                        list.forEach { listLang.add(it.name) }
                        val selectedProfileSaved = LoginResponse.BingeSubscription()
                        selectedProfileSaved.rmn = rmn
                        selectedProfileSaved.firstName = t.userData?.firstName
                        selectedProfileSaved.lastName = t.userData?.lastName
                        selectedProfileSaved.emailId = t.userData?.email
                        selectedProfileSaved.aliasName = t.userData?.aliasName
                        selectedProfileSaved.imageUrl = t.userData?.image
                        sharedPrefs.setSelectedProfile(selectedProfileSaved)
                        sharedPrefs.setPrefLanguage(listLang)
                        sharedPrefs.setAutoPlayTrailerOn(t.userData?.isTrailerAutoPlay?:true)
                        sharedPrefs.setAllowWatchNotification(t.userData?.isWatchNotificationEnabled?:true)
                        sharedPrefs.setAllowTransactionalNotification(t.userData?.isTransactionalNotificationEnabled?:true)
                    }
                }
            })
    }
}