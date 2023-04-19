package com.tatasky.binge.ui.features.details

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.database.model.TokenContentDBModel
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.learnactions.LearnActionHelper
import com.tatasky.binge.shemaroo.modal.ShemarooSafeUrlResponse
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseViewModel
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.player.PlayerModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.voot.model.VootPlayebackResponse
import com.tatasky.binge.voot.model.VootRequest
import com.ttn.ttnplayer.player.SubtitleDTO
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

open class DetailViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val database: AppDatabase,
    val sharedPrefs: PrefsRepo,
    val laHelper: LearnActionHelper
) : CancellationBaseViewModel(useCase, sharedPrefs) {

    var lastWatched: IsFavouriteResponse.Data? = null
    var tvodToken: String? = null
    lateinit var taContentType: String
    lateinit var taContentId: String
    var onlyMessage: Boolean = false
    private val vodDetailsResponse = MutableLiveData<SingleEvent<DetailsResponse>>()
    private val isContentFavourite = MutableLiveData<SingleEvent<Boolean>>()
    private lateinit var seriesCompositeDisposable: Disposable
    private val favResponse = MutableLiveData<SingleEvent<IsFavouriteResponse>>()

    //    var fetchingSeries = MutableLiveData<Boolean>()
    private val recommendations = MutableLiveData<SingleEvent<RecommendationResponse>>()
    private val seriesEpisodesList = MutableLiveData<SingleEvent<SeriesListResponse?>>()
    private val episodeSearchResponse = MutableLiveData<SingleEvent<SeriesListResponse?>>()
    private val tokenListener = MutableLiveData<SingleEvent<String>>()
    private val sonylivShortToken = MutableLiveData<SingleEvent<String>>()
    val fetchingEpisodeSearch = MutableLiveData<SingleEvent<Boolean>>()


    val SERIES_LIMIT = 10

    val VIDEO_UNAVAILABLE_MESSAGE =
        "We are unable to play your video right now. Please try again in a few minutes"
    val VIDEO_UNAVAILABLE_MESSAGE_URL = "Error. Content not found!"
    val VIDEO_UNAVAILABLE_TITLE = "Video Unavailable"
    private val shemarooPlaybackUrls = MutableLiveData<SingleEvent<ShemarooSafeUrlResponse>>()
    private val shemarooTrailerUrls = MutableLiveData<SingleEvent<ShemarooSafeUrlResponse>>()
    private val chaupalPlaybackUrls = MutableLiveData<SingleEvent<ChaupalUrlResponse>>()
    private val chaupalTrailerUrls = MutableLiveData<SingleEvent<ChaupalUrlResponse>>()

    private val genericPlaybackUrls = MutableLiveData<SingleEvent<GenericPartnerDRMResponse>>()
    private val vootPlaybackUrls = MutableLiveData<SingleEvent<VootPlayebackResponse>>()
    private val vootTrailerUrls = MutableLiveData<SingleEvent<VootPlayebackResponse>>()
    private val zee5Tag = MutableLiveData<SingleEvent<Zee5TagResponse>>()
    private val _signoutResponse = MutableLiveData<SingleEvent<BaseResponse>>()
    private val csPlayback = MutableLiveData<SingleEvent<String>>()
    private var seriesDisposable = CompositeDisposable()
    private lateinit var _episodeSearchDisposable: Disposable

    private val hoichoiPlaybackUrls = MutableLiveData<SingleEvent<HoichoiPlayebackResponse>>()


    private val appleRedemptionUrl = MutableLiveData<SingleEvent<AppleRedemptionResponse>>()

    fun getAppleRedemptionUrl(): LiveData<SingleEvent<AppleRedemptionResponse>> = appleRedemptionUrl

    var episodeSearchQuery = ""


    val _voiceText = MutableLiveData<SingleEvent<String>>()

    fun getVoiceText(): LiveData<SingleEvent<String>> = _voiceText

    fun getCloudinaryUrl(): String? {
        return sharedPrefs.getCloudenieryUrl()
    }

    fun clearSeriesApiCalls() {
        seriesDisposable.clear()
    }

    fun fetchRecommendations(id: String, type: String) {

        taContentId = id
        taContentType = type

        val request = DetailRequest(type, id, "ENG", "WEB", id, 10, 0)
        val disposable = useCase.executeRecommendationRails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    //it.data?.layoutType = ItemLayoutType.LANDSCAPE.name
                    updateCrownForReommendation(it)
                },
                {
                }
            )
        addDisposable(disposable)
    }

    private fun updateCrownForReommendation(it: RecommendationResponse) {
        viewModelScope.launch {
            val crwnDeferred = async {
            }
            crwnDeferred.await()
            crownCalculation(it)

            recommendations.value = SingleEvent(it)
        }
    }

    fun fetchSeriesList(
        seriesId: String,
        offset: Int,
        pageLimit: Int,
        showLoader: Boolean,
        isLastWatch: Boolean,
        isAutoScroll: Boolean
    ) {
        if (::seriesCompositeDisposable.isInitialized) {
            seriesCompositeDisposable.dispose()
        }
//        seriesCompositeDisposable.clear()
//        fetchingSeries.postValue(showLoader)
        val request =
            SeriesRequest(
                seriesId,
                pageLimit,
                offset,
                sharedPrefs.getProfileId(),
                sharedPrefs.getOriginalSubscriberId(),
                isLastWatch,
                isAutoScroll
            )
        seriesCompositeDisposable = useCase.executeSeriesList(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                //setProgressing(false)
                if (offset > 0)
                    seriesEpisodesList.value = SingleEvent(SeriesListResponse().apply {
                        this.code = CUSTOM_RESPONSE_CODE_SERIES_ADDING
                    })
                else
                    seriesEpisodesList.value = SingleEvent(SeriesListResponse().apply {
                        this.code = RESPONSE_CODE_NETWORK_ERROR
                    })

//                fetchingSeries.postValue(false)
                if (showLoader) {
                    onlyMessage = true
                    setRetryError(error, false)
                }
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<SeriesListResponse>(), Disposable {
                override fun onError(error: ErrorModel?) {
                    if (offset > 0)
                        seriesEpisodesList.value = SingleEvent(SeriesListResponse().apply {
                            this.code = CUSTOM_RESPONSE_CODE_SERIES_ADDING
                        })

                    //fetchingSeries.postValue(false)
                    if (showLoader) {
                        onlyMessage = true
                        setError(error)
                        seriesEpisodesList.value = SingleEvent(SeriesListResponse().apply {
                            this.code = RESPONSE_CODE_NETWORK_ERROR
                        })

                    }
                }

                override fun onSuccessResponse(it: SeriesListResponse) {
                    getEpisodeLastWatch(it)
                }

                override fun onSubscribe(d: Disposable) {
                    seriesCompositeDisposable = d
                    addDisposable(seriesCompositeDisposable)
                    seriesDisposable.add(seriesCompositeDisposable)
                    super.onSubscribe(d)
                }

                override fun isDisposed(): Boolean {
                    return true
                }

                override fun dispose() {

                }

            })
//        seriesCompositeDisposable.add(disposable)
        addDisposable(seriesCompositeDisposable)
    }


    private fun getEpisodeLastWatch(seriesList: SeriesListResponse) {
        val disposable = useCase.getLastWatchEpisode(ContentIdAndTypeRequest().apply {
            this.profileId = sharedPrefs.getProfileId()
            this.subscriberId = sharedPrefs.getOriginalSubscriberId()
            this.isLoggedIn = sharedPrefs.getLoginStatus()
            this.contentIdAndType = seriesList.data?.contentItem!!.map {
                ContentIdAndTypeRequest.ContentIdAndType(it.id.toIntOrNull(), it.contentType)
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<EpisodeListingResponse>() {
                override fun onSuccessResponse(watchDurationList: EpisodeListingResponse) {
                    updateWatchDuration(seriesList, watchDurationList)
                    crownCalculation(seriesList)
                    seriesEpisodesList.value = SingleEvent(seriesList)

                }

                override fun onError(error: ErrorModel?) {
                    crownCalculation(seriesList)
                    seriesEpisodesList.value = SingleEvent(seriesList)
//                    fetchingSeries.postValue(false)
                }

                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                    seriesDisposable.add(d)
                }

            })
    }

    private fun updateWatchDuration(
        seriesList: SeriesListResponse,
        watchDurationList: EpisodeListingResponse
    ) {
        var i = 0
        val seriesSize = seriesList.data?.contentItem?.size ?: 0
        if (seriesList.data != null && seriesList.data?.contentItem != null
            && seriesSize > 0
            && watchDurationList.data != null && watchDurationList.data?.size ?: 0 == seriesSize
        )
            for (obj in seriesList.data?.contentItem!!) {
                val watchEpisode = watchDurationList.data!![i]
                if (watchEpisode.secondsWatched ?: 0 > 0)
                    obj.secondsWatched = watchEpisode.secondsWatched ?: 0
                i++
            }
    }

    fun getBrandDetails(vodId: String, detailsType: String, showLoader: Boolean) {
        //detailsType, vodId, "ENG", "WEB", vodId
        if (showLoader)
            setProgressing(true)
        val request: DetailRequest = DetailRequest(
            detailsType, vodId, "ENG", "WEB", vodId, 0, 0,
            sharedPrefs.getProfileId(),
            sharedPrefs.getOriginalSubscriberId()
        )
        val disposable = useCase.executeBrandDetails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                if (showLoader) setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<DetailsResponse>() {
                override fun onError(error: ErrorModel?) {
                    if (showLoader) {
                        setProgressing(false)
                        setError(error)
                    }
                }

                override fun onSuccessResponse(it: DetailsResponse) {
//                    setProgressing(false)
                    if (it.data == null || it.code != CODE_SUCCESS) {
                        setProgressing(false)
//                        if (it.code == 8) onlyMessage = true
                        onlyMessage = false
                        setError(ErrorModel(it.code, it.message))
                    } else {
                        vodDetailsResponse.postValue(SingleEvent(it))
                    }
                }

            })
    }


    fun fetchLastWatchedFavourite(id: String, contentType: String, isPartnerSubscribed: Boolean) {
        var uniqueId = sharedPrefs.getAnonymousId() ?: ""
        var subsType= ""
        if(isPartnerSubscribed){
            subsType="subscribed"
        }
        else{
            subsType= "freemium"
        }/*else {
            subsType="unsubscribed"
        }*/
        val toggleFavouriteRequest = ToggleFavouriteRequest(
            profileId = sharedPrefs.getProfileId() ?: "",
            subscriberId = sharedPrefs.getOriginalSubscriberId(),
            contentId = id,
            contentType = contentType,
            uniqueId = uniqueId,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            subsType
        )

        val disposable =
            useCase.fetchLastWatch(toggleFavouriteRequest,sharedPrefs.getSubscribedPack())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<IsFavouriteResponse>(false) {
                    override fun onError(error: ErrorModel?) {
                        e("fetchLastWatch", "inside onError")
//                        setProgressing(false)
                        favResponse.postValue(SingleEvent(IsFavouriteResponse()))
                    }

                    override fun onSuccessResponse(it: IsFavouriteResponse) {
//                            setProgressing(false)
                        if (contentType.equals(
                                TYPE_TV_SHOWS,
                                true
                            ) && it.data?.contentId?.equals(it.data?.vodId, true) == false
                        ) {
                            favResponse.postValue(SingleEvent(IsFavouriteResponse().apply {
                                this.data?.favourite = it.data?.favourite ?: false
                            }))
                        } else {
                            lastWatched = it.data
                            favResponse.postValue(SingleEvent(it))
                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }
                })
    }

    fun markFavourite(id: String, contentType: String, showLoader: Boolean) {
        setProgressing(showLoader)
        val toggleFavouriteRequest = ToggleFavouriteRequest(
            profileId = sharedPrefs.getProfileId()!!,
            subscriberId = sharedPrefs.getOriginalSubscriberId()!!,
            contentId = id,
            contentType = contentType,
            uniqueId = sharedPrefs.getAnonymousId() ?: ""
        )
        val disposable = useCase.toggleFavourite(toggleFavouriteRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<IsFavouriteResponse>(true) {
                override fun onError(error: ErrorModel?) {
                    e("toggleFavorite", "inside onError")
                    onlyMessage = true
//                    setError(error)
                    setProgressing(false)
                }

                override fun onSuccessResponse(it: IsFavouriteResponse) {
                    setProgressing(false)
                    isContentFavourite.postValue(SingleEvent(it.data!!.favourite))
                }
            })
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefs.getLoginStatus()
    }


    fun getDetailsResponse(): LiveData<SingleEvent<DetailsResponse>> = vodDetailsResponse

    fun getRecommendationResponse(): LiveData<SingleEvent<RecommendationResponse>> = recommendations

    fun getSeriesList(): LiveData<SingleEvent<SeriesListResponse?>> = seriesEpisodesList

    fun getEpisodeSearchResponse(): LiveData<SingleEvent<SeriesListResponse?>> =
        episodeSearchResponse

    fun getLastWatchResponse(): LiveData<SingleEvent<IsFavouriteResponse>> = favResponse

    fun getIsFavouriteContent(): LiveData<SingleEvent<Boolean>> = isContentFavourite

    fun getShemarooPlaybackUrls(): LiveData<SingleEvent<ShemarooSafeUrlResponse>> =
        shemarooPlaybackUrls

    fun getSignoutResponse(): LiveData<SingleEvent<BaseResponse>> = _signoutResponse
    fun getVootPlaybackUrls(): LiveData<SingleEvent<VootPlayebackResponse>> = vootPlaybackUrls
    fun getVootTrailerUrls(): LiveData<SingleEvent<VootPlayebackResponse>> = vootTrailerUrls
    fun getZee5TagResponse(): LiveData<SingleEvent<Zee5TagResponse>> = zee5Tag
    fun getCSPlaybackUrl(): LiveData<SingleEvent<String>> = csPlayback

    fun getShemarooTrailerUrls(): LiveData<SingleEvent<ShemarooSafeUrlResponse>> =
        shemarooTrailerUrls

    fun getChaupalTrailerUrls(): LiveData<SingleEvent<ChaupalUrlResponse>> =
        chaupalTrailerUrls

    fun getChaupalPlaybackUrls(): LiveData<SingleEvent<ChaupalUrlResponse>> =
        chaupalPlaybackUrls

    fun getGenericPlaybackUrls() : LiveData<SingleEvent<GenericPartnerDRMResponse>> = genericPlaybackUrls

    fun getContentToken(): LiveData<SingleEvent<String>> = tokenListener
    fun getSonylivShortToken(): LiveData<SingleEvent<String>> = sonylivShortToken

    fun generatePlayerModel(detailResponse: DetailsResponse): PlayerModel {
        val taShowType = detailResponse.data?.metaDetails?.taShowType ?: ""
        val meta = detailResponse.data!!.metaDetails
        val detail = detailResponse.data!!.detail

        val provider = meta?.provider
        val providerContentId = meta?.providerContentId

        val audio = meta?.audio ?: emptyList()
        var contractName = detail!!.contractName
        if (PROVIDER_SHEMAROO.equals(provider)) {
            contractName = "FREE"
        }
        val resumeTime = lastWatched?.secondsWatched ?: 0
        val totalDuration = lastWatched?.durationInSeconds ?: meta?.duration ?: 0
        val hd = meta!!.hd
        val contentType = meta.vodContentType ?: meta.contentType
        val dashWidewinePlayUrl = lastWatched?.dashWidewinePlayUrl ?: detail.dashWidewinePlayUrl

        val dashWidewineLicenseUrl = lastWatched?.dashWidewineLicenseUrl ?:detail.dashWidewineLicenseUrl

        val title = lastWatched?.contentTitle ?: meta.title
        val parentTitle = meta.getParentTitle()
        val vodId: String? = lastWatched?.vodId ?: meta.vodId ?: lastWatched?.contentId

        val contentId: String? = vodId ?: meta.id
        val favourite: Boolean = false

        val videoEntitlements = detail.entitlements
        val image = meta.boxCoverImage
        val cookies = detail.cookies
        val smartUrl = lastWatched?.partnerDeepLinkUrl ?: meta.partnerDeepLinkUrl
        val partnerContentType = meta.partnerContentType
        val partnerSubscriptionType = meta.partnerSubscriptionType
        val subtitleGenericUrl = lastWatched?.subtitlePlayUrl
            ?: meta.subtitlePlayUrl
        val subtitleUrl= java.util.ArrayList<SubtitleDTO>()
        subtitleGenericUrl?.forEach {
            val subtitle=SubtitleDTO()
            subtitle.url=it.url
            subtitle.lang=it.lang
            subtitleUrl.add(subtitle)
        }
        return createPlayerModel(
            contractName!!,
            title!!,
            dashWidewinePlayUrl,
            dashWidewineLicenseUrl,
            false,
            favourite!!,
            (resumeTime * 1000).toLong(),
            videoEntitlements!!,
            sharedPrefs.getOriginalSubscriberId()!!,
            contentType!!,
            vodId,
            contentId!!,
            meta.genre!!,
            "rail",
            meta.actor!!,
            hd,
            provider,
            providerContentId,
            audio,
            image,
            taShowType,
            cookies,
            totalDuration.toLong(),
            smartUrl,
            partnerContentType,
            parentTitle ?: "",
            partnerSubscriptionType,
            subtitleUrl=subtitleUrl
        )
    }

    fun getHoichoiPlaybackUrls(): LiveData<SingleEvent<HoichoiPlayebackResponse>> = hoichoiPlaybackUrls

    fun orientationEnabled() = sharedPrefs.orientationEnabled()

    fun disableOrientation() = sharedPrefs.setOrientationEnabled(false)

    fun generatePlayerModel(seriesItem: ContentItem, taShowType: String): PlayerModel {
        val detail = seriesItem.playerDetails ?: Detail().apply {
            contractName = seriesItem.contractName
            entitlements = seriesItem.entitlements
        }
        val provider = seriesItem.provider
        val providerContentId = seriesItem.providerContentId
        val audio = seriesItem.audio ?: emptyList()
        var contractName = detail.contractName
        if (PROVIDER_SHEMAROO.equals(provider)) {
            contractName = "FREE"
        }
        val hd = false
        val contentType = seriesItem.contentType
        val dashWidewinePlayUrl = detail.dashWidewinePlayUrl ?: detail.playUrl
        val dashWidewineLicenseUrl = detail.dashWidewineLicenseUrl
        val title = seriesItem.title
        val parentTitle =
            vodDetailsResponse.value?.peekContent()?.data?.metaDetails?.getParentTitle() ?: ""
        var vodId: String? = null
        if (seriesItem.vodId.isNotEmpty()) {
            vodId = seriesItem.vodId
        }

        val contentId: String = seriesItem.id
        val favourite: Boolean = false
        val videoEntitlements = detail.entitlements
        val image = seriesItem.getImageItem()
        val cookies = seriesItem.playerDetails?.cookies
        val smartUrl = seriesItem.partnerDeepLinkUrl?:""
        val partnerContentType = seriesItem.partnerContentType
        val partnerSubscriptionType = seriesItem.partnerSubscriptionType
        return createPlayerModel(
            contractName?:"",
            title,
            dashWidewinePlayUrl,
            dashWidewineLicenseUrl,
            false,
            favourite,
            (seriesItem.secondsWatched * 1000).toLong(),
            videoEntitlements,
            sharedPrefs.getOriginalSubscriberId()?:"",
            contentType,
            vodId,
            contentId,
            seriesItem.genres,
            "rail",
            emptyList(),
            hd,
            provider,
            providerContentId,
            audio,
            image,
            taShowType,
            cookies,
            (seriesItem.durationInSeconds).toLong(),
            smartUrl,
            partnerContentType,
            parentTitle,
            partnerSubscriptionType
        )
    }


    protected fun createPlayerModel(
        contractName: String,
        title: String,
        playbackUrl: String?,
        LA_URL: String?,
        isTrailer: Boolean,
        isFavourite: Boolean,
        resumeTime: Long,
        entitlements: List<String>?,
        sid: String,
        contentType: String,
        vodId: String?,
        contentId: String,
        genre: List<String>,
        source: String,
        actors: List<String>,
        hd: Boolean,
        provider: String?,
        providerContentId: String?,
        audio: List<String>?,
        image: String?,
        taShowType: String,
        cookies: String?,
        totalDuration: Long,
        smartUrl: String,
        partnerContentType: String?,
        partnerTitle: String,
        partnerSubscriptionType: String?,
        isLiveContent: Boolean = false,
        subtitleUrl: java.util.ArrayList<SubtitleDTO>?=null
    ): PlayerModel {
        val header_key_requests =
            getCustomHeader(title, sid, contentType)
        return PlayerModel(
            title,
            playbackUrl,
            LA_URL,
            contractName,
            entitlements,
            contentType,
            isFavourite,
            isTrailer,
            resumeTime,
            contentId,
            vodId,
            header_key_requests,
            null,
            genre,
            source,
            actors,
            hd,
            false,
            provider,
            providerContentId,
            audio,
            image,
            taShowType,
            cookies,
            totalDuration,
            smartUrl,
            partnerContentType,
            partnerTitle,
            partnerSubscriptionType,
            isLiveContent,
            subtitleUrl
        )
    }

    private fun generateHeader(
        headerKey: String,
        headerValue: String,
        header_key_requests: ArrayList<String>
    ): ArrayList<String> {
        header_key_requests.add(headerKey)
        header_key_requests.add(headerValue)
        return header_key_requests
    }

    private fun getCustomHeader(
        title: String,
        sid: String,
        contentType: String
    ): ArrayList<String> {
        var header_key_requests = ArrayList<String>()
        header_key_requests = generateHeader("s_id", sid, header_key_requests)
        header_key_requests = generateHeader("device_id", "", header_key_requests)
//        if (contentType == "CATCH_UP" || contentType == "CUSTOM_CATCH_UP_DETAIL") {
//            header_key_requests = generateHeader("service_id", channelAssetId, header_key_requests)
//            header_key_requests = generateHeader("event_name", title, header_key_requests)
//        } else {
//            header_key_requests = generateHeader("asset_id", vodAssetId, header_key_requests)
//        }
        return header_key_requests
    }

    fun actionCW(id: String, contentType: String, watchDuration: Int, durationInSeconds: Int) {
        var watchDuration = watchDuration
        if (watchDuration == 0) watchDuration = 1
        val request = CWRequest(
            sharedPrefs.getOriginalSubscriberId(),
            sharedPrefs.getProfileId() ?: "",
            contentType = contentType,
            contentId = id,
            watchDuration = watchDuration,
            totalDuration = durationInSeconds,
            uniqueId = sharedPrefs.getAnonymousId() ?: "",
            isLoggedIn = sharedPrefs.getLoginStatus()
        )

        val disposable = useCase.getCWAction(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    //                    relatedSeries.value = SingleEvent(it)
//                    fetchingSeries.postValue(false)
                },
                {

                }
            )
        addDisposable(disposable)
    }

    fun fetchShemarooMeContentPlayback(signedURL: String, isTrailer: Boolean) {
        if (!isTrailer)
            setProgressing(true)
        val disposable = useCase.getShemarooUrlData(signedURL)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ShemarooSafeUrlResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: ShemarooSafeUrlResponse) {
                    if (isTrailer)
                        shemarooTrailerUrls.postValue(SingleEvent(t))
                    else {
                        setProgressing(false)
                        shemarooPlaybackUrls.postValue(SingleEvent(t))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    if (!isTrailer) setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    fun getTARecommendations(
        placeHolder: String,
        contentId: String,
        contentType: String,
        showType: String,
        provider: String,
        parentContentType: String,
        parentId: String,
        placeHolderFallback: String? = null
    ) {
        val request = TARequest(
            placeHolder, "10", null,
            id = contentId,
            contentType = contentType,
            showType = showType,
            provider = provider,
            isRelated = true,
            isLoggedIn = isLoggedIn(),
            body = EmptyBody(),
            masterGenre = android.text.TextUtils.join(",", sharedPrefs.getPrefGenres())
        )


        fun defineTitle(it : RecommendationResponse){
            var title = it.data?.title ?: ""
            if (title.isEmpty()) {

                val titleBuffer = StringBuffer("Related ")
                if (contentType.contains(TYPE_MOVIES)) {
                    titleBuffer.append("Movies")
                } else if (contentType.contains(TYPE_TV_SHOWS)
                    || contentType.contains(TYPE_CATCH_UP)
                ) {
                    titleBuffer.append("Shows")
                } else if (contentType.contains(TYPE_WEB_SHORTS)) {
                    titleBuffer.append("Shorts")
                } else if (contentType.contains(TYPE_BRAND)) {
                    titleBuffer.append("Brand")
                } else if (contentType.contains(TYPE_SERIES)) {
                    titleBuffer.append("Series")
                }
                title = titleBuffer.toString()
                it.data?.title = title
            }
        }

        fun fetchTAFallbackRecommendations(){
            request.placeHolder = placeHolderFallback ?: ""
            useCase.executeTARails(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.data == null
                            || it.data?.filteredContentItems == null
                            || it.data?.filteredContentItems?.isEmpty() == true
                        ) {
                            fetchRecommendations(parentId, parentContentType)
                        } else {
                            it.data?.apply{
                                layoutType = ItemLayoutType.LANDSCAPE.name
                                this.placeHolder = placeHolderFallback ?: ""
                                sectionSource = RECOMMENDATION
                            }
                            defineTitle(it)
                            updateCrownForReommendation(it)
                        }
                    },
                    {
                        e("CallbackWrapper", "inside onError $it")
                        fetchRecommendations(contentId, contentType)
                    }
                )
        }

        val disposable = useCase.executeTARails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    //it.data?.layoutType = ItemLayoutType.LANDSCAPE.name
                    if (it.data == null
                        || it.data?.filteredContentItems == null
                        || it.data?.filteredContentItems?.isEmpty() == true
                    ) {
                        fetchTAFallbackRecommendations()
                    } else {
                        it.data?.layoutType = ItemLayoutType.LANDSCAPE.name
                        it.data?.placeHolder = placeHolder
                        it.data?.sectionSource = RECOMMENDATION

                        //it.data?.filteredContentItems = it.data?.filteredTAContentItems!!
                        defineTitle(it)
                        updateCrownForReommendation(it)
                        //recommendations.value = SingleEvent(it)
                    }
                },
                {
                    e("CallbackWrapper", "inside onError $it")
                    fetchTAFallbackRecommendations()
                }
            )
    }

    fun showHideLoader(isLoader: Boolean) {
        setProgressing(isLoader)
    }

    fun trackFavoriteLearnAction(
        contentType: String,
        id: String,
        taShowType: String,
        provider: String,
        contractName: String,
        partnerSubscriptionType: String?,
        refUsecase : String
    ) {

        laHelper.hitFavoriteLearnAction(
            contentType,
            id,
            taShowType,
            provider,
            contractName,
            partnerSubscriptionType,
            refUsecase
        )
    }

    fun trackOnceIn24hrLearnAction(
        partnerSubscriptionType: String?,
        contentType: String,
        id: String,
        taShowType: String,
        provider: String,
        contractName: String,
        type: String,
        refUsecase: String
    ) {

        laHelper.trackOnceIn24hrLearnAction(
            partnerSubscriptionType,
            contentType,
            id,
            taShowType,
            provider,
            contractName,
            sharedPrefs.getLoginStatus(),
            type,
            refUsecase
        )
        e(
            "DetailViewModel",
            "trackLearnActionWatch refUsecase: $refUsecase, $contentType, $id, $taShowType, $provider,$type"
        )
    }

    fun generateControlToken(
        epids: List<Epid>,
        contentId: String,
        isLoader: Boolean,
        provider: String = PROVIDER_TATA_SKY
    ) {
        /*val epid = Epid()
        val list : ArrayList<Epid> = ArrayList()
        list.add(epid)
        val epids = list*/
        if (isLoader)
            setProgressing(true)
        val controlRequest = ControlRequest(epids = epids, provider = provider)
        val disposable = useCase.generateControlToken(controlRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ControlTokenResponse>(false) {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: ControlTokenResponse) {
                    if (isLoader) {
                        setProgressing(false)
                        tokenListener.postValue(SingleEvent(t.data?.token ?: ""))
                    }
                    saveTokenInDB(t.data, contentId)
                }

                override fun onError(error: ErrorModel?) {
                    if (isLoader) {
                        setProgressing(false)
                        onlyMessage = true
                        setError(error)
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    fun fetchZee5Tag(partnerWebUrl: String?) {
        if (partnerWebUrl.isNullOrEmpty()) {
            onlyMessage = true
            setError(ErrorModel().apply {
                this.message = VIDEO_UNAVAILABLE_MESSAGE_URL
                this.title = COMMON_ERROR_TITLE
                this.statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
            })
        } else {
            setProgressing(true)
            useCase.fetchZee5Tag()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<Zee5TagResponse>() {
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }

                    override fun onSuccessResponse(t: Zee5TagResponse) {
                        setProgressing(false)
                        if (t.data == null || t.data?.tag.isNullOrEmpty()) {
                            onlyMessage = true
                            setError(ErrorModel().apply {
                                this.message = VIDEO_UNAVAILABLE_MESSAGE_URL
                                this.title = COMMON_ERROR_TITLE
                                this.statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
                            })
                        } else {
                            zee5Tag.postValue(SingleEvent(t))
                        }
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        onlyMessage = true
                        setError(error.apply {
                            this?.message = VIDEO_UNAVAILABLE_MESSAGE_URL
                            this?.title = COMMON_ERROR_TITLE
                            this?.statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
                        })
                    }
                })
        }
    }

    private fun saveTokenInDB(data: ControlTokenResponse.Data?, contentId: String) {
        if (data != null) {
            val tokenModel = TokenContentDBModel(
                expiryIn = data.expires_in,
                timestamp = System.currentTimeMillis(),
                contentId = contentId,
                token = data.token ?: ""
            )
            database.tokenDao.deleteTokenContent(contentId)
            database.tokenDao.insertTokenContent(tokenModel)
        }
    }

    private fun saveHoichoiTokenInDB(data: HoichoiPlayebackResponse.Data?, contentId: String) {
        data?.let {
            val tokenModel = TokenContentDBModel(
                expiryIn = it.expiryDuration?.toLong(),
                timestamp = System.currentTimeMillis(),
                contentId = contentId,
                token = it.token ?: ""
            )
            database.tokenDao.deleteTokenContent(contentId)
            database.tokenDao.insertTokenContent(tokenModel)
        }

    }

    fun isTokenExpired(contentId: String): Boolean {
        val laModel = database.tokenDao.getTokenContents(contentId)
        if (laModel.isNotEmpty() && laModel[0].timestamp != null && laModel[0].expiryIn != null) {
            val diff = (System.currentTimeMillis() - laModel[0].timestamp!!)
            e(
                "DetailViewModel",
                "inside isTokenExpired laModel : $laModel diff : $diff , " +
                    "laModel[0].expiryIn: ${laModel[0].expiryIn}"
            )
            if (diff < laModel[0].expiryIn!!) {
                tvodToken = laModel[0].token
                return false
            } else
                database.tokenDao.deleteTokenContent(contentId)
        }
        return true
    }

    @SuppressLint("CheckResult")
    fun fetchVootPlaybackUrl(request: VootRequest, isTrailer: Boolean) {
        if (!isTrailer) setProgressing(true)
        useCase.getVootPlaybackData(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<VootPlayebackResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: VootPlayebackResponse) {
                    setProgressing(false)
                    if (t.data == null || t.data?.url == null) {
                        onlyMessage = true
                        setError(ErrorModel(statusCode = t.code, message = t.message))
                    } else
                        if (isTrailer)
                            vootTrailerUrls.postValue(SingleEvent(t))
                        else
                            vootPlaybackUrls.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    @SuppressLint("CheckResult")
    fun fetchVootKidsPlaybackUrl(request: VootRequest, isTrailer: Boolean) {
        if (!isTrailer) setProgressing(true)
        useCase.getVootKidsPlaybackData(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<VootPlayebackResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: VootPlayebackResponse) {
                    setProgressing(false)
                    if (t.data == null || t.data?.url == null) {
                        onlyMessage = true
                        setError(ErrorModel(statusCode = t.code, message = t.message))
                    } else
                        if (isTrailer)
                            vootTrailerUrls.postValue(SingleEvent(t))
                        else
                            vootPlaybackUrls.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    fun getToken(contentId: String): String? {
        val laModel = database.tokenDao.getTokenContents(contentId)
        if (laModel.isNotEmpty() && laModel[0].timestamp != null && laModel[0].expiryIn != null) {
            val diff = (System.currentTimeMillis() - laModel[0].timestamp!!)
            e(
                "DetailViewModel",
                "inside isTokenExpired laModel : $laModel diff : $diff , " +
                    "laModel[0].expiryIn: ${laModel[0].expiryIn}"
            )
            if (diff < laModel[0].expiryIn!!) {
                return laModel[0].token
            } else
                database.tokenDao.deleteTokenContent(contentId)
        }
        return null
    }

    @SuppressLint("CheckResult")
    fun fetchCSBoxsetDetails(vodId: String, provider: String) {
        setProgressing(true)
        useCase.fetchBoxsetDetails(vodId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<DetailsResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: DetailsResponse) {
                    setProgressing(false)
                    if (t.data == null ||
                        ((PROVIDER_EPIC_ON.equals(provider, true) ||
                            PROVIDER_DOCU_BAY.equals(provider, true) || PROVIDER_HOICHOI.equals(
                            provider,
                            true
                        )) &&
                            t.data?.detail?.playUrl.isNullOrEmpty())
                        || (PROVIDER_CURIOSITY_STREAM.equals(provider, true) &&
                            t.data?.detail?.dashWidewinePlayUrl.isNullOrEmpty())
                    ) {
                        onlyMessage = true
                        setError(ErrorModel().apply {
                            this.message = VIDEO_UNAVAILABLE_MESSAGE_URL
                            this.title = COMMON_ERROR_TITLE
                            this.statusCode = CUSTOM_RESPONSE_CODE_ZEE5_ERROR
                        })
                    } else {
                        e("CSPlay", "t.data?.detail?.playUrl: ${t.data?.detail?.playUrl}")
                        if (PROVIDER_EPIC_ON.equals(provider, true) ||
                            PROVIDER_DOCU_BAY.equals(provider, true) || PROVIDER_HOICHOI.equals(
                                provider,
                                true
                            )
                        ) {
                            csPlayback.postValue(SingleEvent(t.data?.detail?.playUrl ?: ""))
                        } else
                            csPlayback.postValue(
                                SingleEvent(
                                    t.data?.detail?.dashWidewinePlayUrl ?: ""
                                )
                            )
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error)
                }
            })
    }

    fun generateSonylivShortToken(isLoader: Boolean) {
        if (isLoader)
            setProgressing(true)
        val disposable = useCase.generateSonylivToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ControlTokenResponse>(false) {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: ControlTokenResponse) {
                    if (isLoader) {
                        setProgressing(false)
                        sonylivShortToken.postValue(SingleEvent(t.data?.token ?: ""))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    if (isLoader) {
                        setProgressing(false)
                        onlyMessage = true
                        setError(error)
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    fun fetchEpisodeSearchResponse(
        metaDetails: MetaDetails?,
        pageNumber: Int?,
        searchQuery: String?,
        showLoader: Boolean = true
    ) {
        if (::_episodeSearchDisposable.isInitialized) {
            _episodeSearchDisposable.dispose()
        }
        if (showLoader)
            fetchingEpisodeSearch.postValue(SingleEvent(true))

        var parentId = metaDetails?.brandId
        if (metaDetails?.parentContentType == TYPE_SERIES
            || metaDetails?.contentType == TYPE_SERIES
        )
            parentId = metaDetails.seriesId
        val parentType = metaDetails?.parentContentType
        useCase.getEpisodeSearchResponse(
            EpisodeSearchRequest(
                parentType,
                searchQuery,
                parentId,
                pageNumber
            )
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setProgressing(false)
                setRetryError(error, false)
                fetchingEpisodeSearch.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<SeriesListResponse>() {
                override fun onSuccessResponse(t: SeriesListResponse) {
                    fetchingEpisodeSearch.postValue(SingleEvent(false))
                    if (t.data == null || t.code != CODE_SUCCESS) {
                        setError(ErrorModel(t.code, t.message))
                    } else {
                        crownCalculation(t)
                        episodeSearchResponse.postValue(SingleEvent(t))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    fetchingEpisodeSearch.postValue(SingleEvent(false))
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    _episodeSearchDisposable = d
                    addDisposable(d)
                }

            })
    }

    @SuppressLint("CheckResult")
    fun fetchHoichoiPlaybackUrl(request: HoichoiRequest, contentId: String) {
        setProgressing(true)
        useCase.getHoichoiPlaybackData(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<HoichoiPlayebackResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: HoichoiPlayebackResponse) {
                    setProgressing(false)
                    if (t.data == null || t.data?.token == null) {
                        onlyMessage = true
                        setError(ErrorModel(statusCode = t.code, message = t.message))
                    } else {

                        hoichoiPlaybackUrls.postValue(SingleEvent(t))
                        saveHoichoiTokenInDB(t.data, contentId)
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    fun fetchChaupalContentPlayback(contenId: String, contentType: String, isTrailer: Boolean) {
        if (!isTrailer)
            setProgressing(true)
        val disposable = useCase.getChaupalUrlData(contenId, contentType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ChaupalUrlResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: ChaupalUrlResponse) {
                    if (isTrailer)
                        if(t.data?.playUrls != null)
                            chaupalTrailerUrls.postValue(SingleEvent(t))
                        else{
                            onlyMessage = true
                            val error = ErrorModel()
                            setError(error.apply {
                                this.message = VIDEO_UNAVAILABLE_MESSAGE
                                this.title = VIDEO_UNAVAILABLE_TITLE
                            })
                        }
                    else {
                        setProgressing(false)
                        if(t.data?.playUrls != null)
                            chaupalPlaybackUrls.postValue(SingleEvent(t))
                        else{
                            onlyMessage = true
                            val error = ErrorModel()
                            setError(error.apply {
                                this.message = VIDEO_UNAVAILABLE_MESSAGE
                                this.title = VIDEO_UNAVAILABLE_TITLE
                            })
                        }
                    }
                }

                override fun onError(error: ErrorModel?) {
                    if (!isTrailer) setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    fun fetchPlanetMarathiPlayUrl(contenId: String, contentType: String){
        setProgressing(true)
        val disposable = useCase.getPlanetMarathiPlayUrl(contenId, contentType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<ChaupalUrlResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: ChaupalUrlResponse) {
                    setProgressing(false)
                    if(t.data?.playUrl != null)
                        chaupalPlaybackUrls.postValue(SingleEvent(t))
                    else{
                        onlyMessage = true
                        val error = ErrorModel()
                        setError(error.apply {
                            this.message = VIDEO_UNAVAILABLE_MESSAGE
                            this.title = VIDEO_UNAVAILABLE_TITLE
                        })
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    fun fetchGenericPartnerDRMAPI(
        providerContentId: String?,
        provider: String,
        contentTypeId: String,
        contentType: String,
    ) {
        setProgressing(true)
        val disposable = useCase.fetchGenericPartnerDRMAPI(
            providerContentId,
            provider,
            contentTypeId,
            contentType
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<GenericPartnerDRMResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: GenericPartnerDRMResponse) {
                    setProgressing(false)
                    if(t.data?.playerDetail?.playUrl != null)
                        genericPlaybackUrls.postValue(SingleEvent(t))
                    else{
                        onlyMessage = true
                        val error = ErrorModel()
                        setError(error.apply {
                            this.message = VIDEO_UNAVAILABLE_MESSAGE
                            this.title = VIDEO_UNAVAILABLE_TITLE
                        })
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }

    fun fetchAppleRedemptionUrl(){
        setProgressing(true)
        var body=AppleRedemptionRequest(sharedPrefs.getDsn(),sharedPrefs.getBaId(), sharedPrefs.getDeviceType() ?: "")
        val disposable = useCase.getAppleRedemptionUrl(body,sharedPrefs.getOriginalSubscriberId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<AppleRedemptionResponse>() {

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: AppleRedemptionResponse) {
                    setProgressing(false)
                    appleRedemptionUrl.postValue(SingleEvent(t))

                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    onlyMessage = true
                    setError(error.apply {
                        this?.message = VIDEO_UNAVAILABLE_MESSAGE
                        this?.title = VIDEO_UNAVAILABLE_TITLE
                    })
                }
            })
    }


    private fun crownCalculation(it : RecommendationResponse) {
        val mNonSubscribedPartnerList = sharedPrefs.getNonSubscribedPartnerList()

        val isGuestUser = sharedPrefs.getLoginStatus()
        val currentSub = sharedPrefs.getSubscribedPack()
        val currentSubStatus = (currentSub != null) && !currentSub.isInactive
        val freeEpVerb = sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage.toString()

        fun checkCrownConditions(it : ContentItem){
            it.appleRedemptionStatus = sharedPrefs.getSubscribedPack()?.appleRedemptionStatus
            it.isPartnerSubscribed = currentSubStatus && (mNonSubscribedPartnerList?.contains(it.provider.lowercase()) == false)
            it.isCrown = isShowCrownOnContent(
                it.isPartnerSubscribed,
                isGuestUser,
                it.provider,
                it.partnerSubscriptionType,
                it.appleRedemptionStatus
            )
        }

        it.data?.contentItem?.forEach {
            checkCrownConditions(it)
        }
    }
    private fun crownCalculation(it : SeriesListResponse?) {
        val mNonSubscribedPartnerList = sharedPrefs.getNonSubscribedPartnerList()

        val isGuestUser = sharedPrefs.getLoginStatus()
        val currentSub = sharedPrefs.getSubscribedPack()
        val currentSubStatus = (currentSub != null) && !currentSub.isInactive
        val freeEpVerb = sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage.toString()

        fun checkCrownConditions(it : ContentItem){
            it.appleRedemptionStatus = sharedPrefs.getSubscribedPack()?.appleRedemptionStatus
            it.isPartnerSubscribed = currentSubStatus && (mNonSubscribedPartnerList?.contains(it.provider.lowercase()) == false)
            it.isCrown = isShowCrownOnContent(
                it.isPartnerSubscribed,
                isGuestUser,
                it.provider,
                it.partnerSubscriptionType,
                it.appleRedemptionStatus
            )
        }

        it?.data?.contentItem?.forEach {
            checkCrownConditions(it)
        }
    }

}
