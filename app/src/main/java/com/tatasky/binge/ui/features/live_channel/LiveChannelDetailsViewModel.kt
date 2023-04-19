package com.tatasky.binge.ui.features.live_channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.DigitalFeedPlaybackUrlsResponse
import com.tatasky.binge.data.networking.models.response.LiveChannelDetailsResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.helper.DigitalFeedUrlDecryptionHelper
import com.tatasky.binge.learnactions.LearnActionHelper
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.ui.features.player.PlayerViewModel
import com.tatasky.binge.utils.CODE_SUCCESS
import com.tatasky.binge.utils.e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

class LiveChannelDetailsViewModel @Inject constructor(
    mUseCase: CommonUseCase,
    mDatabase: AppDatabase,
    mSharedPrefs: PrefsRepo,
    learnActionHelper: LearnActionHelper,
) : PlayerViewModel(mUseCase, mDatabase, mSharedPrefs, learnActionHelper) {

    private var retryAttempts = 0

    private val _digitalFeedUrls: MutableLiveData<SingleEvent<DigitalFeedPlaybackUrlsResponse>> =
        MutableLiveData()
    val digitalFeedUrls: LiveData<SingleEvent<DigitalFeedPlaybackUrlsResponse>> = _digitalFeedUrls

    private val _playbackBtnStateHolder: MutableLiveData<SingleEvent<LiveChannelPlaybackButtonStateEnum>> =
        MutableLiveData(SingleEvent(LiveChannelPlaybackButtonStateEnum.STATE_PLAY))
    val playbackBtnStateHolder: LiveData<SingleEvent<LiveChannelPlaybackButtonStateEnum>> =
        _playbackBtnStateHolder

    private val _liveChannelDetails: MutableLiveData<SingleEvent<LiveChannelDetailsResponse>> =
        MutableLiveData()
    val liveChannelDetails: LiveData<SingleEvent<LiveChannelDetailsResponse>> = _liveChannelDetails

    fun arePreviousAndCurrentContentSame(previousContentId: String?, currentContentId: String?) =
        previousContentId == currentContentId

    fun Long.nextContentAPICallDelay() = abs(this - System.currentTimeMillis())

    fun getUpcomingContentMetaDetails() =
        liveChannelDetails.value?.peekContent()?.data?.channelScheduleData?.find {
            it.epgState == EpgStates.FORWARD.value
        }

    fun getCurrentContentMetaDetails() =
        liveChannelDetails.value?.peekContent()?.data?.meta?.getOrNull(0)

    fun getCurrentChannelDetails() =
        liveChannelDetails.value?.peekContent()?.data?.channelMeta

    fun isContentSubscribed(channelId: String) =
        sharedPrefs.getAllowedLiveChannelIds()?.contains(channelId) == true

    fun getUpdatedDigitalFeedPlaybackUrls(
        playbackUrl: String?,
        playbackLicenseUrl: String?,
    ): Pair<String?, String> {
        var updatePlayUrl = playbackUrl
        var updatedLicenseUrl = playbackLicenseUrl
        val aesEncryptionSecretKeyV1 =
            sharedPrefs.getConfigResponse()?.data?.config?.digitalFeedDecryptionKey?.aesEncryptionSecretKeyV1
        val aesEncryptionSecretKeyV2 =
            sharedPrefs.getConfigResponse()?.data?.config?.digitalFeedDecryptionKey?.aesEncryptionSecretKeyV2
        playbackUrl?.let {
            val secretKey = DigitalFeedUrlDecryptionHelper.getSecretKey(
                it,
                aesEncryptionSecretKeyV1,
                aesEncryptionSecretKeyV2
            )
            val separatorLastIndex =
                it.lastIndexOf(DigitalFeedUrlDecryptionHelper.ENCRYPTED_URL_KEY_SEPARATOR)
            updatePlayUrl = DigitalFeedUrlDecryptionHelper.decryptDigitalDeliveryUrl(it.substring(0,
                separatorLastIndex), secretKey)
        }
        updatedLicenseUrl = if (playbackLicenseUrl.isNullOrEmpty())
            ""
        else {
            val secretKeyLicense = DigitalFeedUrlDecryptionHelper.getSecretKey(
                    playbackLicenseUrl,
                    aesEncryptionSecretKeyV1,
                    aesEncryptionSecretKeyV2
            )
            val separatorLicenseLastIndex =
                playbackLicenseUrl.lastIndexOf(DigitalFeedUrlDecryptionHelper.ENCRYPTED_URL_KEY_SEPARATOR)
            DigitalFeedUrlDecryptionHelper.decryptDigitalDeliveryUrl(
                playbackLicenseUrl.substring(0, separatorLicenseLastIndex),
                secretKeyLicense
            )
        }
        return Pair(updatePlayUrl, updatedLicenseUrl)
    }

    fun generatePlayerModel(detailResponse: LiveChannelDetailsResponse.Data?): PlayerModel {
        val contentMeta = detailResponse?.meta?.getOrNull(0)
        val detail = detailResponse?.detail
        val channelDetail = detailResponse?.channelMeta
        val provider = contentMeta?.provider
        val providerContentId = contentMeta?.id
        val audio = contentMeta?.audio ?: emptyList()
        val contractName = detail?.contractName ?: ""
        val resumeTime = 0
        val totalDuration = contentMeta?.duration ?: 0L
        val hd = channelDetail?.hd
        val contentType = contentMeta?.contentType ?: ""
        val dashWidewinePlayUrl = detail?.dashWidewinePlayUrl
        val dashWidewineLicenseUrl = detail?.dashWidewineLicenseUrl
        val title = channelDetail?.channelName ?: ""
        val vodId = contentMeta?.id
        val contentId = contentMeta?.id
        val favourite: Boolean = false
        val videoEntitlements = detail?.entitlements
        val image = contentMeta?.boxCoverImage
        val cookies = detail?.authorizedCookies
        val genre = contentMeta?.genre?.toList() ?: listOf()
        val actor = contentMeta?.actor?.toList() ?: listOf()

        return createPlayerModel(
            contractName,
            title,
            dashWidewinePlayUrl,
            dashWidewineLicenseUrl,
            false,
            favourite,
            (resumeTime * 1000).toLong(),
            videoEntitlements,
            sharedPrefs.getOriginalSubscriberId(),
            contentType,
            vodId,
            contentId ?: "",
            genre,
            "rail",
            actor,
            hd ?: false,
            provider,
            providerContentId,
            audio,
            image,
            "",
            cookies,
            totalDuration.toLong(),
            "",
            contentType,
            title,
            null,
            true
        )
    }


    fun fetchPlaybackUrlsForDigitalFeed(partnerName: String, channelId: String) {
        setProgressing(true)
        mUseCase.run {
            fetchPlaybackUrlsForDigitalFeed(partnerName, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<DigitalFeedPlaybackUrlsResponse>() {
                    override fun onSuccessResponse(t: DigitalFeedPlaybackUrlsResponse) {
                        setProgressing(false)
                        when (t.code) {
                            CODE_SUCCESS -> {
                                if (t.data != null)
                                    _digitalFeedUrls.postValue(SingleEvent((t)))
                                else
                                    setError(ErrorModel())
                            }
                            else -> setError(ErrorModel())
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

    fun togglePlaybackButton(state: LiveChannelPlaybackButtonStateEnum = LiveChannelPlaybackButtonStateEnum.STATE_PLAY) {
        when (state) {
           LiveChannelPlaybackButtonStateEnum.STATE_PAUSE ->
                _playbackBtnStateHolder.postValue(SingleEvent(LiveChannelPlaybackButtonStateEnum.STATE_PAUSE))
            LiveChannelPlaybackButtonStateEnum.STATE_RESUME ->
                _playbackBtnStateHolder.postValue(SingleEvent(LiveChannelPlaybackButtonStateEnum.STATE_RESUME))
            else ->
                _playbackBtnStateHolder.postValue(SingleEvent(LiveChannelPlaybackButtonStateEnum.STATE_PLAY))
        }
    }
    
    fun fetchSelectedLiveChannelDetails(channelId: String, showLoader: Boolean) {
        if (showLoader)
            setProgressing(true)
        mUseCase.run {
            getLiveChannelContentDetails(channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<LiveChannelDetailsResponse>() {
                    override fun onSuccessResponse(t: LiveChannelDetailsResponse) {
                        if (showLoader)
                            setProgressing(false)
                        when (t.code) {
                            CODE_SUCCESS -> {
                                if (t.data != null) {
                                    if (arePreviousAndCurrentContentSame(
                                            getCurrentContentMetaDetails()?.id,
                                            t.data?.meta?.getOrNull(0)?.id
                                        ) && retryAttempts < maxAllowedRetryAttempts
                                    ) {
                                        _liveChannelDetails.postValue(SingleEvent((t)))
                                        viewModelScope.launch {
                                            val delayInMs =
                                                sharedPrefs.getConfigResponse()?.data?.config?.channelDetailRetry?.times(
                                                    1000
                                                ) ?: DELAY_IN_MS_FOR_NEXT_ATTEMPT
                                            delay(delayInMs)
                                            getCurrentChannelDetails()?.id?.let { it1 ->
                                                retryAttempts++
                                                fetchSelectedLiveChannelDetails(it1, false)
                                            }
                                        }
                                    } else {
                                        retryAttempts = 0
                                        _liveChannelDetails.postValue(SingleEvent((t)))
                                        viewModelScope.launch {
                                            t.data?.meta?.getOrNull(0)?.endTime?.let {
                                                e("DelayTime","it.nextContentAPICallDelay() : ${it.nextContentAPICallDelay()}")
                                                delay(it.nextContentAPICallDelay())
                                                getCurrentChannelDetails()?.id?.let { channelId ->
                                                    fetchSelectedLiveChannelDetails(channelId, false)
                                                }
                                            }
                                        }
                                    }
                                } else
                                    setError(ErrorModel())
                            }
                            else -> setError(ErrorModel())
                        }
                    }

                    override fun onError(error: ErrorModel?) {
                        if (showLoader)
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
    
    companion object {
        private const val maxAllowedRetryAttempts = 5
        private const val DELAY_IN_MS_FOR_NEXT_ATTEMPT = 180000L
    }
}