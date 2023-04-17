package com.tatasky.binge.ui.features.parentalcontrol

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.analytics.CHANGE_PIN
import com.tatasky.binge.analytics.FORGOT_PIN
import com.tatasky.binge.analytics.SETUP_PIN
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.ParentalPinRequest
import com.tatasky.binge.data.networking.models.requests.UpdateAgeRatingRequest
import com.tatasky.binge.data.networking.models.response.AgeRatingsResponse
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ACTION_PIN_CHANGE
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ACTION_PIN_CREATE
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ACTION_PIN_FORGOT
import com.tatasky.binge.ui.features.parentalcontrol.sidemenu.adapter.ParentalControlRatingAdapter
import com.tatasky.binge.ui.features.parentalcontrol.sidemenu.adapter.RatingSelector
import com.tatasky.binge.utils.CODE_SUCCESS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ParentalControlViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo,
) : BaseViewModel() {
    var pinEntrySource: String? = null
    var authTokenValue: String = ""
    var ageRatingValue: String? = null
    var parentalPinValue: String? = null

    var actionBeforeOpeningBottomSheet: String? = null
    var fromNudge : Boolean? = null
    var isParentalControlSuccess: Boolean = false
    val parentalControlBottomDialogResult = MutableLiveData<SingleEvent<String>>()

    private val _ageRatings = MutableLiveData<SingleEvent<AgeRatingsResponse>>()
    val ageRatingsResponse: LiveData<SingleEvent<AgeRatingsResponse>> = _ageRatings

    private val _updateAgeRatingResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    val updateAgeRatingResponse: LiveData<SingleEvent<BaseResponse>> = _updateAgeRatingResponse

    private val _selectedAgeRatingValue = MutableLiveData<SingleEvent<AgeRatingsResponse.AgeRatings>>()
    val selectedAgeRatingValue :LiveData<SingleEvent<AgeRatingsResponse.AgeRatings>> = _selectedAgeRatingValue

    private val _saveParentalPinResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    val saveParentalPinResponse: LiveData<SingleEvent<BaseResponse>> = _saveParentalPinResponse

    private val _saveParentalPinError = MutableLiveData<SingleEvent<ErrorModel>>()
    val saveParentalPinError: LiveData<SingleEvent<ErrorModel>> = _saveParentalPinError

    private val _validateParentalPinResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    val validateParentalPinResponse : LiveData<SingleEvent<BaseResponse>> = _validateParentalPinResponse

    private val _validateParentalPinError = MutableLiveData<SingleEvent<ErrorModel>>()
    val validateParentalPinError: LiveData<SingleEvent<ErrorModel>> = _validateParentalPinError

    private var ratingSelector = object : RatingSelector {
        override fun onRatingSelect(value: AgeRatingsResponse.AgeRatings) {
            _selectedAgeRatingValue.postValue(SingleEvent(value))
        }
    }

    private var parentalControlRatingAdapter =
        ParentalControlRatingAdapter(mutableListOf(), ratingSelector)

    fun getParentalControlRatingAdapter() = parentalControlRatingAdapter

    fun setParentalControlRatingAdapter(list: List<AgeRatingsResponse.AgeRatings>?, selPosition: Int?) {
        if (!list.isNullOrEmpty()) {
            parentalControlRatingAdapter.updateList(list, selPosition)
        }
    }

    fun getSource(actionBeforeOpeningBottomSheet: String?): String =
        when (actionBeforeOpeningBottomSheet) {
            ACTION_PIN_FORGOT -> FORGOT_PIN
            ACTION_PIN_CREATE -> SETUP_PIN
            ACTION_PIN_CHANGE -> CHANGE_PIN
            else -> ""
        }

    fun saveParentalPin(
        parentalPinValue: String,
        otp: String,
        isLogin: Boolean,
        ageRating: String? = sharedPrefs.getParentalRating()?.ageRatingName
    ) {
        setProgressing(true)
        useCase.run {
            saveParentalPin(
                ParentalPinRequest(
                    mobileNumber = sharedPrefs.getClearRMN(),
                    bingeSubscriberId = sharedPrefs.getOriginalSubscriberId(),
                    baId = sharedPrefs.getBaId(),
                    parentalLock = parentalPinValue,
                    otp = otp,
                    isLogin = isLogin,
                    ageRating = ageRating
                ),
                authTokenValue
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                    override fun onSuccessResponse(t: BaseResponse) {
                        setProgressing(false)
                        if (t.code == CODE_SUCCESS) {
                            _saveParentalPinResponse.postValue(SingleEvent(t))
                        } else {
                            setError(
                                ErrorModel(
                                    t.code,
                                    t.message
                                )
                            )
                            _saveParentalPinError.postValue(
                                SingleEvent(
                                    ErrorModel(
                                        t.code,
                                        t.message
                                    )
                                )
                            )
                            //handle error codes here, added them fro reference
//                            when (t.code) {
//                                20096 -> { /*Null or Empty binge subscriber id.*/
//                                }
//                                20097 -> { /*Binge Subscriber not found.*/
//                                }
//                                11002 -> { /*Parental lock code must be of four digit numeric.*/
//                                }
//                                40005 -> { /*RMN must be of 10 digits.*/
//                                }
//                                40008 -> { /*OTP cannot be left empty.*/
//                                }
//                                10003 -> { /*Please enter a 6-digit OTP*/
//                                }
//                                20100 -> { /*No Subscriber profile found against this Subscriber Id*/
//                                }
//                                20009 -> { /*Null or empty ba id.*/
//                                }
//                                20008 -> { /*Invalid ba id.*/
//                                }
//                            }
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

    fun validateParentalPin(parentalPinValue: String) {
        setProgressing(true)
        useCase.run {
            validateParentalPin(
                ParentalPinRequest(
                    mobileNumber = sharedPrefs.getClearRMN(),
                    bingeSubscriberId = sharedPrefs.getOriginalSubscriberId(),
                    baId = sharedPrefs.getBaId(),
                    parentalLock = parentalPinValue
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                    override fun onSuccessResponse(t: BaseResponse) {
                        setProgressing(false)
                        if (t.code == CODE_SUCCESS) {
                            _validateParentalPinResponse.postValue(SingleEvent(t))
                        } else {
                            _validateParentalPinError.postValue(
                                SingleEvent(
                                    ErrorModel(
                                        t.code,
                                        t.message
                                    )
                                )
                            )
                            //handle error codes here, added them fro reference
//                            when (t.code) {
//                                20096 -> { /*Null or Empty binge subscriber id.*/
//                                }
//                                11002 -> { /*Parental lock code must be of four digit numeric.*/
//                                }
//                                20100 -> { /*No Subscriber profile found against this Subscriber Id.*/
//                                }
//                                20009 -> { /*Null or empty ba id.*/
//                                }
//                                20008 -> { /*Invalid ba id.*/
//                                }
//                                11004 -> { /*Inavlid parental lock code.*/
//                                }
//                            }
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

    fun getAgeRatings() {
        setProgressing(true)
        useCase.run {
            getAgeRatings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<AgeRatingsResponse>() {
                    override fun onSuccessResponse(t: AgeRatingsResponse) {
                        setProgressing(false)
                        if (t.code == CODE_SUCCESS) {
                            _ageRatings.postValue(SingleEvent(t))
                        } else {
                            setError(
                                ErrorModel(
                                    t.code,
                                    t.message
                                )
                            )
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

    fun updateAgeRating(parentalLock: String?, ageRating: String?) {
        setProgressing(true)
        useCase.run {
            updateAgeRating(
                UpdateAgeRatingRequest(
                    bingeSubscriberId = sharedPrefs.getOriginalSubscriberId(),//sharedPrefs.getBingeSid()?:sharedPrefs.getLoginResponse()?.bingeSubscriberId,
                    baId = sharedPrefs.getBaId(),
                    parentalLock = parentalLock,
                    ageRating = ageRating,
                    mobileNumber = sharedPrefs.getClearRMN()
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                    override fun onSuccessResponse(t: BaseResponse) {
                        setProgressing(false)
                        if (t.code == CODE_SUCCESS) {
                            _updateAgeRatingResponse.postValue(SingleEvent(t))
                        } else {
                            setError(
                                ErrorModel(
                                    t.code,
                                    t.message
                                )
                            )
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
}