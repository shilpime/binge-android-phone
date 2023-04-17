package com.tatasky.binge.ui.features.subscription_freemium.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.NewBingeUserRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.ERROR_CODE_SUBSCRIBER_NOT_FOUND
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ManagedAppViewModel @Inject constructor(
    val sharedPrefs: PrefsRepo,
    private val commonUseCase: CommonUseCase,
    val appsFlyerHelper: AppsFlyerHelper

) : FreemiumSubscriptionViewModel(sharedPrefs, commonUseCase, appsFlyerHelper) {

    private val _managedAppsResponse = MutableLiveData<SingleEvent<ManagedAppResponse>>()
    fun getManagedAppResponse(): LiveData<SingleEvent<ManagedAppResponse>> = _managedAppsResponse
    fun fetchManagedAppsUrl(
        journeySource: String?,
        journeySourceRefId: String?,
        source: String? = "",
    ) {
        Log.e("runtimeRecordApiCallStart", Date().time.toString())

        Log.d("TickTickSource", journeySource ?: "")
        setProgressing(true)
        val rmn = sharedPrefs.getRMN()
        val disposable = commonUseCase.getManagedAppsUrl(
            baid = sharedPref.getBaId(),
            journeySource = journeySource,
            journeySourceRefId = journeySourceRefId,
            mixpanelId = sharedPrefs.getMixPanelId() ?: "",
            source = source,
            rmn = rmn,
            origin = getMixpanelSource(journeySource ?: ""),
            subscriptionType = sharedPrefs.getSubscriptionType(),
            appsFlyerHelper.appsFlyerId,
            BuildConfig.VERSION_NAME
        )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ManagedAppResponse>() {
                override fun onSuccessResponse(t: ManagedAppResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
//                            Log.e("runtimeApiCallResponse", Date().time.toString())

                            setProgressing(false)
                            _managedAppsResponse.postValue(SingleEvent(t))
                        }
                        else -> {
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })

    }

    var managedAppRetryFlag = 0
    private val _managedAppSummeryResponse = MutableLiveData<SingleEvent<ManagedAppResponse>>()
    fun getManagedAppSummeryResponse(): LiveData<SingleEvent<ManagedAppResponse>> =
        _managedAppSummeryResponse

    fun fetchManagedAppSummeryUrl(
        currentJourneyRef: String,
        currentJourneyRefKey: String,
        cartId: String
    ) {
        Log.d("TickTickSource", currentJourneyRef)

        setProgressing(true)
        val disposable = commonUseCase.getManagedAppSummeryUrl(
            sharedPrefs.getBaId(),
            currentJourneyRef,
            currentJourneyRefKey,
            sharedPrefs.getMixPanelId() ?: "",
            cartId,
            appsFlyerHelper.appsFlyerId,
            BuildConfig.VERSION_NAME
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ManagedAppResponse>() {
                override fun onSuccessResponse(t: ManagedAppResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            setProgressing(false)
                            managedAppRetryFlag = 0
                            _managedAppSummeryResponse.postValue(SingleEvent(t))
                        }
                        ERROR_CODE_SUBSCRIBER_NOT_FOUND -> {
                            if (managedAppRetryFlag < 5) {
                                val delay =
                                    sharedPref.getConfigResponse()?.data?.config?.newUserDelay ?: 2
                                Completable.timer(
                                    delay.toLong(),
                                    TimeUnit.SECONDS
                                )
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        fetchManagedAppSummeryUrl(
                                            currentJourneyRef,
                                            currentJourneyRefKey,
                                            cartId
                                        )
                                    }
                                managedAppRetryFlag++
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
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })

    }

    private val _managedAppRefreshTokenResponse = MutableLiveData<SingleEvent<ManagedAppResponse>>()
    fun getManagedAppRefreshTokenResponse(): LiveData<SingleEvent<ManagedAppResponse>> =
        _managedAppRefreshTokenResponse

    fun fetchManagedAppRefreshTokenResponse(
        currentJourneyRef: String,
        currentJourneyRefKey: String,
        cartId: String
    ) {
        setProgressing(true)
        val disposable = commonUseCase.getManagedAppRefreshToken(
            sharedPrefs.getBaId(),
            currentJourneyRef,
            currentJourneyRefKey,
            sharedPrefs.getMixPanelId() ?: "",
            cartId,
            appsFlyerHelper.appsFlyerId,
            BuildConfig.VERSION_NAME
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ManagedAppResponse>() {
                override fun onSuccessResponse(t: ManagedAppResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            setProgressing(false)

                            _managedAppRefreshTokenResponse.postValue(SingleEvent(t))
                        }

                        else -> {
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })

    }

    private val _migrationApiResponse = MutableLiveData<SingleEvent<MigrateUserTickTickResponse>>()
    fun getMigrationResponse(): LiveData<SingleEvent<MigrateUserTickTickResponse>> =
        _migrationApiResponse

    fun fetchMigrateUserResponse(cartId: String) {
        setProgressing(true)
        val disposable = commonUseCase.migrateUserInfo(cartId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<MigrateUserTickTickResponse>() {
                override fun onSuccessResponse(t: MigrateUserTickTickResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            setProgressing(false)

                            _migrationApiResponse.postValue(SingleEvent(t))

                        }

                        else -> {
                            setError(ErrorModel(t.code, t.message))
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })

    }

    private var retryCount: Int = 0
    private val _silentLoginResponse = MutableLiveData<SingleEvent<NewBingeUserResponse>>()
    fun getSilentLoginResponse(): LiveData<SingleEvent<NewBingeUserResponse>> = _silentLoginResponse
    fun fetchSilentLoginResponse(userDetails: MigrateUserTickTickResponse) {
        setProgressing(true)

        sharedPrefs.saveLoginAccessToken(sharedPrefs.getAccessToken())
        sharedPrefs.saveLoginDeviceToken(sharedPrefs.getDeviceToken())
        val request = NewBingeUserRequest(
            login = SOURCE_OTP,
            subscriberId = userDetails.data?.dthSubscriberId
                ?: sharedPrefs.getOriginalSubscriberId(),
            mobileNumber = userDetails.data?.mobileNumber ?: sharedPrefs.getClearRMN(),
            baId = userDetails.data?.baId ?: sharedPrefs.getBaId(),
            isCreate = false,
            dthStatus = userDetails.data?.dthStatus ?: sharedPrefs.getDthStatusFreemium(),
            isPastBingeUser = false,
            referenceId = sharedPrefs.getReferenceId(),
            bingeSubscriberId = userDetails.data?.accountId,
            silentLoginEvent = userDetails.data?.silentLoginEvent
        )
        val subscribeWith = commonUseCase.createNewBingeMobileUser(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<NewBingeUserResponse>() {
                override fun onError(error: ErrorModel?) {
                    //retry 2 times
                    if (retryCount <= 3) {
                        retryCount++
                        fetchSilentLoginResponse(userDetails)
                    } else {
                        sharedPrefs.setSilentLoginCalled(false)
                        setError(error)
                    }
                }

                override fun onSuccessResponse(t: NewBingeUserResponse) {
                    if (t.code == CODE_SUCCESS) {
                        sharedPrefs.saveSilentLoginTimestamp(
                            userDetails.data?.silentLoginTimestamp ?: ""
                        )
                        if (t.bingeUserData?.primePackDetails != null)
                            sharedPrefs.savePrimePackDetails(t.bingeUserData?.primePackDetails)
                        if (t.bingeUserData != null) {
                            saveLoggedInDetails(t)
                        }
                        sharedPrefs.setSilentLoginCalled(false)

                    } else {
                        setError(ErrorModel(t.code, t.message))
                    }
                }


                override fun onSubscribe(d: Disposable) {

                }
            })

    }


    fun getMixpanelSource(journeySource: String): String {
        return when (journeySource) {
            DRAWER_CYOP -> CURATED_PACK_SELECTION
            DRAWER_MYOP -> CURATED_PACK_SELECTION
            HOME_CONTENT -> CONTENT_PLAY
            SCREEN_PLAN -> TICK_NUDGE
            MYPLAN_CHANGE -> MYPLAN_EDIT
            else -> ""
        }
    }


}