package com.tatasky.binge.ui.features.onboarding.marketing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.MarketingResponse
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.CODE_SUCCESS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MarketingViewModel @Inject constructor(
    val useCase: CommonUseCase
) : BaseViewModel() {
    private val marketingResponse = MutableLiveData<SingleEvent<MarketingResponse>>()

    fun getMarketingList(): LiveData<SingleEvent<MarketingResponse>> = marketingResponse

    fun getMarketingResponse(){
        setProgressing(true)
        val disposable = useCase.fetchMarketingResponse()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<MarketingResponse>() {
                override fun onSuccessResponse(t: MarketingResponse) {
                    when(t.code){
                        CODE_SUCCESS ->{
                            setProgressing(false)
                            if(!t.data?.list.isNullOrEmpty())
                                marketingResponse.postValue(SingleEvent(t))
                            else
                                setError(ErrorModel())
                        }
                        else->{
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
}