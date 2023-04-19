package com.tatasky.binge.ui.features.subscription_freemium.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.ui.features.subscription.view.SubscriptionTenureBottomSheetDialogFragment
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity
import com.tatasky.binge.ui.features.subscription_freemium.PaymentJourneyActivity
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumCombinedAdapter
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumPackListAdapter
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.util.concurrent.TimeUnit


open class FreemiumSubscriptionViewModel @Inject constructor(
    private val sharedPrefs: PrefsRepo,
    private val commonUseCase: CommonUseCase,
    private val appsFlyerHelper: AppsFlyerHelper
) : CancellationBaseViewModel(commonUseCase, sharedPrefs) {

    val sharedPref=sharedPrefs
    private var mToChoosePartnerId: String? = null

    val adapter = FreemiumCombinedAdapter(mutableListOf(),this)
    val adapterNew = FreemiumPackListAdapter(mutableListOf(),this)
    var callFromRenew = false
    var shouldObserveInTenureBottomSheet = false

    private var _fetchPackListResponse = MutableLiveData<SingleEvent<PackListResponse>>()
    fun getPacksResponse(): LiveData<SingleEvent<PackListResponse>> = _fetchPackListResponse


    private val _selectedTenure = MutableLiveData<SingleEvent<Tenure>>()
    fun getSelectedTenure(): LiveData<SingleEvent<Tenure>> = _selectedTenure

    private val _selectedPack = MutableLiveData<SingleEvent<PartnerPacks?>>()
    fun getSelectedPack(): LiveData<SingleEvent<PartnerPacks?>> = _selectedPack


    private val _previouslyUsedMobileNumberResponse =
        MutableLiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>>()
    val previouslyUsedMobileNumberResponse: LiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>> =
        _previouslyUsedMobileNumberResponse

    private val _previouslyUsedMobileNumberError =
        MutableLiveData<SingleEvent<ErrorModel>>()
    val previouslyUsedMobileNumberError: LiveData<SingleEvent<ErrorModel>> =
        _previouslyUsedMobileNumberError

    var fetchedEligiblePackResponse: EligiblePackResponse? = null
    private var _fetchEligiblePackListResponse = MutableLiveData<SingleEvent<EligiblePackResponse>>()
    fun getEligiblePacksResponse(): LiveData<SingleEvent<EligiblePackResponse>> = _fetchEligiblePackListResponse

    private val _packProceedClicked = MutableLiveData<SingleEvent<PartnerPacks>>()
    fun getPackProceedClikced(): LiveData<SingleEvent<PartnerPacks>> = _packProceedClicked

    private lateinit var _mockResponse:EligiblePackResponse

    private var _proRatedResponse = MutableLiveData<SingleEvent<ProRatedResponse>>()
    fun getProRateResponse():LiveData<SingleEvent<ProRatedResponse>> = _proRatedResponse

    fun setPackProceedClicked(pack:PartnerPacks){
        _packProceedClicked.postValue(SingleEvent(pack))
    }

    fun setTenureSelected(tenure:Tenure){
        _selectedTenure.postValue(SingleEvent(tenure))
    }

    fun setPackSelected(pack:PartnerPacks?){
        _selectedPack.postValue(SingleEvent(pack))
    }

    fun getCurrentSubscription(): PartnerPacks? {
        return sharedPref.getSubscribedPack()
    }
    var addPackCalled = false
    var modifyPackCalled = false //This will be also true for Renew use case

    private val _addOrModifyPackResponse = MutableLiveData<SingleEvent<AddPackResponse>>()
    private val _juspayProcessPayload = MutableLiveData<SingleEvent<JSONObject>>()
    private val _walletBalanceResponse = MutableLiveData<SingleEvent<WalletBalanceResponse>>()
    private val _payByDTHBalanceResponse = MutableLiveData<SingleEvent<BaseResponse>>()

    fun getJuspayProcessPayload(): LiveData<SingleEvent<JSONObject>> = _juspayProcessPayload
    fun getWalletBalance(): LiveData<SingleEvent<WalletBalanceResponse>> = _walletBalanceResponse
    fun getAddOrModifyPackResponse() : LiveData<SingleEvent<AddPackResponse>> = _addOrModifyPackResponse
    fun getPayByDTHBalanceResponse(): LiveData<SingleEvent<BaseResponse>> = _payByDTHBalanceResponse

    fun callAddOrModifyPack(cartId: String?, isRenew: Boolean) {
        if (sharedPrefs.getSubscribedPack() == null || sharedPrefs.getSubscribedPack()?.subscriptionStatus.equals(
                SubscriptionPackStatusEnum.DEACTIVE.status,
                true
            ) || (sharedPrefs.getSubscribedPack()?.freeTrialStatus == true)
        )
            addPack(null, if (isRenew) sharedPrefs.getSubscribedPack()?.productId ?: "" else "", "", "", cartId, isRenew)
        else
            modifyPack(null, sharedPrefs.getSubscribedPack()?.productId?:"", "", "", cartId, isRenew)
    }

    fun getPreviouslyUsedMobileNumbers() {
        setProgressing(true)
        commonUseCase.run {
            getPreviouslyUsedMobileNumbers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<PreviouslyUsedMobileNumbersResponse>() {
                    override fun onSuccessResponse(t: PreviouslyUsedMobileNumbersResponse) {
                        setProgressing(false)
                        _previouslyUsedMobileNumberResponse.postValue(SingleEvent((t)))
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        _previouslyUsedMobileNumberError.postValue(SingleEvent(error!!))
                        //setError(error)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
        }
    }

    fun payByDTHBalance(payByDTHBalanceRequest: PayByDTHBalanceRequest) {
        setProgressing(true)
        val subscribeWith = commonUseCase.payByDTHBalance(payByDTHBalanceRequest).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                    _handleErrorResponse.postValue(SingleEvent(ErrorModel(error?.code ?: 0, error?.message)))
                }

                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            _payByDTHBalanceResponse.postValue(SingleEvent(t))
                        }
                        INSUFFICIENT_DTH_BALANCE -> _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                        PAYMENT_FAILURE -> _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                        else -> {
                            setError(ErrorModel(t.code, t.message))
                            _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun fetchBalance(
        proRatedAmount: String?,
        packID: String?,
        callFromRenew: Boolean = false,
        shouldObserveInTenureBottomSheet: Boolean = false,
        fromScreenName: String? = null,
        balanceResponseLambda: ((WalletBalanceResponse?) -> Unit)? = null
    ) {
        this.shouldObserveInTenureBottomSheet  = shouldObserveInTenureBottomSheet
        this.callFromRenew = callFromRenew
        setProgressing(true)
        val subscribeWith = commonUseCase.fetchBalance(WalletBalanceRequest(sharedPref.getBaId()!!,proratedAmount = proRatedAmount, packId = packID)).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<WalletBalanceResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    if (fromScreenName != null &&
                        fromScreenName in setOfAllowedFromScreenNameForDthUserInCaseOfError
                    )
                        _walletBalanceResponse.postValue(SingleEvent(WalletBalanceResponse()))
                    else {
                        setError(error)
                        _handleErrorResponse.postValue(
                            SingleEvent(
                                ErrorModel(
                                    error?.code ?: 0,
                                    error?.message
                                )
                            )
                        )
                    }
                }

                override fun onSuccessResponse(t: WalletBalanceResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            balanceResponseLambda?.invoke(t)
                            sharedPref.setFetchedBalanceData(
                                Gson().toJson(
                                    t
                                )
                            )
                            _walletBalanceResponse.postValue(SingleEvent(t))
                        }
                        else -> {
                            if (fromScreenName != null &&
                                fromScreenName in setOfAllowedFromScreenNameForDthUserInCaseOfError
                            )
                                _walletBalanceResponse.postValue(SingleEvent(WalletBalanceResponse()))
                            else {
                                _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                                setError(ErrorModel(t.code, t.message))
                            }
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    var packValidateRetryFlag = 0

    fun packValidate(
        selectedTenureID: String,
        packID: String?,
        isMigrated: Boolean,
        migratedVerbiage: String?,
        callPackValidate: Boolean = true,
        selectedTenureAmount: String,
        proRatedAmount: String?
    ) {
        setProgressing(true)
        if(isMigrated){
            _handleErrorResponse.postValue(SingleEvent(ErrorModel(message = migratedVerbiage)))
            setError(ErrorModel(message = migratedVerbiage))
        } else if (!callPackValidate){
            if (sharedPref.getSubscribedPack() == null || sharedPref.getSubscribedPack()?.subscriptionStatus.equals(
                    SubscriptionPackStatusEnum.DEACTIVE.status,
                    true
                ) || (sharedPref.getSubscribedPack()?.freeTrialStatus == true)
            )
                addPack(null, selectedTenureID ?: "",selectedTenureAmount,proRatedAmount)
            else
                modifyPack(null, selectedTenureID ?: "",selectedTenureAmount,proRatedAmount)
        }
        else {
            val dis = packID?.let {
                commonUseCase.packValidate(
                    packId = selectedTenureID,
                    baId = sharedPref.getBaId()
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retryWhen { retryHandler ->
                        retrySubject = PublishSubject.create<Any>()
                        retryHandler.zipWith(retrySubject.toFlowable(
                            BackpressureStrategy.LATEST
                        ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
                    }
                    .subscribeWith(object : CallbackWrapper<PackValidationResponse>() {
                        @SuppressLint("CheckResult")
                        override fun onSuccessResponse(t: PackValidationResponse) {
                            when (t.code) {
                                CODE_SUCCESS -> {
                                    setProgressing(false)
                                    if (sharedPref.getSubscribedPack() == null || sharedPref.getSubscribedPack()?.subscriptionStatus.equals(
                                            SubscriptionPackStatusEnum.DEACTIVE.status,
                                            true
                                        ) || (sharedPref.getSubscribedPack()?.freeTrialStatus == true)
                                    )
                                        addPack(
                                            t,
                                            selectedTenureID ?: "",
                                            selectedTenureAmount,
                                            proRatedAmount
                                        )
                                    else
                                        modifyPack(
                                            t,
                                            selectedTenureID ?: "",
                                            selectedTenureAmount,
                                            proRatedAmount
                                        )
                                }
                                ERROR_CODE_SUBSCRIBER_NOT_FOUND -> {
                                    if (packValidateRetryFlag < 5) {
                                        val delay =
                                            sharedPref.getConfigResponse()?.data?.config?.newUserDelay ?: 2
                                        Completable.timer(
                                            delay.toLong(),
                                            TimeUnit.SECONDS
                                        )
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe {
                                                packValidate(
                                                    selectedTenureID,
                                                    packID,
                                                    isMigrated,
                                                    migratedVerbiage,
                                                    callPackValidate,
                                                    selectedTenureAmount,
                                                    proRatedAmount
                                                )
                                            }
                                        packValidateRetryFlag++
                                    } else {
                                        setProgressing(false)
                                        setError(ErrorModel(t.code, t.message))
                                        _handleErrorResponse.postValue(
                                            SingleEvent(
                                                ErrorModel(
                                                    t.code,
                                                    t.message
                                                )
                                            )
                                        )
                                    }
                                }
                                else -> {
                                    setProgressing(false)
                                    setError(ErrorModel(t.code, t.message))
                                    _handleErrorResponse.postValue(
                                        SingleEvent(
                                            ErrorModel(
                                                t.code,
                                                t.message
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        override fun onError(error: ErrorModel?) {
                            setProgressing(false)
                            setError(error)
                            _handleErrorResponse.postValue(SingleEvent(ErrorModel(error?.code ?: 0, error?.message)))
                        }

                    })
            }
        }
    }


    //null or expired {bingeStatus fromsubscriberdetail info }
    fun addPack(
        response: PackValidationResponse?,
        selectedTenureID: String,
        selectedTenureAmount: String,
        proRatedAmount: String?,
        cartId: String? = null,
        isRenew: Boolean = false
    ){
        setProgressing(true)
        addPackCalled = true
        val dis = commonUseCase.addPack(AddPackRequest(
            sid = sharedPref.getOriginalSubscriberId(),
            packId = selectedTenureID,
            baId = sharedPref.getBaId(),
            amount = response?.data?.totalAmount?:selectedTenureAmount,
            startDate = response?.data?.term?.startDate?:"", // from response
            endDate = response?.data?.term?.endDate?:"",  // from response
            language = "ENGLISH", // from response
            deviceType = "ANDROID",
            subscriptionType = sharedPref.getSubscriptionType(),
            appsflyerId = appsFlyerHelper.appsFlyerId,
            cartId = cartId,
            userIsOnTickTick = if (cartId != null) true else sharedPref.getSubscribedPack()?.flexiPlan
        )).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object :CallbackWrapper<AddPackResponse>(){
                override fun onSuccessResponse(t: AddPackResponse) {
                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS -> {
                            _addOrModifyPackResponse.postValue(SingleEvent(t))
                            sharedPref.saveAddModifyResponse(
                                Gson().toJson(
                                    t
                                )
                            )
                            if(t.data?.DTH != true) {
                                t.data?.paymentPayload?.let {
                                    _juspayProcessPayload.postValue(
                                        SingleEvent(
                                            PaymentUtility.createAndGetProcessPayload(it)
                                        )
                                    )
                                }?: run {
                                    // Handle null data
                                    // Handle error on fragment or base framework
                                    setError(
                                        ErrorModel(
                                            t.code,
                                            t.message
                                        )
                                    )
                                    // Handling error on activity
                                    _handleErrorResponse.postValue(
                                        SingleEvent(
                                            ErrorModel(
                                                -1,
                                                COMMON_ERROR_MSG
                                            )
                                        )
                                    )
                                }
                            }
                        }
                        else -> {
                            _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                            setError(ErrorModel(t.code , t.message))
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                    _handleErrorResponse.postValue(SingleEvent(ErrorModel(error?.code ?: 0, error?.message)))
                }
            })
    }

    fun modifyPack(
        response: PackValidationResponse?,
        selectedTenureID: String,
        selectedTenureAmount: String,
        proRatedAmount: String?,
        cartId: String? = null,
        isRenew: Boolean = false
    ){
        setProgressing(true)
        modifyPackCalled = true
        val dis = commonUseCase.modifyPack(AddPackRequest(
            sid = sharedPref.getOriginalSubscriberId(),
            packId = selectedTenureID,
            baId = sharedPref.getBaId(),
            amount = response?.data?.totalAmount?:proRatedAmount?:selectedTenureAmount,
            startDate = response?.data?.term?.startDate?:"", // from response
            endDate = response?.data?.term?.endDate?:"",  // from response
            language = "ENGLISH", // from response
            deviceType = "ANDROID",
            subscriptionType = sharedPref.getSubscriptionType(),
            appsflyerId = appsFlyerHelper.appsFlyerId,
            cartId = cartId,
            userIsOnTickTick = if (cartId != null) true else sharedPref.getSubscribedPack()?.flexiPlan
        )).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object :CallbackWrapper<AddPackResponse>(){
                override fun onSuccessResponse(t: AddPackResponse) {
                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS -> {
                            _addOrModifyPackResponse.postValue(SingleEvent(t))
                            sharedPref.saveAddModifyResponse(
                                Gson().toJson(
                                    t
                                )
                            )
                            if(t.data?.upFrontMoneyCollected == true){
                                //payment succ popup
                            }
                            if(t.data?.DTH != true) {
                                t.data?.paymentPayload?.let {
                                    _juspayProcessPayload.postValue(
                                        SingleEvent(
                                            PaymentUtility.createAndGetProcessPayload(it)
                                        )
                                    )
                                }?: run {
                                    // Handle null data
                                    // Handle error on fragment or base framework
                                    setError(ErrorModel(t.code , t.message))
                                    // Handling error on activity
                                    _handleErrorResponse.postValue(SingleEvent(ErrorModel(-1, COMMON_ERROR_MSG)))
                                }
                            }
                        }
                        else -> {
                            setError(ErrorModel(t.code , t.message))
                            _handleErrorResponse.postValue(SingleEvent(ErrorModel(t.code, t.message)))
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    // Handle error on fragment or base framework
                    setError(error)
                    // Handling error on activity
                    _handleErrorResponse.postValue(SingleEvent(ErrorModel(error?.code ?: -1, error?.message)))
                }
            })
    }




    fun fetchFreemiumCurrentSubscription(){
        setProgressing(true)
        val dis = commonUseCase.getFreemiumCurrentPack(
            baId = sharedPref.getBaId(),
            accountId = sharedPref.getOriginalSubscriberId(),
            freemiumUserType = sharedPref.getDthStatusFreemium(),
            userIsOnTickTick = sharedPrefs.getSubscribedPack()?.flexiPlan
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<PurchasePackResponse>(){
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: PurchasePackResponse) {
                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS -> {
                            sharedPref.saveSubscribedPack(t.data,subscriptionAnalytics)
                            if (!t.data?.subscriptionStatus.isNullOrBlank())
                                updateInpack()
                            else
                                setError(ErrorModel(t.code , t.message))
                        }
                        else -> {
                            setError(ErrorModel(t.code , t.message))
                        }
                    }
                }
            })
    }

    //Freemium Pack listing
    fun fetchEligiblePackList(){
//        fetchedEligiblePackResponse = _mockResponse
//        _fetchEligiblePackListResponse.postValue(SingleEvent(_mockResponse))
//        updateCombinedAdapter()

        setProgressing(true)
        val singleEligiblePackResponse = if(sharedPref.getLoginStatus()){
            commonUseCase.fetchEligiblePackList(
                sharedPref.getBaId(),
                sharedPref.getSubscriptionType()?:""
            )
        } else {
            commonUseCase.fetchEligiblepackForNonLoggedIn()
        }
        val subscribeWith =
            singleEligiblePackResponse.subscribeOn(
                Schedulers.io()
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<EligiblePackResponse>() {
                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                    }

                    override fun onSuccessResponse(t: EligiblePackResponse) {
                        setProgressing(false)
                        if (t.code == 0) {
                            fetchedEligiblePackResponse = t
                            _fetchEligiblePackListResponse.postValue(SingleEvent(t))
                            updateCombinedAdapter()
                        } else setError(ErrorModel(t.code, t.message))
                        setProgressing(false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
    }


    fun setMockResponse(response: EligiblePackResponse) {
        this._mockResponse = response
    }



    private fun updateCombinedAdapter() {
        var selectedPackIndex = -1
        fetchedEligiblePackResponse?.let {eligiblePackresponse ->
            val packList = mutableListOf<PartnerPacks>()
            var lowestPackPriceWithSelectedProvider = Int.MAX_VALUE
            eligiblePackresponse.data?.forEachIndexed { index, it1 ->
                it1.componentList.forEach {  it2 ->
                    if(it2.componentName.equals(it1.productId,true)){
                        packList.add(it1)
                    }
                    if (it2.partnerList.find {it.included ==true && it.partnerId == mToChoosePartnerId } != null
                        && lowestPackPriceWithSelectedProvider > it1.getFormattedPrice()
                    ) {
                        lowestPackPriceWithSelectedProvider = it1.getFormattedPrice()
                        selectedPackIndex = index
                    }
                }
            }
            eligiblePackresponse.data?.let {
                adapter.updateList1(it)
                val x  = mutableListOf<PartnerPacks>()
                x.addAll(it)
                adapterNew.updateList1(x)
            }
            //update selection or focus of recycler view item with selected index
            _updateCardFocus.postValue(SingleEvent(selectedPackIndex))
        }
    }

    private var _updateCardFocus = MutableLiveData<SingleEvent<Int>>()

    fun updateCardFocus() = _updateCardFocus
    private var _cancellationResponse = MutableLiveData<SingleEvent<FreemiumCancellationResponse>>()

    private var _cancellationRevokeResponse = MutableLiveData<SingleEvent<CancellationResponse>>()

    private val _cancelRequest = MutableLiveData<SingleEvent<Cancellation>>()

    fun requestSubscriptionCancellation(cancellationRequest: Cancellation) {
        setProgressing(true)
        val subscribeWith = commonUseCase.freemiumRequestSubscriptionCancellation(
            FreemiumSubscriptionCancellationRequest(
                sharedPref.getOriginalSubscriberId(),
                sharedPref.getBaId(),
                cancellationRequest.primeCancel, cancelBinge = cancellationRequest.bingeCancel
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<FreemiumCancellationResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: FreemiumCancellationResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            _cancellationResponse.postValue(SingleEvent(t))
                            subscriptionAnalytics.trackSubscriptionCancel(t.data?.bingeSubscriptionExpiryDate, t.data?.amazonPrimeVideoExpiryDate)
                        }
                        else -> {
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun requestSubscriptionRevokeCancellation() {
        setProgressing(true)
        val subscribeWith = commonUseCase.requestSubscriptionRevokeCancellation(sharedPref.getBaId()!!).subscribeOn(
            Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<CancellationResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
            }

            override fun onSuccessResponse(t: CancellationResponse) {
                setProgressing(false)
                when (t.code) {
                    CODE_SUCCESS -> {
                        _cancellationRevokeResponse.postValue(SingleEvent(t))
                    }
                    else -> {
                        setError(ErrorModel(t.code, t.message))
                    }
                }
            }

            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                addDisposable(d)
            }
        })
    }

    fun getCancellationResponse(): LiveData<SingleEvent<FreemiumCancellationResponse>> =
        _cancellationResponse

    fun getCancellationRevokeResponse(): LiveData<SingleEvent<CancellationResponse>> =
        _cancellationRevokeResponse

    fun cancelRequest(): LiveData<SingleEvent<Cancellation>> = _cancelRequest

    fun setPacksToCancel(cancelBinge: Boolean, cancelPrime: Boolean) {
        _cancelRequest.postValue(SingleEvent(Cancellation(cancelBinge, cancelPrime)))
    }

    fun getSubscriptionType() = sharedPref.getSubscriptionType()

    var proRatedDis = CompositeDisposable()
    fun fetchProRatedData(pack: PartnerPacks, tenure: Tenure) {
        setProgressing(true)
        proRatedDis.dispose()
        proRatedDis = CompositeDisposable()

        val currentPack = sharedPref.getSubscribedPack()
        var currentPackId:String? = ""
        currentPackId = if(currentPack?.productId.equals("DEFAULT",true)){
            currentPack?.productId
        } else {
            currentPack?.tenure?.let{ tenureList ->
                tenureList.find { tenure ->
                    tenure.currentTenure == true
                }?.tenureId?:currentPack.productId
            }?:currentPack?.productId
        }

        val dis = commonUseCase.proratedBalance(
            ProRatedBalanceRequest(
                accountId = sharedPref.getOriginalSubscriberId(),
                baId = sharedPref.getBaId(),
                currentPackId = currentPackId,
                updatedPackId = tenure.tenureId,
                subscriptionType = sharedPref.getSubscriptionType()
            )
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ProRatedResponse>() {
                override fun onSuccessResponse(t: ProRatedResponse) {
                    setProgressing(false)
                    _proRatedResponse.postValue(SingleEvent(t))
                }
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    proRatedDis.add(d)
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

            })
    }

    fun setToSubscribePartner(partnerId: String) {
        mToChoosePartnerId = partnerId
    }

    companion object {
        //In case of DTH balance fetch error, Below screen will allow user to go on PG
        val setOfAllowedFromScreenNameForDthUserInCaseOfError = setOf(
            SubscriptionTenureBottomSheetDialogFragment::class.java.simpleName,
            PaymentJourneyActivity::class.java.simpleName,
            LandingActivity::class.java.simpleName,
            FreemiumSubscriptionActivity::class.java
        )
    }
}
