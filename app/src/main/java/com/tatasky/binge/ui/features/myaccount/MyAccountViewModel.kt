package com.tatasky.binge.ui.features.myaccount

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.Gson
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.FetchProfileRequest
import com.tatasky.binge.data.networking.models.requests.WalletBalanceRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.interfaces.DeviceDeleteClickListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseViewModel
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.device_management.DeviceListManagementAnalytics
import com.tatasky.binge.ui.features.myaccount.adapter.DeviceAdapter
import com.tatasky.binge.ui.features.sidemenunavdrawer.NavDrawerActions
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.COMMON_ERROR_TITLE
import com.tatasky.binge.utils.DTH_W_BINGE_NEW_USER
import com.tatasky.binge.utils.NON_DTH_USER
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


open class MyAccountViewModel @Inject constructor(private val moEngageHelper: MoEngageHelper, private val useCase: CommonUseCase, open val sharedPrefs: PrefsRepo) : CancellationBaseViewModel(useCase,sharedPrefs) {
    var navDrawerAction = MutableLiveData<SingleEvent<NavDrawerActions>>()
    var onlyMessage: Boolean = false
    var isRenew: Boolean = false

    var currentDeviceId: String = ""

    @Inject
    lateinit var miscAnalytics: MiscAnalytics

    private val profileInfo = MutableLiveData<SingleEvent<SubscriberProfileListModel?>>()
    fun setFromSavedProfileInfo(){
        Gson().fromJson<SubscriberProfileListModel>(
            sharedPrefs.getFetchedProfileData(),
            SubscriberProfileListModel::class.java
        )?.let {
            profileInfo.postValue(SingleEvent(it))
        }
    }
    private val loggedoutState = MutableLiveData<SingleEvent<Boolean>>()
    //    private val profileInfoError = MutableLiveData<SingleEvent<ErrorModel>>()
    private val _walletBalanceResponse = MutableLiveData<SingleEvent<WalletBalanceResponse?>>()
    private val refreshAccountStatus = MutableLiveData<SingleEvent<BaseResponse?>>()
    fun setFromSavedWalletBalance() {
        Gson().fromJson<WalletBalanceResponse>(
            sharedPrefs.getFetchedBalanceData(),
            WalletBalanceResponse::class.java
        )?.let {
            _walletBalanceResponse.postValue(SingleEvent(it))
        }
    }

    private val _unreadNotificationCount = MutableLiveData<SingleEvent<Int>>()
    private val _refreshFlag = MutableLiveData<SingleEvent<Boolean>>()

    @Inject
    lateinit var deviceListManagementAnalytics: DeviceListManagementAnalytics
    fun unreadNotificationCount(): LiveData<SingleEvent<Int>> = _unreadNotificationCount
    fun getProfileInfo(): LiveData<SingleEvent<SubscriberProfileListModel?>> = profileInfo
    //    fun getProfileInfoError(): LiveData<SingleEvent<ErrorModel>> = profileInfoError
    fun getWalletBalance(): LiveData<SingleEvent<WalletBalanceResponse?>> = _walletBalanceResponse
    fun getRefreshAccountStatus(): LiveData<SingleEvent<BaseResponse?>> = refreshAccountStatus
    fun getRefreshFlag(): LiveData<SingleEvent<Boolean>> = _refreshFlag

    private val deviceListResponse = MutableLiveData<SingleEvent<DeviceListResponse>>()
    fun getDeviceListResponse(): LiveData<SingleEvent<DeviceListResponse>> = deviceListResponse
    var isPrimaryDevice = false

    private val _editAliasResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    fun getEditAliasResponse(): LiveData<SingleEvent<BaseResponse>> = _editAliasResponse

    private val _clickedItem = MutableLiveData<SingleEvent<DeviceList>>()

    private val _currentSubscription = MutableLiveData<SingleEvent<PartnerPacks?>>()

    fun getCurrentSubscription() = _currentSubscription

    fun getSubscription() {
        _currentSubscription.postValue(SingleEvent(sharedPrefs.getSubscribedPack()))
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
                    setError(error)
                }

                override fun onSuccessResponse(t: PurchasePackResponse) {
                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS -> {
                            sharedPrefs.saveSubscribedPack(t.data,subscriptionAnalytics)
                            updateInpack()
                        }
                        else -> {
                            setError(ErrorModel(t.code , t.message))
                        }
                    }
                }
            })
    }



    fun getUnreadNotificationCount() {
        val disposable = moEngageHelper.getAllNotificationsList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                var count = 0
                try {
                    val unreadNotificationList = it?.filter { promotionalMessage ->
                        !promotionalMessage.isClicked
                    }
                    count = unreadNotificationList?.size?:0
                } catch (e: Exception) {
                }
                _unreadNotificationCount.postValue(SingleEvent(count))
            }, {
            })
        addDisposable(disposable)
    }

    fun getUnReadCleverTapNotificationCount(context: Context) {
        if (sharedPrefs.getLoginStatus()) {
            var count = 0;
            var messages = CleverTapAPI.getDefaultInstance(context)?.allInboxMessages
            if (messages != null) {
                if (messages.isNotEmpty()) {
                    messages.forEach {
                        if (!sharedPrefs.isNotificationClickedIdExit(it.messageId)) {
                            count += 1
                        }
                    }
                }
            }
            _unreadNotificationCount.postValue(SingleEvent(count))
        } else {
            _unreadNotificationCount.postValue(SingleEvent(0))
        }
    }



    fun fetchProfileInfo() {
        val rmn = sharedPrefs.getUserDetails()?.rmn ?: ""
        val baId = sharedPrefs.getBaId()

        //setProgressing(true)
        val disposable = useCase.getProfileInfo(FetchProfileRequest(baId, rmn))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setProgressing(false)
//            setRetryError(error)
            }.retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                _refreshFlag.postValue(SingleEvent(false))
                retryHandler.zipWith(retrySubject.toFlowable(BackpressureStrategy.LATEST), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }.subscribeWith(object : CallbackWrapper<SubscriberProfileListModel>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    //added this in freemium for navDrawer Caching
                    profileInfo.postValue(
                        SingleEvent(null)
                    )
                }

                override fun onSuccessResponse(t: SubscriberProfileListModel) {
                    setProgressing(false)
                    sharedPrefs.setFetchedProfileData(
                        Gson().toJson(
                            t
                        )
                    )

                    if(t.code== CODE_SUCCESS) {
                        val clearRMN = sharedPrefs.getClearRMN()
                        if (clearRMN.isNotEmpty() && clearRMN != t.userData?.rmn) {
                            sharedPrefs.resetHotstarPopupCount()
                            sharedPrefs.clearHotstarPopupData()
                        }
                        t.userData?.let {
                            splashAnalytics.trackUserProfile(it)
                        }
//                    sharedPrefs.setClearRMN(t.userData?.rmn ?: "")
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
                        profileInfo.postValue(SingleEvent(t))
                    } else {
                        profileInfo.postValue(
                            SingleEvent(null)
                        )
                    }
                }
            })
    }

    private val onDeviceListener = object : DeviceDeleteClickListener {
        override fun onDeviceDelete(item: DeviceList) {
            _clickedItem.postValue(SingleEvent(item))
        }

    }
    private val mAdapter = DeviceAdapter(
        mutableListOf(),
        currentDeviceId,
        onDeviceListener
    )

    fun getAdapter(): RecyclerView.Adapter<*> {
        return mAdapter
    }

    fun getClickedItem(): LiveData<SingleEvent<DeviceList>> {
        return _clickedItem
    }

    // If the tempBaId we are getting in this method is null then
    // it means user is logged in and we already have the Baid saved in our storage
    fun fetchDeviceList(tempBaid: String? /*Target BAID of which to list devices*/) {
        setProgressing(true)
        onlyMessage = false
        val disposable = useCase.getDeviceList(tempBaid ?: sharedPrefs.getBaId()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnError { error ->
            setProgressing(false)
            setRetryError(error, false)
            //setError(error)
        }.retryWhen { retryHandler ->
            retrySubject = PublishSubject.create<Any>()
            retryHandler.zipWith(retrySubject.toFlowable(BackpressureStrategy.LATEST), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
        }.subscribeWith(object : CallbackWrapper<DeviceListResponse>() {
            override fun onError(it: ErrorModel?) {
                setProgressing(false)
                setError(it)
            }

            override fun onSuccessResponse(t: DeviceListResponse) {
                onlyMessage = true
                deviceListResponse.postValue(SingleEvent(t))
                setProgressing(false)
            }
        })
    }

    fun setDeviceList(deviceList: List<DeviceList>, smallDeviceFooterMessage: String?, smallDevicesCount: Int, shouldHideSmallDevicesHeader: Boolean) {
        mAdapter.updateList(deviceList, currentDeviceId, isPrimaryDevice, smallDeviceFooterMessage, smallDevicesCount, shouldHideSmallDevicesHeader)
    }

    private val removeDeviceResponse = MutableLiveData<BaseResponse>()
    fun getRemoveDeviceResponse(): LiveData<BaseResponse> = removeDeviceResponse

    fun removeDevice(deviceId: String, tempBaId: String?, deviceName: String) {
        setProgressing(true)
        // If the tempBaId we are getting in this method is null then
        // it means user is logged in and we already have the Baid saved in our storage
        val disposable = useCase.removeDevice(tempBaId ?: sharedPrefs.getBaId()!!, deviceId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnError { error -> setRetryError(
            error,
            false
        ) }.retryWhen { retryHandler ->
            retrySubject = PublishSubject.create<Any>()
            retryHandler.zipWith(retrySubject.toFlowable(BackpressureStrategy.LATEST), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
        }.subscribeWith(object : CallbackWrapper<BaseResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
                deviceListManagementAnalytics.trackDeviceLimitRemoveError(error?.message?: COMMON_ERROR_TITLE, deviceName,"")
            }

            override fun onSuccessResponse(t: BaseResponse) {
                setProgressing(false)
                removeDeviceResponse.postValue(t)
            }
        })
    }

    @SuppressLint("CheckResult")
    fun editAliasName(baId: String, aliasName: String) {
        setProgressing(true)
        useCase.editAliasName(baId, aliasName).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<BaseResponse>() {
            override fun onError(error: ErrorModel?) {
                setProgressing(false)
                setError(error)
            }

            override fun onSuccessResponse(it: BaseResponse) {
                setProgressing(false)
                _editAliasResponse.postValue(SingleEvent(it))

            }

            override fun onSubscribe(d: Disposable) {
                addDisposable(d)
            }
        })
    }

    private fun updatedDeviceList(deviceList: ArrayList<DeviceList>): ArrayList<DeviceList> {
        var finalList = deviceList.sortedWith(compareBy({ it.deviceName }))
        for (item in finalList) {

        }
        return finalList as ArrayList<DeviceList>
    }

    fun selectDefaultBingeAccount(){
        getSelectBAIDAdapter().setSelected(0)
    }

    fun fetchBalance() {
        setProgressing(false)
        val subscribeWith = useCase.fetchBalance(WalletBalanceRequest(sharedPrefs.getBaId()!!)).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<WalletBalanceResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    //setError(error)
                    _refreshFlag.postValue(SingleEvent(false))
                    //added this in freemium for navDrawer Caching
                    _walletBalanceResponse.postValue(SingleEvent(null))
                }

                override fun onSuccessResponse(t: WalletBalanceResponse) {
                    setProgressing(false)
                    _refreshFlag.postValue(SingleEvent(false))
                    when (t.code) {
                        CODE_SUCCESS -> {
                            sharedPrefs.setFetchedBalanceData(
                                Gson().toJson(
                                    t
                                )
                            )
                            _walletBalanceResponse.postValue(SingleEvent(t))
                        }
                        else -> {
                            //added this in freemium for navDrawer Caching
                            _walletBalanceResponse.postValue(SingleEvent(null))
                            //setError(ErrorModel(t.code, t.message))
                        }
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                    _refreshFlag.postValue(SingleEvent(true))
                }
            })
    }

    fun refreshAccount() {
        var refreshId = if (NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
            || DTH_W_BINGE_NEW_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
        ) sharedPrefs.getBaId()
        else
            sharedPrefs.getOriginalSubscriberId()
        setProgressing(false)
        val subscribeWith = useCase
            .refreshAccount(refreshId, sharedPrefs.getDthStatusFreemium())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
//                    refreshAccountStatus.postValue(SingleEvent(null))
                    _refreshFlag.postValue(SingleEvent(false))

                }

                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    _refreshFlag.postValue(SingleEvent(false))
                    refreshAccountStatus.postValue(SingleEvent(t))
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                    _refreshFlag.postValue(SingleEvent(true))
                }
            })
    }
    fun getUserLoogedIn() = loggedoutState
    fun updateLoggedOutState(isLoggedOut: Boolean) {
        loggedoutState.postValue(SingleEvent(isLoggedOut))
    }
}