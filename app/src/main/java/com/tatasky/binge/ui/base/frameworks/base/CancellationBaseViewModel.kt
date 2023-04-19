package com.tatasky.binge.ui.base.frameworks.base

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.onboarding.login.adapter.BAIDAdapter
import com.tatasky.binge.ui.features.onboarding.login.select_baid.BAIDSelector
import com.tatasky.binge.ui.features.recharge.JuspayInitiationResponse
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.updateprofile.ProfileAnalytics
import com.tatasky.binge.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import javax.inject.Inject


open class CancellationBaseViewModel constructor(
    private val case: CommonUseCase,
    private val sharedPrefs: PrefsRepo
    ): BaseViewModel() {

    var source: String? = null /**After consumption make sure to make it null**/
    @Inject
    lateinit var profileAnalytics: ProfileAnalytics
    @Inject
    lateinit var splashAnalytics: SplashAnalytics
    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    private val _numberOfBingeAccounts = MutableLiveData<SingleEvent<BAIdListResponse>>()
    private var bingeAccountResponse = MutableLiveData<SingleEvent<NewBingeUserResponse>>()
    private var baIdLookupResponse = MutableLiveData<SingleEvent<BAIdListResponse>>()
    private var _switchAccountResponse = MutableLiveData<SingleEvent<SwitchAccountResponse>>()
    private var _deviceForceLogout = MutableLiveData<SingleEvent<Boolean>>()
    private var closePrimeActivity = MutableLiveData<SingleEvent<Unit>>()
    protected val _rechargeResponse = MutableLiveData<SingleEvent<RechargeResponse>>()
    private val _maxDeviceLimitReachedResponse = MutableLiveData<SingleEvent<ErrorModel>>()
    // Handle error response on activity, where base fragment is not used
    protected val _handleErrorResponse = MutableLiveData<SingleEvent<ErrorModel>>()
    private val _juspayInitiatePayload = MutableLiveData<SingleEvent<JSONObject>>()

    fun rechargeResponse(): LiveData<SingleEvent<RechargeResponse>> = _rechargeResponse
    fun deviceForceLogout(): LiveData<SingleEvent<Boolean>> = _deviceForceLogout
    fun getSwitchAccountResponse(): LiveData<SingleEvent<SwitchAccountResponse>> = _switchAccountResponse
    fun getNumberOfBingeAccounts(): LiveData<SingleEvent<BAIdListResponse>> = _numberOfBingeAccounts
    fun getBaLookupResponse(): LiveData<SingleEvent<BAIdListResponse>> = baIdLookupResponse
    fun getBingeAccountResponse() : LiveData<SingleEvent<NewBingeUserResponse>> = bingeAccountResponse
    fun getClosePrimeActivity() : LiveData<SingleEvent<Unit>> = closePrimeActivity
    fun getMaxDeviceLimitReachedResponse(): LiveData<SingleEvent<ErrorModel>> =
        _maxDeviceLimitReachedResponse
    fun getErrorResponse(): LiveData<SingleEvent<ErrorModel>> = _handleErrorResponse
    fun getJuspayInitiatePayload(): LiveData<SingleEvent<JSONObject>> = _juspayInitiatePayload

    fun fetchJuspayInitiatePayload(
        juspayInitiateRequest: HashMap<String, String>,
        showLoader: Boolean = true
    ) {
        if (showLoader)
            setProgressing(true)
        case.run {
            initiateJuspay(if (sharedPrefs.getLoginStatus()) juspayInitiateRequest else null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<JuspayInitiationResponse>() {
                    override fun onSuccessResponse(t: JuspayInitiationResponse) {
                        if (showLoader)
                            setProgressing(false)
                        when(t.code){
                            CODE_SUCCESS -> {
                                _juspayInitiatePayload.postValue(SingleEvent(PaymentUtility.createAndGetInitiatePayload(t)))
                            }
                            else -> {
                                _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
//                                setError(ErrorModel(t.code , t.message))
                            }
                        }
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
//                        setError(error)
                        _handleErrorResponse.postValue(SingleEvent(ErrorModel(error?.code ?: 0, error?.message)))
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
        }
    }

    fun closePrimeActivity(){
        closePrimeActivity.postValue(SingleEvent(Unit))
    }

    private val liveSelectedBingeUser =
        MutableLiveData<SingleEvent<LoginResponse.BingeSubscription>>()

    private var baIdSelector = object : BAIDSelector {
        override fun onSidSelect(baid: LoginResponse.BingeSubscription) {
            liveSelectedBingeUser.postValue(SingleEvent(baid))
        }
    }
    private var baIdAdapter = BAIDAdapter(emptyList(), baIdSelector)
    private fun setBAidAdapter(dataList: List<LoginResponse.BingeSubscription>) {
        baIdAdapter.updateList(dataList, sharedPrefs.getBaId())
    }

    fun getSelectBAIDAdapter() = baIdAdapter
    fun getSelectedBAID(): LiveData<SingleEvent<LoginResponse.BingeSubscription>> =
        liveSelectedBingeUser

    fun fetchBaIdList(sid: String, triggerCancellation : Boolean = true) {
        setProgressing(true)
        val subscribeWith = case.getBaIdList(sid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<BAIdListResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
            }

            override fun onSuccessResponse(t: BAIdListResponse) {
                setProgressing(false)
                if (t.code == 0) {
                    sharedPrefs.saveNumberOfBingeAccount(t.data?.listOfBaIds?.size ?: 1)
                    if((t.data?.listOfBaIds?.size?:0) > 0)
                        setBAidAdapter(t.data?.listOfBaIds?: emptyList())
                    if(triggerCancellation) {
                        baIdLookupResponse.postValue(SingleEvent(t))
                        if(t.data?.listOfBaIds?.size?:0==1){
                            liveSelectedBingeUser.postValue(SingleEvent(t.data?.listOfBaIds?.get(0)!!))
                        }
                    }
                    else
                        _numberOfBingeAccounts.postValue(SingleEvent(t))
                } else {
                    setError(ErrorModel(t.code, t.message))
                }
            }

            override fun onSubscribe(d: Disposable) {
                addDisposable(d)
            }
        })
    }


    fun createBingeAccount(sid:String){
        setProgressing(true)
        val subscribeWith = case.createBingeAccount(sid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object :CallbackWrapper<NewBingeUserResponse>(){
                override fun onSuccessResponse(t: NewBingeUserResponse) {
                    setProgressing(false)
                    if(t.code==0) {
                        bingeAccountResponse.postValue(SingleEvent(t))
                        sharedPrefs.saveDeviceCancellationFlag(false)
                    } else {
                        setError(ErrorModel(t.code, t.message))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }
            })
    }

    @SuppressLint("CheckResult")
    fun switchBAID(baId: String,dsn:String?, targetBaId: String) {
        setProgressing(true)
        case.switchAccount(baId, dsn, targetBaId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<SwitchAccountResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
            }

            override fun onSuccessResponse(it: SwitchAccountResponse) {
                setProgressing(false)
                when (it.code) {
                    CODE_SUCCESS -> {
                        _switchAccountResponse.postValue(SingleEvent(it))
                    }
                    LOGIN_MAX_DEVICE_ERROR_CODE -> {
                        profileAnalytics.trackSwitchAccountMaxDevice(
                            sharedPrefs.getOriginalSubscriberId()
                        )
                        _maxDeviceLimitReachedResponse.postValue(
                            SingleEvent(
                                ErrorModel(
                                    it.code,
                                    it.message,
                                    it.subMessage ?: ""
                                )
                            )
                        )
                    }
                    else -> {
                        setError(ErrorModel(code = it.code, message = it.message))
                    }
                }
            }
            override fun onSubscribe(d: Disposable) {
                addDisposable(d)
            }
        })
    }

    fun saveLoggedInDetails(loginResponse: NewBingeUserResponse) {
        sharedPrefs.setLoginAgain(true)
        sharedPrefs.removeTempToken()
        sharedPrefs.saveDTHAccountStatus(
            loginResponse.bingeUserData?.dthAccountStatus ?: AccountStatusEnum.ACTIVE.status
        )
        loginResponse.bingeUserData?.accountSubStatus?.let{
            sharedPrefs.saveDTHAccountSubStatus(it)
        }

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
        sharedPrefs.saveSubscriptionType(loginResponse.bingeUserData?.subscriptionType ?: "")

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

            try {
                val instance = Gson().fromJson<LoginResponse.BingeSubscription>(
                    sharedPrefs.getAccountDetails(),
                    LoginResponse.BingeSubscription::class.java
                )

                loginResponse.bingeUserData?.rmn = instance.rmn
            }catch (e:Exception){

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
        splashAnalytics.trackUserDetails(
            loginResponse.bingeUserData?.mixpanelid,
            sharedPrefs.getOriginalSubscriberId(),
            loginResponse.bingeUserData,
            sharedPrefs.getAnonymousId(),
            sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
            sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
        )
    }

    @SuppressLint("CheckResult")
    open fun removeDeviceAndSignout() {
        setProgressing(true)
        case.logout(sharedPrefs.getBaId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    _deviceForceLogout.postValue(SingleEvent(true))
                }

                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    _deviceForceLogout.postValue(SingleEvent(true))
                }
            })
    }

    fun getDthStatus() = sharedPrefs.getDthStatus()

    @SuppressLint("CheckResult")
    /*Source from where the user initiated recharge*/
    fun startRecharge(source: String? = null, amount: String? = null) {
        setProgressing(true)
        case.initiateRecharge(sharedPrefs.getOriginalSubscriberId(), amount)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RechargeResponse>() {
                override fun onSuccessResponse(t: RechargeResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            _rechargeResponse.postValue(SingleEvent(t))
//                            splashAnalytics.trackRechargeInitiate(SOURCE_SUBSCRIPTION, "")
                            val fetchBalanceData = sharedPrefs.getFetchedBalanceData()?.let {
                                Gson().fromJson(
                                    sharedPrefs.getFetchedBalanceData(),
                                    WalletBalanceResponse::class.java
                                )
                            }
                            subscriptionAnalytics.trackRechargeInitiate(source ?: "", fetchBalanceData?.data?.recommendedRechargeAmount ?: "")
                        }
                        else -> {
                            setError(ErrorModel(message = t.message))
                            _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                        }
                    }
                }

                override fun onError(errorModel: ErrorModel?) {
                    setProgressing(false)
                    setError(errorModel)
                    _handleErrorResponse.postValue(SingleEvent(ErrorModel(errorModel?.code ?: 0, errorModel?.message)))
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }
    fun checkForManagedAppEligibility(lambda: (Boolean) -> Unit) {
        setProgressing(true)
        case.run {
            checkManagedAppEligibility()
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


    }
}
