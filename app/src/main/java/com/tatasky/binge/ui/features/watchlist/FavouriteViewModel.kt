package com.tatasky.binge.ui.features.watchlist

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.tatasky.binge.analytics.SOURCE_BINGE_LIST
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.CallbackWrapper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.ContentIdAndTypeRequest
import com.tatasky.binge.data.networking.models.requests.WatchRequest
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.BaseResponse.CODE.OK_0
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.interfaces.ContentItemTransitions
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.utils.*
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class FavouriteViewModel @Inject constructor(val useCase: CommonUseCase, val sharedPrefs: PrefsRepo) : BaseViewModel() {
    var isLoadingContent: Boolean = false

    var cwWaitForTvod : Boolean = false
    private var watchlistResponse: RecommendationResponse? = null
    private val RecommendationResponse = MediatorLiveData<SingleEvent<RecommendationResponse>>()
    private val errorResponse = MediatorLiveData<SingleEvent<String>>()
    private val _clickedItem = MutableLiveData<SingleEvent<ContentItemTransitions>>()
    private val _changedTotalRailsCount = MediatorLiveData<SingleEvent<Int>>()
    private var disposable: Disposable? = null
    var watchPageOffset = 0
    var pagingState: String? = null
    var tvodResponse: RecommendationResponse? = null
    private var totalRails = 0
    private var homeDisposable: Disposable? = null
    private val mSelectedItemsSize = MutableLiveData<SingleEvent<Int>>()

/*
    private val loadMoreClick = object : CommonLoadMoreClickListener {
        override fun onLoadMoreClick(pageOffset: Int) {
            watchPageOffset++// = pageOffset/PAGELIMIT
            _loadMoreClicker.postValue(SingleEvent(true))
            fetchWatchList(false)
        }
    }*/

    fun getChangedCount(): LiveData<SingleEvent<Int>> = _changedTotalRailsCount

    val mBannerClick = object : CommonDTOClickListener {
        override fun onSubItemClick(
            iListItem: ContentItem,
            iItemPosition: Int,
            iSectionPosition: Int,
            iSectionSource: String,
            transitions: List<Pair<View, String>>?,
            railTitle: String,
            origin: String?,
            gamesMixpanelInfoModel: GamesMixpanelInfoModel?
        ) {
            val extras = if (!transitions.isNullOrEmpty())
                FragmentNavigatorExtras(*transitions.toTypedArray())
            else FragmentNavigatorExtras()
            iListItem.railName = railTitle
            iListItem.source = "Watchlist"
            iListItem.origin = origin ?: EventConstants.TYPE_EDITORIAL
            iListItem.contentPosition = (iItemPosition+1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            _clickedItem.postValue(SingleEvent(ContentItemTransitions(iListItem, extras,
                iSectionSource
            )))
        }
    }
    private val mAdapter =
        ItemGridAdapter(
            mBannerClick,
            mutableListOf(),
            0,
            sharedPrefs.getCloudenieryUrl(),
            null,
            sharedPrefs.getProviderLogo(),
            sharedPrefs = sharedPrefs,
            mSelectedItemsSize = mSelectedItemsSize,
            origin = SOURCE_BINGE_LIST
        )

    fun getWatchlistAdapter(): ItemGridAdapter {
        return mAdapter
    }

    @SuppressLint("CheckResult")
    fun fetchWatchList(showLoader: Boolean, isUserRefresh : Boolean) {
        if (showLoader) {
            watchPageOffset = 0
            setProgressing(true)
        }
        val request = if (watchPageOffset == 0) {
            WatchRequest(
                sharedPrefs.getOriginalSubscriberId(),
                sharedPrefs.getProfileId()!!,
                null, watchPageOffset,
                isForceRefresh = isUserRefresh
            )
        } else {
            WatchRequest(
                sharedPrefs.getOriginalSubscriberId(),
                sharedPrefs.getProfileId()!!,
                pagingState, watchPageOffset,
                isForceRefresh = isUserRefresh
            )
        }

        disposable?.dispose()
        useCase.executeFavouritesList(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setProgressing(false)
                isLoadingContent = false
                errorResponse.postValue(SingleEvent(error.message ?: ""))
                if (showLoader || isUserRefresh) {
                    setRetryError(error, false)
                }
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    disposable = d
                    addDisposable(d)
                }

                override fun onSuccessResponse(t: RecommendationResponse) {
                    isLoadingContent = false
                    setProgressing(false)
                    if (t.data == null || t.code != OK_0) {
                        setError(ErrorModel(message = t.message))
                        errorResponse.postValue(SingleEvent(t.message!!))
                    } else {
                        watchlistResponse = t
                        val isRental: Boolean = isRentalContent(
                            t.data?.contentItem as ArrayList<ContentItem>? ?: ArrayList())
                        if (isRental && tvodResponse != null) {
                            updateCWRentalItems()
                        }
                        else if(isRental) {
                            e("SubViewModel","curl : inside cwresponse")
                            cwWaitForTvod = true
                            fetchTVoD(showLoader)
                        }
                        if(!isRental)
                            RecommendationResponse.postValue(SingleEvent(t))
                    }
                }

                override fun onError(error: ErrorModel?) {
                    isLoadingContent = false
                    setProgressing(false)
                    setError(error)
                    errorResponse.postValue(SingleEvent(error?.message?: COMMON_ERROR_MSG))
                }

            })
    }

    fun removeSelectedWatchlist(lambda: ((Boolean) -> Unit)? = null) {
        setProgressing(true)

        useCase.run {
            removeBingeList(
                ContentIdAndTypeRequest().apply {
                    this.profileId = sharedPrefs.getProfileId()
                    this.subscriberId = sharedPrefs.getOriginalSubscriberId()
                    this.contentIdAndType = mAdapter.getSelectedItemArray().toList()
                }
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<BaseResponse>() {

                    override fun onSuccessResponse(t: BaseResponse) {
                        setProgressing(false)
                        lambda?.invoke(true)
                    }

                    override fun onError(error: ErrorModel?) {
                        setProgressing(false)
                        setError(error)
                        lambda?.invoke(false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        addDisposable(d)
                    }

                })
        }

    }

    private fun updateCWRentalItems() {
        watchlistResponse?.let {
            cwWaitForTvod = false
            watchlistResponse?.data?.contentItem = filterRentalExpiry(
                watchlistResponse?.data?.contentItem as ArrayList<ContentItem>?,
                tvodResponse?.data?.contentItem
            ) ?: ArrayList()
            RecommendationResponse.postValue(SingleEvent(it))
        }
    }

    fun getClickedItem(): LiveData<SingleEvent<ContentItemTransitions>> {
        return _clickedItem
    }

    fun getSelectedItemSize():LiveData<SingleEvent<Int>>{
       return mSelectedItemsSize
    }

    fun getFavListResponse(): LiveData<SingleEvent<RecommendationResponse>> = RecommendationResponse
    fun getErrorResponse(): LiveData<SingleEvent<String>> = errorResponse


    fun updateList(railResponse: RecommendationResponse) {
        pagingState = railResponse.data?.pagingState
        mAdapter.removeLoading()
        mAdapter.updateCW(true)
        //mAdapter.setTotalItemsCount(railResponse.data?.totalCount ?: 0)
        if (watchPageOffset == 0) {
            mAdapter.updateListForDiff(
                railResponse.data?.filteredContentItems?.toMutableList() ?: mutableListOf()
            )
        } else {
            mAdapter.addToList(railResponse.data?.filteredContentItems ?: mutableListOf())
        }
        setProgressing(false)
        if (railResponse.data?.continuePagination == true) {
            totalRails = mAdapter.itemCount + 1
            _changedTotalRailsCount.postValue(SingleEvent(totalRails))
        }
        e("WatchlistViewModel", "totalRailsCount : $totalRails , adapterCount : ${mAdapter.itemCount}")
        /*if(railResponse.data?.continuePagination == true){
            mAdapter.addLoading()
        }*/
    }


    @SuppressLint("CheckResult")
    fun fetchTVoD(isShowLoader: Boolean) {
        isLoadingContent = true
        if (isShowLoader)
            setProgressing(true)
        homeDisposable?.dispose()
        useCase.getTvodContent(sharedPrefs.getOriginalSubscriberId() ?: ""
            /*"3001180136"*/, 0, 10)//sid
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                setProgressing(false)
                isLoadingContent = false
                if (isShowLoader) {
                    setRetryError(error, false)
                }
            }
            .retryWhen { retryHandler ->
                retrySubject = PublishSubject.create<Any>()
                retryHandler.zipWith(retrySubject.toFlowable(
                    BackpressureStrategy.LATEST
                ), BiFunction<Throwable, Any, Any> { t1, t2 -> t2 })
            }
            .subscribeWith(object : CallbackWrapper<RecommendationResponse>(false)
            {
                override fun onSuccessResponse(t: RecommendationResponse) {
                    tvodResponse = t
                    if(cwWaitForTvod){
                        updateCWRentalItems()
                    }
                //fetchWatchList(isShowLoader, false)
                }

                override fun onError(error: ErrorModel?) {
                    if(cwWaitForTvod){
                        updateCWRentalItems()
                    }
                //fetchWatchList(isShowLoader, false)
                }

                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    homeDisposable = d
                    addDisposable(d)
                }
            })
    }
}