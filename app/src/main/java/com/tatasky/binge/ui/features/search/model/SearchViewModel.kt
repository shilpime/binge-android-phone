package com.tatasky.binge.ui.features.search.model

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.analytics.SOURCE_SEARCH
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.getSearchResultContentAnalyticsModel
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.EmptyBody
import com.tatasky.binge.data.networking.models.requests.SearchRequest
import com.tatasky.binge.data.networking.models.requests.TARequest
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.interfaces.ContentItemTransitions
import com.tatasky.binge.interfaces.FilterItemTransitions
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.SuggestionSuggestors
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.model.RailItemsModel
import com.tatasky.binge.ui.features.search.SearchAnalytics
import com.tatasky.binge.ui.features.search.adapter.HistoryAdapter
import com.tatasky.binge.ui.features.search.adapter.SuggestionsAdapter
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class SearchViewModel @Inject constructor(val useCase: CommonUseCase, val sharedPrefs: PrefsRepo) :
    BaseViewModel() {
    @Inject
    lateinit var searchAnalytics: SearchAnalytics
    private var taSearchResultContentItems: List<ContentItem>? = null
    private var intentUrl: String? = null
    private lateinit var loginDisposable: Disposable
    var onlyMessage: Boolean = false
    private val filterMap: HashMap<String, RecommendationResponse> = HashMap()

    private val _searchResponse = MutableLiveData<SingleEvent<RecommendationResponse>>()
    private val _searchSuggestionResponse = MutableLiveData<SingleEvent<RecommendationResponse>>()
    private val _searchLandingResponse = MutableLiveData<SingleEvent<RecommendationResponse>>()
    private val _searchRailsResponse = MutableLiveData<SingleEvent<HomeResponse>>()
    private var _searchLanguageFilterResponse: HomeResponse? = null
    private var _searchgenreFilterResponse: HomeResponse? = null
    private val _searchUserPreferredLanguageFilterResponse =
        MutableLiveData<SingleEvent<List<ContentItem>>>()
    private val _searchUserPreferredGenreFilterResponse =
        MutableLiveData<SingleEvent<List<ContentItem>>>()

    private var _lastSearchedQuery = ""
//    private val _searchFiltersResponse = MutableLiveData<SingleEvent<RecommendationResponse>>()

    private val _searchFiltersResponse =
        MutableLiveData<SingleEvent<HashMap<String, RecommendationResponse>>>()
    private val _clickedItem = MutableLiveData<SingleEvent<ContentItemTransitions>>()
    private val _clickedSuggestedItem = MutableLiveData<SingleEvent<ContentItemTransitions>>()
    private val _clickedHistory = MutableLiveData<SingleEvent<String>>()
    private var _languageModel = MutableLiveData<String>()
    private lateinit var _filterDisposable: Disposable
    private lateinit var _searchDisposable: Disposable
    private var _genreModel = MutableLiveData<String>()
    private val _filterItem = MutableLiveData<SingleEvent<FilterItemTransitions>>()
    private val _changedTotalRailsCount = MutableLiveData<Int>()
    private val _appRailItemClick = MutableLiveData<SingleEvent<ContentItemTransitions>>()

    private val _previouslyUsedMobileNumberResponse =
        MutableLiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>>()
    val previouslyUsedMobileNumberResponse: LiveData<SingleEvent<PreviouslyUsedMobileNumbersResponse?>> =
        _previouslyUsedMobileNumberResponse

    private val _previouslyUsedMobileNumberError =
        MutableLiveData<SingleEvent<ErrorModel>>()
    val previouslyUsedMobileNumberError: LiveData<SingleEvent<ErrorModel>> =
        _previouslyUsedMobileNumberError

    //Using for showing images on Language/Genre when user comes from Deeplinking
    private val _browseByLangOrGenreRail =
        MutableLiveData<SingleEvent<MutableList<HomeResponse.Items>?>>()
    val getBrowseByLangOrGenreResponse: LiveData<SingleEvent<MutableList<HomeResponse.Items>?>> =
        _browseByLangOrGenreRail


    val historyVisible = MutableLiveData<Boolean>()
    val _voiceText = MutableLiveData<SingleEvent<String>>()
    val fetchingSearch = MutableLiveData<SingleEvent<Boolean>>()
    val isDeviceTablet = MutableLiveData<Boolean>()
    var isSearchTrendingFetched = false
    private var disposable: Disposable? = null
    private var disposable1: Disposable? = null

    var isFromToggle: Boolean? = null
    var searchQuery = ""
    var searchPageOffset = 0
    var trendingOffset = 0
    var searchPageName: String = ""
    val PAGE_LIMIT = 20
    var freeToggle = false
    var contentType: String? = null

    private var mShowLoader = false
    private var mIntent = ""
    private var mQueryString = ""
    private var mGenre = ""
    private var mLanguage = ""
    private var mIsFilter = false
    private var mPageName: String? = null
    private var mIsRetry = false
    private var mCallTA = true


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
            iListItem.railName = railTitle
            iListItem.source = SOURCE_SEARCH
            iListItem.origin = EDITORIAL
            iListItem.contentPosition = (iItemPosition + 1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            searchAnalytics.trackSearchResultClicks(
                (iItemPosition + 1).toString(),
                iListItem.title
            )
            _clickedItem.postValue(
                SingleEvent(
                    ContentItemTransitions(
                        iListItem,
                        extras,
                        iSectionSource,
                        gamesMixpanelInfoModel = gamesMixpanelInfoModel,
                        contentAnalyticsModel = contentAnalyticsModel
                    )
                )
            )
        }
    }
    val mTrendingBannerClick = object : CommonDTOClickListener {
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
            iListItem.railName = "Trending"
            iListItem.source = SOURCE_SEARCH
            iListItem.origin = EDITORIAL
            iListItem.contentPosition = (iItemPosition + 1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            searchAnalytics.trackSearchHomeClick(iListItem.railName)
            _clickedItem.postValue(
                SingleEvent(
                    ContentItemTransitions(
                        iListItem,
                        extras,
                        iSectionSource,
                        contentAnalyticsModel = contentAnalyticsModel.copy(
                            railTitle = iListItem.railName
                        )
                    )
                )
            )
        }
    }
    val mAdapter =
        ItemGridAdapter(
            mBannerClick,
            mutableListOf(),
            0,
            sharedPrefs.getCloudenieryUrl(),
            null,
            sharedPrefs.getProviderLogo(),
            sharedPrefs = sharedPrefs,
            EDITORIAL
        )

    fun getUserPreferredGenreFilter(): MutableLiveData<SingleEvent<List<ContentItem>>> =
        _searchUserPreferredGenreFilterResponse

    fun getUserPreferredLanguageFilter(): MutableLiveData<SingleEvent<List<ContentItem>>> =
        _searchUserPreferredLanguageFilterResponse

    fun getSearchLandingResponse(): LiveData<SingleEvent<RecommendationResponse>> =
        _searchLandingResponse

    fun getSearchRailResponse(): LiveData<SingleEvent<HomeResponse>> =
        _searchRailsResponse

    fun getSearchResponse(): LiveData<SingleEvent<RecommendationResponse>> = _searchResponse
    fun getSearchSuggestionResponse(): LiveData<SingleEvent<RecommendationResponse>> =
        _searchSuggestionResponse

    fun getSearchFiltersResponse(): LiveData<SingleEvent<HashMap<String, RecommendationResponse>>> =
        _searchFiltersResponse

    fun getChangedCount(): LiveData<Int> = _changedTotalRailsCount

    fun getClickedItem(): LiveData<SingleEvent<ContentItemTransitions>> {
        return _clickedItem
    }

    fun getSuggestionClickItem(): LiveData<SingleEvent<ContentItemTransitions>> {
        return _clickedSuggestedItem
    }

    fun getAppClickedItem(): LiveData<SingleEvent<ContentItemTransitions>> {
        return _appRailItemClick
    }

    val mFilterClick = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            iSectionType: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            iListItem.railName = railTitle
            iListItem.contentPosition = (iItemPosition + 1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            iListItem.origin = origin ?: EventConstants.TYPE_EDITORIAL
            val extras = if (!transitions.isNullOrEmpty())
                FragmentNavigatorExtras(*transitions.toTypedArray())
            else FragmentNavigatorExtras()
            if (iSectionType.equals("APPS", true)) {
                _appRailItemClick.postValue(
                    SingleEvent(
                        ContentItemTransitions(
                            iListItem,
                            extras,
                            iSectionType,
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    )
                )
            } else {
                _filterItem.postValue(
                    SingleEvent(
                        FilterItemTransitions(
                            iListItem.title,
                            iSectionType,
                            extras,
                            iListItem.getLangGenreBackDrop(iSectionType) ?: "",
                            iListItem.getLangGenreIcon(iSectionType),
                            railTitle,
                            iListItem.pageType,
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    )
                )
            }
        }
    }
    val recentSearchAdapter = HistoryAdapter(this)
    val mSuggestionAdapter = SuggestionsAdapter(this)


    fun updateRecentSearchList() {
        recentSearchAdapter.updateList(getSearchHistory())
    }

    fun setClickedHistory(suggestion: String) {
        _clickedHistory.postValue(SingleEvent(suggestion))
    }

    fun setClickedSuggestion(suggestion: ContentItem) {
        suggestion.source = SOURCE_SEARCH
        suggestion.origin = EDITORIAL
        _clickedSuggestedItem.postValue(
            SingleEvent(
                ContentItemTransitions(
                    suggestion,
                    FragmentNavigatorExtras(),
                    EDITORIAL,
                    contentAnalyticsModel = getSearchResultContentAnalyticsModel()
                )
            )
        )
    }

    fun getClickedHistory(): LiveData<SingleEvent<String>> = _clickedHistory

//    fun setMockResponse(response: RecommendationResponse) {
//        this._mockResponse = response
//    }

    fun refreshApi() {
        searchPageOffset = 0
        fetchSearchList(
            mShowLoader, mIntent,
            mQueryString,
            mGenre,
            mLanguage,
            mIsFilter
        )
    }

    fun refreshLangGenreApi(shouldCallTA: Boolean? = null) {
        searchPageOffset = 0
        fetchLanguageGenreList(
            mShowLoader,
            mIntent,
            mGenre,
            mLanguage,
            mPageName,
            mIsRetry,
            if (shouldCallTA == true) shouldCallTA else mCallTA
        )
    }

    @SuppressLint("CheckResult")
    fun fetchAutoSuggestions(
        showLoader: Boolean,
        queryString: String
    ) {
        val currentSub = sharedPrefs.getSubscribedPack()
        val currentSubStatus = (currentSub != null) && !currentSub.isInactive
        val mNonSubscribedPartnerList = sharedPrefs.getNonSubscribedPartnerList()

        useCase.getSearchSuggestions(queryString)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setProgressing(false)
                setRetryError(error, false)
                fetchingSearch.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                @SuppressLint("CheckResult")
                override fun onSuccessResponse(t: RecommendationResponse) {
                    var suggestionList = t.data?.contentItem ?: ArrayList<ContentItem>()
                    suggestionList = suggestionList.filter {
                        if (it.suggestor.equals(
                                SuggestionSuggestors.ProviderSuggestor.name,
                                true
                            )
                        )
                            it.contentType = TYPE_SUB_PAGE
                        if (it.suggestor.equals(
                                SuggestionSuggestors.KeywordSuggestor.name,
                                true
                            )
                            || it.suggestor.equals(
                                SuggestionSuggestors.GenreSuggestor.name,
                                true
                            )
                            || it.suggestor.equals(
                                SuggestionSuggestors.LanguageSuggestor.name,
                                true
                            )
                        ) true
                        else {
                            it.appleRedemptionStatus = currentSub?.appleRedemptionStatus
                            it.isPartnerSubscribed =
                                currentSubStatus && (mNonSubscribedPartnerList?.contains
                                    (it.provider.lowercase()) == false)
                            isValidRentalContent(
                                it,
                                sharedPrefs.getDthStatusFreemium(),
                                "",
                                "",
                                ""
                            )
                        }
                    } as ArrayList<ContentItem>

                    for (i in 0 until suggestionList.size) {
                        suggestionList[i].suggestionPosition = i + 1
                    }

                    t.data?.contentItem = suggestionList

                    _searchSuggestionResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                }
            })
    }


    @SuppressLint("CheckResult")
    fun fetchSearchList(
        showLoader: Boolean,
        intent: String,
        queryString: String,
        genre: String,
        language: String,
        isFilter: Boolean
    ) {
        mShowLoader = showLoader
        mIntent = intent
        mQueryString = queryString
        mGenre = genre
        mLanguage = language
        mIsFilter = isFilter

        if (::_searchDisposable.isInitialized) {
            _searchDisposable.dispose()
        }
        if (showLoader)
            fetchingSearch.postValue(SingleEvent(true))
        if (searchPageOffset == 0)
            intentUrl = ""
        val searchRequest = SearchRequest(
            preferLang = sharedPrefs.getPrefLanguages(),
            preferGenre = sharedPrefs.getPrefGenres(),
            pageNumber = searchPageOffset + 1,
            queryString = queryString,
            intentUrl = intentUrl,
            freeToggle = freeToggle,
            contentType = contentType
        )
        useCase.getSearchV2(searchRequest)
//        useCase.getSearchKeystrokeResult(intent, queryString, genre, language, isFilter, searchPageOffset, 20)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setProgressing(false)
                setRetryError(error, false)
                fetchingSearch.postValue(SingleEvent(false))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                @SuppressLint("CheckResult")
                override fun onSuccessResponse(t: RecommendationResponse) {
                    fetchingSearch.postValue(SingleEvent(false))
                    if (t.data == null || t.code != CODE_SUCCESS) {
                        setError(ErrorModel(t.code, t.message))
                    } else {
                        intentUrl = t.data?.intentUrl ?: ""
                        t.data?.let {
                            crownCalculation(it)
                        }
                        _searchResponse.postValue(SingleEvent(t))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    fetchingSearch.postValue(SingleEvent(false))
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    _lastSearchedQuery = queryString
                    _searchDisposable = d
                    addDisposable(d)
                }
            })
    }

    fun getLastSearchedQuery() = _lastSearchedQuery

    fun fetchSearchLandingList(isShowLoader: Boolean) {
        if (isShowLoader)
            fetchingSearch.postValue(SingleEvent(true))
        val searchRequest = SearchRequest(
            preferLang = sharedPrefs.getPrefLanguages(),
            preferGenre = sharedPrefs.getPrefGenres(),
            pageNumber = trendingOffset + 1,
            freeToggle = freeToggle,
            contentType = contentType

        )
        val disposable = useCase.getSearchV2(searchRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    setProgressing(false)
                    fetchingSearch.postValue(SingleEvent(false))
                    _searchLandingResponse.postValue(SingleEvent(t))
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    if (trendingOffset == 0) {
                        isSearchTrendingFetched = false
                        _searchLandingResponse.postValue(SingleEvent(RecommendationResponse()))
                    }
                    fetchingSearch.postValue(SingleEvent(false))
                    //setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    addDisposable(d)
                }
            })
    }

    @SuppressLint("CheckResult")
    fun fetchSearchRails(intent: String?, fetchAll: Boolean, isForDeeplink: Boolean = false) {
        fetchingSearch.postValue(SingleEvent(true))
//        disposable1?.dispose()
        useCase.getSearchRails(intent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                fetchingSearch.postValue(SingleEvent(false))
                if (trendingOffset == 0) {
                    isSearchTrendingFetched = false
                }
                setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<HomeResponse>() {
                override fun onSuccessResponse(t: HomeResponse) {
                    setProgressing(false)
                    if (isForDeeplink) {
                        t.data?.let {
                            _browseByLangOrGenreRail.postValue(SingleEvent(t.data?.finalItems))
                        }
                    } else {
                        fetchingSearch.postValue(SingleEvent(false))
                        if (t.data == null) {
                            isSearchTrendingFetched = false
                            setError(ErrorModel(t.code, t.message))
                        } else {
                            if (fetchAll) {
                                //Fetch All rails
                                if (intent == INTENT_LANGUAGE) {
                                    _searchLanguageFilterResponse = t
                                    getPrefLangGenre(USER_PREFERRED_LANGUAGE_TYPE, true)
                                    fetchSearchRails(INTENT_GENRE, true)
                                } else if (intent == INTENT_GENRE) {
                                    _searchgenreFilterResponse = t
                                    getPrefLangGenre(USER_PREFERRED_GENRE_TYPE, true)
                                } else {
                                    _searchRailsResponse.postValue(SingleEvent(t))
                                    fetchSearchRails(INTENT_LANGUAGE, true)
                                }
                            } else if (intent == INTENT_LANGUAGE) {
                                _searchLanguageFilterResponse = t
                                getPrefLangGenre(USER_PREFERRED_LANGUAGE_TYPE, true)

                            } else if (intent == INTENT_GENRE) {
                                _searchgenreFilterResponse = t
                                getPrefLangGenre(USER_PREFERRED_GENRE_TYPE, true)

                            } else {
                                _searchRailsResponse.postValue(SingleEvent(t))
                            }
                        }
                    }
                }

                @SuppressLint("CheckResult")
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    isSearchTrendingFetched = false
                    fetchingSearch.postValue(SingleEvent(false))
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    disposable1 = d
                    addDisposable(d)
                }
            })

//        useCase.getSearchRails(INTENT_LANGUAGE).subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnError { error ->
//                fetchingSearch.postValue(SingleEvent(false))
//                if(trendingOffset == 0){
//                    isSearchTrendingFetched = false
//                }
//                setRetryError(error, false)
//            }
//            .retryWhen { retryHandler ->
//                retrySubject = PublishSubject.create<Any>()
//                retryHandler.zipWith(retrySubject.toFlowable(
//                    BackpressureStrategy.LATEST
//                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
//            }
//            .subscribeWith(object : CallbackWrapper<HomeResponse>() {
//                override fun onSuccessResponse(t: HomeResponse) {
//                    setProgressing(false)
//                    fetchingSearch.postValue(SingleEvent(false))
//                    if (t.data == null) {
//                        isSearchTrendingFetched = false
//                        setError(ErrorModel(t.code, t.message))
//                    } else {
//                        if(isFetchOther) {
//                            _searchLanguageFilterResponse.postValue(SingleEvent(t))
//                            fetchSearchRails(INTENT_LANGUAGE, false)
//                        }else{
//                            fetchSearchRails(INTENT_GENRE, false)
//                        }
//                    }
//                }
//
//                @SuppressLint("CheckResult")
//                override fun onError(error: ErrorModel?) {
//                    setProgressing(false)
//                    isSearchTrendingFetched = false
//                    fetchingSearch.postValue(SingleEvent(false))
//                    setError(error)
//                }
//
//                override fun onSubscribe(d: Disposable) {
//                    super.onSubscribe(d)
//                    disposable = d
//                    addDisposable(d)
//                }
//            })
    }


    @SuppressLint("CheckResult")
    fun fetchFiltersList(intent: String, isFetchOther: Boolean) {
        fetchingSearch.postValue(SingleEvent(true))
        disposable?.dispose()
        useCase.getLanguageGenreList(intent)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                fetchingSearch.postValue(SingleEvent(false))
                if (trendingOffset == 0) {
                    isSearchTrendingFetched = false
                }
                setRetryError(error, false)
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    setProgressing(false)
                    fetchingSearch.postValue(SingleEvent(false))
                    if (t.data == null) {
                        isSearchTrendingFetched = false
                        setError(ErrorModel(t.code, t.message))
                    } else {
                        filterMap.put(intent, t)
                        if (intent == INTENT_LANGUAGE && isFetchOther) {
                            fetchFiltersList(INTENT_GENRE, true)
                        } else {
                            _searchFiltersResponse.postValue(SingleEvent(filterMap))
                        }
                    }
                }

                @SuppressLint("CheckResult")
                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    isSearchTrendingFetched = false
                    fetchingSearch.postValue(SingleEvent(false))
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    disposable = d
                    addDisposable(d)
                }
            })
    }


    fun updateList(
        railResponse: RecommendationResponse,
        contentAnalyticsModel: ContentAnalyticsModel,
    ) {
        setProgressing(false)
        mAdapter.removeLoading()
        mAdapter.setAutoUpdate(true)
//        mAdapter.setTotalItemsCount(railResponse.data?.totalCount ?: 0)

        val itemCount = railResponse.data?.itemCount ?: 0
        val totalSearchCount = railResponse.data?.totalCount ?: 0

        railResponse.data?.dthStatus = sharedPrefs.getDthStatusFreemium()
        val totalItems = if (searchPageOffset != 0) mAdapter.itemCount + (itemCount) else itemCount
        railResponse.data?.continuePagination =
            false//itemCount > 0 && totalItems < totalSearchCount
        if (searchPageOffset == 0) {
            mAdapter.updateListForDiff(
                railResponse.data?.filteredContentItems?.toMutableList() ?: mutableListOf(),
                true,
                railResponse.data?.continuePagination ?: false,
                contentAnalyticsModel
            )
            _changedTotalRailsCount.postValue(railResponse.data?.totalCount ?: 0)
        } else {
            mAdapter.addToList(
                railResponse.data?.filteredContentItems ?: mutableListOf(),
                railResponse.data?.continuePagination ?: false,
                contentAnalyticsModel
            )
        }
        fetchingSearch.postValue(SingleEvent(false))
    }

    fun clearAllRecentSearch() {
        sharedPrefs.clearAllSearchKeyword()
        updateRecentSearchList()
    }

    fun clearRecentSearchItem(keyword: String) {
        sharedPrefs.clearRecentSearchItem(keyword)
        updateRecentSearchList()
    }

    fun setLanguageFilterTitle(languageTitle: String) {
        this._languageModel.postValue(languageTitle)
    }

    fun setGenreFilterTitle(genreTitle: String) {
        this._genreModel.postValue(genreTitle)
    }

    fun getSearchAdapter(): RecyclerView.Adapter<*> {
        return mAdapter
    }

    fun resetSearchResults(contentAnalyticsModel: ContentAnalyticsModel) {
        //mAdapter.setTotalItemsCount(0)
        mAdapter.updateList(mutableListOf(), false, contentAnalyticsModel)
//        mSuggestionAdapter.updateList(emptyList())
    }

    fun getLanguageFilterTitle(): LiveData<String>? = _languageModel
    fun getGenreFilterTitle(): LiveData<String>? = _genreModel
    fun getFilterItemClick(): LiveData<SingleEvent<FilterItemTransitions>> = _filterItem

    private fun getSearchHistory(): List<String> {
        var searchKeywords = sharedPrefs.getSearchKeywords().toMutableList()
        searchKeywords.remove("")
        if (searchKeywords.isEmpty()) hideHistory(true) else hideHistory(false)
        return searchKeywords
    }

    private fun hideHistory(hide: Boolean) {
        historyVisible.postValue(!hide)
    }

    fun getVoiceText(): LiveData<SingleEvent<String>> = _voiceText

    fun fetchTALanguageGenreList(
        showLoader: Boolean,
        intent: String,
        genre: String,
        language: String,
        pageName: String?,
        isRetry: Boolean
    ) {
//        var placeHolder = "UC_BBG"
        if (showLoader)
            setProgressing(true)
        if (::_filterDisposable.isInitialized) {
            _filterDisposable.dispose()
        }
        /*if(genre.isEmpty()){
            placeHolder = "UC_BBL"
        }*/
        var placeHolder = "UC_BBG"
        if (pageName.equals("MOVIES", true)) {
            placeHolder = "UC_BBG_MOVIES"
        } else if (pageName.equals("TV_SHOWS", true)) {
            placeHolder = "UC_BBG_TVSHOWS"
        }
        if (genre.isEmpty()) {
            if (pageName.equals("MOVIES", true)) {
                placeHolder = "UC_BBL_MOVIES"
            } else if (pageName.equals("TV_SHOWS", true)) {
                placeHolder = "UC_BBL_TVSHOWS"
            } else {
                placeHolder = "UC_BBL"
            }
        }
        e("curl", "placeHolder : $placeHolder")
        val request = TARequest(
            placeHolder, "10",
            isLoggedIn = sharedPrefs.getLoginStatus(),
            filterLanguage = language,
            subGenre = genre,
            languageGenre = true,
            body = EmptyBody(),
            freeToggle = freeToggle
        )

        val disposable = useCase.executeTARails(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                fetchLanguageGenreList(
                    showLoader,
                    intent,
                    genre,
                    language,
                    pageName,
                    isRetry,
                    false
                )
            }
            .subscribe(
                {
                    setProgressing(false)
                    //it.data?.layoutType = ItemLayoutType.LANDSCAPE.name
                    taSearchResultContentItems = it.data?.filteredContentItems
                    fetchLanguageGenreList(
                        showLoader,
                        intent,
                        genre,
                        language,
                        pageName,
                        isRetry,
                        false
                    )
                },
                {
                    setProgressing(false)
                    e("CallbackWrapper", "inside onError $it")
                    fetchLanguageGenreList(
                        showLoader,
                        intent,
                        genre,
                        language,
                        pageName,
                        isRetry,
                        false
                    )
                }
            )
    }

    @SuppressLint("CheckResult")
    fun fetchLanguageGenreList(
        showLoader: Boolean,
        intent: String,
        genre: String,
        language: String,
        pageName: String?,
        isRetry: Boolean,
        callTA: Boolean = true
    ) {

        mShowLoader = showLoader
        mIntent = intent
        mGenre = genre
        mLanguage = language
        mPageName = pageName
        mIsRetry = isRetry
        mCallTA = callTA


        if (callTA && searchQuery.isNullOrEmpty()) {
            fetchTALanguageGenreList(showLoader, intent, genre, language, searchPageName, isRetry)
            return
        }
        if (showLoader)
            setProgressing(true)
        if (::_filterDisposable.isInitialized) {
            _filterDisposable.dispose()
        }
        var isFilter = false
        val arrfilterLang = ArrayList<String>()
        if (language.isNotEmpty()) {
            isFilter = true
            arrfilterLang.add(language)
        }
        val arrfilterGenre = ArrayList<String>()
        if (genre.isNotEmpty()) {
            isFilter = true
            arrfilterGenre.add(genre)
        }

        val searchRequest = SearchRequest(
            queryString = searchQuery,
            filterGenre = arrfilterGenre,
            filterLanguage = arrfilterLang,
            pageName = searchPageName,
            pageNumber = searchPageOffset + 1,
            filter = isFilter,
            freeToggle = freeToggle,
            contentType = contentType
        )
        useCase.getSearchV2(searchRequest)
//        useCase.getSearchKeystrokeResult(intent,"", genre, language, true, searchPageOffset, 20)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                fetchingSearch.postValue(SingleEvent(false))
                setProgressing(false)
                if (isRetry)
                    setRetryError(error, false)
                else
                    setError(ErrorModel(statusCode = RESPONSE_CODE_NETWORK_ERROR))
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    setProgressing(false)
                    fetchingSearch.postValue(SingleEvent(false))
                    if (t.data == null) {
                        setError(ErrorModel(t.code, t.message))
                    } else {
                        if (searchPageOffset == 0 && !(contentType.equals(
                                PROVIDER_GAMEZOP,
                                true
                            ))
                        ) {
                            t.data!!.contentItem.addAll(
                                0,
                                removeDuplicateContent(
                                    t.data!!.contentItem,
                                    taSearchResultContentItems ?: java.util.ArrayList()
                                )
                            )
                        }
                        intentUrl = t.data?.intentUrl
                        _searchResponse.postValue(SingleEvent(t))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    setProgressing(false)
                    fetchingSearch.postValue(SingleEvent(false))
                    setError(error)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    _filterDisposable = d
                    addDisposable(d)
                }
            })
    }

    fun updateLangGenreList(
        railResponse: RecommendationResponse,
        isSquareLayout: Boolean = false,
        contentAnalyticsModel: ContentAnalyticsModel,
    ) {
        setProgressing(false)
        mAdapter.removeLoading()
        mAdapter.setTotalItemsCount(railResponse.data?.totalCount ?: 0)
        mAdapter.setAutoUpdate(true)

        val itemCount = railResponse.data?.itemCount ?: 0
        val totalItems = mAdapter.itemCount + (itemCount)
        val totalSearchCount = railResponse.data?.totalCount ?: 0

        railResponse.data?.continuePagination =
            false//itemCount > 0 && totalItems < totalSearchCount

        if (searchPageOffset == 0) {
            if (isSquareLayout) {
                mAdapter.updateLayoutType(ItemLayoutType.SQUARE.name)
            }

            if (isFromToggle == true) {
                isFromToggle = null
                mAdapter.updateListForDiff(
                    railResponse.data?.filteredContentItems?.toMutableList() ?: mutableListOf(),
                    true,
                    railResponse.data?.continuePagination ?: false,
                    contentAnalyticsModel
                )
                _changedTotalRailsCount.postValue(railResponse.data?.totalCount ?: 0)
            } else {
                mAdapter.updateList(
                    railResponse.data?.filteredContentItems?.toMutableList() ?: mutableListOf(),
                    railResponse.data?.continuePagination ?: false,
                    contentAnalyticsModel
                )
                _changedTotalRailsCount.postValue(railResponse.data?.totalCount ?: 0)
            }
        } else {
            mAdapter.addToList(
                railResponse.data?.filteredContentItems ?: mutableListOf(),
                railResponse.data?.continuePagination ?: false,
                contentAnalyticsModel
            )
        }
        fetchingSearch.postValue(SingleEvent(false))
    }

    fun showMoreButton() {
        mAdapter.addLoading()
    }

    @SuppressLint("CheckResult")
    fun getPrefLangGenre(type: String, mergeUserPreference: Boolean = false) {
        useCase.getPrefLangGenre(sharedPrefs.getLoginStatus(), type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : CallbackWrapper<GenreListResponse>() {
                override fun onSuccessResponse(t: GenreListResponse) {
                    val lowercaseSet = LinkedHashSet<String>()
                    for (data in t.data?.list ?: mutableListOf()) {
                        lowercaseSet.add(data.lowercase())
                    }
                    if (USER_PREFERRED_GENRE_TYPE == type) {
                        sharedPrefs.setPrefGenre(t.data?.list)
                        sharedPrefs.saveGenreAPITime(getCurrentDate())
                        if (mergeUserPreference) {
                            _searchUserPreferredGenreFilterResponse.postValue(
                                SingleEvent(
                                    mergeUserPreference(
                                        lowercaseSet,
                                        _searchgenreFilterResponse
                                    )
                                )
                            )
                        }
                    } else {
                        if (mergeUserPreference) {
                            _searchUserPreferredLanguageFilterResponse.postValue(
                                SingleEvent(
                                    mergeUserPreference(
                                        lowercaseSet,
                                        _searchLanguageFilterResponse
                                    )
                                )
                            )
                        }
                    }
                }

                @SuppressLint("CheckResult")
                override fun onError(error: ErrorModel?) {
                    if (USER_PREFERRED_GENRE_TYPE == type) {
                        var genreContentList = mutableListOf<ContentItem>()
                        _searchgenreFilterResponse?.data?.items?.let {
                            if (it.isNotEmpty()) {
                                genreContentList = it[0].contentItem
                            }
                        }
                        _searchUserPreferredGenreFilterResponse.postValue(
                            SingleEvent(genreContentList)
                        )
                    } else {
                        var languageContentList = mutableListOf<ContentItem>()
                        _searchLanguageFilterResponse?.data?.items?.let {
                            if (it.isNotEmpty()) {
                                languageContentList = it[0].contentItem
                            }
                        }
                        _searchUserPreferredLanguageFilterResponse.postValue(
                            SingleEvent(languageContentList)
                        )
                    }
                }
            })
    }

    private fun mergeUserPreference(set: HashSet<String>, value: HomeResponse?): List<ContentItem> {
        val contentItemList = mutableListOf<ContentItem>()
        value?.data?.items?.let {
            if (it.isNotEmpty()) {
                val idValueMap = it[0].contentItem.associateBy { it.title.lowercase() }
                val sortedList = set.mapNotNull { idValueMap[it] }
                contentItemList.addAll(sortedList)
                it[0].contentItem.removeAll(sortedList)
                contentItemList.addAll(it[0].contentItem)
                return contentItemList
            }
        }

        return contentItemList
    }

    fun disposeSearch() {
        if (::_searchDisposable.isInitialized) {
            _searchDisposable.dispose()
            setProgressing(false)
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

    fun setVerbiageForSearchPage() = sharedPrefs.getConfigResponse()?.data?.config?.search
    private fun crownCalculation(it: HomeResponse.Items?) {
        val mNonSubscribedPartnerList = sharedPrefs.getNonSubscribedPartnerList()

        val isGuestUser = sharedPrefs.getLoginStatus()
        val currentSub = sharedPrefs.getSubscribedPack()
        val currentSubStatus = (currentSub != null) && !currentSub.isInactive
        val freeEpVerb =
            sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage.toString()

        fun checkCrownConditions(it: ContentItem) {
            it.appleRedemptionStatus = sharedPrefs.getSubscribedPack()?.appleRedemptionStatus
            it.isPartnerSubscribed =
                currentSubStatus && (mNonSubscribedPartnerList?.contains(it.provider.lowercase()) == false)
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
}
