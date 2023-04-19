package com.tatasky.binge.ui.features.onboarding.login

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.onboarding.login.adapter.BAIDAdapter
import com.tatasky.binge.ui.features.onboarding.login.adapter.SelectSIDAdapter
import com.tatasky.binge.ui.features.onboarding.login.select_baid.BAIDSelector
import com.tatasky.binge.ui.features.onboarding.login.select_sid.SIDSelector
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 11/10/19.
 */
open class LoginViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo,
    val pubnubHelper: PubnubHelper,
) : BaseViewModel() {

    fun isDTHUser() = !NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
    val _maxDeviceLimitReachedResponse = MutableLiveData<SingleEvent<ErrorModel>>()
    var nosBaids: Int = 1
    var resendDurationLeft: Long = 0
    var OTP_RESEND_DURATION: Int = 30 * 1000
    // Free trial login UI
    var showFreeTrailUI = true // By default everyone will see free trial UI
    var untrackedMixpanelId : String? = null
    var untrackedMixpanelUnifiedId : String? = null
    protected lateinit var _searchDisposable: Disposable
    private val liveSelectedBingeUser =
        MutableLiveData<SingleEvent<LoginResponse.BingeSubscription>>()
    private val liveSelectedSubscriberDetails =
        MutableLiveData<SingleEvent<SubscriberIdListResponse.SubscriberDetail>>()
    var timer: CountDownTimer? = null
    var loginAuth = SOURCE_OTP
    var loginType = RMN
    var SOURCE = SOURCE_LOGOUT
    var loginSource = SOURCE_APP_LAUNCH

    //-----------Guest login and otp
    var rmn: String = ""
    var otp: String = ""
    var packageId: String? = null
    var isNewUser: Boolean = false

    var sidSelector = object :
        SIDSelector {
        override fun onSidSelect(sid: SubscriberIdListResponse.SubscriberDetail) {
            liveSelectedSubscriberDetails.postValue(SingleEvent(sid))
            loginDTO.sid = sid.sid
            activeBingeUserList = sid.listOfBaIds
        }
    }
    private var baidSelector = object :
        BAIDSelector {
        override fun onSidSelect(baid: LoginResponse.BingeSubscription) {
            liveSelectedBingeUser.postValue(SingleEvent(baid))
        }
    }
    private var sidAdapter = SelectSIDAdapter(emptyList(), sidSelector)
    private var baidAdapter = BAIDAdapter(emptyList(), baidSelector)


    var resendCount = 0
    var forgetResendCount = 0
    var resetFields = false
    var isTimerStarted = false
    var forgetIsTimerStarted = false
    // Holds subscriber details to use in BAID listing screen
    var selectedSubscriberDetails: SubscriberIdListResponse.SubscriberDetail? = null

    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    init {
        OTP_RESEND_DURATION = sharedPrefs.getOtpDuration() * 1000
    }

    var loginDTO: LoginDTO = LoginDTO()

    @Inject
    lateinit var splashAnalytics: SplashAnalytics

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    private var errorResponse = MutableLiveData<SingleEvent<String>>()
    private var otpResponse = MutableLiveData<SingleEvent<GetOtpResponse>>()
    private var _otpResponseError = MutableLiveData<SingleEvent<ErrorModel>>()
    private var eulaResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    private var subIdLookupResponse = MutableLiveData<SingleEvent<SubscriberIdListResponse>>()
    private var baIdLookupResponse = MutableLiveData<SingleEvent<BAIdListResponse>>()
    private var activeBingeUserList: List<LoginResponse.BingeSubscription> = emptyList()
    private var forceLoginAgain = MutableLiveData<SingleEvent<String?>>()
    private var bingeSubscriptionError = MutableLiveData<SingleEvent<String?>>()
    private var validatePasswordOtpResponse = MutableLiveData<SingleEvent<ValidateOTPResponse>>()
    protected var newBingeUserResponse = MutableLiveData<SingleEvent<NewBingeUserResponse>>()
    var existingBingeUserLogin = MutableLiveData<SingleEvent<NewBingeUserResponse>>()
    private var forgetPasswordInitiateResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    private val _rechargeResponse = MutableLiveData<SingleEvent<RechargeResponse>>()
    private val _walletBalanceResponse = MutableLiveData<SingleEvent<WalletBalanceResponse>>()
    private val dunningRechargeResponse = MutableLiveData<SingleEvent<DunningResponse>>()
    private val dthStatusError = MutableLiveData<SingleEvent<ErrorModel>>()

    private var _forceChangePasswordResponse =
        MutableLiveData<SingleEvent<ForgetPasswordResponse>>()

    fun getError(): LiveData<SingleEvent<String>> = errorResponse
    fun getOtpResponse(): LiveData<SingleEvent<GetOtpResponse>> = otpResponse
    fun getOtpResponseError(): LiveData<SingleEvent<ErrorModel>> = _otpResponseError
    fun getForceLoginAgain(): LiveData<SingleEvent<String?>> = forceLoginAgain
    fun getBingeSubscriptionError(): LiveData<SingleEvent<String?>> = bingeSubscriptionError

    fun getForgetPasswordResponse(): LiveData<SingleEvent<BaseResponse>> =
        forgetPasswordInitiateResponse

    fun getSubLookupResponse(): LiveData<SingleEvent<SubscriberIdListResponse>> =
        subIdLookupResponse

    fun getBaLookupResponse(): LiveData<SingleEvent<BAIdListResponse>> = baIdLookupResponse

    fun getValidateResponse(): LiveData<SingleEvent<ValidateOTPResponse>> =
        validatePasswordOtpResponse

    fun getWalletBalance(): LiveData<SingleEvent<WalletBalanceResponse>> = _walletBalanceResponse

    fun getDunningResponse(): LiveData<SingleEvent<DunningResponse>> = dunningRechargeResponse


    fun getSelectedSID(): LiveData<SingleEvent<SubscriberIdListResponse.SubscriberDetail>> =
        liveSelectedSubscriberDetails

    fun getSelectedBAID(): LiveData<SingleEvent<LoginResponse.BingeSubscription>> =
        liveSelectedBingeUser

    fun rechargeResponse(): LiveData<SingleEvent<RechargeResponse>> = _rechargeResponse

    fun getNewBingeUserResponse(): LiveData<SingleEvent<NewBingeUserResponse>> =
        newBingeUserResponse

    fun getExistingBingeUserLoginResponse(): LiveData<SingleEvent<NewBingeUserResponse>> =
        existingBingeUserLogin

    fun getDthStatusError(): LiveData<SingleEvent<ErrorModel>> =
        dthStatusError


    fun getForceChangePasswordResponse(): LiveData<SingleEvent<ForgetPasswordResponse>> =
        _forceChangePasswordResponse

    fun getMaxDeviceLimitReachedResponse(): LiveData<SingleEvent<ErrorModel>> =
        _maxDeviceLimitReachedResponse

    /*Need To Keep it only for TestCases*/
    @SuppressLint("CheckResult")
    fun generateOTP(isPassword:Boolean) {
        loginType = if (loginDTO.actualRMN.isEmpty())
            SID
        else
            RMN
        setProgressing(true)
        useCase.generateOTP(loginDTO,isPassword)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<GetOtpResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: GetOtpResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            otpResponse.postValue(SingleEvent(t))
                        }
                        else -> {
                            _otpResponseError.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    _searchDisposable = d
                    addDisposable(d)
                }
            })
    }
    /*Need To Keep it only for TestCases*/
    fun loginWithPassword(pwd: String) {
        setProgressing(true)
        val subscribeWith = useCase.loginWithPassword(loginDTO.apply { this.pwd = pwd })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ValidateOTPResponse>() {
                override fun onSuccessResponse(it: ValidateOTPResponse) {
                    setProgressing(false)
                    loginAuth = SOURCE_PASSWORD
                    if (it.code == CODE_SUCCESS) {
                        validatePasswordOtpResponse.postValue(SingleEvent(it))
                    } else {
//                        trackLoginFail(loginType, loginAuth, "Incorrect password")
                        when (it.code) {
                            RESPONSE_CODE_WRONG_PASSWORD -> {
                                errorResponse.postValue(
                                    SingleEvent(
                                        it.message
                                            ?: "The Password entered is incorrect. Please try again."
                                    )
                                )
                            }
                            else -> {
                                setError(ErrorModel(it.code, it.message))
                            }
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                    loginAuth = SOURCE_PASSWORD
//                    trackLoginFail(
//                        loginType,
//                        loginAuth,
//                        error?.message ?: "Incorrect password"
//                    )
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    _searchDisposable = d
                    addDisposable(d)
                }
            })
    }

    fun trackLoginFail(
        type: String,
        auth: String,
        value: String,
        reason: String,
        Source: String,
        errorCode: String
    ) {
        loginAnalytics.trackLoginFailure(
            type,
            auth,
            reason,
            value,
            Source,
            errorCode
        )
    }

    /*Need To Keep it only for TestCases*/
    @SuppressLint("CheckResult")
    fun loginWithOTP(fetchedOtp: String) {
        setProgressing(true)
        loginDTO.otp = fetchedOtp
        useCase.loginViaOtpUser(loginDTO)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ValidateOTPResponse>() {
                override fun onSuccessResponse(t: ValidateOTPResponse) {
                    setProgressing(false)
                    loginAuth = SOURCE_OTP
                    if (t.code == CODE_SUCCESS) {

                        validatePasswordOtpResponse.postValue(SingleEvent(t))
                    } else {
//                        trackLoginFail(
//                            loginType,
//                            loginAuth,
//                            t.message ?: "The OTP entered is incorrect. Please try again."
//                        )
                        when (t.code) {
                            RESPONSE_CODE_WRONG_OTP -> {
                                errorResponse.postValue(
                                    SingleEvent(
                                        t.message
                                            ?: "The OTP entered is incorrect. Please try again."
                                    )
                                )
                            }
                            else -> {
                                setError(ErrorModel(t.code, t.message))
                            }
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                    loginAuth = SOURCE_OTP
                    /*trackLoginFail(
                        loginType,
                        loginAuth,
                        error?.message ?: "The OTP entered is incorrect. Please try again."
                    )*/
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    _searchDisposable = d
                    addDisposable(d)
                }
            })
    }

    fun fetchSubIdList() {
        setProgressing(true)
        useCase.run {
            getSubIdList(loginDTO.rmn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<SubscriberIdListResponse>() {
                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                        trackLoginFail(
                            loginType,
                            loginAuth,
                            if (rmn.isNotEmpty()) rmn else loginDTO.rmn,
                            error?.message ?: "The OTP entered is incorrect. Please try again.",
                            loginSource,
                            error?.code.toString()
                        )
                    }

                    override fun onSuccessResponse(t: SubscriberIdListResponse) {
                        setProgressing(false)
                        if (t.subscribersList != null) {
                            subIdLookupResponse.postValue(SingleEvent(t))
                        } else {
                            trackLoginFail(
                                loginType,
                                loginAuth,
                                if (rmn.isNotEmpty()) rmn else loginDTO.rmn,
                                t.message ?: "The OTP entered is incorrect. Please try again.",
                                loginSource,
                                t.code.toString()
                            )
                            when (t.code) {
                                FORCE_LOGIN_ERROR_CODE, FORCE_LOGIN_ERROR_CODE2 -> {
                                    forceLoginAgain.postValue(SingleEvent(t.message))
                                }
                                else -> {
                                    setError(ErrorModel(t.code, t.message))
                                }
                            }
                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                        _searchDisposable = d
                        addDisposable(d)
                    }
                })
        }
    }

    fun saveLoggedInDetails(loginResponse: NewBingeUserResponse) {
        sharedPrefs.resetFSDialog()
        showFreeTrailUI = loginResponse.bingeUserData?.loginFreeTrialAvailed ?: true
        sharedPrefs.setIsEligibleForFreeTrial(showFreeTrailUI)
        sharedPrefs.removeTempToken()
        sharedPrefs.setLoginAgain(true)
        sharedPrefs.saveNumberOfBingeAccount(nosBaids)
        sharedPrefs.saveRMN(rmn)
        sharedPrefs.setClearRMN(rmn)
        loginResponse.bingeUserData?.dthStatus?.let { sharedPrefs.saveDTHStatusFreemium(it) }
        sharedPrefs.setLoggedInWithPassword(loginAuth.equals(SOURCE_PASSWORD, true))
//        sharedPrefs.removeTempToken()
        sharedPrefs.setWelcomeDialogStatus(false)
        sharedPrefs.setAccessToken(sharedPrefs.getLoginAccessToken())
        sharedPrefs.setDeviceToken(sharedPrefs.getLoginDeviceToken())
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

        sharedPrefs.saveSubscribedPack(loginResponse.bingeUserData?.partnerSubscriptions,subscriptionAnalytics)
//        sharedPrefs.saveSubscriptionType(loginResponse.bingeUserData?.subscriptionDetailInfo?.subscriptionType ?: "")
        //Subscription Type to be used Confirmed with BE
        sharedPrefs.saveSubscriptionType(loginResponse.bingeUserData?.subscriptionType?:"")
        sharedPrefs.saveContentPlaybackAllowed(
            loginResponse.bingeUserData?.partnerSubscriptions?.contentPlayBackHybrid ?: false
        )
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
            loginResponse.bingeUserData?.rmn = if(loginType == RMN) loginDTO.actualRMN else loginResponse.bingeUserData?.rmn
            //Set RMN in case RMN is null from the login success responseq
            loginResponse.bingeUserData?.let {
                if (it.rmn.isNullOrEmpty() && loginType == RMN) {
                    it.rmn = loginDTO.rmn
                }
            }
            sharedPrefs.saveAccountDetails(
                Gson().toJson(
                    loginResponse.bingeUserData
                )
            )
        }
        loginResponse.bingeUserData?.mixpanelid?.let { sharedPrefs.saveMixPanelId(it) }
        loginResponse.bingeUserData?.referenceId?.let { sharedPrefs.saveRefrenceId(it) }
        loginResponse.bingeUserData?.rmn = rmn
        /*Start of Analytics*/
        splashAnalytics.trackUserDetails(
            loginResponse.bingeUserData?.mixpanelid,
            sharedPrefs.getOriginalSubscriberId(),
            loginResponse.bingeUserData,
            sharedPrefs.getAnonymousId(),
            sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
            sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
        )
        /*
        Used in Landing Activity after login success toast
        loginAnalytics.trackLoginSuccess(
            loginType,
            loginAuth,
            rmn,
            loginSource,
            userState,
            isNewUser
        )*/
        splashAnalytics.updateUserProperty(RMN,sharedPrefs.getClearRMN())
        /*End of Analytics*/
        val channelNameSub = "sub_" + sharedPrefs.getOriginalSubscriberId()
        val channelNameRmn = "rmn_" + sharedPrefs.getClearRMN()
        when (sharedPrefs.getDthStatusFreemium()) {
            DTH_W_BINGE_OLD_USER -> pubnubHelper.initiatePubnub(channelNameSub)
            else -> pubnubHelper.initiatePubnub(channelNameRmn)
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

    fun setSelectSidAdapter(dataList: List<SubscriberIdListResponse.SubscriberDetail>) {
        sidAdapter.updateList(dataList)
    }

    fun setBAidAdapter(dataList: List<LoginResponse.BingeSubscription>) {
        baidAdapter.updateList(dataList)
    }

    fun getSelectSIDAdapter() = sidAdapter
    fun getSelectBAIDAdapter() = baidAdapter
    fun saveLoginToken(it: ValidateOTPResponse) {
        sharedPrefs.saveLoginAccessToken(it.data?.userAuthenticateToken)
        sharedPrefs.saveLoginDeviceToken(it.data?.deviceAuthenticateToken)
    }

    fun clearApiCalls() {
        clearDisposables()
    }

    fun removeDisposable(){
        if (::_searchDisposable.isInitialized) {
            _searchDisposable.dispose()
        }
    }

    /*fun checkForManagedAppEligibility(lambda: (Boolean) -> Unit) {
        setProgressing(true)
        useCase.run {
            checkManagedAppEligibility()//TODO Vishu need to change with actual api method for managedApp
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<UserPlaybackEligibilityResponse>() {

                    override fun onSuccessResponse(t: UserPlaybackEligibilityResponse) {
                        setProgressing(false)
                        sharedPrefs.setManagedAppEnabled(t.data.enableManageApp)
                        lambda.invoke(t.data.enableManageApp)

                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        lambda.invoke(sharedPrefs.isManagedAppEnabled())
                        //setError(error)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
        }


    }*/

    fun setVerbiageForLoginScreen(): ConfigResponse.LoginScreen? {
        return sharedPrefs.getConfigResponse()?.data?.config?.loginScreen
    }

}
