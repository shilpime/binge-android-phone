package com.tatasky.binge.ui.features.more

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.HelpCenterResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.myaccount.MyAccountViewModel
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.utils.CODE_SUCCESS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import com.clevertap.android.sdk.CleverTapAPI
import android.content.Context
import java.util.Date

/**
 * Created by Srikant Karnani on 18/11/19.
 */
@SuppressLint("CheckResult")
open class SettingsViewModel @Inject constructor(
    moEngageHelper: MoEngageHelper,
    private val commonUseCase: CommonUseCase,
    override val sharedPrefs: PrefsRepo
) : MyAccountViewModel(
    moEngageHelper = moEngageHelper,
    useCase = commonUseCase,
    sharedPrefs = sharedPrefs
) {
    var deviceId : String = ""

    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    @Inject
    lateinit var moreAnalytics : MoreAnalytics

    private val _faqResponse = MutableLiveData<SingleEvent<HelpCenterResponse>>()
    private val _signoutResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    private val _toggleSetting = MutableLiveData<SingleEvent<String>>()
    fun getSignoutResponse(): LiveData<SingleEvent<BaseResponse>> = _signoutResponse
    fun getToggledSetting(): LiveData<SingleEvent<String>> = _toggleSetting

    override fun removeDeviceAndSignout() {
        setProgressing(true)
        sharedPrefs.setLogoutCalled(true)
        val disposable = commonUseCase.logout(sharedPrefs.getBaId()!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    setError(error)
                    loginAnalytics.trackLogoutFailed(error?.message ?: "")
//                    moreAnalytics.trackLogoutFailed(error?.message?:"")
                }

                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    if(t.code == CODE_SUCCESS) {
                        sharedPrefs.setLogoutCalled(false)
                        _signoutResponse.postValue(SingleEvent(t))
                    } else {
                        sharedPrefs.setLogoutCalled(false)
                        loginAnalytics.trackLogoutFailed(t.message ?: "")
//                        moreAnalytics.trackLogoutFailed(t.message?:"")
                    }
                }
            })
    }


    fun toggleSetting(settingType:String){
        setProgressing(true)
        commonUseCase.changeSetting(sharedPrefs.getBaId(),settingType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS->{
                            _toggleSetting.postValue(SingleEvent(settingType))
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
    fun getFaqResponse(): LiveData<SingleEvent<HelpCenterResponse>> = _faqResponse
    fun fetchFaqs() {
        setProgressing(true)
        val disposable = commonUseCase.getHelpCenterURL()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<HelpCenterResponse>() {
                override fun onSuccessResponse(t: HelpCenterResponse) {
                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS->{
                            setProgressing(false)
                            t.data?.helpCenterTokenTimeStamp=Date().time
                            _faqResponse.postValue(SingleEvent(t))
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
    fun deleteAllCleverTapNotifications(context: Context) {
        CleverTapAPI.getDefaultInstance(context)?.apply {
            allInboxMessages?.forEach {
                deleteInboxMessage(it.messageId)
            }
        }
    }

    fun setTextToMenuItems() = sharedPrefs.getConfigResponse()?.data?.config?.hamburger
}
