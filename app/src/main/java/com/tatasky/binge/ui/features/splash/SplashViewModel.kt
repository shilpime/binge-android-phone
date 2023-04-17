package com.tatasky.binge.ui.features.splash

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.GetAppRatingRequest
import com.tatasky.binge.data.networking.models.response.AppRatingResponse
import com.tatasky.binge.data.networking.models.response.ConfigResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.RESPONSE_CODE_NETWORK_ERROR
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    val commonUseCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {
    val configResponse = MutableLiveData<ConfigResponse>()

   /* init {
        fetchConfigResponse()
    }*/

    @SuppressLint("CheckResult")
    fun getAppRatingEligibility(requestBody: GetAppRatingRequest) {
//        setProgressing(true)
        commonUseCase.getAppRatingEligibility(requestBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<AppRatingResponse>() {
                override fun onError(error: ErrorModel?) {
//                    setProgressing(false)
//                    setError(error)
                }

                override fun onSuccessResponse(t: AppRatingResponse) {
//                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS -> {
                            sharedPrefs.setIsEligibleForAppRating(t.data?.allowRating == true)
                        }
                        else -> {
//                            setError(ErrorModel(t.code , t.message))
                        }
                    }
                }
            })
    }

     fun fetchConfigResponse() {
        //setProgressing(true)
        val disposable = commonUseCase.executeConfig()
            .subscribeOn(Schedulers.io())
            .timeout(10000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error -> handleConfigError(error) }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }.subscribeWith(object : CallbackWrapper<ConfigResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                }

                override fun onSuccessResponse(it: ConfigResponse) {
                    setProgressing(false)
                    configResponse.postValue(it)
                }

            })
    }

    fun handleConfigError(error: Throwable) {
        if (isLoggedIn() && sharedPrefs.isConfigured()) {
            if(error is UnknownHostException)
                setError(ErrorModel(message = error.message, statusCode = RESPONSE_CODE_NETWORK_ERROR))
            else{
                setError(ErrorModel(message = error.message))
            }
        } else {
            setRetryError(error, true)
        }
    }

    fun isLoggedIn() = sharedPrefs.getLoginStatus()
}
