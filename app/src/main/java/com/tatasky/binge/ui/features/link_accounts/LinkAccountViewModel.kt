package com.tatasky.binge.ui.features.link_accounts

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.GetOtpResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.data.networking.models.requests.LoginDTO
import com.tatasky.binge.utils.e
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class LinkAccountViewModel @Inject constructor(
    private val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {

    private var otpResponse = MutableLiveData<SingleEvent<GetOtpResponse>>()
    private var mobileNumber:String?=""
    fun getMobileNumber():String? {
       return sharedPrefs.getUserDetails()?.rmn
    }

    @SuppressLint("CheckResult")
    fun generateOTP(mobileNumber: String) {
        setProgressing(true)
        useCase.generateOTP(LoginDTO().apply {
            rmn = mobileNumber
        },false)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error -> setRetryError(error, false) }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<GetOtpResponse>() {
                override fun onError(error: ErrorModel?) {
                    e("LinkAccountsFragment", "inside errorMessage : " + error)
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(it: GetOtpResponse) {
                    setProgressing(false)
                    if (it.data == null) {
                        setError(ErrorModel(it.code, it.message))
                    } else {
                        otpResponse.postValue(SingleEvent(it))
                    }
                }
            })
    }

}