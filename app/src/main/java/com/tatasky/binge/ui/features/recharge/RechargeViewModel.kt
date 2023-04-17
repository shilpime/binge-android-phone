package com.tatasky.binge.ui.features.recharge

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.WalletBalanceRequest
import com.tatasky.binge.data.networking.models.response.RechargeResponse
import com.tatasky.binge.data.networking.models.response.WalletBalanceResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.CODE_SUCCESS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class RechargeViewModel @Inject constructor(val sharedPrefs: PrefsRepo, private val useCase: CommonUseCase) : BaseViewModel() {
    private val _rechargeResponse = MutableLiveData<SingleEvent<RechargeResponse>>()
    private val _walletBalanceResponse = MutableLiveData<SingleEvent<WalletBalanceResponse>>()

    fun rechargeResponse(): LiveData<SingleEvent<RechargeResponse>> = _rechargeResponse
    fun getWalletBalance(): LiveData<SingleEvent<WalletBalanceResponse>> = _walletBalanceResponse


    @SuppressLint("CheckResult")
    fun startRecharge(subscriberId: String? = null, amount: String) {
        val sid = subscriberId ?: sharedPrefs.getOriginalSubscriberId()
        if (sid.isBlank()) {
            forceLogoutUser()
        } else {
            setProgressing(true)
            useCase.initiateRecharge(sid ?: "", amount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<RechargeResponse>() {
                    override fun onSuccessResponse(t: RechargeResponse) {
                        setProgressing(false)
                        when (t.code) {
                            CODE_SUCCESS -> _rechargeResponse.postValue(SingleEvent(t))
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
    }

    fun fetchBalance() {
        setProgressing(true)
        val subscribeWith = useCase.fetchBalance(WalletBalanceRequest(sharedPrefs.getBaId()!!))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<WalletBalanceResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                }

                override fun onSuccessResponse(t: WalletBalanceResponse) {
                    setProgressing(false)
                    when (t.code) {
                        CODE_SUCCESS -> {
                            _walletBalanceResponse.postValue(SingleEvent(t))
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
}