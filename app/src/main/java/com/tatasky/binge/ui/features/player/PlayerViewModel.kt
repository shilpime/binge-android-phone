package com.tatasky.binge.ui.features.player

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.EpisodeRequest
import com.tatasky.binge.data.networking.models.requests.LionsgateRequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.data.networking.models.requests.PartnerContentAnalyticsRequest
import com.tatasky.binge.data.networking.models.requests.PlanetMarathiAnalyticsRequest
import com.tatasky.binge.learnactions.LearnActionHelper
import com.tatasky.binge.lionsgatehelper.LionsgateAnalyticsBody
import com.tatasky.binge.shemaroo.helper.ShemarooAnalyticsBody
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.details.DetailViewModel
import com.tatasky.binge.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

open class PlayerViewModel @Inject constructor(
    val mUseCase: CommonUseCase,
    val mDatabase: AppDatabase,
    val mSharedPrefs: PrefsRepo,
    val learnActionHelper: LearnActionHelper
) : DetailViewModel(mUseCase, mDatabase,
    mSharedPrefs, learnActionHelper) {
//    private var mShemarooAnalyticsBody = ShemarooAnalyticsBody()

    private val _previouslyUsedMobileNumberResponse =
        MutableLiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>>()
    private val _previouslyUsedMobileNumberError =
        MutableLiveData<SingleEvent<ErrorModel>>()
    private val _playerProgressListener = MutableLiveData<Boolean>()
    private val _playerModelListener = MutableLiveData<SingleEvent<PlayerModel>>()
    private val _nextEpisodePlay = MutableLiveData<SingleEvent<ContentItem>>()
    private val _nextPreviousEpisodeResponse =
        MutableLiveData<SingleEvent<NextPreviousEpisodeResponse>>()
    private val _tvodPurchaseExpiryResponse = MutableLiveData<SingleEvent<PlaybackExpiryResponse>>()
    var timer: CountDownTimer? = null

    val previouslyUsedMobileNumberResponse: LiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>> =
        _previouslyUsedMobileNumberResponse

    val previouslyUsedMobileNumberError: LiveData<SingleEvent<ErrorModel>> =
        _previouslyUsedMobileNumberError

    fun getPlaybackExpiry(): LiveData<SingleEvent<PlaybackExpiryResponse>> =
        _tvodPurchaseExpiryResponse

    fun getNextPreviousEpisodeDetails(): LiveData<SingleEvent<NextPreviousEpisodeResponse>> =
        _nextPreviousEpisodeResponse

    fun getPlayerModel(): LiveData<SingleEvent<PlayerModel>> = _playerModelListener
    fun getNextEpisodePlayItem(): LiveData<SingleEvent<ContentItem>> = _nextEpisodePlay

    private val _vootPwaDeeplinkUrl = MutableLiveData<SingleEvent<String>>()
    fun getVootPwaTokenResponse():LiveData<SingleEvent<String>> = _vootPwaDeeplinkUrl

    private val lionsgateToken = MutableLiveData<SingleEvent<LionsGateResponse>>()
    fun getLionsgateTokenResponse():LiveData<SingleEvent<LionsGateResponse>> = lionsgateToken

    init {
        showLoader()
    }

    fun showLoader() {
        setPlayerProgressing(true)
    }

    fun hideLoader() {
        setPlayerProgressing(false)
    }

    private fun setPlayerProgressing(boolean: Boolean) {
        _playerProgressListener.postValue(boolean)
    }

    val playerProgressListener: LiveData<Boolean>
        get() = _playerProgressListener

    fun fetchNextAndPreviousEpisode(episodeId: String) {
        setBottomSheetProgressing(true)
        var disposable = mUseCase.fetchNextAndPreviousEpisodeDetails(
            EpisodeRequest(
                episodeId,
                sharedPrefs.getProfileId(),
                sharedPrefs.getOriginalSubscriberId()
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    setBottomSheetProgressing(false)
                    _nextPreviousEpisodeResponse.postValue(SingleEvent(it))
                },
                {
                    setBottomSheetProgressing(false)
                    e("fetchNextAndPreviousEpisode",it.localizedMessage)
                })
        addDisposable(disposable)
    }

    fun startPlayer(playerModel: PlayerModel) {
        _playerModelListener.postValue(SingleEvent(playerModel))
    }

    fun callPurchaseExpiryAPI(contentId: String) {
        val disposable = mUseCase.contentPlaybackExpiry(contentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _tvodPurchaseExpiryResponse.postValue(SingleEvent(it))
                },
                {
                })
        addDisposable(disposable)
    }

    fun callShemaroomeAnalyticsPlayEvent(
        it: PlayerModel,
        eventName : String,
        eventAttr : String,
        eventValue : Int
    ) {
        val language = if(it.getAudioLanguages()?.size?:0 > 0 ) it.getAudioLanguages()?.get(0)
        else ""
        val genre = if(it.getGenre()?.size?:0 > 0 ) it.getGenre()?.get(0)
        else ""

        val shemaroAnalyticsInfo = sharedPrefs.getPartnerDetail(
            PROVIDER_SHEMAROO,
            sharedPrefs.getSubscribedPack()?.analyticsInfo
        )
        if(shemaroAnalyticsInfo?.enabled == true &&
            shemaroAnalyticsInfo?.partnerDeviceId != null &&
            shemaroAnalyticsInfo?.partnerUserId != null ) {
            val mShemarooAnalyticsBody =
                ShemarooAnalyticsBody(
                    _id = shemaroAnalyticsInfo?.partnerUserId
                        ?: "",//shemarooMe Analytics Id from partner info or new key inside current subscription api
                    e_n = eventName,
                    e_a = eventAttr,
                    e_v = eventValue,
                    pvId = System.currentTimeMillis(),
                    rand = System.currentTimeMillis(),
                    url = it.getSmartUrl(),
                    dimension2 = language ?: "",//language
                    dimension5 = genre ?: "",//genre
                    providerContentId = it.getProviderContentId() ?: "",//providerContentId
                    mediaTitle = it.getTitle() ?: "",
                    totalDuration = it.getTotalDuration(),
                    idsite = shemaroAnalyticsInfo.idSiteValue,
                    deviceId = shemaroAnalyticsInfo?.partnerDeviceId
                        ?: ""//dsn from shemaroo analytics key from current subscription

                )
            val disposable = mUseCase.shemarooMeAnalytics(mShemarooAnalyticsBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        //inside success
                    },
                    {
                        //inside failure
                    })
            addDisposable(disposable)
        }
    }


    fun callEpiconAnalyticsPlayEvent(playerModel: PlayerModel, playerDuration : Long) {
        val epiconAnalyticsInfo = sharedPrefs.getPartnerDetail(
            PROVIDER_EPIC_ON,
            sharedPrefs.getSubscribedPack()?.analyticsInfo
        )
        if(epiconAnalyticsInfo?.enabled == true &&
            epiconAnalyticsInfo?.partnerDeviceId != null &&
            epiconAnalyticsInfo?.partnerUserId != null ) {
            val epiconAnalyticsBody =
                PartnerContentAnalyticsRequest(
                    subscriberID = sharedPrefs.getOriginalSubscriberId(),
                    title = playerModel.getTitle(),
                    contentType = playerModel.getContentType(),
                    partnerContentType = playerModel.getPartnerContentType(),
                    partnerUserID = epiconAnalyticsInfo.partnerUserId,
                    providerName = playerModel.getProvider(),
                    providerContentID = playerModel.getProviderContentId(),
                    duration = playerDuration
                )
            val disposable = mUseCase.hitEpiconAnalytics(deviceId = epiconAnalyticsInfo.partnerDeviceId!!,
                body = epiconAnalyticsBody
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        //inside success
                    },
                    {
                        //inside failure
                    })
            addDisposable(disposable)
        }
    }

    fun getPreviouslyUsedMobileNumbers() {
        setProgressing(true)
        mUseCase.run {
            getPreviouslyUsedMobileNumbers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<PreviouslyUsedMobileNumbersResponse>() {
                    override fun onSuccessResponse(t: PreviouslyUsedMobileNumbersResponse) {
                        setProgressing(false)
                        _previouslyUsedMobileNumberResponse.postValue(SingleEvent((t)))
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        _previouslyUsedMobileNumberError.postValue(SingleEvent(error!!))
                        //setError(error)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
        }
    }


    fun checkForGuestUserPlaybackEligibility(lambda :(Boolean)->Unit) {
        setProgressing(true)
        mUseCase.run {
            checkForGuestUserPlaybackEligibility()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<UserPlaybackEligibilityResponse>() {

                    override fun onSuccessResponse(t: UserPlaybackEligibilityResponse) {
                        setProgressing(false)
                        lambda.invoke(t.data.contentPlayBackAllowed)

                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        lambda.invoke(false)
                        //setError(error)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
        }
    }


    fun setNextEpisodeToPlay(contentItem: ContentItem) {
        _nextEpisodePlay.postValue(SingleEvent(contentItem))
    }


    fun callPlanetMarathiAnalyticsPlayEvent(
        it: PlayerModel,
        eventName : String,
    ) {
        val body = PlanetMarathiAnalyticsRequest(
            eventId = DeviceInfoUtils.getRandomUUID,
            eventType = eventName,
            timestamp = System.currentTimeMillis()/1000,
            entityID = it.getProviderContentId(),
            signedUrl = it.getPlaybackUrl()
        )
        val disposable = mUseCase.planetMarathiAnalytics(body, it.getContentType()?:"")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    //inside success
                },
                {
                    //inside failure
                })
        addDisposable(disposable)

    }

    fun generateVootPwaToken(partnerDeeplinkUrl: String) {
        val requestMap : HashMap<String, String> = HashMap()
        requestMap["rmn"] = sharedPrefs.getClearRMN()
        requestMap["uniqueId"] = sharedPrefs.getProfileId()?:""
        val d = mUseCase.generateVootPwaToken(requestMap).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<VootPwaResponse>() {
                override fun onSuccessResponse(t: VootPwaResponse) {
                    setProgressing(false)
                    val x = mapToString(t.data as Map<String, Any>)
                    _vootPwaDeeplinkUrl.postValue(SingleEvent("${partnerDeeplinkUrl}&${x}"))
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


    fun fetchLionsgateToken(mpdUrl: String?) {
        setProgressing(true)
        val d = mUseCase.fetchLionsGateToken(LionsgateRequest(mpdUrl?:""))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<LionsGateResponse>() {
                override fun onSuccessResponse(t: LionsGateResponse) {
                    setProgressing(false)
                    lionsgateToken.postValue(SingleEvent(t))
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

    fun callLionsgateAnalyticsPlayEvent(
        it: PlayerModel,
        subtitle : String,
        subtitle_lang : String,
        seconds_watched : Long
    ) {
        val language = if((it.getAudioLanguages()?.size ?: 0) > 0) it.getAudioLanguages()?.get(0)
        else ""
        val genre = if((it.getGenre()?.size ?: 0) > 0) it.getGenre()?.get(0)
        else ""
        val percentage = ((seconds_watched.toDouble() / it.getTotalDuration().toDouble() )* 100).toInt()
        val percentageComplete = "$percentage%"
        val lionsgateInfo = sharedPrefs.getPartnerDetail(
            PROVIDER_LIONSGATE,
            sharedPrefs.getSubscribedPack()?.partnerUniqueIdInfo
        )
        val lionsgateAnalyticsBody = LionsgateAnalyticsBody(
            content_id = it.getProviderContentId()?:"",
            title = it.getTitle() ?: "",
            contentType = it.getContentType()?:"" ,
            totalDuration = it.getTotalDuration(),
            partnerUniqueId = lionsgateInfo?.partnerUniqueId ?: "",
            genre = genre ?:"",
            video_language = language?:"",
            subtitle = subtitle,
            subtitle_language = subtitle_lang,
            seconds_watched = seconds_watched,
            percentage_complete = percentageComplete,
        )

//        if( lionsgateInfo?.partnerUniqueId != null ) {
        val disposable = mUseCase.lionsgateAnalytics(lionsgateAnalyticsBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    //inside success
                },
                {
                    //inside failure
                })
        addDisposable(disposable)
//        }
    }
}
