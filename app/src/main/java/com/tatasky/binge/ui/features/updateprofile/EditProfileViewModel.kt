package com.tatasky.binge.ui.features.updateprofile

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.FetchProfileRequest
import com.tatasky.binge.data.networking.models.requests.UpdateEmailRequest
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.ImageUploadResponse
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.SubscriberIdListResponse
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.RESPONSE_CODE_INVALID_EMAIL
import com.tatasky.binge.utils.RESPONSE_CODE_INVALID_FIRST_NAME
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class EditProfileViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {

    var profilePicExists: Boolean = false
    private val _validEmail = MutableLiveData<SingleEvent<Boolean>>()
    private val _validName = MutableLiveData<SingleEvent<Boolean>>()
    private val _editProfileResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    private val _fetchProfileResponse = MutableLiveData<SingleEvent<SubscriberProfileListModel>>()
    fun getEditProfileResponse(): LiveData<SingleEvent<BaseResponse>> = _editProfileResponse

    private val _editImageProfileResponse = MutableLiveData<SingleEvent<ImageUploadResponse>>()
    fun getEditImageProfileResponse(): LiveData<SingleEvent<ImageUploadResponse>> =
        _editImageProfileResponse

    fun getValidEmail(): LiveData<SingleEvent<Boolean>> = _validEmail
    fun getValidName(): LiveData<SingleEvent<Boolean>> = _validName
    fun getFetchProfileInfo(): LiveData<SingleEvent<SubscriberProfileListModel>> =
        _fetchProfileResponse

    fun updateEmailAddress(updateRequest: UpdateEmailRequest) {
        setProgressing(true)
        val notUsable =
            useCase.updateEmail(updateRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                    }

                    override fun onSuccessResponse(t: BaseResponse) {
                        when (t.code) {
                            CODE_SUCCESS -> _editProfileResponse.postValue(SingleEvent(t))
                            RESPONSE_CODE_INVALID_EMAIL -> _validEmail.postValue(SingleEvent(false))
                            else -> setError(ErrorModel(message = t.message))
                        }
                        setProgressing(false)
                    }
                })
    }

    fun updateEmailAndName(updateRequest: UpdateEmailRequest, calledFrom: String /*Header Value*/) {
        setProgressing(true)
        val notUsable =
            useCase.updateEmailAndName(updateRequest, calledFrom)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                    }

                    override fun onSuccessResponse(t: BaseResponse) {
                        when (t.code) {
                            CODE_SUCCESS -> _editProfileResponse.postValue(SingleEvent(t))
                            RESPONSE_CODE_INVALID_EMAIL -> _validEmail.postValue(SingleEvent(false))
                            RESPONSE_CODE_INVALID_FIRST_NAME -> _validName.postValue(
                                SingleEvent(
                                    false
                                )
                            )
                            else -> setError(ErrorModel(message = t.message))
                        }
                        setProgressing(false)
                    }
                })
    }

    @SuppressLint("CheckResult")
    fun updateProfileImage(file: File) {
        setProgressing(true)
        useCase.updateProfileImage(
            createPart(file),
            sharedPrefs.getOriginalSubscriberId() ?: "",
            sharedPrefs.getProfileId() ?: ""
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ImageUploadResponse>() {
                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSuccessResponse(t: ImageUploadResponse) {
                    profilePicExists = true
                    _editImageProfileResponse.postValue(SingleEvent(t))
                    setProgressing(false)
                }
            })
    }

    fun createPart(file: File): MultipartBody.Part {
        var reqFile: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, reqFile)
    }


    fun removeProfileImage() {
        setProgressing(true)
        val disposable = useCase.removeProfileImage(
            sharedPrefs.getOriginalSubscriberId(),
            sharedPrefs.getProfileId()!!
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ImageUploadResponse>() {
                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSuccessResponse(t: ImageUploadResponse) {
                    profilePicExists = false
                    _editImageProfileResponse.postValue(SingleEvent(t))
                    setProgressing(false)
                }
            })
    }

    fun createRequest(email: String, rmn: String?, name: String?): UpdateEmailRequest {
        return UpdateEmailRequest(
            email.trim(),
            rmn ?: "",
            subscriberId = sharedPrefs.getOriginalSubscriberId() ?: "",
            baId = sharedPrefs.getBaId() ?: "",
            name ?: ""
        )
    }

    fun fetchProfileInfo() {
        val rmn = sharedPrefs.getUserDetails()?.rmn ?: ""
        val baId = sharedPrefs.getBaId()

        val disposable = useCase.getProfileInfo(FetchProfileRequest(baId, rmn))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<SubscriberProfileListModel>() {
                override fun onError(error: ErrorModel?) {

                }

                override fun onSuccessResponse(t: SubscriberProfileListModel) {
                    sharedPrefs.setFetchedProfileData(
                        Gson().toJson(
                            t
                        )
                    )
                    if (t.code == CODE_SUCCESS) {
                        _fetchProfileResponse.postValue(SingleEvent(t))
                        val list = t.userData?.languageList ?: emptyList()
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
                        sharedPrefs.setAutoPlayTrailerOn(t.userData?.isTrailerAutoPlay ?: true)
                        sharedPrefs.setAllowWatchNotification(
                            t.userData?.isWatchNotificationEnabled ?: true
                        )
                        sharedPrefs.setAllowTransactionalNotification(
                            t.userData?.isTransactionalNotificationEnabled ?: true
                        )
                    }
                }
            })
    }

    fun getSettingsPageVerbiage(): SubscriberIdListResponse.Settings =
        sharedPrefs.getSubscriberIDListResponse()?.subscribersList?.firstOrNull()?.settings
            ?: SubscriberIdListResponse.Settings(
                editProfile = "Edit Profile",
                videoLang = "Video Languages",
                parentalControl = "Parental Control",
                autoPlay = "Autoplay Trailer",
                notificationSett = "Notification Settings",
                transactionHist = "Transaction History",
                manageDevices = "Manage Devices",
                logout = "Logout",
                loggedIn = "Logged - in Devices",
                choose = "Choose Profile Picture",
                capture = "Capture New",
                from = "From Gallery",
                remove = "Remove Profile Picture",
                close = "close",
                name = "Name",
                email = "Email ID",
                rmn = "Registered Mobile Number"
            )
}
