package com.tatasky.binge.ui.features.home.sub

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.ApiCallback
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.*
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.data.networking.services.CommonService
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.interfaces.*
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseViewModel
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.adapter.HomeAdapter
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PROVIDER
import com.tatasky.binge.utils.RECOMMENDATION
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.collections.ArrayList

class SubViewModel @Inject constructor(
    val useCase: CommonUseCase,
    val commonService: CommonService,
    val sharedPrefs: PrefsRepo,
    var homeAnalytics: HomeAnalytics,
    var miscAnalytics: MiscAnalytics
) : CancellationBaseViewModel(useCase,sharedPrefs) {

    var packUpdated: Boolean =false
    var sizeOfAdapter: Int = 0
    var isLoadMore: Boolean = false
    var isUserLoggedIn : Boolean = false
    var onlyMessage: Boolean = false
    var isPullToRefresh = false
    var subscribed: Boolean = false
    var unsubscribed: Boolean = false
    var cwWaitForTvod : Boolean = false
    var watchlistWaitForTvod = false
    private var cwRailResponse: RecommendationResponse? = null
    private var watchlistRailResponse: RecommendationResponse? = null
    var tvodResponse: RecommendationResponse? = null
    private var items: MutableList<HomeResponse.Items> = mutableListOf()
    private var isRemoved: Boolean = false
    private var isRemovedTvod: Boolean = false
    private var tvodPosition: Int = -1
    private var cwPosition: Int = -1
    private var watchlistPosition : Int = -1
    private var isTvodHitOngoing = false
    var isContinueWatching = false
    var isGameFav = false
    var isWatchlist = false
    var isTvodRail = false
    val PAGELIMIT = 10
    var pageOffset = 0
    var isKidsPage = false
    var latestPageOffsetFromAPI = 0
    private lateinit var pageType: String
    private var pageName: String?= null
    private var pageNameDrp: String = ""
    var searchPageName: String?= null
    var provider : String? = null
    private val updateSwipeRefresh = MutableLiveData<SingleEvent<Boolean>>()
    private val _clickedItem = MutableLiveData<SingleEvent<ContentItemTransitions>>()
    private val _seeAllClickedRail = MutableLiveData<SingleEvent<SeeAllTransition>>()
    private val homeResponse = MutableLiveData<SingleEvent<HomeResponse>>()
    private val _changedTotalRailsCount = MutableLiveData<SingleEvent<Int>>()
    private val _afterAllUpdates = MutableLiveData<SingleEvent<Int>>()
    private var vrHierarchyResponse = HierarchyResponse()
    private var useVrData = false
    private var taHierarchyResponse = HierarchyResponse()
    //    private val _previouslyUsedMobileNumberResponse =
//        MutableLiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>>()
//    private val _previouslyUsedMobileNumberError =
//        MutableLiveData<SingleEvent<ErrorModel>>()
    private var totalRails = 0
    private var watchItem : HomeResponse.Items? = null
    private var cwItem : HomeResponse.Items? = null
    private var tvodItem : HomeResponse.Items? = null
    private var gameFavItem  : HomeResponse.Items? = null

    var isOnProgress = 0
    private lateinit var homeDisposable: Disposable

    private var taHeroBannerConfigData: TaHeroBanner? = null
    private var isTaHeroBannerAdded = false
    private val mCategoryResponse =
        MutableLiveData<SingleEvent<LeftMenuResponse>>()


    fun setpackUpdated(updated : Boolean){
        packUpdated=updated
    }
    private val mItemClickListenere = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            sectionType: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?
        ) {
            val extras = if (!transitions.isNullOrEmpty())
                FragmentNavigatorExtras(*transitions.toTypedArray())
            else FragmentNavigatorExtras()
            iListItem.railName = if(sectionType.equals(ItemViewType.HERO_BANNER.name, true)) EVENT_VALUE_RAIL_HB else railTitle
            iListItem.source = pageName ?: ""
            iListItem.origin = if(RECOMMENDATION.equals(origin,true)) RECOMMENDATION else com.tatasky.binge.utils.EDITORIAL
            iListItem.railCategory =
                if (iListItem.origin.equals(
                        com.tatasky.binge.utils.EDITORIAL,
                        true
                    ) || iListItem.origin.equals(com.tatasky.binge.utils.RECOMMENDATION, true)
                ) "RAIL" else origin ?: ""
            iListItem.contentPosition = "${iItemPosition + 1}"
            e(
                "SubFragment",
                "origin : ${iListItem.origin}," +
                        " railTitle : $railTitle," +
                        " iListItem.source :  ${iListItem.source} ," +
                        " railCategory : ${iListItem.railCategory}," +
                        " contentPosition : ${iListItem.contentPosition}"
            )
            iListItem.railPosition = iSectionPosition.toString()
            if(sectionType.equals(ItemViewType.HERO_BANNER.name, true)
                && true == iListItem.heroBannerType?.equals(HB_SEE_ALL, true)
                && null != iListItem.railId?.toIntOrNull()){

                mSeeAllClickListener.onSeeAllClick(Pair(iListItem.railId.toIntOrNull()!!, ""),
                    sectionType = HB_SEE_ALL_VALUE,
                    railPosition = iListItem.railPosition.toIntOrNull()?:0,
                    placeHolder = "",
                    configType = iListItem.origin,
                    isMixedRail = false,
                    isPrepand = false,
                    item = null,
                    trendingProvider = null,
                    backgroundImage = iListItem?.backgroundImage,
                    layoutType = iListItem?.layoutType,
                    refId = iListItem?.refId
                )
            } else {
                if (iListItem.id == "0") {
                    iListItem.id = iListItem.contentId
                }
                _clickedItem.postValue(
                    SingleEvent(
                        ContentItemTransitions(
                            iListItem,
                            extras,
                            sectionType,
                            gamesMixpanelInfoModel
                        )
                    )
                )
            }
            val source= when(sectionType){
                EventConstants.TYPE_HERO-> {
                    EVENT_VALUE_RAIL_HB
                }
                EventConstants.TYPE_APPS-> APPS
                else-> RAIL
            }
            if(sectionType == EventConstants.TYPE_MID_SCROLL){
                trackMidScrollClicked()
            }

            if(sectionType.equals(ItemViewType.HERO_BANNER.name, true)){
                /*homeAnalytics.trackHBView(hbNumber = (iItemPosition+1).toString(),
                    configType = iListItem.origin.toUpperCase(),
                    contentTitle = iListItem.title,
                    contentType = iListItem.contentType,
                    partnerHome = provider!=null,
                    pageName = pageName ?: "",
                    partner = iListItem.provider)*/
                if (iListItem.contentType.equals(TYPE_GAMES, true)) {
                    var gamePageName = ""
                    if(pageName.equals(PROVIDER_GAMEZOP,true)){
                        gamePageName = SOURCE_GAMES
                    } else {
                        gamePageName = pageName?:""
                    }
                    miscAnalytics.trackGameHeroBannerClick(
                        timestamp = getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT), // DONE
                        heroBannerNumber = (iItemPosition + 1).toString(),
                        pageName = gamePageName,
                        railTitle = iListItem.railName,
                        railPosition = iListItem.railPosition,
                        railType = com.tatasky.binge.analytics.EDITORIAL,
                        railCategory = EVENT_VALUE_RAIL_HB,
                        gameGenre = iListItem.getSubTitle(),
                        gamePartner = iListItem.provider,
                        gamePosition = (iItemPosition + 1).toString(),
                        gameRating = iListItem.gameRating,
                        gameTitle = iListItem.title,
                        freeGame = if (sharedPrefs.getSubscribedPack() == null || sharedPrefs.getSubscribedPack()?.isInactive != false) YES else NO,
                        releaseYear = iListItem.releaseYear ?: "",
                        deviceType = PLATFORM_ANDROID_CAPS,
                        packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                        packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                        source = gamePageName
                    )
                }
                else {
                    miscAnalytics.trackMixPanelHeroBannerClicks(
                        getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT),
                        contentTitle = iListItem.title,
                        bannerPosition = (iItemPosition + 1).toString(),
                        contentType = iListItem.contentType,
                        pageName = iListItem.source,
                        partnerHome = provider != null,
                        partner = iListItem.provider,
                        railType = iListItem.contentConfigType,
                        railCategory = iListItem.railCategory,
                        iListItem.language?.joinToString(",")
                            ?: "",
                        iListItem.language?.getOrNull(0) ?: "",
                        iListItem.genres.joinToString(",")
                            ?: "",
                        iListItem.genres.getOrNull(0) ?: "",
                        iListItem.provider,
                        if (ContentUtil.isContentAuth(
                                iListItem.contractName,
                                sharedPrefs.getSubscribedPack(),
                                sharedPrefs.getLoginStatus(),
                                iListItem.provider,
                                iListItem.partnerSubscriptionType
                            )
                        )
                            YES
                        else
                            NO,
                        iListItem.categoryType,
                        iListItem.masterRating,
                        iListItem.getVodOrParentTitle() ?: iListItem.title,
                        if (ContentUtil.isFreeContent(iListItem.partnerSubscriptionType))
                            YES
                        else
                            NO,
                        iListItem.releaseYear ?: "",
                        PLATFORM_ANDROID,
                        iListItem.actor?.joinToString(",")
                            ?: "",
                        pageName ?: "",
                        sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                        NO,
                        if (iListItem.liveContent) YES else NO,
                        iListItem.contentConfigType
                    )
                }
            }

            if (!(sectionType == EventConstants.TYPE_START_FREE_TRIAL || sectionType == EventConstants.TYPE_SELECT_PAID_PACK || sectionType == EventConstants.TYPE_FREE_TRIAL_UPGRADE)) {
                //Track content item click on a rail/banner etc

                val contentAuth=isFreeContent(iListItem.contractName,sharedPrefs.getPartnerIdsList(),iListItem.partnerId?:"",sharedPrefs.getSubscribedPack()?.subscriptionStatus) || !PREMIUM.equals(iListItem.partnerSubscriptionType,true)
                trackContentItemClick(
                    iListItem,
                    iItemPosition + 1,
                    iSectionPosition,
                    source, iListItem.railName,
                    pageName ?: "",
                    sectionType,
                    if (contentAuth) YES else NO,
                    pageOffset
                )
            } else {
                val nudgeType =
                    if (sectionType == EventConstants.TYPE_FREE_TRIAL_UPGRADE) UPGRADE else "Free"
                trackNudgeClick(
                    sharedPrefs.getDthStatusFreemium(),
                    (pageName?:"Home") + "screen",
                    iListItem.availableDays,
                    iListItem.currentDay,
                    sharedPrefs.getUserDetails()?.freeTrialAvailed ?: false,
                    nudgeType
                )
            }
        }
    }

    private fun trackMidScrollClicked(){
        miscAnalytics.trackMixPanelMidScrollClick()
    }

    /**Track content item click on a rail/banner etc*/
    private fun trackContentItemClick(
        iListItem: ContentItem,
        iItemPosition: Int,
        iSectionPosition: Int,
        source: String,
        railTitle: String,
        pageName: String,
        sectionType: String,
        contentAuth: String,
        pageOffSet : Int
    ) {
        homeAnalytics.trackContentClick(
            source,
            iItemPosition.toString(),
            iSectionPosition.toString(),
            iListItem,
            railTitle,
            provider != null,
            pageName,
            iListItem.origin.toUpperCase(),
            iListItem.contentConfigType.toUpperCase(
                Locale.getDefault()
            ),
            sharedPrefs,
            sectionType,
            contentAuth,
            pageOffSet
        )
        if(!sharedPrefs.getFirstContentClick()) {
            homeAnalytics.trackFirstClick(iListItem)
            sharedPrefs.saveFirstContentClick()
        }
    }

    private fun trackNudgeClick(
        dthStatus: String,
        source: String,
        counterValue: String,
        displayedOn: String,
        freeTrialAvailed: Boolean,
        nudgeType:String
    ) {
        homeAnalytics.trackNudgeClick(
            dthStatus,
            source,
            counterValue,
            displayedOn,
            freeTrialAvailed,
            nudgeType
        )
    }

    private val mContentViewListener = object : CommonContentViewListener {
        override fun onContentViewed(
            hbNumber: String,
            contentItem: ContentItem
        ) {
            homeAnalytics.trackHBView(
                getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT),
                contentTitle = contentItem.title,
                bannerPosition = hbNumber,
                contentType = contentItem.contentType,
                pageName = pageName ?: "",
                partnerHome = provider != null,
                partner = contentItem.provider,
                railType = contentItem.contentConfigType,
                railCategory = contentItem.railCategory,
                contentItem.language?.joinToString(",")
                    ?: "",
                contentItem.language?.getOrNull(0) ?: "",
                contentItem.genres.joinToString(",")
                    ?: "",
                contentItem.genres.getOrNull(0) ?: "",
                contentItem.provider,
                if (ContentUtil.isContentAuth(
                        contentItem.contractName,
                        sharedPrefs.getSubscribedPack(),
                        sharedPrefs.getLoginStatus(),
                        contentItem.provider,
                        contentItem.partnerSubscriptionType
                    )
                )
                    YES
                else
                    NO,
                contentItem.categoryType,
                contentItem.masterRating,
                contentItem.getVodOrParentTitle() ?: contentItem.title,
                if (ContentUtil.isFreeContent(contentItem.partnerSubscriptionType))
                    YES
                else
                    NO,
                contentItem.releaseYear ?: "",
                PLATFORM_ANDROID,
                contentItem.actor?.joinToString(",")
                    ?: "",
                pageName ?: "",
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                NO,
                if (contentItem.liveContent) YES else NO,
                contentItem.contentConfigType,
                configType = contentItem.origin.uppercase(Locale.getDefault()),
            )
        }
    }

    private val mRailScrollListener = object : RailScrollListener{
        override fun onRailScrolled(
            railName: String,
            position: Int,
            railType: String,
            railCategory: String
        ) {
            homeAnalytics.trackRailWatched(
                railName,
                position.toString(),
                pageName ?: "",
                partnerHome = provider != null,
                partnerName = provider ?: "",
                railType,
                railCategory,
                getTimeInUTC(System.currentTimeMillis(), ANALYTICS_TIME_FORMAT),
                DEVICE_TYPE,
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            )
        }
    }

    private val mSeeAllClickListener = object : CommonSeeAllClickListener {
        override fun onSeeAllClick(
            railIdName: Pair<Int, String>,
            sectionType: String,
            railPosition: Int?,
            placeHolder: String,
            configType: String?,
            trendingProvider: String?,
            isMixedRail: Boolean,
            isPrepand: Boolean,
            item : HomeResponse.Items?,
            backgroundImage:String?,
            layoutType: String?,
            refId : String,
            packName : String?
        ) {
            if(item?.contentItem?.getOrNull(0)?.contentType.equals(TYPE_GAMES,true)){
                var gamePageName = ""
                if(pageName.equals(PROVIDER_GAMEZOP,true)){
                    gamePageName = SOURCE_GAMES
                } else {
                    gamePageName = pageName?:""
                }
                homeAnalytics.trackGameSeeAll(
                    gamePageName,
                    railIdName.second,
                    "${(railPosition?:1)}",
                    item?.contentItem?.getOrNull(0)?.provider?:"",
                    com.tatasky.binge.analytics.EDITORIAL,
                    RAIL.uppercase(Locale.getDefault())
                )
            } else {
            homeAnalytics.trackHomeSeeAll(pageName?:"",
                (if(sectionType.equals(HB_SEE_ALL_VALUE, true)) sectionType else configType?:sectionType).toUpperCase(),
                provider?:trendingProvider?:SOURCE_MIX,
                railIdName.second, railPosition?.toString()?:"",
                provider!=null,
                sectionType, //need-confirmation
                item?.sectionType?:"", //need-confirmation
                provider!=null)
            e("SubViewModel","MixedRail isMixedRail:$isMixedRail, isPrepand : $isPrepand")}
            _seeAllClickedRail.postValue(
                SingleEvent(
                    SeeAllTransition(
                        railIdName,
                        sectionType,
                        placeHolder,
                        configType?.toUpperCase() ?: com.tatasky.binge.analytics.EDITORIAL,
                        if (sectionType.equals(HB_SEE_ALL_VALUE)) HB_SEE_ALL_VALUE else pageName
                            ?: "",
                        isMixedRail,
                        isPrepand,
                        item,
                        backgroundImage = backgroundImage,
                        layoutType = layoutType,
                        railPosition = railPosition,
                        refId = refId,
                        packName = packName
                    )
                )
            )

        }
    }

    private val mAdapter =
        HomeAdapter(
            mutableListOf(),
            mItemClickListenere,
            sharedPrefs.getCloudenieryUrl(),
            mSeeAllClickListener,
            mContentViewListener,
            mRailScrollListener,
            sharedPrefs.getProviderLogo(),
            sharedPrefs.getUserDetails()?.freeTrialAvailed,
            sharedPrefs.getFreeTrialStartupData(),
            sharedPrefs = sharedPrefs,
            homeAnalytics
        )

    private fun rotateHero(){
        val increment = sharedPrefs.getCurrentStartingPosition()
        mAdapter.setHeroPosition(increment)
    }

//    fun fetchHomeDataDrp(isShowLoader: Boolean) {
//        if (pageOffset == 0) {
//            //rotateHero()
//            isUserLoggedIn = sharedPrefs.getLoginStatus()
//            isLoadMore = false
//        }
//        if (::pageType.isInitialized) {
//            setProgressing(isShowLoader)
//            isPullToRefresh = true
//            val langList = TextUtils.join(", ", sharedPrefs.getPrefLanguages())
//            var packName = "Guest"
//            if (sharedPrefs.getLoginStatus()) {
//                packName = if (SubscriptionPackStatusEnum.ACTIVE.status.equals(
//                        sharedPrefs.getSubscribedPack()?.subscriptionStatus,
//                        true
//                    )
//                )
//                    sharedPrefs.getSubscribedPack()?.productName ?: "Freemium"
//                else
//                    "Freemium"
//            }
//            val request =
//                HomeRequest(
//                    pageType, PAGELIMIT.toString(), pageOffset.toString(),
//                    subscribed, unsubscribed, langList, packName = packName
//                )
//            if (pageOffset > 0) {
//                showRecyclerLoading()
//            } else
//                isTaHeroBannerAdded = false
//
//            isOnProgress = 0
//
//            val finalHierarchyStructure =
//                if (useVrData) vrHierarchyResponse else taHierarchyResponse
//
//
//            val fromIndex = pageOffset.coerceAtMost(finalHierarchyStructure.data?.size?:0)
//            val toIndex = (fromIndex + 10).coerceAtMost(finalHierarchyStructure.data?.size?:0)
//
//            if(toIndex == finalHierarchyStructure.data?.size)
//                mAdapter.removeLoading()
//
//            val drpResponse = HomeResponse().apply {
//                this.data = HomeResponse.Data().apply {
//                    e("HierarchyData $pageType", "f: $fromIndex t: $toIndex")
//                    this.items =
//                        finalHierarchyStructure.data?.subList(
//                            fromIndex,
//                            toIndex
//                        )?.toMutableList()
//                    this.total = finalHierarchyStructure.data?.size ?: 0
//                    this.offset = pageOffset
//                    this.limit = "10"
//                }
//            }
//            homeResponse.postValue(SingleEvent(drpResponse))
//
//
//        } else {
//            setError(ErrorModel(message = "Page Not Found"))
//        }
//    }



    fun executeHomePage(request: HomeRequest): Single<HomeResponse> {

        val finalHierarchyStructure =
            if (useVrData) vrHierarchyResponse else taHierarchyResponse
        val fromIndex = pageOffset.coerceAtMost(finalHierarchyStructure.data?.size?:0)
        val toIndex = (fromIndex + 10).coerceAtMost(finalHierarchyStructure.data?.size?:0)


        return Single.just(
            HomeResponse().apply {
                this.data = HomeResponse.Data().apply {
                    this.items =
                        finalHierarchyStructure.data?.subList(
                            fromIndex,
                            toIndex
                        )?.toMutableList()
                    this.total = finalHierarchyStructure.data?.size ?: 0
                    this.offset = pageOffset
                    this.limit = "10"
                }
            }
        )
    }




    fun fetchHomeData(isShowLoader: Boolean) {

        //Old Code to load data without DRP
        if(pageOffset == 0) {
            //rotateHero()
            isUserLoggedIn = sharedPrefs.getLoginStatus()
            isLoadMore = false
        }
        if(::pageType.isInitialized) {
            setProgressing(isShowLoader)
            isPullToRefresh = true
            val langList = TextUtils.join(", ", sharedPrefs.getPrefLanguages())
            var packName = "Guest"
            if(sharedPrefs.getLoginStatus()) {
                packName = if (SubscriptionPackStatusEnum.ACTIVE.status.equals(
                        sharedPrefs.getSubscribedPack()?.subscriptionStatus,
                        true
                    )
                )
                    sharedPrefs.getSubscribedPack()?.productName ?: "Freemium"
                else
                    "Freemium"
            }
            val request =
                HomeRequest(
                    pageType, PAGELIMIT.toString(), pageOffset.toString(),
                    subscribed, unsubscribed, langList, packName = packName
                )
            if (pageOffset > 0) {
                showRecyclerLoading()
            } else
                isTaHeroBannerAdded = false
            if (::homeDisposable.isInitialized) {
                homeDisposable.dispose()
            }
            homeDisposable = executeHomePage(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { error ->
                    if (pageOffset == 0) setRetryError(error, false)
                    else {
                        pageOffset -= PAGELIMIT
                        mAdapter.removeLoading()
                    }
                }
                .retryWhen { retryHandler ->
                    retrySubject = PublishSubject.create<Any>()
                    retryHandler.zipWith(retrySubject.toFlowable(
                        BackpressureStrategy.LATEST
                    ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
                }
                .subscribeWith(object : CallbackWrapper<HomeResponse>(), Disposable {
                    override fun onError(error: ErrorModel?) {
                        if (isShowLoader) {
                            setError(error)
                        }
                    }

                    override fun onSuccessResponse(t: HomeResponse) {
                        isOnProgress = 0
                        homeResponse.postValue(SingleEvent(t))
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        homeDisposable = d
                        addDisposable(d)
                    }

                    override fun isDisposed(): Boolean {
                        return true
                    }

                    override fun dispose() {

                    }
                })
            addDisposable(homeDisposable)
        }
        else{
            setError(ErrorModel(message = "Page Not Found"))
        }
    }

    private fun fetchTARails(placeHolder: String, layoutType : String, apiCallback: ApiCallback<RecommendationResponse>) {
        var layoutType = layoutType
        if(layoutType.isEmpty()) layoutType = ItemLayoutType.LANDSCAPE.name
        val subPage = !provider.isNullOrEmpty()
        val request = TARequest(placeHolder,
            PAGE_LIMIT_TA_RAIL,
            null,
            provider = provider?:"",
            body = EmptyBody(),
            subPage = subPage,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            layoutType = layoutType)
        val disposable = useCase.executeTARails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.data == null) {
                        apiCallback.onFailure()
                    } else {
                        e("TARequest", "Inside SuCCESS CODE")
                        apiCallback.onSuccessFullyCallback(it)

                    }
                },
                {
                    e("TARequest", "Inside ERROR CODE: " + it.message)
                    apiCallback.onFailure()
                }
            )
        addDisposable(disposable)
    }

    private fun showRecyclerLoading() {
        mAdapter.addLoading()
    }

    fun updateList(homeResponse: HomeResponse) {
        latestPageOffsetFromAPI = homeResponse.data?.offset?:0
        if (homeResponse.data?.offset ?: 0 == 0) {
            totalRails = homeResponse.data?.total ?: 0
            items.clear()
        }

        /*if (homeResponse.data?.finalItems?.size ?: 0 < homeResponse.data?.items?.size ?: 0) {
            val removedItemsCount =
                (homeResponse.data?.items?.size ?: 0) - (homeResponse.data?.finalItems?.size ?: 0)
            totalRails -= removedItemsCount
        }*/
        _changedTotalRailsCount.postValue(SingleEvent(totalRails))
        var position = 0
        homeResponse.data?.dthStatus = sharedPrefs.getDthStatusFreemium()
        /*Line added to add TA Hero Banner if Banner Rail is coming in CMS
        * and it filter out for specific user or provider*/
        if(latestPageOffsetFromAPI == 0 &&
            homeResponse.data?.total?:0 > 0
            && homeResponse.data?.finalItems?.size?:0 > 0
            && !homeResponse.data?.finalItems!![0].sectionType.equals(HERO_BANNER, true )
        ){
            isOnProgress++
            addHeroBannerWithTA(null)
        }
        /*END of adding*/

        for (item in (homeResponse.data?.items ?: emptyList<HomeResponse.Items>())) {
            e("SubViewModel","item.sectionSource : ${item.sectionSource} ${item.id} ${pageType}")
            if(item.sectionSource.equals(ItemViewType.PRIME.name, true)){
                val primePackDetails = sharedPrefs.getSubscribedPack()?.primePackDetails ?: sharedPrefs.getPrimePackDetails()
                if(true == primePackDetails?.let { it.isActive || it.isSuspended })
                    item.title = sharedPrefs.getConfigResponse()?.data?.config?.amazonSubscribedTitle?:item.title
                else
                    item.title = sharedPrefs.getConfigResponse()?.data?.config?.amazonUnSubscribedTitle?:item.title
            }
            if (item.sectionType.equals(HERO_BANNER, true)) {
                isOnProgress++
                fetchRailContentForHeroBanner(item)
//                addHeroBannerWithTA(item)
            }
            else if(item.sectionSource.equals(PROVIDER, true)){

                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        fetchEditorialRails(item)
                    }

                if(sharedPrefs.isActivePack()) {
                    val selectedPartners = sharedPrefs.getPartnerIdsList()
                    item.filteredContentItems = checkPartnerSubscription(selectedPartners, item.filteredContentItems)
                }
            }
            else if(sharedPrefs.getLoginStatus() && item.sectionSource.equals(TVOD, ignoreCase = true)){
                tvodItem = item
                tvodPosition = homeResponse.data?.finalItems?.indexOf(item) ?: -1
                isTvodRail = true
                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        if(tvodResponse != null)
                            updateTvodContent(tvodResponse!!, item)
                        else {
                            e("SubViewModel","curl : inside tvod rail")
                            fetchTVOD(false)
                        }
                    }

            }
            else if (item.sectionSource.equals(WATCHLIST,ignoreCase = true) && sharedPrefs.getLoginStatus()) {
                watchItem = item
                watchlistPosition = homeResponse.data?.finalItems?.indexOf(item) ?: -1
                isWatchlist = true
                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        fetchWatchlistItem(item)
                    }
            }
            else if (item.sectionSource.equals(CONTINUE_WATCHING,ignoreCase = true)) {
                cwItem = item
                cwPosition = homeResponse.data?.finalItems?.indexOf(item) ?: -1
                isContinueWatching = true
                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        fetchCWData(item)
                    }
            }
            else if (item.sectionSource.equals(RECOMMENDATION, ignoreCase = true)) {
                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        item.configType = RECOMMENDATION
                        fetchRailContentForTA(item)
//                        fetchTARails(item)
                    }
            }
            else if (item.sectionSource.equals(ItemViewType.LANGUAGE.name,ignoreCase = true)) {
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        isOnProgress++
//                        getPrefLangGenre(USER_PREFERRED_LANGUAGE_TYPE, item)
                        fetchRailContentForLanguageGenre(item)
                    }
            }
            else if (item.sectionSource.equals(ItemViewType.GENRE.name,ignoreCase = true)) {
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        isOnProgress++
//                        getPrefLangGenre(USER_PREFERRED_GENRE_TYPE, item)
                        fetchRailContentForLanguageGenre(item)
                    }
            }
            else if (
                sharedPrefs.getLoginStatus()
                && item.sectionSource.equals(ItemViewType.FAVOURITES.name, ignoreCase = true)){
                gameFavItem = item
                isGameFav = true
                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        fetchGameFavs(item)
                    }
            }
            else {
                e("rail detail fetch" , "${item.id} ${item.contentItem.size} ${item.sectionSource}")
                isOnProgress++
                Completable.timer(5, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        fetchEditorialRails(item)
                    }
            }
            position++
        }

        items = homeResponse.data?.items ?: mutableListOf()
        updateAdapter()
    }


    /**
     *Merge HierarchyData and Rail content API response
     */
    private fun railContentDataCorrection(
        item: HomeResponse.Items,
        railItem: RecommendationResponse
    ) {
        //TODO [Dec release] : Find a better approach
        if (item.title.isEmpty()) {
            item.title = railItem.data?.title ?: ""
        }
        item.contentItem = (railItem.data?.contentItem ?: ArrayList()) as ArrayList<ContentItem>
        item.shuffleList.clear()
        item.shuffleList.addAll(railItem.data?.shuffleList ?: ArrayList())
        item.backgroundImage = railItem.data?.backgroundImage ?:""
        item.isAutoScroll = railItem.data?.isAutoScroll ?: false
        item.configType = railItem.data?.configType
        item.provider = railItem.data?.provider
        item.taFallbackContentList = railItem.data?.taFallbackContentList
        if (item.recommendationPosition.isNullOrEmpty()) item.recommendationPosition =
            railItem.data?.recommendationPosition
    }


    private fun fetchRailContentForLanguageGenre(item: HomeResponse.Items) {
        val apiCallback = object : ApiCallback<RecommendationResponse> {

            override fun onSuccessFullyCallback(t: RecommendationResponse?) {
                isOnProgress--
                t?.let{
                    railContentDataCorrection(item,t)
                }
                updateAdapter()

                getPrefLangGenre(
                    if (item.sectionSource.equals(ItemViewType.LANGUAGE.name, ignoreCase = true))
                        USER_PREFERRED_LANGUAGE_TYPE
                    else
                        USER_PREFERRED_GENRE_TYPE,
                    item
                )
            }

            override fun onFailure() {
                //Same on Failure logic that is used in fun fetchEditorialRail
                //minimize total count
                isOnProgress--
                val indexOf = items.indexOf(item) ?: -1
                if (indexOf != -1 && item.filteredContentItems.isEmpty()) {
                    items.removeAt(indexOf)
                }
                updateAdapter()
                if (isOnProgress < 0)
                    mAdapter.removeItem(item)
                totalRails--
            }
        }
        fetchRailData(item.id.toString(), apiCallback)
    }


    private fun fetchRailContentForTA(item: HomeResponse.Items) {
        val apiCallback = object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {
                t?.let{
                    railContentDataCorrection(item,t)
                }
                fetchTARails(item)
            }

            override fun onFailure() {
                fetchTARails(item) // TODO DRP Discuss
            }
        }
        fetchRailData(item.id.toString(), apiCallback)
    }

    private fun fetchRailContentForHeroBanner(item: HomeResponse.Items) {
        val apiCallback = object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {
                t?.let{
                    railContentDataCorrection(item,t)
                }
                addHeroBannerWithTA(item)
            }

            override fun onFailure() {
                //minimize total count
                addHeroBannerWithTA(item) // TODO DRP Discuss
            }
        }
        fetchRailData(item.id.toString(), apiCallback)
    }

    private fun fetchEditorialRails(item: HomeResponse.Items) {
        val apiCallback = object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {
                isOnProgress--
                t?.let{
                    railContentDataCorrection(item,t)
                }
                updateAdapter()
                val indexOf = items.indexOf(item) ?: -1
                if (
                    indexOf != -1 &&
                    item.filteredContentItems.isEmpty() &&
                    !isValidItems(item, sharedPrefs.getDthStatusFreemium())
                ) {
                    items.removeAt(indexOf)
                } else {
                    if (isOnProgress < 0) {
                        mAdapter.replaceContentItems(
                            item,
                            t?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
                        )
                    }
                }
            }

            override fun onFailure() {
                //minimize total count
                isOnProgress--
                val indexOf = items.indexOf(item) ?: -1
                if (indexOf != -1 && item.filteredContentItems.isEmpty()) {
                    items.removeAt(indexOf)
                }
                updateAdapter()
                if (isOnProgress < 0)
                    mAdapter.removeItem(item)
                totalRails--
            }
        }
        fetchRailData(item.id.toString(), apiCallback)
    }

    private fun fetchRailData(railId: String, apiCallback: ApiCallback<RecommendationResponse>) {
        val disposable = useCase.fetchEditorialRailData(railId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.data == null) {
                        apiCallback.onFailure()
                    } else {
                        e("RailRequest", "Inside SuCCESS CODE")
                        apiCallback.onSuccessFullyCallback(it)

                    }
                },
                {
                    e("RailRequest", "Inside ERROR CODE: " + it.message)
                    apiCallback.onFailure()
                })
    }


    private fun fetchVRHomeHierarchy(isShowLoader: Boolean, taFailed: Boolean = false) {
        var packName = "Guest"
        if (sharedPrefs.getLoginStatus()) {
            packName = if (SubscriptionPackStatusEnum.ACTIVE.status.equals(
                    sharedPrefs.getSubscribedPack()?.subscriptionStatus,
                    true
                )
            )
                sharedPrefs.getSubscribedPack()?.productName ?: "Freemium"
            else
                "Freemium"
        }
        if (::pageType.isInitialized) {
            homeDisposable =
                useCase.fetchVRHomeHierarchy(pageType, packName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : CallbackWrapper<HierarchyResponse>(), Disposable {
                        override fun onError(error: ErrorModel?) {
                            //TODO DRP Error handling
                        }

                        override fun onSuccessResponse(t: HierarchyResponse) {
                            isOnProgress = 0
                            vrHierarchyResponse = t
                            useVrData = true
                            e("DRP : ", "fetchVRHierarchy ${pageNameDrp}")

                            if (taFailed) {
                                //if ta failed and no cache available whole use VR data
                                sharedPrefs.getHierarchyData(pageNameDrp)?.let { cachedData ->
                                    if (t.data != null && cachedData.data != null) {
                                        intersectHierarchyResponse(t.data!!, cachedData.data!!)
                                    }
                                } ?: run {
                                    vrHierarchyResponse = t
                                }
                            }

                            fetchHomeData(isShowLoader)
                        }

                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            homeDisposable = d
                            addDisposable(d)
                        }

                        override fun isDisposed(): Boolean {
                            return true
                        }

                        override fun dispose() {

                        }
                    })
            addDisposable(homeDisposable)
        }
    }


    fun checkDRPpages(pages: ArrayList<String>?): Boolean {
        pages?.let {
            for (page in it) {
                if (page.equals(pageNameDrp, true))
                    return true
            }
        }
        return false
    }

    /**
     * Method to check which hierarchy data to be fetched TA/VR
     */
    fun fetchHierarchyData(isShowLoader: Boolean) {

        val config = sharedPrefs.getConfigResponse()?.data?.config
        if (config?.bingeAndroidDrpEnabled == false
            || !checkDRPpages(config?.drpPartnerPages)
        ) {
            fetchVRHomeHierarchy(isShowLoader)
            return
        }

        fetchTAHierarchyData(isShowLoader)
    }

    /**
     * This method is used to find intersection of Cached TA data and VR hierarchy response :: fallback mechanism
     */

    fun intersectHierarchyResponse(
        newData: List<HomeResponse.Items>,
        cachedData: List<HomeResponse.Items>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val ids: Set<Int> =
                cachedData.stream().map { obj -> obj.id }.collect(Collectors.toSet())
            val intersect: List<HomeResponse.Items> = newData.stream()
                .filter { obj -> ids.contains(obj.id) }
                .collect(Collectors.toList())

            vrHierarchyResponse = HierarchyResponse().apply {
                this.data = intersect
            }
        } else {
            val list: MutableList<HomeResponse.Items> = ArrayList()
            var ids: Set<Int> = HashSet<Int>()
            for (item in newData)
                ids = setOf(item.id)

            for (cachedItem in cachedData) {
                if (ids.contains(cachedItem.id))
                    list.add(cachedItem)
            }

            vrHierarchyResponse = HierarchyResponse().apply {
                this.data = list
            }
        }

    }



    private fun fetchTAHierarchyData(isShowLoader: Boolean) {
        var packName = "Guest"
        if (sharedPrefs.getLoginStatus()) {
            packName = if (SubscriptionPackStatusEnum.ACTIVE.status.equals(
                    sharedPrefs.getSubscribedPack()?.subscriptionStatus,
                    true
                )
            )
                sharedPrefs.getSubscribedPack()?.productName ?: "Freemium"
            else
                "Freemium"
        }
        if (::pageType.isInitialized) {
            homeDisposable =
                useCase.fetchTAHomeHierarchy(
                    pageType,
                    packName,
                    sharedPrefs.getLoginStatus(),
                    sharedPrefs.getOriginalSubscriberId()
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : CallbackWrapper<HierarchyResponse>(), Disposable {
                        override fun onError(error: ErrorModel?) {
                            //DRP - If TA Hierarchy fails fetch hierarchy from VR
                            fetchVRHomeHierarchy(isShowLoader, true)
                        }

                        override fun onSuccessResponse(t: HierarchyResponse) {
                            isOnProgress = 0
                            useVrData = false
                            e("DRP : ", "fetchTAHierarchy $pageNameDrp")
                            if (!t.data.isNullOrEmpty()) {
                                sharedPrefs.saveHierarchyData(pageNameDrp, t)
                                taHierarchyResponse = sharedPrefs.getHierarchyData(pageNameDrp)!!
                                fetchHomeData(isShowLoader)
                            } else {
                                fetchVRHomeHierarchy(isShowLoader)
                            }
                        }

                        override fun onSubscribe(d: Disposable) {
                            super.onSubscribe(d)
                            homeDisposable = d
                            addDisposable(d)
                        }

                        override fun isDisposed(): Boolean {
                            return true
                        }

                        override fun dispose() {

                        }
                    })
            addDisposable(homeDisposable)
        }
    }


    @SuppressLint("CheckResult")
    private fun fetchGameFavs(item: HomeResponse.Items) {
        val request =  WatchRequest(
            subscriberId = sharedPrefs.getOriginalSubscriberId(),
            profileId = sharedPrefs.getProfileId()!!,
            pagingState = null,
            offset = 0,
            isForceRefresh = false
        )
        useCase.fetchGameFavs(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>(){
                override fun onSuccessResponse(t: RecommendationResponse) {
                    isOnProgress--
                    item.contentItem = t.data?.contentItem as ArrayList<ContentItem>
                    t?.let{
                        railContentDataCorrection(item,t)
                    }
                    updateAdapter()
                    if (isOnProgress < 0) {
                        mAdapter.replaceContentItems(
                            item,
                            t?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
                        )
                    }
                }

                override fun onError(error: ErrorModel?) {
                    isOnProgress--
                    updateAdapter()
                }

            })

    }



    private fun fetchTARails(item : HomeResponse.Items)
    {
        val apiCallback =  object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {
                if(t?.data?.contentItem.isNullOrEmpty() && !item.taFallbackContentList.isNullOrEmpty()){
                    t?.data?.contentItem = item.taFallbackContentList!!
                }
                createTAMixedRail(item, t?.data?.filteredContentItems)
                item.genreType = t?.data?.genreType
                item.languageType = t?.data?.languageType
                if(t?.data?.actionItemTittle?.isNotEmpty() == true){
                    item.title += " ${t.data?.actionItemTittle}"
                }
                handleGenreAndLanguage(item)
                e("SubViewModel","updateAdapter TA $isOnProgress title : ${item.title}, item size:${item.filteredContentItems.size}")
                isOnProgress--
                updateAdapter()
                if (isOnProgress < 0) {
                    mAdapter.replaceContentItems(
                        item,
                        t?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
                    )
                }
            }

            override fun onFailure() {
                //minimize total count
                if(!item.taFallbackContentList.isNullOrEmpty()){
                    createTAMixedRail(item, item.taFallbackContentList)
                }
                val indexOf = items.indexOf(item) ?: -1
                if (indexOf != -1 && item.filteredContentItems.isEmpty()) {
                    items.removeAt(indexOf)
                }
                isOnProgress--
                updateAdapter()
                if (isOnProgress < 0)
                    mAdapter.removeItem(item)
                totalRails--
            }
        }
        fetchTARails(item.placeHolder, item.layoutType, apiCallback)
    }

    private fun updateTvodContent(t: RecommendationResponse, item: HomeResponse.Items) {
        val indexOf = items.indexOf(item) ?: -1
        if (indexOf != -1) {
            items[indexOf].contentItem = t?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
        }
        else{
            item.contentItem = t?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
        }
        e("SubViewModel","updateAdapter from Tvod $isOnProgress")
        isOnProgress--
        t?.let{
            railContentDataCorrection(item,t)
        }
        updateAdapter()
        if(isOnProgress < 0){
            mAdapter.replaceContentItems(item, t?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList())
        }
        val lastPosition = mAdapter.getLastItemPosition()
        item.lastPosition = lastPosition

        if(isRemovedTvod)
            mAdapter.addItem(tvodPosition, item)
        isRemovedTvod = false
    }

    fun fetchTVOD(isCallHomePage : Boolean) {
        e("SubViewModel","curl : inside fetchTvod $isTvodHitOngoing")
        val item = tvodItem
        if(isTvodHitOngoing) return
        isTvodHitOngoing = true
//        setProgressing(isCallHomePage)
        val apiCallback = object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {
                tvodResponse = t
                isTvodHitOngoing = false
                if(isCallHomePage)
                    fetchHomeData(isCallHomePage)
                if(item!= null && t != null) {
                    updateTvodContent(t, item)
                }
                if(cwWaitForTvod){
                    cwWaitForTvod = false
                    cwRailResponse?.data?.contentItem = filterRentalExpiry(
                        cwRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                        tvodResponse?.data?.contentItem
                    ) ?: ArrayList()
                    updateCWItems(item?.refId?:"")
                }
                if(watchlistWaitForTvod){
                    watchlistRailResponse?.data?.contentItem = filterRentalExpiry(
                        watchlistRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                        tvodResponse?.data?.contentItem
                    ) ?: ArrayList()
                    updateWatchlistItems(item?.refId?:"")
                }
            }

            override fun onFailure() {
                //minimize total count
                isTvodHitOngoing = false
                if(cwWaitForTvod){
                    cwWaitForTvod = false
                    cwRailResponse?.data?.contentItem = filterRentalExpiry(
                        cwRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                        tvodResponse?.data?.contentItem
                    ) ?: ArrayList()
                    updateCWItems(item?.refId?:"")
                }
                if(watchlistWaitForTvod){
                    watchlistRailResponse?.data?.contentItem = filterRentalExpiry(
                        watchlistRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                        tvodResponse?.data?.contentItem
                    ) ?: ArrayList()
                    updateWatchlistItems(item?.refId?:"")
                }
                if(isCallHomePage)
                    fetchHomeData(isCallHomePage)
                if(item != null) {
                    val indexOf = items.indexOf(item) ?: -1
                    if (indexOf != -1 && item.filteredContentItems.isEmpty()) {
                        items.removeAt(indexOf)
                    }
                    isOnProgress--
                    updateAdapter()
                    isRemovedTvod = true
                    if (isOnProgress < 0)
                        mAdapter.removeItem(item)
                    totalRails--
                }
            }
        }
        val disposable = useCase.getTvodContent(sharedPrefs.getOriginalSubscriberId()?: "", 0, 10)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.data == null) {
                        apiCallback.onFailure()
                    } else {
                        e("TVODRequest", "Inside SuCCESS CODE")
                        apiCallback.onSuccessFullyCallback(it)

                    }
                },
                {
                    e("TVODRequest", "Inside ERROR CODE: " + it.message)
                    apiCallback.onFailure()
                }
            )
        addDisposable(disposable)
    }


    private fun updateAdapter() {
        e("SubViewModel","updateAdapter : $isOnProgress , offset : $pageOffset" +
                ", latestPageOffsetFromAPI: $latestPageOffsetFromAPI" +
                ", adapter size : ${mAdapter.itemCount}," +
                "sharedPrefs.getLanguageWidgetVisibility() : ${sharedPrefs.getLanguageWidgetVisibility()}")
        if(isOnProgress == 0){
            mAdapter.setPartnerPack(sharedPrefs.getSubscribedPack())
            handleLanguageWidget()
            setPackInAdapter()
            mAdapter.removeLoading()
            mAdapter.updateIsRefresh(isPullToRefresh)
            mAdapter.updateIsPackUpdated(packUpdated)
            isPullToRefresh = false
            packUpdated = false
            updateSwipeRefresh.postValue(SingleEvent(true))
            setProgressing(false)
            if (latestPageOffsetFromAPI == 0) {
                mAdapter.setDthStatus(sharedPrefs.getDthStatusFreemium())
                e("SubViewModel", "updateList offset : $pageOffset , size : ${items.size}")
//                handleLanguageWidget()
                mAdapter.updateList(items)
                rotateHero()
                if(items.size == 0)
                    homeResponse.postValue(SingleEvent(HomeResponse()))
            } else {
                e("SubViewModel","addTomDataList offset : $pageOffset , size : ${items.size}")
                mAdapter.addToList(items)
            }
            sizeOfAdapter = mAdapter.itemCount
        }
    }

    fun setPackInAdapter(){
        mAdapter.setPartnerPack(sharedPrefs.getSubscribedPack())
    }
    fun notifyAdapter(){
        mAdapter.notifyDataSetChanged()
    }

    private fun fetchCWData(item: HomeResponse.Items) {
        getContinueWatchingData(object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {

                cwRailResponse = t
                val isRental : Boolean = isRentalContent(
                    cwRailResponse?.data?.contentItem as ArrayList<ContentItem>? ?: ArrayList())
                if(isRental && tvodResponse != null){
                    e("SubViewModel","curl : inside cwresponse where tvResponse not null")
                    cwWaitForTvod = false
                    cwRailResponse?.data?.contentItem = filterRentalExpiry(
                        cwRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                        tvodResponse?.data?.contentItem
                    ) ?: ArrayList()
                    updateCWItems(item.refId)
                }
                else if(isRental) {
                    e("SubViewModel","curl : inside cwresponse")
                    cwWaitForTvod = true
                    fetchTVOD(false)
                }
                if(!isRental)
                    updateCWItems(item.refId)
            }

            override fun onFailure() {
                //minimize total count
                val indexOf = items.indexOf(item) ?: -1
                if (indexOf != -1 && item.filteredContentItems.isEmpty()) {
                    items.removeAt(indexOf)
                }
                isOnProgress--
                updateAdapter()
                isRemoved = true
                if(isOnProgress < 0)
                    mAdapter.removeItem(item)
                totalRails--
                _afterAllUpdates.postValue(SingleEvent(items.size))
            }
        })
    }
    private fun fetchWatchlistItem(item: HomeResponse.Items) {
        getWatchlistContent(object : ApiCallback<RecommendationResponse> {
            override fun onSuccessFullyCallback(t: RecommendationResponse?) {

                watchlistRailResponse = t
                val isRental : Boolean = isRentalContent(
                    watchlistRailResponse?.data?.contentItem as ArrayList<ContentItem>? ?: ArrayList())
                if(isRental && tvodResponse != null){
                    e("SubViewModel","curl : inside cwresponse where tvResponse not null")
                    watchlistWaitForTvod = false
                    watchlistRailResponse?.data?.contentItem = filterRentalExpiry(
                        watchlistRailResponse?.data?.contentItem as ArrayList<ContentItem>?,
                        tvodResponse?.data?.contentItem
                    ) ?: ArrayList()
                    updateWatchlistItems(item.refId)
                }
                else if(isRental) {
                    e("SubViewModel","curl : inside watchlistresponse")
                    watchlistWaitForTvod = true
                    fetchTVOD(false)
                }
                if(!isRental)
                    updateWatchlistItems(item.refId)
            }

            override fun onFailure() {
                //minimize total count
                val indexOf = items.indexOf(item) ?: -1
                if (indexOf != -1 && item.filteredContentItems.isEmpty()) {
                    items.removeAt(indexOf)
                }
                isOnProgress--
                updateAdapter()
                isRemoved = true
                if(isOnProgress < 0)
                    mAdapter.removeItem(item)
                totalRails--
                _afterAllUpdates.postValue(SingleEvent(items.size))
            }
        })
    }

    private fun updateWatchlistItems(refId: String) {
        isOnProgress--
        if(watchItem != null) {
            val contents =
                watchlistRailResponse?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
            val indexOf = items.indexOf(watchItem!!)
            /*contents.forEach {
                it.refId = refId
            }*/
            if (indexOf != -1) {
                items[indexOf].contentItem = contents
            } else {
                watchItem?.contentItem = contents
            }
            e("SubViewModel","updateAdapter from watchlist $isOnProgress contents: ${contents.size}")
            updateAdapter()
            if (isOnProgress < 0) {
                mAdapter.replaceContentItems(watchItem!!, contents)
            }
            val lastPosition = mAdapter.getLastItemPosition()
            watchItem?.lastPosition = lastPosition

            if (isRemoved && mAdapter.itemCount > watchlistPosition)
                mAdapter.addItem(watchlistPosition, watchItem!!)
            isRemoved = false
            if(contents.size == 0)
                _afterAllUpdates.postValue(SingleEvent(totalRails-1))
        }
    }

    private fun getWatchlistContent(apiCallback: ApiCallback<RecommendationResponse>) {
        val request =
            WatchRequest(
                sharedPrefs.getOriginalSubscriberId(),
                sharedPrefs.getProfileId()!!,
                null, 0,
                isForceRefresh = true
            )

        val disposable = useCase.executeFavouritesList(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.data == null) {
                        apiCallback.onFailure()
                    } else {
                        apiCallback.onSuccessFullyCallback(it)

                    }
                },
                {
                    apiCallback.onFailure()
                }
            )
        addDisposable(disposable)
    }

    private fun updateCWItems(refId: String) {
        isOnProgress--
        if(cwItem != null) {
            val contents =
                cwRailResponse?.data?.filteredContentItems as ArrayList<ContentItem>? ?: ArrayList()
            val indexOf = items.indexOf(cwItem!!)
            /*contents.forEach {
                it.refId = refId
            }*/
            if (indexOf != -1) {
                items[indexOf].contentItem = contents
            } else {
                cwItem?.contentItem = contents
            }
            e("SubViewModel","updateAdapter from cwItem $isOnProgress")
            updateAdapter()
            if (isOnProgress < 0) {
                mAdapter.replaceContentItems(cwItem!!, contents)
            }
            cwItem?.continueWatching = true
            val lastPosition = mAdapter.getLastItemPosition()
            cwItem?.lastPosition = lastPosition

            if (isRemoved && mAdapter.itemCount > cwPosition)
                mAdapter.addItem(cwPosition, cwItem!!)
            isRemoved = false
            if(contents.size == 0)
                _afterAllUpdates.postValue(SingleEvent(totalRails-1))
        }
    }


    private fun getContinueWatchingData(apiCallback: ApiCallback<RecommendationResponse>) {
        val request = CWRequest(
            sharedPrefs.getOriginalSubscriberId(),
            profileId = sharedPrefs.getProfileId()?:"",
            seeAll = false,
            pagingState = null, offset = 0,
            provider = provider,
            isLoggedIn = sharedPrefs.getLoginStatus(),
            uniqueId = sharedPrefs.getAnonymousId()?:""
        )
        val disposable = useCase.executeCWRails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.data == null) {
                        apiCallback.onFailure()
                    } else {
                        apiCallback.onSuccessFullyCallback(it)

                    }
                },
                {
                    apiCallback.onFailure()
                }
            )
        addDisposable(disposable)

    }

    fun getClickedItem(): LiveData<SingleEvent<ContentItemTransitions>> {
        return _clickedItem
    }

    fun getSeeAllClickedRail(): LiveData<SingleEvent<SeeAllTransition>> {
        return _seeAllClickedRail
    }

    fun getAdapter(): HomeAdapter {
        return mAdapter
    }

    fun setPageType(pageTypeValue: String) {
        pageType = pageTypeValue
    }

    fun setPageName(pageNameValue: String) {
        pageName = pageNameValue
        if (mAdapter!=null)
            mAdapter.setPageName(pageName?:"")
    }

    fun setPageNameDrp(pageNameValue: String) {
        pageNameDrp = pageNameValue
    }

    fun getPageNameDrp(): String {
        return pageNameDrp
    }

    fun getHomeData(): LiveData<SingleEvent<HomeResponse>> = homeResponse

    fun getChangedCount(): LiveData<SingleEvent<Int>> = _changedTotalRailsCount

    fun getAfterAllUpdate(): LiveData<SingleEvent<Int>> = _afterAllUpdates

    fun updateListenerForAddPack(onAddPackClickListener: AddPackListener, btnAdd : CharSequence) {
        mAdapter.setAddPackListener(onAddPackClickListener, btnAdd)
    }

    fun refreshContinueWatching() {
        cwItem?.let { fetchCWData(it) }
    }

    fun refreshGameFav(){
        gameFavItem?.let{
            fetchGameFavs(it)
        }
    }


    private fun handleTAHeroBanner(
        pageType: String,
        apiCallback: ApiCallback<RecommendationResponse>
    ) {
        val mTaHeroBannerList =sharedPrefs.getTAHeroBanner()
        e("TAHeroBanner","inside handleTAHeroBanner $mTaHeroBannerList")
        if (mTaHeroBannerList != null && mTaHeroBannerList.isNotEmpty()) {
            for (banner in mTaHeroBannerList) {
                if (banner.pageType == pageType) {
                    e("TAHeroBanner","inside banner page found $banner")
                    taHeroBannerConfigData = banner
                }
            }
        }
        if (taHeroBannerConfigData != null && taHeroBannerConfigData?.count?:0 > 0) {
            e("TAHeroBanner","inside banner page found $taHeroBannerConfigData")
            val request = TARequest(
                taHeroBannerConfigData?.placeHolder ?: "",
                (taHeroBannerConfigData?.count ?:"5").toString(),
                taHeroBannerConfigData?.pageType,
                isLoggedIn = sharedPrefs.getLoginStatus(),
                body = EmptyBody())
            val disposable = useCase.executeTARails(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.data == null) {
                            e("TAHeroBanner","inside herobanner fail ${it.data?.contentItem?.size}")
                            apiCallback.onFailure()
                        } else {
                            e("TAHeroBanner","inside herobanner success ${it.data?.contentItem?.size}")
                            apiCallback.onSuccessFullyCallback(it)
                        }
                    },
                    {
                        e("TARequest", "Inside ERROR CODE: " + it.message)
                        apiCallback.onFailure()
                    }
                )
            addDisposable(disposable)
        }
        else{
            isOnProgress--
            updateAdapter()
        }
    }

    private fun addHeroBannerWithTA(item: HomeResponse.Items?) {
        e("TAHeroBanner","inside addHeroBannerWithTA isTaBannerAdded : $isTaHeroBannerAdded")
        if (!isTaHeroBannerAdded) {
            Completable.timer(5, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    e("TAHeroBanner","inside runner")
                    handleTAHeroBanner(pageType,
                        object : ApiCallback<RecommendationResponse> {
                            override fun onSuccessFullyCallback(it: RecommendationResponse?) {
                                if(!isTaHeroBannerAdded) {
                                    if (item == null) {
                                        val heroBanner = HomeResponse.Items()
                                        heroBanner.sectionType = "HERO_BANNER"
                                        heroBanner.title = "TA_HERO_BANNER"
                                        heroBanner.contentItem =
                                            it?.data?.filteredContentItems as ArrayList<ContentItem>?
                                                ?: ArrayList()
                                        items.add(0, heroBanner)
                                    } else {
                                        if(it?.data?.contentItem.isNullOrEmpty() && !item.taFallbackContentList.isNullOrEmpty()){
                                            it?.data?.contentItem = item.taFallbackContentList!!
                                        }
                                        createTAHeroBanner(item, it?.data?.filteredContentItems)
                                    }
                                }
                                isTaHeroBannerAdded = true
                                e("SubViewModel","updateAdapter from HeroBanner $isOnProgress")

                                if(isOnProgress <= 0)
                                    isOnProgress = 0
                                else
                                    isOnProgress--
                                updateAdapter()
                            }

                            override fun onFailure() {
                                isOnProgress--
                                item?.taFallbackContentList?.let{
                                    createTAHeroBanner(item, it)
                                }
                                updateAdapter()
                            }
                        })
                }
        }
        else{
            isOnProgress--
            updateAdapter()
        }
    }

    private fun createTAHeroBanner(
        item: HomeResponse.Items?,
        it: List<ContentItem>?
    ) {
        if (item != null && taHeroBannerConfigData != null) {

            var uniqueItems =  removeDuplicateContent(item.contentItem, it ?: ArrayList())
            val heroBannerIncrementValue = sharedPrefs.getConfigResponse()?.data?.config?.heroBannerRotation?.heroBannerIncrementValue?:0
            if(heroBannerIncrementValue >0) {

                if (taHeroBannerConfigData?.position.equals("APPEND")) {
                    item.contentItem.addAll(uniqueItems)
                } else {
                    item.contentItem.addAll(0, uniqueItems)
                }
            }
            else{
                Log.e("countItemSize", "${item.contentItem.size} ${uniqueItems.size}")
                for (i in 0 until item.contentItem.size)
                {
                    e("SubViewHero","uniqueItems.size: ${uniqueItems.size}," +
                            " item.contentItem[i].position : ${item.contentItem[i].position}")
                    if (item.contentItem[i].position ==0 || uniqueItems.size < item.contentItem[i].position ) {
                        uniqueItems.add(item.contentItem[i])
                    }
                    else{
                        uniqueItems.add(item.contentItem[i].position - 1,item.contentItem[i])
                    }
                }
                item.contentItem.clear()
                item.contentItem.addAll(uniqueItems)
            }
        }
    }


    fun refreshTvodRail() {
        e("SubViewModel","curl : inside refreshTvodRail")
        tvodItem?.let { fetchTVOD(false) }
    }


    @SuppressLint("DefaultLocale")
    private fun handleGenreAndLanguage(item: HomeResponse.Items) {
        if (item.title.toLowerCase().contains("genre")) {
            if (item.genreType != null && !item.genreType.isNullOrEmpty()) {
                if (item.title.toLowerCase().contains("genre1")) {
                    item.title =
                        item.title.replace("genre1", item.genreType!!, true)
                }
                if (item.title.toLowerCase().contains("genre2")) {
                    item.title =
                        item.title.replace("genre2", item.genreType!!, true)
                }
                if (item.title.toLowerCase().contains("genre")) {
                    item.title =
                        item.title.replace("genre", item.genreType!!, true)
                }
            }
        }
        if (item.title.toLowerCase().contains("language")) {
            if (item.languageType != null && !item.languageType.isNullOrEmpty()) {
                if (item.title.toLowerCase().contains("language1")) {
                    item.title =
                        item.title.replace("language1", item.languageType!!, true)
                }
                if (item.title.toLowerCase().contains("language2")) {
                    item.title =
                        item.title.replace("language2", item.languageType!!, true)
                }
                if (item.title.toLowerCase().contains("language")) {
                    item.title =
                        item.title.replace("language", item.languageType!!, true)
                }
            }
        }
    }

    fun getPullToRefreshChange():  LiveData<SingleEvent<Boolean>> = updateSwipeRefresh


    private fun createTAMixedRail(
        item: HomeResponse.Items?,
        it:  List<ContentItem>?
    ) {
        if(it?.size?:0 > 0) {
            if (item != null && it != null) {
                if (item.contentItem.size <= 0)
                    item.recommendationPosition = null

                val uniqueItems =  removeDuplicateContent(item.contentItem, it)
                if ("APPEND".equals(item.recommendationPosition, true)) {
                    item.contentItem.addAll(0, uniqueItems)
                } else {
                    item.contentItem.addAll(uniqueItems)
                }
            }
        }
        else if (item != null){
            item.contentItem = ArrayList()
        }
    }

    @SuppressLint("CheckResult")
    fun fetchMenuItems(lambda: (list: List<LeftMenuItem>) -> Unit) {
        setProgressing(true)

        useCase.executeHomeMenuItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setRetryError(error, false)
                //setError(ErrorModel())
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<LeftMenuResponse>() {
                override fun onError(error: ErrorModel?) {
                }

                override fun onSuccessResponse(t: LeftMenuResponse) {
                    t.data?.items?.let { menuItemList ->
                        LeftMenuResponseCache.setLeftMenuItemList(menuItemList)
                        lambda(menuItemList)
                    }?: run {
                        val menuItemList = ArrayList<LeftMenuItem>()
                        LeftMenuResponseCache.setLeftMenuItemList(menuItemList)
                        lambda(menuItemList)
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    fun getLanguageWidgetVisibility():Boolean{
        return mAdapter.getLanguageWidgetVisibility()
    }

    fun handleLanguageWidget() {
        mAdapter.setLanguageWidgetVisibility(
            sharedPrefs.getLanguageWidgetVisibility()
        )
    }

    fun setGameWidgetVisibilityInAdapter(show : Boolean){
        mAdapter.setGameWidgetVisibility(
            show
        )
    }


    @SuppressLint("CheckResult")
    private fun getPrefLangGenre(
        type:String,
        langGenreResponse: HomeResponse.Items? = null) {
        useCase.getPrefLangGenre(sharedPrefs.getLoginStatus(), type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<GenreListResponse>() {
                override fun onSuccessResponse(t: GenreListResponse) {
                    val lowercaseSet = LinkedHashSet<String>()
                    for(data in t.data?.list?: mutableListOf()){
                        lowercaseSet.add(data.lowercase())
                    }
                    langGenreResponse?.contentItem?.let {
                        val mergedData = mergeUserPreference(lowercaseSet, it)
                                as ArrayList<ContentItem>
                        langGenreResponse.contentItem = mergedData
                        if (isOnProgress < 0) {
                            mAdapter.replaceContentItems(
                                langGenreResponse,
                                mergedData
                            )
                        }
                    }
                }

                @SuppressLint("CheckResult")
                override fun onError(error: ErrorModel?) {

                }
            })
    }

    private fun mergeUserPreference(
        set: HashSet<String>,
        value: ArrayList<ContentItem>,
    ): List<ContentItem> {
        val contentItemList = mutableListOf<ContentItem>()
        if (value.isNotEmpty()) {
            val idValueMap = value.associateBy {it.title.lowercase()}
            val sortedList = set.mapNotNull { idValueMap[it] }
            contentItemList.addAll(sortedList)
            value.removeAll(sortedList)
            contentItemList.addAll(value)
            return contentItemList
        }
        return contentItemList
    }

    fun setViewModelError(error:ErrorModel?){
        setError(error)
    }

    @SuppressLint("CheckResult")
    fun setAppRatingEligibility(requestBody: SetAppRatingRequest) {
//        setProgressing(true)
        useCase.setAppRatingEligibility(requestBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<AppRatingResponse>() {
                override fun onError(error: ErrorModel?) {
//                    setProgressing(false)
//                    setError(error)
                }

                override fun onSuccessResponse(t: AppRatingResponse) {
//                    setProgressing(false)
                    when(t.code){
                        CODE_SUCCESS -> {
                            sharedPrefs.setIsEligibleForAppRating(t.data?.allowRating == true)
                        }
                        else -> {
//                            setError(ErrorModel(t.code , t.message))
                        }
                    }
                }
            })
    }

    fun refreshWatchlistRail() {
        e("SubViewModel","curl : inside refreshWatchRail")
        watchItem?.let { fetchWatchlistItem(it) }
    }
}