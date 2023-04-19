package com.tatasky.binge.ui.features.onboarding.login.bottomsheet

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tatasky.binge.analytics.MIXPANEL_ID
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.FetchProfileRequest
import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
import com.tatasky.binge.data.networking.models.requests.ValidateOtpGuestLoginRequest
import com.tatasky.binge.data.networking.models.requests.WalletBalanceRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.onboarding.login.LoginViewModel
import com.tatasky.binge.ui.features.onboarding.login.adapter.PartnerLogoRVAdapter
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.adapter.GuestLoginPreviouslyUsedMobileAdapter
import com.tatasky.binge.utils.CODE_LOGOUT_ALL
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.DTH_W_BINGE_OLD_USER
import com.tatasky.binge.utils.LOGIN_MAX_DEVICE_ERROR_CODE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import javax.inject.Inject

class GuestLoginViewModel @Inject constructor(
    useCase: CommonUseCase,
    sharedPrefs: PrefsRepo,
    pubnubHelper: PubnubHelper,
) : LoginViewModel(useCase, sharedPrefs, pubnubHelper) {

    var cartId: String=""
    private var lastClickOnValidateOtpInMillis: Long = 0
    var referenceId: String? = null
    val guestLoginResult = MutableLiveData<SingleEvent<String>>()

    private val _isSubscriptionFetched = MutableLiveData<SingleEvent<Boolean>>()
    val isSubscriptionFetched = _isSubscriptionFetched

    private val _generateOtpResponse = MutableLiveData<SingleEvent<GetOtpGuestLoginResponse>>()
    val generateOtpResponse: LiveData<SingleEvent<GetOtpGuestLoginResponse>> = _generateOtpResponse

    private val _generateOtpResponseError = MutableLiveData<SingleEvent<ErrorModel>>()
    val generateOtpResponseError = _generateOtpResponseError

    private val _validateOtpResponse = MutableLiveData<SingleEvent<ValidateOTPResponse>>()
    val validateOtpResponse: LiveData<SingleEvent<ValidateOTPResponse>> = _validateOtpResponse

    private val _validateOtpResponseError = MutableLiveData<SingleEvent<ErrorModel>>()
    val validateOtpResponseError = _validateOtpResponseError
    //-----------Parental pin setup
    var isLoggedIn = false
    var isParentalPinSetupRequested = false
    var isParentalPinVerificationRequested = false
    var isParentalPinResetRequested = false
    var parentalPinValue: String = ""

    //-----------Previously Used Mobile Number
    var previouslyUsedMobileNumberList = mutableListOf<UsedMobileNumber>()

    val guestLoginPreviouslyUsedMobileAdapter =
        GuestLoginPreviouslyUsedMobileAdapter(mutableListOf()) { usedMobileNumber ->
            _previouslyUsedMobileNumberLiveData.postValue(SingleEvent(usedMobileNumber))
            showFreeTrailUI = usedMobileNumber.freeTrialEligible
            sharedPrefs.setIsEligibleForFreeTrial(showFreeTrailUI)
        }

    private val _previouslyUsedMobileNumberLiveData =
        MutableLiveData<SingleEvent<UsedMobileNumber>>()
    val previouslyUsedMobileNumberLiveData: LiveData<SingleEvent<UsedMobileNumber>> =
        _previouslyUsedMobileNumberLiveData

    private var partnerLogoRVAdapter: PartnerLogoRVAdapter? = null
    fun getPartnerLogoAdapter() = partnerLogoRVAdapter

    private var _subscriptionStatusInfo : LoginResponse.SubscriptionStatusInfo? = null
    fun getSubscriptionStatusInfo() = _subscriptionStatusInfo

    fun setPartnerLogoAdapter(providers: List<Providers>?) {
        partnerLogoRVAdapter = PartnerLogoRVAdapter()
        partnerLogoRVAdapter?.submitList(providers)
    }


    fun fetchFreemiumCurrentSubscription(){
        setProgressing(true)
        val dis = useCase.getFreemiumCurrentPack(
            baId = sharedPrefs.getBaId(),
            accountId = sharedPrefs.getOriginalSubscriberId(),
            freemiumUserType = sharedPrefs.getDthStatusFreemium(),
            userIsOnTickTick = sharedPrefs.getSubscribedPack()?.flexiPlan
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<PurchasePackResponse>(){
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    _isSubscriptionFetched.postValue(SingleEvent(true))
                    //setError(error)
                }

                override fun onSuccessResponse(t: PurchasePackResponse) {
                    setProgressing(false)
                    _isSubscriptionFetched.postValue(SingleEvent(true))
                    when(t.code){
                        CODE_SUCCESS -> {
                            sharedPrefs.saveSubscribedPack(t.data,subscriptionAnalytics)
                            updateInpack()
                        }
                        CODE_LOGOUT_ALL -> {
                            setError(ErrorModel(t.code , t.message))
                        }
                    }
//                    setError(ErrorModel(CODE_LOGOUT_ALL , t.message)) //only for testing
                }
            })
    }

    fun generateOtp() {
        setProgressing(true)
        useCase.run {
            generateOtpGuestLogin(rmn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<GetOtpGuestLoginResponse>() {
                    override fun onSuccessResponse(t: GetOtpGuestLoginResponse) {
                        setProgressing(false)
                        if (t.code == CODE_SUCCESS) {
                            if (t.data == null) {
                                _generateOtpResponseError.postValue(SingleEvent(ErrorModel()))
                            } else {
                                _generateOtpResponse.postValue(SingleEvent((t)))
                            }
                        } else {
                            _generateOtpResponseError.postValue(
                                SingleEvent(
                                    ErrorModel(
                                        t.code,
                                        t.message
                                    )
                                )
                            )
                            //handle error codes here, added them fro reference
//                            when (t.code) {
//                                20090 -> {/*Please enter valid mobile number*/
//                                }
//                                10002 -> {/*Error while generating the OTP*/
//                                }
//                                40004 -> {/*Mobile Number cannot be left empty.*/
//                                }
//                            }
                        }
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        _generateOtpResponseError.postValue(SingleEvent(error!!))
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        _searchDisposable = d
                        addDisposable(d)
                    }
                })
        }
    }

    fun validateOtp(isFromAutoFillOtp: Boolean) {
        if (isFromAutoFillOtp) {
            removeDisposable()
            val now: Long = SystemClock.elapsedRealtime()
            if (now - lastClickOnValidateOtpInMillis < 59000) return
            lastClickOnValidateOtpInMillis = now
        }
        setProgressing(true)
        useCase.run {
            validateOtpGuestLogin(
                ValidateOtpGuestLoginRequest(
                    mobileNumber = rmn,
                    otp = otp
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<ValidateOTPResponse>() {
                    override fun onSuccessResponse(t: ValidateOTPResponse) {
                        setProgressing(false)
                        when (t.code) {
                            CODE_SUCCESS -> {
                                if (t.data == null) {
                                    _validateOtpResponseError.postValue(SingleEvent(ErrorModel()))
                                    trackLoginFail(
                                        loginType,
                                        loginAuth,
                                        rmn.ifEmpty { loginDTO.rmn },
                                        t.message ?: "",
                                        loginSource,
                                        t.code.toString()
                                    )

                                } else {
//                                    loginDTO.rmn = rmn
//                                    saveLoginToken(t)
//                                    fetchSubIdList()
//                                    createNewBingeMobileUser(null, rmn)
                                    _validateOtpResponse.postValue(SingleEvent((t)))
//                                    val bingeUser = LoginRequest(
//                                        baId = "5000000541", loginType = loginAuth,
//                                        subscriberId = "3001302474", rmn = "9902853731"
//                                    )
//                                    loginBingeUser(bingeUser = bingeUser)
                                }
                            }
                            else -> {
                                _validateOtpResponseError.postValue(
                                    SingleEvent(
                                        ErrorModel(
                                            t.code,
                                            t.message
                                        )
                                    )
                                )
                                trackLoginFail(
                                    loginType,
                                    loginAuth,
                                    rmn.ifEmpty { loginDTO.rmn },
                                    t.message ?: "",
                                    loginSource,
                                    t.code.toString()
                                )

                            }

                            //handle error codes here, added them fro reference
                            //                            when (t.code) {
                            //                                1 -> {/*Sorry for the inconvenience. We are experiencing some difficulties. Please try again.*/
                            //                                }
                            //                                40004 -> {/*Mobile Number cannot be left empty.*/
                            //                                }
                            //                                40008 -> {/*OTP cannot be left empty.*/
                            //                                }
                            //                                60001 -> {/*The OTP entered is incorrect. Please try again.*/
                            //                                }
                            //                                20090 -> {/*Please enter valid mobile number*/
                            //                                }
                            //                                20084 -> {/*AnonymousId doesn't matched with deviceId*/
                            //                                }
                            //                            }
                        }
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        _validateOtpResponseError.postValue(SingleEvent(error!!))
                        trackLoginFail(
                            loginType,
                            loginAuth,
                            rmn.ifEmpty { loginDTO.rmn },
                            error.message ?: "",
                            loginSource,
                            error.code.toString()
                        )
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        _searchDisposable = d
                        addDisposable(d)
                    }
                })
        }
    }

    fun createNewBingeMobileUser(request: NewBingeUserRequest) {
        setProgressing(true)
        if(DTH_W_BINGE_OLD_USER.equals(request.dthStatus, true))
            request.isCreate = false
        sharedPrefs.tempSaveSid(request.subscriberId ?: "", request.dthStatus?:"")
        isNewUser = request.isCreate
        val subscribeWith = useCase.createNewBingeMobileUser(
            request
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<NewBingeUserResponse>() {
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

                override fun onSuccessResponse(t: NewBingeUserResponse) {
                    setProgressing(false)
                    if (t.code == CODE_SUCCESS) {
                        sharedPrefs.setAppLaunchValueLoggedIn(0)
                        sharedPrefs.setStartLaunchCount(false)
                        if (t.bingeUserData?.primePackDetails != null)
                            sharedPrefs.savePrimePackDetails(t.bingeUserData?.primePackDetails)
                        loginDTO.name = t.bingeUserData?.aliasName
                        loginDTO.email = t.bingeUserData?.emailId
                        if (t.bingeUserData == null) {
                            setError(ErrorModel())
                            trackLoginFail(
                                loginType,
                                loginAuth,
                                if (rmn.isNotEmpty()) rmn else loginDTO.rmn,
                                t.message ?: "",
                                loginSource,
                                t.code.toString()
                            )
                        } else {
                            saveLoggedInDetails(t)
                            t.bingeUserData?.let{
                                _subscriptionStatusInfo = it.subscriptionStatusInfo
                            }
                            fetchProfileInfo()
//                            newBingeUserResponse.postValue(SingleEvent(t))
                            existingBingeUserLogin.postValue(SingleEvent(t))
                        }
                    } else {
                        when (t.code) {
                            LOGIN_MAX_DEVICE_ERROR_CODE -> {
                                /*profileAnalytics.trackSwitchAccountMaxDevice(
                                    sharedPrefs.getOriginalSubscriberId()
                                )*/
                                loginAnalytics.trackLoginMaxDevice(
                                    sharedPrefs.getTempSavedSid()?:""
                                )
                                _maxDeviceLimitReachedResponse.postValue(
                                    SingleEvent(
                                        ErrorModel(
                                            t.code,
                                            t.message,
                                            t.subMessage ?: ""
                                        )
                                    )
                                )
                            }
//                            DTH_ERROR_CODE, DTH_FRESH_IF_ERROR_CODE -> {
//                                tempSid = sid
//                                dthStatusError.postValue(SingleEvent(ErrorModel(t.code, t.message)))
//                            }
//                            FORCE_LOGIN_ERROR_CODE, FORCE_LOGIN_ERROR_CODE2 -> {
//                                forceLoginAgain.postValue(SingleEvent(t.message))
//                            }
                            else -> {
                                trackLoginFail(
                                    loginType,
                                    loginAuth,
                                    if (rmn.isNotEmpty()) rmn else loginDTO.rmn,
                                    t.message ?: "",
                                    loginSource,
                                    t.code.toString()
                                )
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

    fun fetchBalance() {
        val subscribeWith = useCase.fetchBalance(WalletBalanceRequest(sharedPrefs.getBaId()!!)).subscribeOn(Schedulers.io())
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
                    addDisposable(d)
                }
            })
    }

    /**
     * Clevertap Changes
     * selectedProfileSaved.rmn = rmn this value is empty here, hence changed to
     * selectedProfileSaved.rmn =  t.userData?.rmn  line 389
     */
    fun fetchProfileInfo() {
        val rmn = sharedPrefs.getUserDetails()?.rmn ?: ""
        val baId = sharedPrefs.getBaId()
        val originalSID=sharedPrefs.getOriginalSubscriberId()

        val disposable = useCase.getProfileInfo(FetchProfileRequest(baId, rmn))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<SubscriberProfileListModel>() {
                override fun onError(error: ErrorModel?) {

                }

                override fun onSuccessResponse(t: SubscriberProfileListModel) {
                    setProgressing(false)
                    sharedPrefs.setFetchedProfileData(
                        Gson().toJson(
                            t
                        )
                    )
                    if(t.code== CODE_SUCCESS) {
                        t.userData?.let {
                            splashAnalytics.trackUserProfile(it)
                        }
                        val list = t.userData?.languageList?: emptyList()
                        val listLang = ArrayList<String>()
                        list.forEach { listLang.add(it.name) }
                        val selectedProfileSaved = LoginResponse.BingeSubscription()
                        selectedProfileSaved.rmn =  rmn
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
                        //On update of edit profile, the below line will be executed
                        splashAnalytics.updateUserDetails(originalSID,selectedProfileSaved)
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    fun fetchMixpanelUniqueId(referenceId: String) {
        useCase.fetchMixpanelUniqueId(referenceId)
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
                            splashAnalytics.setUserIdentity(it)
                            splashAnalytics.updateUserProperty(MIXPANEL_ID, it)
                        }
                    }
                }
            })
    }

    fun getVerbiageFromConfig() : ConfigResponse.Config? =
        sharedPrefs.getConfigResponse()?.data?.config
}
