package com.tatasky.binge.ui.features.update_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.analytics.COMMON_ERROR_MSG
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.ChangePasswordRequest
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.ForgetPasswordResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.UNAUTHORISED_MESSAGE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdatePasswordViewModel @Inject constructor(
    private val useCase: CommonUseCase,
    private val sharedPrefs: PrefsRepo
) : BaseViewModel() {
    @Inject
    lateinit var updatePasswordAnalytics: UpdatePasswordAnalytics

    private var _confirmPassword: String = ""
    private var _newPassword: String = ""
    private var _oldPassword: String = ""
    private lateinit var _mobileNumber: String
    private val _updatePasswordResponse = MutableLiveData<SingleEvent<ForgetPasswordResponse>>()
    fun getUpdatePasswordResponse(): LiveData<SingleEvent<ForgetPasswordResponse>> =
        _updatePasswordResponse

    private val fetchOTPResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    fun fetchOTPResponse(): LiveData<SingleEvent<BaseResponse>> = fetchOTPResponse

    fun callUpdatePasswordAPI() {
        setProgressing(true)
        val changePasswordRequest = ChangePasswordRequest(_newPassword, oldPass = _oldPassword)
        val subscribeWith =
            useCase.changePassword(sharedPrefs.getOriginalSubscriberId()!!, changePasswordRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<ForgetPasswordResponse>() {
                    override fun onError(error: ErrorModel?) {
                        updatePasswordAnalytics.trackUpdatePasswordFailure(
                            error?.message ?: COMMON_ERROR_MSG
                        )
                        setError(error)
                        setProgressing(false)
                    }

                    override fun onSuccessResponse(t: ForgetPasswordResponse) {
                        setProgressing(false)
                        if (t.code == 0)
                            _updatePasswordResponse.postValue(SingleEvent(t))
                        else {
                            setError(ErrorModel(t.code, t.message))
                            updatePasswordAnalytics.trackUpdatePasswordFailure(
                                t.message ?: COMMON_ERROR_MSG
                            )
                        }
                    }
                    override fun onSubscribe(d: Disposable) {
                        addDisposable(d)
                    }
                }
                )
    }

    fun callFetchOTPAPI(rmn: String) {
        fetchOTPResponse.postValue(SingleEvent(BaseResponse()))
    }

    fun onConfirmTextChanged(text: CharSequence) {
        _confirmPassword = text.trim().toString()
    }

    fun onOldTextChanged(text: CharSequence) {
        _oldPassword = text.trim().toString()
    }

    fun onNewTextChanged(text: CharSequence) {
        _newPassword = text.trim().toString()
    }

    fun getOldPassword() = _oldPassword
    fun getNewPassword() = _newPassword
    fun getConfirmPassword() = _confirmPassword
    fun getMobileNumber() = _mobileNumber
    fun setMobileNumber(mobileNumber: String) {
        _mobileNumber = mobileNumber
    }

}