package com.tatasky.binge.ui.features.home.bottomsheet

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.home.adapter.SelectLanguageAdapter
import com.tatasky.binge.ui.features.home.bottomsheet.select_language.models.SaveLanguageBody
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.Profile
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.UserPreferredLanguage
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.preferredLanguageBody
import com.tatasky.binge.utils.INTENT_LANGUAGE
import com.tatasky.binge.utils.REFRESH_HOME
import com.tatasky.binge.utils.e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HomeBottomSheetViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {

    private var mLanguageFetchEnabled: Boolean = true
    private val mShowToastMessage = MutableLiveData<SingleEvent<String>>()
    private val mRefreshLanguageWidgetStatus = MutableLiveData<String>()
    private var mAllLanguages: List<ContentItem>? = null
    private var mUserPreferredLanguages: HashSet<Int>? = null
    private var mMaxLanguageSelectErrorMessage:String? = null
    private val mLanguageAdapter =
        SelectLanguageAdapter(
            sharedPrefs.getCloudenieryUrl()
        ) { isError ->
            if(isError){
                mMaxLanguageSelectErrorMessage?.let {
                    mShowToastMessage.value = SingleEvent(it)
                }
            }else{
                saveLanguages()
            }
        }
    private val mPopulateLanguage = MutableLiveData<SingleEvent<Boolean>>()

    @SuppressLint("CheckResult")
    fun fetchLanguages(fromClassName: String? = null) {
        if (!mLanguageFetchEnabled) return
        mAllLanguages = null
        mUserPreferredLanguages = null
        fetchUserSavedLanguages(fromClassName)
        mLanguageFetchEnabled = false
        setProgressing(true)
        useCase.getLanguageGenreList(INTENT_LANGUAGE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onError(error: ErrorModel?) {
                    setError(error)
                    mLanguageFetchEnabled = true
                }
                override fun onSuccessResponse(homeResponse: RecommendationResponse) {
                    homeResponse.let {
                        it.data?.contentItem?.let { languageList ->
                            mAllLanguages = languageList
                        }
                        mLanguageAdapter.setMaxAllowedSelectedLanguage(it.data?.maxLanguageAllowed)
                        mMaxLanguageSelectErrorMessage = it.data?.maxLanguageErrorMessage
                    }
                    handleSelectedLanguages(fromClassName)
                    mLanguageFetchEnabled = true
                    setProgressing(false)
                }
            })


    }

    @SuppressLint("CheckResult")
    fun saveLanguages(showErrorOnEmptySelection: Boolean = true, lambda: (() -> Unit)? = null) {
        val selectedLanguageList = mLanguageAdapter.getSelectedLanguageItemList()
        val selectedLanguageNamesList = mLanguageAdapter.getSelectedLanguageNameList()
        if (showErrorOnEmptySelection && selectedLanguageList.isEmpty() && mLanguageAdapter.isSaveLanguageButtonAvailable()) {
            mShowToastMessage.postValue(SingleEvent("Please Select one language"))
            return
        }
//        sharedPrefs.setLanguageWidgetVisibility(false)
//        sharedPrefs.setPrefLanguage(selectedLanguageNamesList)
//        lambda?.invoke()
        val saveLanguagesBody = SaveLanguageBody().apply {
            this.baId = sharedPrefs.getBaId()
            this.bingeSubscriberId= sharedPrefs.getOriginalSubscriberId()
            this.languages = selectedLanguageList
            this.mobileNumber = sharedPrefs.getClearRMN()

        }

        val aid = sharedPrefs.getAnonymousId()
        e("CommonViewModel", "aid: $aid")

        setProgressing(true)
        useCase.saveLanguages(
            saveLanguagesBody
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<BaseResponse>() {
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    error?.message?.let {
                        mShowToastMessage.postValue(SingleEvent(it))
                    }

                }

                override fun onSuccessResponse(t: BaseResponse) {
                    setProgressing(false)
                    sharedPrefs.setPrefLanguage(selectedLanguageNamesList)
                    sharedPrefs.setLanguageWidgetVisibility(false)
                    mRefreshLanguageWidgetStatus.postValue(REFRESH_HOME)
                    lambda?.invoke()
                }
            })

    }

    private fun fetchUserSavedLanguages(fromClassName: String?) {
        setProgressing(true)
        useCase.run {
            fetchUserPreferredLanguage(
                preferredLanguageBody(
                    sharedPrefs.getBaId(),
                    sharedPrefs.getOriginalSubscriberId(),
                    sharedPrefs.getClearRMN())
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<UserPreferredLanguage>() {

                    override fun onSuccessResponse(t: UserPreferredLanguage) {
                        setProgressing(false)
                        mUserPreferredLanguages = hashSetOf()
                        var currentProfile:Profile? = null
                        t.data.profileList?.let {
                            for (profile in it) {
                                if (sharedPrefs.getProfileId() == profile.profileId) {
                                    currentProfile = profile
                                    break
                                }
                            }
                        }
                        currentProfile?.let {
                            for (languageItem in it.preferredLanguages) {
                                mUserPreferredLanguages?.add(languageItem.id)
                            }
                        }
                        handleSelectedLanguages(fromClassName)
                    }

                    override fun onError(error: ErrorModel?) {
                        mUserPreferredLanguages = hashSetOf()
                        handleSelectedLanguages(fromClassName)
                        setProgressing(false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }

                })
        }
    }

    fun showToast(): LiveData<SingleEvent<String>> = mShowToastMessage
    fun refreshLanguageWidgetStatus(): LiveData<String> = mRefreshLanguageWidgetStatus
    fun getLanguagePopulateCallback(): LiveData<SingleEvent<Boolean>> = mPopulateLanguage

    fun handleSelectedLanguages(fromClassName: String?) {

        mAllLanguages?.let { languages ->
            mUserPreferredLanguages?.let { preferredLanguages ->
                mLanguageAdapter.setLanguageItemList(languages,preferredLanguages, fromClassName)
                mPopulateLanguage.postValue(SingleEvent(true))
            }
        }
    }


    fun refreshLanguageWidget() {
        mRefreshLanguageWidgetStatus.postValue("")
    }

    fun getLanguageAdapter(): SelectLanguageAdapter {
        return mLanguageAdapter
    }


}