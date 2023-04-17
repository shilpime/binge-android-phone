package com.tatasky.binge.ui.features.common

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.pubnub.PubnubHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.home.adapter.CategoriesAdapter
import com.tatasky.binge.ui.features.splash.SplashAnalytics
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 18/11/19.
 */
class CommonSampleViewModel @Inject constructor(
    val useCase: CommonUseCase,
    private val moEngageHelper: MoEngageHelper,
    val database: AppDatabase,
    val subscriptionAnalytics: SubscriptionAnalytics,
    val splashAnalytics: SplashAnalytics,
    val pubnubHelper: PubnubHelper,
    val sharedPrefs: PrefsRepo) :
    BaseViewModel() {

    private lateinit var loginDisposable: Disposable
    var source: String? = null /**After consumption make sure to make it null**/
    private val _previouslyUsedMobileNumberResponse =
        MutableLiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>>()
    private val _previouslyUsedMobileNumberError =
        MutableLiveData<SingleEvent<ErrorModel>>()

    val previouslyUsedMobileNumberResponse: LiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>> =
        _previouslyUsedMobileNumberResponse
    val previouslyUsedMobileNumberError: LiveData<SingleEvent<ErrorModel>> =
        _previouslyUsedMobileNumberError

    val _subscriptionDrawerDisplay =
        MutableLiveData<SingleEvent<Boolean>>()
    val subscriptionDrawerDisplay : LiveData<SingleEvent<Boolean>> = _subscriptionDrawerDisplay

    private val _validateContentRatingResponse =
        MutableLiveData<SingleEvent<ValidateContentRatingResponse>>()
    private val _validateContentRatingError =
        MutableLiveData<SingleEvent<ErrorModel>>()
    private val mCategoriesList =
        MutableLiveData<SingleEvent<List<LeftMenuItem>>>()
    private val mSelectedCategory = MutableLiveData<SingleEvent<Pair<String, String>>>()
    val validateContentRatingResponse: LiveData<SingleEvent<ValidateContentRatingResponse>> =
        _validateContentRatingResponse
    val validateContentRatingError: LiveData<SingleEvent<ErrorModel>> =
        _validateContentRatingError

    private var orientation = MutableLiveData<SingleEvent<OrientationManager.ScreenOrientation>>()
    private var unreadNotificationCount = MutableLiveData<SingleEvent<Int>>()
    fun getLiveNotificationCount(): LiveData<SingleEvent<Int>> = unreadNotificationCount
    private var reselected = MutableLiveData<SingleEvent<Boolean>>()
    private var subscriptionResponse = MutableLiveData<SingleEvent<PurchasePackResponse>>()
    private var homeSelected = MutableLiveData<SingleEvent<Boolean>>()
    private var _deviceForceLogout = MutableLiveData<SingleEvent<Boolean>>()



    var fetchedEligiblePackResponse: EligiblePackResponse? = null
    private var _fetchEligiblePackListResponse = MutableLiveData<SingleEvent<EligiblePackResponse>>()
    fun getEligiblePacksResponse(): LiveData<SingleEvent<EligiblePackResponse>> = _fetchEligiblePackListResponse

    private var subscribeBtnVisibility = MutableLiveData<SingleEvent<Boolean>>()
    fun getSubscribeBtnVisibility(): LiveData<SingleEvent<Boolean>> = subscribeBtnVisibility

    fun setSubscribeBtnVisibilty(b:Boolean){
        subscribeBtnVisibility.postValue(SingleEvent(b))
    }

    private val mCategoriesAdapter =
        CategoriesAdapter(
            mSelectedCategory,sharedPrefs.getCloudenieryUrl()
        )


    private var _paymentStatus = MutableLiveData<SingleEvent<PaymentStatusResponse>>()
    fun getPaymentStatus():LiveData<SingleEvent<PaymentStatusResponse>> = _paymentStatus


    fun isDTHUser() = !NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true)
    fun isLoggedIn() = sharedPrefs.getLoginStatus()
    fun orientationEnabled() = sharedPrefs.orientationEnabled()
    fun getLiveOrientation(): LiveData<SingleEvent<OrientationManager.ScreenOrientation>> = orientation
    fun getReselected(): LiveData<SingleEvent<Boolean>> = reselected
    fun homeSelected(): LiveData<SingleEvent<Boolean>> = homeSelected
    fun getSubscriptionResponse(): LiveData<SingleEvent<PurchasePackResponse>> = subscriptionResponse
    fun deviceForceLogout(): LiveData<SingleEvent<Boolean>> = _deviceForceLogout
    fun getCategoryList(): LiveData<SingleEvent<List<LeftMenuItem>>> = mCategoriesList
    val refreshHome: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData<SingleEvent<Boolean>>()
    private val userResponse = MutableLiveData<SingleEvent<MigrateUserResponse>>()
    val migrateUserResponse : MutableLiveData<SingleEvent<MigrateUserResponse>> = userResponse

    var fakeRefreshHome = MutableLiveData<SingleEvent<Boolean>>()
    fun getFakeRefreshHome():LiveData<SingleEvent<Boolean>> = fakeRefreshHome

    var isCCTOpen = false
    var retryCount = 0

    private var switchAccountAtvResponse = MutableLiveData<SingleEvent<SwitchAccountResponse>>()
    fun getSwitchAccountAtvResponse(): LiveData<SingleEvent<SwitchAccountResponse>> = switchAccountAtvResponse

    fun enableOrientation() = sharedPrefs.setOrientationEnabled(true)
    fun saveOrientation(o: OrientationManager.ScreenOrientation) {
        orientation.postValue(SingleEvent(o))
    }

    var paymentStatusDisposable = CompositeDisposable()
    fun disposePaymentStatus(){
        paymentStatusDisposable.clear()
        paymentStatusDisposable.dispose()
        setProgressing(false)
    }
    fun fetchPaymentStatus(orderId:String){
//        setProgressing(true)
        val subscribeWith =
            useCase.fetchPaymentStatus(
                PaymentStatusRequest(
                    baId = sharedPrefs.getBaId(),
                    paymentTransaction = orderId
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<PaymentStatusResponse>(),Disposable{
                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                    }
                    override fun onSuccessResponse(t: PaymentStatusResponse) {
                        setProgressing(false)
                        _paymentStatus.postValue(SingleEvent(t))
                    }
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        paymentStatusDisposable.add(d)
                        addDisposable(d)
                    }

                    override fun dispose() {

                    }

                    override fun isDisposed(): Boolean {
                        return true
                    }
                })

    }

    fun setTotalUnreadCount() {
        val disposable = moEngageHelper.getAllNotificationsList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                var count = 0
                try {
                    val unreadNotificationsList = it?.filter { promotionalMessage ->
                        !promotionalMessage.isClicked
                    }
                    count = unreadNotificationsList?.size?:0
                } catch (e: Exception) {
                }
                unreadNotificationCount.postValue(SingleEvent(count))
            }, {
            })
        addDisposable(disposable)
    }

    fun setReselectedTab(){
        reselected.postValue(SingleEvent(true))
    }

    fun setHomeSelectedTab(){
        homeSelected.postValue(SingleEvent(true))
    }

    @SuppressLint("CheckResult")
    fun switchAccountAtv(baId:String){
        setProgressing(true)
        useCase.switchAccountAtv(baId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<SwitchAccountResponse>(){
                override fun onSuccessResponse(it: SwitchAccountResponse) {
                    setProgressing(false)
                    if(it.code== CODE_SUCCESS)
                        switchAccountAtvResponse.postValue(SingleEvent(it))
                    else
                        switchAccountAtvResponse.postValue(SingleEvent(SwitchAccountResponse()))
                }
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    switchAccountAtvResponse.postValue(SingleEvent(SwitchAccountResponse()))
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


    fun fetchEligiblePackList(){
        setProgressing(true)
        val singleEligiblePackResponse = if(sharedPrefs.getLoginStatus()){
            useCase.fetchEligiblePackList(
                sharedPrefs.getBaId(),
                sharedPrefs.getSubscriptionType()?:""
            )
        } else {
            useCase.fetchEligiblepackForNonLoggedIn()
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
                        } else setError(ErrorModel(t.code, t.message))
                        setProgressing(false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
    }



    @SuppressLint("CheckResult")
    fun fetchCurrentSubscription(isShowLoader : Boolean) {
        if(sharedPrefs.getDthStatusFreemium().isEmpty()) {
            migrateOldBingeDTHUser(true)
        }
        else {
            val sid = sharedPrefs.getOriginalSubscriberId() ?: ""
            val baId = sharedPrefs.getBaId() ?: ""
            setProgressing(isShowLoader)
            useCase.getFreemiumCurrentPack(
                baId = baId,
                accountId = sid,
                freemiumUserType = sharedPrefs.getDthStatusFreemium(),
                userIsOnTickTick = sharedPrefs.getSubscribedPack()?.flexiPlan
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<PurchasePackResponse>() {
                    override fun onError(error: ErrorModel?) {
                        if (isShowLoader) setProgressing(false)
                        setError(error)
                    }

                    override fun onSuccessResponse(t: PurchasePackResponse) {
                        if (isShowLoader) setProgressing(false)
//                        setError(ErrorModel(CODE_LOGOUT_ALL , t.message)) //only for testing
                        when (t.code) {
                            CODE_SUCCESS -> {
                                if (t.data != null) {
                                    t.data?.let {
                                        if (true == t.data?.forceLogoutHybrid) {
                                            forceDeviceStatusLogoutUser()
                                            return
                                        }
                                        sharedPrefs.saveSubscribedPack(
                                            t.data!!,
                                            subscriptionAnalytics
                                        )
                                    }
                                } else {
                                    sharedPrefs.saveSubscribedPack(null, subscriptionAnalytics)
                                }
                                if (isShowLoader)
                                    subscriptionResponse.postValue(SingleEvent(t))
                                updateInpack()
                            }
                            else -> {
                                setError(ErrorModel(message = t.message, statusCode = t.code))
                            }
                        }
                    }
                })
        }
    }



    @SuppressLint("CheckResult")
    fun removeDeviceAndSignout() {
        setProgressing(true)
        useCase.logout(sharedPrefs.getBaId())
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

    fun deleteAllUnusedToken() {
        val list = database.tokenDao.getAllTokenContents()
        if (list.isNotEmpty()){
            for(laModel in list){
                val diff = (System.currentTimeMillis() - laModel.timestamp!!)
                if (diff > laModel.expiryIn!!)
                    database.tokenDao.deleteTokenContent(laModel.contentId)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun generateAID(createUser:Boolean = false,request:NewBingeUserRequest? = null ,isCurrentAPI : Boolean= false ) {
        val aid = sharedPrefs.getAnonymousId()
        e("CommonViewModel","aid: $aid")
        if(aid != null && !sharedPrefs.getGAuthToken().isNullOrEmpty()) {
            if(createUser)
                createUpdateNewBingeMobileUser(isCurrentAPI,request)
            updateMoE(aid)
            return
        }
        useCase.generateAid()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                if(retryCount < 3){
                    retryCount++
                    generateAID(createUser, request, isCurrentAPI)
                }
                else{
                    //handle login case for Migration
                }
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<AnonymousResponse>() {
                override fun onError(error: ErrorModel?) {
                }
                override fun onSuccessResponse(t: AnonymousResponse) {
                    if(createUser)
                        createUpdateNewBingeMobileUser(isCurrentAPI,request)

                    t.data?.anonymousId?.let {
                        sharedPrefs.saveAnonymousId(it)
                        updateMoE(it)
                    }
                    t.data?.preferredLanguages?.let {
                        sharedPrefs.saveGuestPreferredLanguages(it)
                    }
                    t.data?.profileId?.let {
                        sharedPrefs.saveGuestProfileId(it)
                    }
                    t.data?.gAuthToken?.let {
                        sharedPrefs.saveGAuthToken(it)
                    }
                }
            })
    }

    fun updateMoE(aid: String) {
        if(!sharedPrefs.getLoginStatus()){ //guest
            moEngageHelper.setUniqueId(aid)
            moEngageHelper.updateProperty(ANONYMOUS_ID,aid)
        }
        else{
            //add user-attribute with AID
            moEngageHelper.updateProperty(ANONYMOUS_ID,aid)
        }
//        moEngageHelper.updateProperty(PLATFORM, MOE_PLATFORM)
    }

    var lastClickMillis: Long = 0
    fun getPreviouslyUsedMobileNumbers() {
        val now: Long = SystemClock.elapsedRealtime()
        if (now - lastClickMillis > 1000) {
            loginAPICall()
        }
        lastClickMillis = now
    }

    private fun loginAPICall() {

        setProgressing(true)
        if (::loginDisposable.isInitialized) {
            loginDisposable.dispose()
        }
        useCase.run {
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
                        loginDisposable = d
                        addDisposable(d)
                    }
                })
        }
    }

    fun validateContentRating(contentAgeRatingValue: String?) {
        setProgressing(true)
        useCase.run {
            validateContentRating(
                ValidateContentRatingRequest(
                    baId = sharedPrefs.getBaId(),
                    mobileNumber = sharedPrefs.getClearRMN(),
                    bingeSubscriberId = sharedPrefs.getOriginalSubscriberId(),
                    isLogin = false,
                    contentAgeRating = contentAgeRatingValue
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<ValidateContentRatingResponse>() {
                    override fun onSuccessResponse(t: ValidateContentRatingResponse) {
                        setProgressing(false)
                        _validateContentRatingResponse.postValue(SingleEvent((t)))
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        _validateContentRatingError.postValue(SingleEvent(error!!))
                        setError(error)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
        }
    }

    fun fetchCategories() {
        setProgressing(true)
        useCase.run {
            fetchCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<LeftMenuResponse>() {

                    override fun onSuccessResponse(t: LeftMenuResponse) {
                        setProgressing(false)
                        t.data?.items?.let { categoriesList ->
                            mCategoriesList.postValue(SingleEvent(categoriesList))
                        }
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }

                })
        }
    }



    fun getCategoryAdapter(): CategoriesAdapter {
        return mCategoriesAdapter
    }

    fun setCategoryItemList(categoriesList: List<LeftMenuItem>) {
        mCategoriesAdapter.setCategoryItemList(categoriesList)
    }

    fun getSelectedCategory(): LiveData<SingleEvent<Pair<String, String>>> =
        mSelectedCategory


    @SuppressLint("CheckResult")
    fun migrateOldBingeDTHUser(isCurrentAPI: Boolean) {
        sharedPrefs.removeSubscribedPack()
        useCase.migrateUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                if(retryCount < 3){
                    retryCount++
                    migrateOldBingeDTHUser(isCurrentAPI)
                }
                else {
                    if(!isCurrentAPI)
                        userResponse.postValue(SingleEvent(MigrateUserResponse()))
                }
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<MigrateUserResponse>() {
                override fun onError(error: ErrorModel?) {
                }
                override fun onSuccessResponse(t: MigrateUserResponse) {
                    t.data?.let {
                        //clear current susbcription of BAU
                        sharedPrefs.saveSubscribedPack(null,subscriptionAnalytics)
                        it.gAuthToken?.let { it1 -> sharedPrefs.saveGAuthToken(it1)}
                        it.anonymousId?.let{it1 -> sharedPrefs.saveAnonymousId(it1)}
                        it.mixpanelId?.let { it1 -> sharedPrefs.saveMixPanelId(it1) }
                        it.referenceId?.let { it1 -> sharedPrefs.saveRefrenceId(it1) }
                        it.dthStatus?.let { it1 -> sharedPrefs.saveDTHStatusFreemium(it1) }
                        it.rmn?.let { it1 -> sharedPrefs.setClearRMN(it1) }
                        it.userAuthenticateToken?.let { it1 -> sharedPrefs.setAccessToken(it1) }
                        it.userAuthenticateToken?.let { it1 -> sharedPrefs.saveLoginAccessToken(it1) }
                        sharedPrefs.saveLoginDeviceToken(sharedPrefs.getDeviceToken())
                        val request = NewBingeUserRequest(
                            login = SOURCE_OTP,
                            subscriberId = sharedPrefs.getOriginalSubscriberId(),
                            mobileNumber = sharedPrefs.getClearRMN(),
                            baId = sharedPrefs.getBaId(),
                            isCreate = true,
                            dthStatus = sharedPrefs.getDthStatusFreemium(),
                            isPastBingeUser = it.isPastBingeUser,
                            referenceId = sharedPrefs.getReferenceId()
                        )
                        request.isCreate = it.dthStatus.equals(DTH_WO_BINGE_USER)

                        if(!sharedPrefs.getAnonymousId().isNullOrEmpty())
                            createUpdateNewBingeMobileUser(isCurrentAPI,request)
                        else
                            generateAID(createUser = true, request, isCurrentAPI = isCurrentAPI)

                    } ?: userResponse.postValue(SingleEvent(t))


                }
            })
    }


    fun createUpdateNewBingeMobileUser(isCurrentAPI: Boolean, request: NewBingeUserRequest? = null) {
        if(request != null){
            val subscribeWith = useCase.createNewBingeMobileUser(
                request
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<NewBingeUserResponse>() {
                    override fun onError(error: ErrorModel?) {
                        //logout user
                        if(request.isCreate) {
                            sharedPrefs.removeTempToken()
                            sharedPrefs.setAccessToken("")
                        }
                        if(!isCurrentAPI)
                            userResponse.postValue(SingleEvent(MigrateUserResponse()))
                    }

                    override fun onSuccessResponse(t: NewBingeUserResponse) {
                        if (t.code == CODE_SUCCESS) {
                            sharedPrefs.setAppLaunchValueLoggedIn(0)
                            sharedPrefs.setStartLaunchCount(false)
                            if (t.bingeUserData?.primePackDetails != null)
                                sharedPrefs.savePrimePackDetails(t.bingeUserData?.primePackDetails)
                            if (t.bingeUserData != null) {
                                saveLoggedInDetails(t)
                                if(isCurrentAPI)
                                    fetchCurrentSubscription(false)
                            }
                        }

                        userResponse.postValue(SingleEvent(MigrateUserResponse()))
                    }

                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }
                })
        }
    }

    private fun saveLoggedInDetails(loginResponse: NewBingeUserResponse) {
        loginResponse.bingeUserData?.dthStatus?.let { sharedPrefs.saveDTHStatusFreemium(it) }
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
        splashAnalytics.trackUserDetails(
            loginResponse.bingeUserData?.mixpanelid,
            sharedPrefs.getOriginalSubscriberId(),
            loginResponse.bingeUserData,
            sharedPrefs.getAnonymousId(),
            sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
            sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
        )
        splashAnalytics.updateUserProperty(RMN,sharedPrefs.getClearRMN())
//        trackEventLoginSuccess()
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

    @SuppressLint("CheckResult")
    fun removeAllDevices() {
        setProgressing(true)
        sharedPrefs.setLogoutCalled(true)
        useCase.removeAllDevices(sharedPrefs.getBaId())//changed to BaID as requested by Ashima
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    sharedPrefs.setLogoutCalled(false)
                    _deviceForceLogout.postValue(SingleEvent(false))
                }

                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    sharedPrefs.setLogoutCalled(false)
                    _deviceForceLogout.postValue(SingleEvent(false))
                }
            })
    }


    fun checkForManagedAppEligibility(lambda: (Boolean) -> Unit) {
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


    }

}