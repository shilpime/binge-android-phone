package com.tatasky.binge.ui.features.subscription.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.analytics.COMMON_ERROR_MSG
import com.tatasky.binge.analytics.SOURCE_SUBSCRIPTION
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseViewModel
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.CombinedAdapter
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.SubscriptionPackStatusEnum
import com.tatasky.binge.utils.subscriptionTypeFtv
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(val sharedPref: PrefsRepo, private val commonUseCase: CommonUseCase) : CancellationBaseViewModel(commonUseCase,sharedPref) {


    var isFromLogin: Boolean = false
    var holdCurrentSubscriptionResponse = false
    var firstTimeLogin = false
    var sufficientBalance = true
    private var mToChoosePartnerId: String? = null
    val adapter = CombinedAdapter(mutableListOf(), mutableListOf(), emptySet(),this)
    private lateinit var fetchedPackResponse: PackListResponse
    private val _walletBalanceResponse = MutableLiveData<SingleEvent<WalletBalanceResponse>>()
    private var _fetchPackListResponse = MutableLiveData<SingleEvent<PackListResponse>>()
    private var _cancellationResponse = MutableLiveData<SingleEvent<CancellationResponse>>()
    private var _currentSubscriptionResponse = MutableLiveData<SingleEvent<PurchasePackResponse>>()
    private var _currentSubscriptionResponseAfterCancellation = MutableLiveData<SingleEvent<PurchasePackResponse>>()
    private var _cancellationRevokeResponse = MutableLiveData<SingleEvent<CancellationResponse>>()
    private var _purchasePackResponse = MutableLiveData<SingleEvent<PurchasePackResponse>>()
    private var _trialUpgradeResponse = MutableLiveData<SingleEvent<TrialUpgradeResponse>>()
    private var _disableProceedBtn = MutableLiveData<SingleEvent<Boolean>>()
    fun getWalletBalance(): LiveData<SingleEvent<WalletBalanceResponse>> = _walletBalanceResponse
    private val _cancelRequest = MutableLiveData<SingleEvent<Cancellation>>()
    private val _knowMoreClicked = MutableLiveData<SingleEvent<PartnerPacks.KnowMore>>()
    private var droppedPartnerPacks = hashSetOf<String>()
    private var newAddedPartnerPacks = hashSetOf<String>()
    var selectedPack: PartnerPacks? = null
    var previousPackType: String? = null
    fun fetchPackList(partnerId : String?= null) {
        setProgressing(true)
        val subscribeWith = commonUseCase.fetchPackList(partnerId, sharedPref.getBaId()!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<PackListResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
            }

            override fun onSuccessResponse(t: PackListResponse) {
                setProgressing(false)
                if (t.code == 0) {
                    if(t.data?.packsList.isNullOrEmpty()){
                        setError(ErrorModel())
                    } else {
                        fetchedPackResponse = t
                        _fetchPackListResponse.postValue(SingleEvent(t))
                        updateCombinedAdapter()
                    }
                } else setError(ErrorModel(t.code, t.message))
                setProgressing(false)
            }

            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                addDisposable(d)
            }
        })
    }



    private fun updateCombinedAdapter() {
//        val listOfPackOfferModels = mutableListOf<PackOfferModel>()
//        listOfPackOfferModels.add(PackOfferModel(0, fetchedPackResponse.data?.packsList, null))
//        listOfPackOfferModels.add(PackOfferModel(1, fetchedPackResponse.data?.packsList, null))
//        (fetchedPackResponse.data?.additionalAppsInfo ?: emptyList()).forEach {
//            listOfPackOfferModels.add(PackOfferModel(2, fetchedPackResponse.data?.packsList, it))
//        }
        _knowMoreClicked.value?.getContentIfNotHandled()
        if (getCurrentSubscription()?.isDummyUser == true && true == getCurrentSubscription()?.subscriptionDetailInfo?.bingeAccountStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)) {
            adapter.updateList((fetchedPackResponse.data?.packsList ?: emptyList()).onEach {
                it.knowMoreDetails = null
                it.eligibleFirestick = false
            }, fetchedPackResponse.data?.additionalAppsInfo
                    ?: emptyList(), getCurrentSubscription()?.takeIf { !it.doNotConsiderThePack }?.packId, getCurrentSubscription()?.takeIf { !it.doNotConsiderThePack }?.alternatePaidPackId,
                fetchedPackResponse.data?.packsList?.maxByOrNull { it.getFormattedPrice() }?.appList?.toSet()
                    ?.map { it.providerId!! }
                    ?.toSet()
                    ?: emptySet(), getCurrentSubscription()?.takeIf { it.isDummyUser == false }
                    ?.let { sharedPref.isActivePack() }, setDefaultSelectedPack(), fetchedPackResponse.data?.packsList?.indexOfFirst { it.packId.equals(fetchedPackResponse.data?.defaultPackSelectedId, true) }, fetchedPackResponse.data?.verbiage?.recomendedMessage, true)
        } else {
            adapter.updateList(fetchedPackResponse.data?.packsList
                    ?: emptyList(), fetchedPackResponse.data?.additionalAppsInfo
                    ?: emptyList(), getCurrentSubscription()?.takeIf { !it.doNotConsiderThePack }?.packId, getCurrentSubscription()?.takeIf { !it.doNotConsiderThePack }?.alternatePaidPackId, fetchedPackResponse.data?.packsList?.minByOrNull { it.getFormattedPrice() }?.appList?.toSet()
                    ?.map { it.providerId!! }
                    ?.toSet()
                    ?: emptySet(), getCurrentSubscription()?.takeIf { it.isDummyUser == false }
                    ?.let { sharedPref.isActivePack() }, setDefaultSelectedPack(), fetchedPackResponse.data?.packsList?.indexOfFirst { it.packId.equals(fetchedPackResponse.data?.defaultPackSelectedId, true) }, fetchedPackResponse.data?.verbiage?.recomendedMessage)
        }
    }

    private fun setDefaultSelectedPack(): Int {
        var selectedPackIndex = -1
        var lowestPackPriceWithSelectedProvider = Int.MAX_VALUE
        if (mToChoosePartnerId != null) {
            run loop@{
                fetchedPackResponse.data?.packsList?.forEachIndexed { index, pack ->
                    if (pack.appList.find { it.providerId == mToChoosePartnerId } != null && lowestPackPriceWithSelectedProvider > pack.getFormattedPrice()) {
                        lowestPackPriceWithSelectedProvider = pack.getFormattedPrice()
                        selectedPackIndex = index
                    }
                }
            }
        }
        if (selectedPackIndex == -1) {
            selectedPackIndex = 0
            run loop@{
                fetchedPackResponse.data?.packsList?.forEachIndexed { index, pack ->
                    if (getCurrentSubscription()?.packId in setOf(pack.packId, pack.alternatePaidPackId) || getCurrentSubscription()?.alternatePaidPackId in setOf(pack.packId, pack.alternatePaidPackId)) {
                        selectedPackIndex = index
                        return@loop
                    }
                    if (pack.packId == fetchedPackResponse.data?.defaultPackSelectedId) {
                        selectedPackIndex = index
                    }
                }
            }
        }
        fetchedPackResponse.data?.packsList?.let {
            setSelectedPosition(selectedPackIndex)
        }
        return selectedPackIndex
    }

    fun requestSubscriptionReactivation() {
        setProgressing(true)
        val subscribeWith = commonUseCase.requestSubscriptionReactivation(SubscriptionCreationRequest(sharedPref.getOriginalSubscriberId()!!, sharedPref.getBaId()!!, selectedPack?.packId!!)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
//                subscriptionAnalytics.trackSubscribeFailure(
//                    error?.message ?: COMMON_ERROR_MSG,
//                    selectedPack?.packName ?: "",
//                    selectedPack?.packType ?: "",
//                    "",
//                    ""
//                )
            }

            override fun onSuccessResponse(t: PurchasePackResponse) {
                setProgressing(false)
                _purchasePackResponse.postValue(SingleEvent(t))
            }

            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                addDisposable(d)
            }
        })
    }

    fun requestSubscriptionCreation() {
        setProgressing(true)
        val subscribeWith = commonUseCase.requestSubscriptionCreation(SubscriptionCreationRequest(sharedPref.getOriginalSubscriberId()!!, sharedPref.getBaId()!!, selectedPack?.packId!!)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
//                subscriptionAnalytics.trackSubscribeFailure(
//                    error?.message ?: COMMON_ERROR_MSG,
//                    selectedPack?.packName ?: "",
//                    selectedPack?.packType ?: "",
//                    "",""
//                )
            }

            override fun onSuccessResponse(t: PurchasePackResponse) {
                setProgressing(false)
                _purchasePackResponse.postValue(SingleEvent(t))
            }

            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                addDisposable(d)
            }
        })
    }

    fun requestSubscriptionModification() {
        setProgressing(true)
        val subscribeWith = commonUseCase.requestSubscriptionModification(getCurrentSubscription()?.isCancelled == true ,SubscriptionModificationRequest().apply {
            this.baid = sharedPref.getBaId()
            this.sid = sharedPref.getOriginalSubscriberId()
            this.newAddedPackList = newAddedPartnerPacks.map { SubscriptionModificationRequest.AddPackId(it) }
            this.droppedPackList = droppedPartnerPacks.map { SubscriptionModificationRequest.DropPackId(it) }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
//                subscriptionAnalytics.trackSubscribeFailure(
//                    error?.message ?: COMMON_ERROR_MSG,
//                    selectedPack?.packName ?: "",
//                    selectedPack?.packType ?: "",
//                    "",""
//                )
            }

            override fun onSuccessResponse(t: PurchasePackResponse) {
                setProgressing(false)
                _purchasePackResponse.postValue(SingleEvent(t))
            }

            override fun onSubscribe(d: Disposable) {
                super.onSubscribe(d)
                addDisposable(d)
            }
        })
    }


    fun requestSubscriptionCancellation(cancellationRequest: Cancellation) {
        setProgressing(true)
        holdCurrentSubscriptionResponse()
        val subscribeWith = commonUseCase.requestSubscriptionCancellation(SubscriptionCancellationRequest(
            sharedPref.getBaId(),
            if (cancellationRequest.bingeCancel) getCurrentSubscription()!!.packId!! else null,
            cancellationRequest.primeCancel, cancelBinge = cancellationRequest.bingeCancel))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<CancellationResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: CancellationResponse) {
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
        val subscribeWith = commonUseCase.requestSubscriptionRevokeCancellation(sharedPref.getBaId()!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<CancellationResponse>() {
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

    fun fetchBalance() {
        setProgressing(true)
        val subscribeWith = commonUseCase.fetchBalance(
            WalletBalanceRequest(sharedPref.getBaId(), selectedPack?.packId))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<WalletBalanceResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: WalletBalanceResponse) {
                    setProgressing(false)
                    _walletBalanceResponse.postValue(SingleEvent(t))
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun setSelectedPosition(position: Int) {
        if (::fetchedPackResponse.isInitialized) {
            selectedPack = fetchedPackResponse.data?.packsList?.get(position)
            _disableProceedBtn.postValue(SingleEvent(
                (getCurrentSubscription()?.packId in setOf(selectedPack?.packId, selectedPack?.alternatePaidPackId) || getCurrentSubscription()?.alternatePaidPackId  in setOf(selectedPack?.packId, selectedPack?.alternatePaidPackId))
                        && ((sharedPref.isActivePack()) || true == getCurrentSubscription()?.subscriptionDetailInfo?.subscriptionType?.equals(subscriptionTypeFtv,true))))
        }
    }

    fun getPurchasedPackResponse(): LiveData<SingleEvent<PurchasePackResponse>> =
        _purchasePackResponse

    fun getPacksResponse(): LiveData<SingleEvent<PackListResponse>> = _fetchPackListResponse
    fun getCancellationResponse(): LiveData<SingleEvent<CancellationResponse>> =
        _cancellationResponse

    fun getCancellationRevokeResponse(): LiveData<SingleEvent<CancellationResponse>> =
        _cancellationRevokeResponse

    fun getCurrentSubscriptionResponse(): LiveData<SingleEvent<PurchasePackResponse>> =
        _currentSubscriptionResponse

    fun getUpgradeTrialResponse(): LiveData<SingleEvent<TrialUpgradeResponse>> =
        _trialUpgradeResponse

    fun getKnowMoreClickedDetail(): LiveData<SingleEvent<PartnerPacks.KnowMore>> =
        _knowMoreClicked

    fun setKnowMoreClickedDetail(knowMore: PartnerPacks.KnowMore) {
        _knowMoreClicked.postValue(SingleEvent(knowMore))
    }

    fun getCurrentSubscriptionResponseAfterCancellation(): LiveData<SingleEvent<PurchasePackResponse>> =
        _currentSubscriptionResponseAfterCancellation

    fun getDisableProceed(): LiveData<SingleEvent<Boolean>> = _disableProceedBtn

    fun cancelRequest(): LiveData<SingleEvent<Cancellation>> = _cancelRequest


    fun saveSubscription(packResponse: PartnerPacks?) {
        if (packResponse != null) {
            val selectedProfile = sharedPref.getSelectedProfile()
            selectedProfile?.partnerSubscriptions = packResponse
            selectedProfile?.let {
                sharedPref.setSelectedProfile(it)
            }
            if (packResponse.eligibleFirestick)
                sharedPref.setFirestickDialogShown(false)
            if(packResponse.forceLogoutHybrid){
                forceDeviceStatusLogoutUser()
                return
            }
            sharedPref.saveSubscribedPack(packResponse,subscriptionAnalytics)
        } else {
            sharedPref.saveSubscribedPack(null,subscriptionAnalytics)
        }
        sharedPref.setPackSelectionJourneyCompleted()
    }

    fun getCurrentSubscription(): PartnerPacks? {
        return sharedPref.getSubscribedPack()
    }

    fun fetchCurrentSubscription() {
        setProgressing(true)
        val dis = commonUseCase.getFreemiumCurrentPack(accountId = sharedPref.getOriginalSubscriberId(), baId = sharedPref.getBaId(), freemiumUserType = sharedPref.getDthStatusFreemium(), userIsOnTickTick = null)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: PurchasePackResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            if(true==t.data?.forceLogoutHybrid){
                                forceDeviceStatusLogoutUser()
                                return
                            }
                            sharedPref.saveSubscribedPack(t.data,subscriptionAnalytics)
                            updateInpack()
                        }
                        else -> {
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }
            })
    }

    fun fetchCurrentSubscriptionAfterCancellation() {
        setProgressing(true)
        val dis = commonUseCase.getFreemiumCurrentPack(
            accountId = sharedPref.getOriginalSubscriberId(),
            baId = sharedPref.getBaId(),
            freemiumUserType = sharedPref.getDthStatusFreemium(),
            userIsOnTickTick = sharedPref.getSubscribedPack()?.flexiPlan
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: PurchasePackResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            sharedPref.saveSubscribedPack(t.data,subscriptionAnalytics)
                            _currentSubscriptionResponseAfterCancellation.postValue(SingleEvent(t))
                        }
                        else -> {
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }
            })
    }

    fun dropPack(packId: String) {
        droppedPartnerPacks.add(packId)
    }

    fun addNewPack(packId: String) {
        newAddedPartnerPacks.add(packId)
    }

    fun clearAddAndDrop() {
        newAddedPartnerPacks.clear()
        droppedPartnerPacks.clear()
    }

    fun setPacksToCancel(cancelBinge: Boolean, cancelPrime: Boolean) {
        _cancelRequest.postValue(SingleEvent(Cancellation(cancelBinge, cancelPrime)))
    }

    fun setToSubscribePartner(partnerId: String) {
        mToChoosePartnerId = partnerId
    }

    @SuppressLint("CheckResult")
    fun startRecharge(amount: String) {
        setProgressing(true)
        commonUseCase.initiateRecharge(sharedPref.getOriginalSubscriberId() ?: "", amount)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RechargeResponse>() {
                override fun onSuccessResponse(t: RechargeResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            _rechargeResponse.postValue(SingleEvent(t))
                            subscriptionAnalytics.trackRechargeInitiate(SOURCE_SUBSCRIPTION, amount)
                        }
                        else -> {
                            setError(ErrorModel(message = t.message))
                        }
                    }
                }

                override fun onError(errorModel: ErrorModel?) {
                    setProgressing(false)
                    setError(errorModel)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

            })
    }

    fun isCurrentSubscribedPackActive() = sharedPref.isActivePack()
    fun getMaximumRechargeAmount() = sharedPref.getMaxRechargeAmount()
    fun isFsTaken() = sharedPref.isFirestickTaken()
//    fun isModifyButtonEnabled() = (sharedPref.getBingeButtonsEligibility()[sharedPref.getSubscriptionType()]?:PackButton()).modifyButton
//    fun isCancelButtonEnabled() = (sharedPref.getBingeButtonsEligibility()[sharedPref.getSubscriptionType()]?:PackButton()).cancelButton
    fun getSubscriptionType() = sharedPref.getSubscriptionType()

    fun selectFireStickPack() : Boolean{
        if (::fetchedPackResponse.isInitialized) {
            fetchedPackResponse.data?.packsList?.forEachIndexed { index, partnerPacks ->
                if(partnerPacks.eligibleFirestick){
                    subscriptionAnalytics.trackUpSellConverted(selectedPack?.packPrice?:"", partnerPacks.packPrice?:"")
                    setSelectedPosition(index)
                    return true
                }
            }
        }
        return false
    }

    fun fetchTrialUpgradeDetails() {
        setProgressing(true)
        val subscribeWith = commonUseCase.fetchTrialUpgradeDetails(
            UpgradeTrialRequest(
                getCurrentSubscription()?.packId ?: "", sharedPref.getOriginalSubscriberId(), sharedPref.getBaId()
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<TrialUpgradeResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: TrialUpgradeResponse) {
                    setProgressing(false)
                    if (t.code == 0 /*&& !t.data?.upgradedProviders.isNullOrEmpty() && !t.data?.basePackProviders.isNullOrEmpty()*/) {
                        _trialUpgradeResponse.postValue(SingleEvent(t))
                    } else if(t.code != 0){
                        setError(ErrorModel(t.code, t.message))
                    } else {
                        setError(ErrorModel())
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    private fun holdCurrentSubscriptionResponse() {
        holdCurrentSubscriptionResponse = true
    }

    fun releaseCurrentSubscriptionResponse() {
        holdCurrentSubscriptionResponse = false
        updateInpack()
    }
}