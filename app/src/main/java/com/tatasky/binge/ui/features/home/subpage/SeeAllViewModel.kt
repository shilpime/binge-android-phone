package com.tatasky.binge.ui.features.home.subpage

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.tatasky.binge.analytics.EVENT_VALUE_SEE_ALL
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.interfaces.ContentItemTransitions
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.model.RailItemsModel
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 2/12/19.
 */
class SeeAllViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo
) : BaseViewModel() {

    private lateinit var loginDisposable: Disposable

    var cwWaitForTvod : Boolean = false
    private var cwRailResponse: RecommendationResponse? = null
    private var gameFavRailResponse : RecommendationResponse? = null
    private var watchlistRailResponse: RecommendationResponse? = null

    var onlyMessage: Boolean = false
    var tvodResponse: RecommendationResponse? = null
    var pagingState: String? = null
    var PAGELIMIT = 10
    var pageOffset = 0
    var isInitialized = false
    var continueWatching = false
    var gameFav = false
    var gameCw = false
    var watchlistRail = false
    var configType = EventConstants.TYPE_EDITORIAL
    var source = EVENT_VALUE_SEE_ALL
    var railName = ""
    var railPosition = ""
    var layoutType = ItemLayoutType.LANDSCAPE.name
    var refId = ""
    public var provider: String? = null
    private val _railResponse = MutableLiveData<SingleEvent<RecommendationResponse>>()
    private val _clickedItem = MutableLiveData<SingleEvent<ContentItemTransitions>>()
    private val _changedTotalRailsCount = MediatorLiveData<SingleEvent<Int>>()
    private val _anyResponseToHide = MediatorLiveData<SingleEvent<Boolean>>()
    private var totalRails = 0

    private val _previouslyUsedMobileNumberResponse =
        MutableLiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>>()
    val previouslyUsedMobileNumberResponse: LiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>> =
        _previouslyUsedMobileNumberResponse

    private val _previouslyUsedMobileNumberError =
        MutableLiveData<SingleEvent<ErrorModel>>()
    val previouslyUsedMobileNumberError: LiveData<SingleEvent<ErrorModel>> =
        _previouslyUsedMobileNumberError

    val mBannerClick = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            iSectionSource: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            val extras = if (!transitions.isNullOrEmpty())
                FragmentNavigatorExtras(*transitions.toTypedArray())
            else FragmentNavigatorExtras()
            iListItem.railName = railName
            iListItem.source = source
            iListItem.origin = configType
            iListItem.contentPosition = (iItemPosition+1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            iListItem.refId = refId
            _clickedItem.postValue(
                SingleEvent(
                    ContentItemTransitions(
                        iListItem,
                        extras,
                        iSectionSource,
                        gamesMixpanelInfoModel,
                        contentAnalyticsModel = contentAnalyticsModel
                    )
                )
            )
        }
    }

    val mAdapter =
        ItemGridAdapter(
            listener = mBannerClick,
            mList = mutableListOf<ContentItem>(),
            cloudinaryUrl = sharedPrefs.getCloudenieryUrl(),
            loadMoreClickListener = null,
            providerLogos = sharedPrefs.getProviderLogo(),
            sharedPrefs = sharedPrefs,
            origin = configType,
            railTitle = railName
        )

    val mAdapterUnsubscribed =
        ItemGridAdapter(
            listener = mBannerClick,
            mList = mutableListOf<ContentItem>(),
            cloudinaryUrl = sharedPrefs.getCloudenieryUrl(),
            loadMoreClickListener = null,
            providerLogos = sharedPrefs.getProviderLogo(),
            sharedPrefs = sharedPrefs,
            origin = configType,
            railTitle = railName
        )

    fun fetchRailData(
        railId: String,
        showLoader: Boolean,
        item: RecommendationResponse?,
        sectionType: String? = null
    ) {
        isInitialized = true
        if (showLoader)
            setProgressing(true)
        if (pageOffset > 0) {
            showRecyclerLoading()
        }
        val disposable = useCase.executeRailsData(railId, PAGELIMIT, pageOffset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setRetryError(error, false)
                _anyResponseToHide.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    e("fetchSeeAll","item size : ${item?.data?.contentItem?.size}")
                    if(item != null && !item.data?.filteredContentItems.isNullOrEmpty()){
                        val uniqueItems =  removeDuplicateContent(t.data?.contentItem?: ArrayList(),
                            item.data?.filteredContentItems ?: ArrayList())
                        if (!item.isPrepand) {
                            t.data?.contentItem?.addAll(uniqueItems)
                        } else {
                            t.data?.contentItem?.addAll(0, uniqueItems)
                        }
                        /*t.data?.contentItem?.let { removeDuplicateContent(
                            it,
                            it.data?.filteredContentItems
                        ) }*/
                    }
                    if (sectionType != null) {
                        t.data?.sectionType = sectionType
                    }
                    _railResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }


    private fun showRecyclerLoading() {
        /*mAdapter.addLoading()*/
    }
    private fun crownCalculation(it : HomeResponse.Items?) {
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

        it?.filteredContentItems?.forEach {
            checkCrownConditions(it)
        }
    }


    fun updateList(railResponse: RecommendationResponse, contentAnalyticsModel: ContentAnalyticsModel) {
        pagingState = railResponse.data?.pagingState
        mAdapter.removeLoading()
        if ((railResponse.data?.filteredContentItems?.size ?: 0) >= 0) {
            crownCalculation(railResponse.data)

            if (pageOffset == 0) {
                if(railResponse.data?.sectionType == ItemViewType.TITLE_RAIL.name){
                    mAdapter.updateLayoutType(ItemLayoutType.TITLE_RAIL.name)
                }else{
                    mAdapter.updateLayoutType(railResponse.data?.layoutType ?: ItemLayoutType.LANDSCAPE.name)
                }
                if(layoutType.equals(ItemLayoutType.SQUARE.name,true)){
                    mAdapter.updateLayoutType(ItemLayoutType.SQUARE.name)
                }
                totalRails = railResponse.data?.totalCount ?: 0
                _changedTotalRailsCount.postValue(SingleEvent(totalRails))
                if(continueWatching)
                    mAdapter.updateListForDiff(
                        railResponse.data?.filteredContentItems ?: mutableListOf(),
                        contentAnalyticsModel = contentAnalyticsModel
                    )
                else
                    mAdapter.updateList(
                        railResponse.data?.filteredContentItems ?: mutableListOf(),
                        contentAnalyticsModel
                    )
            } else {
                mAdapter.addToList(
                    railResponse.data?.filteredContentItems ?: mutableListOf(),
                    contentAnalyticsModel
                )
            }

            if (railResponse.data?.filteredContentItems?.size ?: 0 < railResponse.data?.contentItem?.size ?: 0) {
                val removedItemsCount =
                    (railResponse.data?.contentItem?.size
                        ?: 0) - (railResponse.data?.filteredContentItems?.size ?: 0)
                totalRails -= removedItemsCount
            }

            if (railResponse.data?.continuePagination == true) {
                totalRails = mAdapter.itemCount + 1
            }
            e("SeeAllViewModel", "totalRailsCount : $totalRails , adapterCount : ${mAdapter.itemCount}")
            if (totalRails != mAdapter.itemCount)
                _changedTotalRailsCount.postValue(SingleEvent(totalRails))
        } else {
            totalRails = mAdapter.itemCount
            _changedTotalRailsCount.postValue(SingleEvent(totalRails))
        }
        setProgressing(false)
    }

    fun updateAppsList(railResponse: AppResponse, contentAnalyticsModel: ContentAnalyticsModel) {
        mAdapter.removeLoading()
        mAdapter.isSubscribed(true)
        mAdapter.updateLayoutType(ItemLayoutType.APP_RAIL.name)
        if (pageOffset == 0) {
            mAdapter.updateList(
                railResponse.subscribedContent,
                contentAnalyticsModel
            )

        } else {
            mAdapter.addToList(
                railResponse.subscribedContent,
                contentAnalyticsModel
            )
        }

        mAdapterUnsubscribed.removeLoading()
        mAdapterUnsubscribed.updateLayoutType(ItemLayoutType.APP_RAIL.name)
        if (pageOffset == 0) {
            mAdapterUnsubscribed.updateList(railResponse.unsubscribedContent, contentAnalyticsModel)

        } else {
            mAdapterUnsubscribed.addToList(railResponse.unsubscribedContent, contentAnalyticsModel)
        }
        setProgressing(false)
    }


    fun getClickedItem(): LiveData<SingleEvent<ContentItemTransitions>> {
        return _clickedItem
    }

    fun getRailResponse(): LiveData<SingleEvent<RecommendationResponse>> = _railResponse
    fun getChangedCount(): LiveData<SingleEvent<Int>> = _changedTotalRailsCount
    fun getAnyResponseHideLoader(): LiveData<SingleEvent<Boolean>> = _anyResponseToHide

    @SuppressLint("CheckResult")
    fun fetchGameFavs() {
        gameFav = true
        val request =  WatchRequest(
            subscriberId = sharedPrefs.getOriginalSubscriberId(),
            profileId = sharedPrefs.getProfileId()!!,
            pagingState = pagingState,
            offset = pageOffset,
            isForceRefresh = false
        )
        useCase.fetchGameFavsOrCw(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
//                    t.data?.let { it ->
//                        val dummyRecommendationResponse = RecommendationResponse()
//                        dummyRecommendationResponse.data = HomeResponse.Items()
//                        dummyRecommendationResponse.data?.let { it1 ->
//                            it1.contentItem = it.list
//                        }

                        _railResponse.postValue(SingleEvent(t))
//                    }
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

            })

    }



    @SuppressLint("CheckResult")
    fun fetchGameCw() {
        gameCw = true
        val request =  WatchRequest(
            subscriberId = sharedPrefs.getOriginalSubscriberId(),
            profileId = sharedPrefs.getProfileId()!!,
            pagingState = pagingState,
            offset = pageOffset,
            isForceRefresh = false
        )
        useCase.fetchGameFavsOrCw(request,true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
//                    t.data?.let { it ->
//                        val dummyRecommendationResponse = RecommendationResponse()
//                        dummyRecommendationResponse.data = HomeResponse.Items()
//                        dummyRecommendationResponse.data?.let { it1 ->
//                            it1.contentItem = it.list
//                        }

                        _railResponse.postValue(SingleEvent(t))
//                    }
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

            })

    }


    fun fetchRecommendations(showLoader: Boolean, id: String, type: String) {
        if (showLoader)
            setProgressing(true)
        if (pageOffset > 0) {
            showRecyclerLoading()
        }
        val request = DetailRequest(type, id, "ENG", "WEB", id, PAGELIMIT, pageOffset)
        val disposable = useCase.executeRecommendationRails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setRetryError(error, false)
                _anyResponseToHide.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    _railResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun getContinueWatchingData(isShowLoader: Boolean) {
        continueWatching = true
        if (pageOffset > 0 && pagingState.isNullOrEmpty()) {
            _anyResponseToHide.postValue(SingleEvent(false))
            return
        }
        if (isShowLoader)
            setProgressing(true)
        if (pageOffset > 0) {
            showRecyclerLoading()
        }
        val request = CWRequest(
            sharedPrefs.getOriginalSubscriberId(),
            sharedPrefs.getProfileId()?:"",
            seeAll = true,
            pagingState = pagingState, offset = pageOffset,
            provider = provider,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            uniqueId = sharedPrefs.getAnonymousId()?:""
        )
        val disposable = useCase.executeCWRails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setRetryError(error, false)
                _anyResponseToHide.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {

                    cwRailResponse = t
                    val isRental: Boolean = isRentalContent(
                        t.data?.contentItem as ArrayList<ContentItem>? ?: ArrayList())
                    if (isRental && tvodResponse != null) {
                        updateCWRentalItems()
                    }
                    else if(isRental) {
                        e("SubViewModel","curl : inside cwresponse")
                        cwWaitForTvod = true
                        fetchTVoD(isShowLoader, true)
                    }
                    if(!isRental)
                        _railResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun hideAdapterLoader() {
        mAdapter.removeLoading()
    }


    fun fetchTARails(isShowLoader: Boolean, placeHolder: String, isRelatedRail: Boolean) {
        if (isShowLoader)
            setProgressing(true)

        val request = TARequest(placeHolder,
            PAGE_LIMIT_TA_RAIL,
            null,
            body = EmptyBody(),
            subPage = !isRelatedRail,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            provider = provider ?: "")
        val disposable = useCase.executeTARails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                if(isShowLoader)setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    t.data?.let {
                        it.layoutType = ItemLayoutType.LANDSCAPE.name
                        it.placeHolder = placeHolder
                        it.sectionSource = RECOMMENDATION

                        if (it.actionItemTittle?.isNotEmpty() == true) {
                            it.title += " ${it.actionItemTittle}"
                        }
                        handleGenreAndLanguage(it)
                    }

//                    t.data?.filteredContentItems = t.data?.filteredTAContentItems!!
                    _railResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun getTARecommendations(
        isShowLoader: Boolean,
        placeHolder: String,
        contentId: String,
        contentType: String,
        showType: String,
        provider: String
    ) {
        if (isShowLoader)
            setProgressing(true)
        if (pageOffset > 0) {
            showRecyclerLoading()
        }
        val request = TARequest(placeHolder, "20", null,
            id = contentId,
            contentType = contentType,
            showType = showType,
            provider = provider,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            isRelated = true,
            body = EmptyBody())

        val disposable = useCase.executeTARails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                if(isShowLoader)setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    t.data?.layoutType = ItemLayoutType.LANDSCAPE.name
                    t.data?.placeHolder = placeHolder
                    t.data?.sectionSource = RECOMMENDATION
                    //t.data?.filteredContentItems = t.data?.filteredTAContentItems!!
                    _railResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun fetchTVoD(isShowLoader: Boolean, isCW: Boolean) {
        if (isShowLoader)
            setProgressing(true)
        if (pageOffset > 0) {
            showRecyclerLoading()
        }
        val disposable = useCase.getTvodContent(
            sharedPrefs.getOriginalSubscriberId() ?: ""
            /*"3001180136"*/, 0, 10)//sid
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    tvodResponse = t
                    if (!isCW)
                        _railResponse.postValue(SingleEvent(t))
                    else if(cwWaitForTvod){
                        if(watchlistRail)
                            updateWatchlistRentalItems()
                        else if(continueWatching)
                            updateCWRentalItems()
                    }
                }

                override fun onError(error: ErrorModel?) {
                    if (!isCW)
                        setError(error)
                    else if(cwWaitForTvod){
                        if(watchlistRail)
                            updateWatchlistRentalItems()
                        else if(continueWatching)
                            updateCWRentalItems()
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    private fun updateCWRentalItems() {
        cwRailResponse?.let {
            cwWaitForTvod = false
            cwRailResponse?.data?.contentItem = filterRentalExpiry(
                cwRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                tvodResponse?.data?.contentItem
            ) ?: ArrayList()
            _railResponse.postValue(SingleEvent(it))
        }
    }

    fun fetchMixedTARails(
        isShowLoader: Boolean,
        placeHolder: String,
        railId: String,
        isPrepand: Boolean,
        isRelatedRail: Boolean
    ) {
        if (isShowLoader)
            setProgressing(true)

        val request = TARequest(placeHolder,
            "20",
            null,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            body = EmptyBody())

        val disposable = useCase.executeTARails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                if(isShowLoader)setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    t.data?.layoutType = ItemLayoutType.LANDSCAPE.name
                    t.data?.placeHolder = placeHolder
                    t.data?.sectionSource = RECOMMENDATION
                    t.isPrepand = isPrepand
                    //_railResponse.postValue(SingleEvent(t))
                    fetchRailData(railId, isShowLoader, t)
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    private fun handleGenreAndLanguage(item: HomeResponse.Items) {
        if (railName.toLowerCase().contains("genre")) {
            if (item.genreType != null && !item.genreType.isNullOrEmpty()) {
                if (railName.toLowerCase().contains("genre1")) {
                    railName =
                        railName.replace("genre1", item.genreType!!, true)
                }
                if (railName.toLowerCase().contains("genre2")) {
                    railName =
                        railName.replace("genre2", item.genreType!!, true)
                }
                if (railName.toLowerCase().contains("genre")) {
                    railName =
                        railName.replace("genre", item.genreType!!, true)
                }
            }
        }
        if (railName.toLowerCase().contains("language")) {
            if (item.languageType != null && !item.languageType.isNullOrEmpty()) {
                if (railName.toLowerCase().contains("language1")) {
                    railName =
                        railName.replace("language1", item.languageType!!, true)
                }
                if (railName.toLowerCase().contains("language2")) {
                    railName =
                        railName.replace("language2", item.languageType!!, true)
                }
                if (railName.toLowerCase().contains("language")) {
                    railName =
                        railName.replace("language", item.languageType!!, true)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun fetchWatchList(isShowLoader: Boolean) {
        watchlistRail = true
        if (pageOffset > 0 && pagingState.isNullOrEmpty()) {
            _anyResponseToHide.postValue(SingleEvent(false))
            return
        }
        if (isShowLoader)
            setProgressing(true)
        if (pageOffset > 0) {
            showRecyclerLoading()
        }
        val request =
            WatchRequest(
                sharedPrefs.getOriginalSubscriberId(),
                sharedPrefs.getProfileId()!!,
                pagingState, pageOffset, false
            )
        val disposable = useCase.executeFavouritesList(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setRetryError(error, false)
                _anyResponseToHide.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {

                    watchlistRailResponse = t
                    val isRental: Boolean = isRentalContent(
                        t.data?.contentItem as ArrayList<ContentItem>? ?: ArrayList())
                    if (isRental && tvodResponse != null) {
                        updateWatchlistRentalItems()
                    }
                    else if(isRental) {
                        e("SubViewModel","curl : inside cwresponse")
                        cwWaitForTvod = true
                        fetchTVoD(isShowLoader, true)
                    }
                    if(!isRental)
                        _railResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    private fun updateWatchlistRentalItems() {
        watchlistRailResponse?.let {
            cwWaitForTvod = false
            watchlistRailResponse?.data?.contentItem = filterRentalExpiry(
                watchlistRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                tvodResponse?.data?.contentItem
            ) ?: ArrayList()
            _railResponse.postValue(SingleEvent(it))
        }
    }


    var lastClickMillis: Long = 0
    fun getPreviouslyUsedMobileNumbers() {
        val now: Long = SystemClock.elapsedRealtime()
        if (now - lastClickMillis > 1000) {
            loginAPICall()
        }
        lastClickMillis = now
    }


    fun loginAPICall() {
        setProgressing(true)
        if (::loginDisposable.isInitialized) {
            loginDisposable.dispose()
        }
        useCase.run {
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
                        loginDisposable = d
                        addDisposable(d)
                    }
                })
        }
    }
}
