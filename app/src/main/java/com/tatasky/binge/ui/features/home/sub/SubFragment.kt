package com.tatasky.binge.ui.features.home.sub

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.SetAppRatingRequest
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.LeftMenuItem
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentSubpageBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.setSingleOnClick
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.bottomsheet.HomeBottomSheetViewModel
import com.tatasky.binge.ui.features.more.SettingsViewModel
import com.tatasky.binge.utils.*
import kotlinx.android.synthetic.main.activity_home.*
import com.tatasky.binge.utils.TextUtils.isNotEmptyAndIsDigitAndIsGreaterThanZero
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class SubFragment: BaseFragment<FragmentSubpageBinding, SubViewModel>(){

    private val TAG: String = SubFragment::class.java.simpleName
    private var totalItemCount: Int = 0
    private var isCategories: Boolean = false
    private var firstCategoryCall=true
    private lateinit var navDrawerViewModel : SettingsViewModel
    private var mHomeBottomSheetViewModel: HomeBottomSheetViewModel? = null
    private var isFirstTime: Boolean = true
    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    private var commonViewModel : CommonSampleViewModel?=null
    private var pageName : String? = null
    private var mShownLanguagesForFirstTime : Boolean = false
    private var mCurrentPreferredLanguages:List<String>? = null
    private  val subFragmentArgs by navArgs<SubFragmentArgs>()
    private val mHandler = Handler(Looper.getMainLooper())

    override fun onNetworkError(errorMessage: String, isRetry: Boolean) {
        super.onNetworkError(errorMessage, isRetry)
        viewModel.isPullToRefresh = false
        binding.swipeRefresh.isRefreshing = false
    }


    private fun getSource(id: Int?) : String {
        return when (id) {
            R.id.action_sub_landing_home -> SOURCE_HOME
            R.id.action_sub_landing_movie -> SOURCE_MOVIES
            R.id.action_sub_landing_shows -> SOURCE_TV_SHOWS
            R.id.action_sub_landing_games -> SOURCE_GAMES
            else -> ""
        }
    }

    override fun toBeCalledOnce() {
        try {
            /**
             * Prime deeplink handling moved to:
             * @see com.tatasky.binge.helper.DeeplinkHelper.DeeplinkHandlerImpl.handleCustomDeeplink
             **/
            // Deeplink handling for Language Genre screen
            if (subFragmentArgs.languageGenreToBeSearched != null &&
                subFragmentArgs.languageGenreSectionType != null
            ) {
                findNavController().navigateSafe(
                    SubFragmentDirections.actionHomeFragmentToLanguageGenreFragment(
                        subFragmentArgs.languageGenreToBeSearched?.toLowerCase(Locale.getDefault())
                            ?.capitalize()
                            ?: "",
                        subFragmentArgs.languageGenreSectionType?.toUpperCase(Locale.getDefault())
                            ?: "",
                        "Deeplink",
                        source = getSource(findNavController().currentDestination?.id),
                        refId = ""
                    )
                )
            }
            if (subFragmentArgs.contentItem != null)
                findNavController().navigateSafe(
                    SubFragmentDirections.actionToDetail(
                        subFragmentArgs.contentItem!!
                    )
                )

            // For Prime and Content deep link handling (Provider available/required)
            if (subFragmentArgs.id != null && subFragmentArgs.contentType != null && !subFragmentArgs.partnerName.isNullOrBlank()) {
                findNavController().navigateSafe(
                    SubFragmentDirections.actionToDetail(
                        ContentItem().apply {
                            if (PROVIDER_PRIME.equals(subFragmentArgs.partnerName, true)) {
                                providerContentId = subFragmentArgs.id!!
                            }
                            id = subFragmentArgs.id!!
                            contentType = subFragmentArgs.contentType!!
                            provider = subFragmentArgs.partnerName!!
                        }
                    )
                )
            }
            // Content deeplink handling when Partner/Provider not required
            else if (subFragmentArgs.id != null && subFragmentArgs.contentType != null) {
                findNavController().navigateSafe(
                    SubFragmentDirections.actionToDetail(
                        ContentItem().apply {
                            id = subFragmentArgs.id!!
                            contentType = subFragmentArgs.contentType!!
                            provider = ""
                        }
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.vm = viewModel
        viewModel.subscribed = viewModel.sharedPrefs.isActivePack()  &&
                sharedPrefs.getPartnerIdsList()?.size?:0 > 0
        viewModel.unsubscribed = !viewModel.subscribed
        setupBottomMenuItemsAndInitView()
    }

    private fun handleNoData() {
        viewModel.setProgressing(false)
        viewModel.isPullToRefresh = false
        binding.swipeRefresh.isRefreshing = false
        binding.homeRecyclerView.hide()
        binding.tvNoData.visibility = View.VISIBLE
    }

    private fun setupBottomMenuItemsAndInitView() {
        val menuItemList = LeftMenuResponseCache.leftMenuItemList
        if (menuItemList.isEmpty()) {
            viewModel.fetchMenuItems {
                initViews(it)
            }
        } else {
            initViews(menuItemList)
        }
    }

    private fun initViews(leftMenuItemList: List<LeftMenuItem>) {
        if(leftMenuItemList.isEmpty()){
            handleNoData()
            return
        }
        arguments?.let {
            val menuItemId = (activity as? LandingActivity)?.currentMenuItemId?.value
            /*if (menuItemId == R.id.others) {
                isCategories = true
                handleViewForCategoriesPage()
            } else {*/
            if(isFirstTime) {
                binding.rlToolbar.hide()
                val menuItem = getPageNameAndType(leftMenuItemList, menuItemId)
                pageName = menuItem.pageName
                viewModel.searchPageName = menuItem.searchPageName
                viewModel.setPageType(menuItem.pageType?:"")
                viewModel.setPageName(pageName?:"Home")
                viewModel.setPageNameDrp(pageName?:"")
                e("fetchHomeData", "using nav inside SubFragment updateInPack : $it")
                viewModel.fetchHierarchyData(true)
//                viewModel.fetchHomeData(true)
            }

            viewModel.miscAnalytics.trackMixPanelHomePageView(
                pageName ?: "",
                drpEnabled = if (viewModel.checkDRPpages(sharedPrefs.getConfigResponse()?.data?.config?.drpPartnerPages)) YES else NO
            )



            //viewModel.searchPageName = it.getString("searchPageName") ?: ""
            val linearLayoutManager = binding.homeRecyclerView.layoutManager as LinearLayoutManager
            binding.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    Log.d("Exchanger", "updateAdapter onScrolled dx: $dx, dy: $dy")
                    if(!viewModel.isLoadMore || viewModel.sizeOfAdapter < 8){
                        if(dy > 0
                            &&!viewModel.isPullToRefresh && viewModel.isOnProgress <= 0
                            && totalItemCount > viewModel.pageOffset) {
                            viewModel.pageOffset += viewModel.PAGELIMIT
                            viewModel.isLoadMore = true
                            e("fetchHomeData", "updateAdapter using onScroll pageOffset:${viewModel.pageOffset}")
                            viewModel.fetchHomeData(false)
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    Log.d("Exchanger", "updateAdapter onScrollStateChanged newState:$newState")
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val first = linearLayoutManager.findFirstVisibleItemPosition()
                        val last = linearLayoutManager.findLastVisibleItemPosition()

                        if (firstIndex == first && firstIndex!=-1)
                            return
                        else
                            if (firstIndex < first) {
                                Log.d("Exchanger", "scrollUp")
                                viewModel.getAdapter().handleHomeTrailerScrolling(
                                    linearLayoutManager.findFirstVisibleItemPosition(),
                                    linearLayoutManager.findLastVisibleItemPosition(),
                                    false
                                )
                            } else {
                                Log.d("Exchanger", "scrollDown")

                                viewModel.getAdapter().handleHomeTrailerScrolling(
                                    linearLayoutManager.findFirstVisibleItemPosition(),
                                    linearLayoutManager.findLastVisibleItemPosition(),
                                    true
                                )
                            }
                        firstIndex = first
                        lastIndex = last


                    }
                }

            })
        }
    }

    private fun handleViewForCategoriesPage() {
        val landingActivity = (activity as? LandingActivity)
        pageName = landingActivity?.categoryPageName
        viewModel.searchPageName = pageName?.toUpperCase()
        viewModel.setPageName(pageName?:"")
        viewModel.setPageType(landingActivity?.categoryPageType?:"")
        binding.tvCategories.show()
        binding.tvCategories.text = pageName
        binding.rlToolbar.show()
        binding.imgBack.hide()
        binding.ivLogo.hide()
        binding.tvCategories.setOnClickListener {
            (activity as LandingActivity).showCategoryBottomSheet()
        }
        binding.searchIcon.setSingleOnClick(2000){
            findNavController().navigateSafe(
                SubFragmentDirections.actionGlobalSearch()
            )
        }

    }


    private fun getPageNameAndType(
        leftMenuItemList: List<LeftMenuItem>,
        menuItemId: Int?
    ) : LeftMenuItem {
        var menuItem = LeftMenuItem()
        try {
            menuItem = leftMenuItemList[
                    when (menuItemId) {
                        R.id.movies -> 1
                        R.id.shows -> 2
                        R.id.sports -> 3
                        R.id.gametab -> 4
                        else -> 0
                    }]
            return menuItem//(menuItem.pageName ?: "", menuItem.pageType ?: "", menuItem.searchPageName ?: "")
        }
        catch (e : Exception){
            menuItem.pageName = ""
            menuItem.pageType = ""
            return menuItem
        }

    }

    private fun onHomeResponseFetched(homeResponse: HomeResponse) {
        isFirstTime = false
        if (homeResponse.data == null) {
            homeResponse.data = HomeResponse.Data()
            homeResponse.data!!.items = mutableListOf()
        }
        if ((homeResponse.data?.items?.size?:0 ==0 || homeResponse.data?.total == 0)
            && homeResponse.data?.offset ?: 0 == 0) {
            binding.tvNoData.show()
            binding.homeRecyclerView.hide()
        } else {
            binding.homeRecyclerView.show()
            binding.tvNoData.hide()
        }


        if (homeResponse.data?.offset ?: 0 == 0) {
            if (::endlessScrollListener.isInitialized) {
                binding.homeRecyclerView.removeOnScrollListener(endlessScrollListener)
            }
            endlessScrollListener = object :
                EndlessRecyclerOnScrollListener(binding.homeRecyclerView.layoutManager as LinearLayoutManager) {
                override fun onLoadMore(current_page: Int) {
                    e("fetchHomeData", "using loadMore isOnProgress:${viewModel.isOnProgress}," +
                            " isPullToRefresh: ${viewModel.isPullToRefresh}")
                    if(!viewModel.isPullToRefresh && viewModel.isOnProgress <= 0
                        && totalItemCount > viewModel.pageOffset) {
                        viewModel.pageOffset += viewModel.PAGELIMIT
                        viewModel.isLoadMore = true
                        e("fetchHomeData", "using loadMore viewModel.pageOffset:${viewModel.pageOffset}")
                        viewModel.fetchHomeData(false)
                    }
                }
            }
            binding.homeRecyclerView.scrollToPosition(0)
            binding.homeRecyclerView.addOnScrollListener(endlessScrollListener)
        }

        viewModel.updateList(homeResponse)
    }

    override fun getViewModelClass(): Class<SubViewModel> = SubViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subpage

    override fun onError(errorModel: ErrorModel) {
        super.onError(errorModel)
        handleNoData()
    }

    private fun checkDrpCacheThreshold(): Boolean {
        if (sharedPrefs?.getConfigResponse() == null
            || sharedPrefs.getConfigResponse()?.data == null
            || sharedPrefs.getConfigResponse()?.data?.config == null
        ) {
            return false
        }
        if (sharedPrefs.getConfigResponse()?.data?.config?.bingeAndroidDrpEnabled == false || sharedPrefs.getConfigResponse()?.data?.config?.bingeAndroidDrpEnabled == null) {
            return false
        }
        val cacheDuration =
            sharedPrefs.getConfigResponse()?.data?.config?.bingeDrpCacheDuration ?: 0
        val lastCachedTime =
            sharedPrefs.getHierarchyData(viewModel.getPageNameDrp())?.cacheTimeStamp ?: 0L

        if(lastCachedTime == 0L)
            return true

        if (cacheDuration == 0) {
            return false
        } else {
            val thresholdCalendarTime = Calendar.getInstance()
            thresholdCalendarTime.timeInMillis = lastCachedTime
            thresholdCalendarTime.add(Calendar.MINUTE, cacheDuration)

            val currentTime = Calendar.getInstance()
            currentTime.timeInMillis = System.currentTimeMillis()

            e("DRP : currentTime : " , "${currentTime[Calendar.HOUR_OF_DAY]} : ${currentTime[Calendar.MINUTE]}")
            e("DRP : thresholdCalendarTime : ",  "${thresholdCalendarTime[Calendar.HOUR_OF_DAY]} : ${thresholdCalendarTime[Calendar.MINUTE]}")
            e("DRP : ",  "${thresholdCalendarTime.compareTo(currentTime)}")

            return thresholdCalendarTime.compareTo(currentTime) < 1

        }
    }

    override fun onResume() {
        super.onResume()
        e(this.javaClass.simpleName, "inside onResume")
        val sharedPrefsLanguages = sharedPrefs.getPrefLanguages()
        if(mCurrentPreferredLanguages == null){
            mCurrentPreferredLanguages = sharedPrefsLanguages
        }else if(mCurrentPreferredLanguages!= sharedPrefsLanguages){
            mCurrentPreferredLanguages = sharedPrefsLanguages
        }
        e("fetchHomeData", "isFirstTime: $isFirstTime, viewModel.isUserLoggedIn : ${viewModel.isUserLoggedIn} ")

        if (viewModel.checkDRPpages(sharedPrefs.getConfigResponse()?.data?.config?.drpPartnerPages)) {
            if (checkDrpCacheThreshold()) {
                e("DRP : ", "Page refreshed cache Duration exeeded")
                refreshPage()
            }
        }
        if(!isFirstTime && sharedPrefs.getLoginStatus() != viewModel.isUserLoggedIn) {
            refreshPage()
            (activity as? LandingActivity)?.showFirestickOfferDialogByFrequency()
        }
        else {
            if(viewModel.isContinueWatching)
                viewModel.refreshContinueWatching()
            if(viewModel.isGameFav)
                viewModel.refreshGameFav()
            if(viewModel.isTvodRail  && sharedPrefs.getLoginStatus())
                viewModel.refreshTvodRail()
            if(viewModel.isWatchlist  && sharedPrefs.getLoginStatus())
                viewModel.refreshWatchlistRail()
        }

        val menuItemId = (activity as? LandingActivity)?.currentMenuItemId?.value

        /*menuItemId?.let{
            if((activity as LandingActivity).packUpdateStatus[menuItemId] == true){
                refreshPage()
                (activity as LandingActivity).packUpdateStatus.put(menuItemId,false)
            }
        }*/

        (activity as? LandingActivity)
            ?.parentalControlSnackbarUtil
            ?.hideParentalControlSnackbar()
        if(isCategories){
            viewModel.sharedPrefs.setInterruptCategoryTabStatus(true)
            handleViewForCategoriesPage()
        }
        else
            binding.rlToolbar.hide()

        (activity as? LandingActivity)?.handleSubscribeButtonVisibility()
        viewModel.getAdapter().handleHomeTrailerPlayBack(false)
        showInAppRatingDialogWithEligibilityCheck()
    }

    private fun showInAppRatingDialogWithEligibilityCheck() {
        val appRatingFrequency =
            sharedPrefs.getConfigResponse()?.data?.config?.appRating?.appRatingFrequency
        if (!isEligibleForAppRating(appRatingFrequency)) return
        d(TAG, "AppRating Eligible for rating")
        context?.let {
            //After showing popup reset this count
            sharedPrefs.saveNumberOfContentPlaybackForAppRating(0)
            val appRatingDialogData =
                sharedPrefs.getConfigResponse()?.data?.config?.appRating

            viewModel.miscAnalytics.trackAppRattingPopupInitiate(
                pageName,
                sharedPrefs.getSubscribedPack()?.productName?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.amountValue?: FREEMIUM
            )
            AppRatingUtil.showAppRatingDialog(
                it,
                appRatingDialogData?.appRatingHeaderVerbiage,
                appRatingDialogData?.appRatingProceedCtaVerbiage,
                appRatingDialogData?.appRatingSkipCtaVerbiage,
                appRatingDialogData?.appRatingImg
            ) { clickedYes ->
                d(TAG, "AppRating Clicked $clickedYes")

                if (clickedYes){
                    viewModel.miscAnalytics.trackAppRattingPopupYes(
                        pageName,
                        sharedPrefs.getSubscribedPack()?.productName?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.amountValue?: FREEMIUM
                    )

                    //Update the API and also local storage
                    val appRatingRequest = SetAppRatingRequest(
                        DeviceInfoUtils.getDeviceId(requireContext()),
                        clickedYes
                    )
                    viewModel.setAppRatingEligibility(appRatingRequest)

                }else{

                    viewModel.miscAnalytics.trackAppRattingPopupNotReally(
                        pageName,
                        sharedPrefs.getSubscribedPack()?.productName?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.amountValue?: FREEMIUM
                    )
                }
            }
        }
    }

    private fun isEligibleForAppRating(appRatingFrequency: String?): Boolean =
        appRatingFrequency?.takeIf { it.isNotEmptyAndIsDigitAndIsGreaterThanZero() }?.let {
            sharedPrefs.getNumberOfContentPlaybackForAppRating() >= it.toInt()
        } ?: false


    private fun isNotDataRefreshingOrLoading() =
        !viewModel.isPullToRefresh
                && viewModel.isOnProgress <= 0
//                && totalItemCount > viewModel.pageOffset

    override fun setObserver() {
        commonViewModel?.getSubscribeBtnVisibility()?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{
                viewModel.setGameWidgetVisibilityInAdapter(it)
            }
        })

        firstCategoryCall =true

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(KEY_REFRESH_GAME_FAV)
            ?.observe(viewLifecycleOwner, Observer {
                if(it)
                    viewModel.refreshGameFav()
            })

        commonViewModel?.getFakeRefreshHome()?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    binding.homeRecyclerView.scrollToPosition(0)
                }
            }
        })

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<SingleEventParcelizeWrapper>(UPDATE_IN_PACK)
            ?.observe(viewLifecycleOwner, Observer {
                it.booleanEventValue.getContentIfNotHandled()?.let { isPackUpdated ->
                    if (isPackUpdated) {
                        findNavController().previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(UPDATE_IN_PACK, SingleEventParcelizeWrapper(SingleEvent(isPackUpdated)))
                        //User's pack details has changed...Refresh the data for Crown visibility
                        d(this.javaClass.simpleName, "currentBackStackEntry UPDATE_IN_PACK $isPackUpdated")
                        //refresh Page
                        doPullToRefreshWithFSChecks()
                    }
                }
            })

        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                e("SubFragment", "inside updateInPack isFirstTime: $isFirstTime, " +
                        "viewModel.isPullToRefresh : ${viewModel.isPullToRefresh} " +
                        " totalItemCount: $totalItemCount" +
                        "viewModel.isOnProgress : ${viewModel.isOnProgress} " +
                        "viewModel.pageOffset : ${viewModel.pageOffset} "
                )
                viewModel.setpackUpdated(true)
                //refresh Page
                if(isNotDataRefreshingOrLoading())
                    doPullToRefreshWithFSChecks()
            }
        })

        viewModel.getPullToRefreshChange().observe(viewLifecycleOwner, Observer {
            if (binding.swipeRefresh.isRefreshing)
                binding.swipeRefresh.isRefreshing = false
        })

        viewModel.getHomeData().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { hr ->
                if(isCategories && firstCategoryCall){
                    e("fetchHomeData", "category cancelled")

                    firstCategoryCall =false
                }else{
                    e("fetchHomeData", "category called")
                    onHomeResponseFetched(hr)}
            }
        })
        viewModel.getChangedCount().observe(viewLifecycleOwner, Observer {
            if (::endlessScrollListener.isInitialized) {
                it.getContentIfNotHandled()?.let { changedCount ->
                    totalItemCount = changedCount
                    endlessScrollListener.setTotalEntries(changedCount)
                }
            }
        })
        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer
        {

            val forward = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                this.duration = 250
            }
            enterTransition = forward

            val backward = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                this.duration = 250
            }
            returnTransition = backward
            reenterTransition = backward
//            exitTransition = backward


            it.getContentIfNotHandled()?.let { contentIfNotHandled ->
                if (contentIfNotHandled.contentItem.contentType == TYPE_GAMES) {
                    if(sharedPrefs.getLoginStatus()) {
                        getGamesActivityIntent(requireContext(), contentIfNotHandled.contentItem, contentIfNotHandled.gamesMixpanelInfoModel)?.let{
                            startActivity(
                                it
                            )
                        }
                    } else {
                        contentIfNotHandled.gamesMixpanelInfoModel?.let{
                            viewModel.miscAnalytics.trackGameClick(
                                pageName = it.pageName,
                                railTitle = it.railTitle,
                                railPosition = it.railPosition,
                                railType = it.railType,
                                railCategory = it.railCategory,
                                gameGenre = it.gameGenre,
                                gamePartner = it.gamePartner,
                                gamePosition = it.gamePosition,
                                gameRating = it.gameRating,
                                gameTitle = contentIfNotHandled.contentItem.title,
                                freeGame = YES,
                                releaseYear = it.releaseYear,
                                deviceType = PLATFORM_ANDROID_CAPS,
                                source = it.source,
                                packPrice = FREEMIUM,
                                packName = FREEMIUM
                            )
                        }
                        commonViewModel?.getPreviouslyUsedMobileNumbers()
                    }
                }
                else if (contentIfNotHandled.sectionSource == EventConstants.TYPE_SELECT_PAID_PACK
                    || contentIfNotHandled.sectionSource == EventConstants.TYPE_START_FREE_TRIAL
                ) {
                    activity?.let { activity ->
                        startActivity(
                            getSubscriptionActivityIntent(
                                activity,
                                false,
                                null,
                                (pageName?:SOURCE_HOME) + "screen",
                                initiateRecharge = false,
                                fromDialog = true,
                                isFromNudge = true
                            )
                        )
                    }
                }
                else if (contentIfNotHandled.sectionSource.equals(
                        ItemViewType.CATEGORY.name
                    )){
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionActionHomeLandingToSubHomeFragment(
                            contentIfNotHandled.contentItem.pageType,//pageType
                            "", //Empty for categories
                            "", // Empty for categories
                            "", // Empty for categories
                            contentIfNotHandled.contentItem.title
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()

                } else if (contentIfNotHandled.sectionSource.equals(
                        ItemViewType.LANGUAGE.name,
                        true
                    ) ||
                    contentIfNotHandled.sectionSource.equals(ItemViewType.GENRE.name, true)
                    || contentIfNotHandled.sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name,true)
                ) {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionHomeFragmentToLanguageGenreFragment(
                            contentIfNotHandled.contentItem.title,
                            contentIfNotHandled.sectionSource,
                            viewModel.searchPageName ?: "",
                            contentIfNotHandled.contentItem.backgroundImage?:"",
                            contentIfNotHandled.contentItem.image,
                            source = getSource(findNavController().currentDestination?.id),
                            refId = contentIfNotHandled.contentItem.refId
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                }
                /*Sprint 1 Freemium - Popular Character*/
                else if (contentIfNotHandled.sectionSource.equals(ItemViewType.CHARACTER.name, true)) {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionHomeFragmentToLanguageGenreFragment(
                            "Chhota Bheem",
                            contentIfNotHandled.sectionSource,
                            viewModel.searchPageName ?: "",
                            contentIfNotHandled.contentItem.image,
                            source = getSource(findNavController().currentDestination?.id),
                            refId = contentIfNotHandled.contentItem.refId
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                }
                else if (contentIfNotHandled.contentItem.contentType == TYPE_SUB_PAGE) {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionActionHomeLandingToSubHomeFragment(
                            contentIfNotHandled.contentItem.pageType,//pageType
                            contentIfNotHandled.contentItem.provider,
                            contentIfNotHandled.contentItem.image,
                            contentIfNotHandled.contentItem.partnerId ?: "",
                            contentIfNotHandled.contentItem.title
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                } else if (contentIfNotHandled.sectionSource == EventConstants.TYPE_SELECT_LANGUAGE_POP_UP) {
                    showLanguageBottomSheet()
                } else if (contentIfNotHandled.sectionSource == ItemViewType.GAME_NUDGE.name){



                    checkManagedAppEligibility(
                        sharedPrefs=sharedPrefs,
                        activity=activity,
                        context = context,
                        partnerId = contentIfNotHandled?.contentItem?.partnerId ?: "",
                        fromScreen = SOURCE_GAME_NUDGE,
                        startPackListing = true
                    )


                } else
                    if (contentIfNotHandled.sectionSource == EventConstants.TYPE_MID_SCROLL_BANNER) {
                        when (contentIfNotHandled.contentItem.screenName) {
                            MID_SCROLL_DETAIL_SCREEN -> {
                                findNavController().navigateSafe(
                                    SubFragmentDirections.actionToDetail(
                                        contentIfNotHandled.contentItem
                                    ), contentIfNotHandled.extras
                                )
                            }
                            MID_SCROLL_MOVIES -> {
                                mHandler.postDelayed({
                                    (requireActivity() as LandingActivity).setSelectedTab(
                                        R.id.movies
                                    )
                                }, 500)

                            }
                            MID_SCROLL_SHOWS -> {
                                mHandler.postDelayed({(requireActivity() as LandingActivity).setSelectedTab(R.id.shows)},500)
                            }
                            MID_SCROLL_GAMEZOP -> {

                                (requireActivity() as LandingActivity).setSelectedTab(R.id.gametab)
                            }
                            MID_SCROLL_SPORTS -> {
                                (requireActivity() as LandingActivity).setSelectedTab(R.id.sports)
                            }
                            else -> {
//                            //Default case of midscroll move to pack listing

                                checkManagedAppEligibility(
                                    sharedPrefs=sharedPrefs,
                                    activity=activity,
                                    context = context,
                                    partnerId = contentIfNotHandled?.contentItem?.partnerId ?: "",
                                    fromScreen = SOURCE_MIDSCROLL_NUDGE,
                                    startPackListing = true,
                                    checkFdo = true
                                )
                            }
                        }
                    } else if (contentIfNotHandled.sectionSource == EventConstants.TYPE_MID_SCROLL) {

                        checkManagedAppEligibility(
                            sharedPrefs=sharedPrefs,
                            activity=activity,
                            context = context,
                            partnerId = contentIfNotHandled?.contentItem?.partnerId ?: "",
                            fromScreen = SOURCE_MIDSCROLL_NUDGE,
                            startPackListing = true,
                            checkFdo = true,
                            packName =  contentIfNotHandled.contentItem.packName ?: ""
                        )


                    } else if (contentIfNotHandled.sectionSource == EventConstants.TYPE_MID_SCROLL_GAMES) {
                        Handler().postDelayed(
                            {
                                (requireActivity() as LandingActivity).setSelectedTab(R.id.gametab)
                                commonViewModel?.fakeRefreshHome?.postValue(SingleEvent(true))
                            }, 500
                        )
                    } else {
                        findNavController().navigateSafe(
                            SubFragmentDirections.actionToDetail(
                                contentIfNotHandled.contentItem
                            ), contentIfNotHandled.extras
                        )
                        (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                    }
            }
        })


        viewModel.getSeeAllClickedRail().observe(viewLifecycleOwner, Observer
        {
            it.getContentIfNotHandled()?.let { seeAllTransition ->
                if (seeAllTransition.sectionSource.equals(ItemViewType.LANGUAGE.name, true)
                    || seeAllTransition.sectionSource.equals(ItemViewType.GENRE.name, true)
                ) {
                } else if (seeAllTransition.sectionSource.equals(
                        ItemViewType.PROVIDER.name,
                        true
                    )
                ) {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionActionHomeLandingToAppSeeAllFragment(
                            seeAllTransition.railIdName.first,
                            seeAllTransition.railIdName.second
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                } else {
                    e("SubFragment","MixedRail isMixedRail:${seeAllTransition.isMixedRail}" +
                            ", isPrepand : ${seeAllTransition.isPrepand}")
                    var railResponse : RecommendationResponse? = null
                    if(!seeAllTransition.placeHolder.isNullOrEmpty()) {
                        railResponse = RecommendationResponse()
                        railResponse.data = seeAllTransition.item
                    }
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionActionHomeLandingToSeeAllFragment(
                            seeAllTransition.railIdName.first.toString(),
                            seeAllTransition.railIdName.second,
                            seeAllTransition.sectionSource,
                            false,
                            null,
                            null,
                            seeAllTransition.placeHolder,
                            seeAllTransition.source,
                            seeAllTransition.configType,
                            seeAllTransition.isMixedRail,
                            seeAllTransition.isPrepand,
                            taContentResponse = railResponse,
                            backgroundImage = seeAllTransition.backgroundImage,
                            layoutType = seeAllTransition.layoutType,
                            railPosition = seeAllTransition.railPosition.toString(),
                            refId = seeAllTransition.refId,
                            packName = seeAllTransition.packName
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                }
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        commonViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[CommonSampleViewModel::class.java]
        super.onActivityCreated(savedInstanceState)
        binding.swipeRefresh.setColorSchemeColors(Color.BLUE, Color.MAGENTA, Color.RED)
        binding.homeRecyclerView.setItemViewCacheSize(50)

        handleCommonViewModelObservers()

        val menuItemList = LeftMenuResponseCache.leftMenuItemList
        val menuItemId = (activity as? LandingActivity)?.currentMenuItemId?.value
        if(menuItemList.isNotEmpty()){
            val menuItem = getPageNameAndType(menuItemList, menuItemId)
            if(pageName!=menuItem.pageName){
                initViews(menuItemList)
            }
        }

        commonViewModel?.progressListener?.observe(viewLifecycleOwner, Observer {
            viewModel.setProgressing(it)
        })

        mHomeBottomSheetViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            )[HomeBottomSheetViewModel::class.java]

        navDrawerViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            )[SettingsViewModel::class.java]

        handleHomeBottomSheetObservers()


        lifecycleScope.launch {
            delay(3000L)
            if ((activity as? LandingActivity)?.intent?.extras?.getString(KEY_FROM_SCREEN)
                    .equals(
                        SOURCE_DEEPLINK
                    )
            ) {
                sharedPrefs.setFirstTimeLanguagePopUpShown(false)
                return@launch

            }
            if (viewModel.sharedPrefs.isFirstTimeLanguagePopUpShown()
                && !mShownLanguagesForFirstTime
            ) {
                showLanguageBottomSheet()
            }
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.pageOffset = 0
            viewModel.isPullToRefresh = true
            incrementLaunchCount(sharedPrefs)
            e("fetchHomeData", "using swipe inside SubFragment updateInPack")
            val menuItemList = LeftMenuResponseCache.leftMenuItemList
            if (!isCategories && menuItemList.isEmpty()) {
                viewModel.fetchMenuItems {
                    initViews(it)
                }
            }
            else if(isCategories && pageName.isNullOrEmpty()){
                (activity as LandingActivity).showCategoryBottomSheet()
            }
            else {
                viewModel.fetchHierarchyData(false)
//                viewModel.fetchHomeData(false)
            }
        }
        (binding.homeRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun handleHomeBottomSheetObservers() {
        mHomeBottomSheetViewModel?.let { homeBottomSheetViewModel ->
            homeBottomSheetViewModel.refreshLanguageWidgetStatus()
                .observe(viewLifecycleOwner, Observer{ refreshWidgetStatus ->
                    when (refreshWidgetStatus) {
                        REFRESH_HOME -> {
                            if (viewModel.getLanguageWidgetVisibility())
                                refreshPage()
                        }
                        else -> {
                            viewModel.handleLanguageWidget()
                        }
                    }

                })

            homeBottomSheetViewModel.getLanguagePopulateCallback()
                .observe(viewLifecycleOwner) {
                    it.getContentIfNotHandled()?.let { status ->
                        if (status) {
                            if (findNavController().currentDestination?.id
                                == R.id.selectLanguageBottomSheetDialog
                            ) {
                                return@let
                            }
                            findNavController().navigateSafe(
                                SubFragmentDirections.actionSelectLanguageBottomSheetDialog()
                            )
                            (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                            mShownLanguagesForFirstTime = true
                        }
                    }

                }

            /*if ((activity as? LandingActivity)?.currentMenuItemId?.value == R.id.others) {
                commonViewModel?.getSelectedCategory()?.observe(viewLifecycleOwner) {
                    it.getContentIfNotHandled()?.let { (categoriesPageName, categoriesPageType) ->
                        viewModel.homeAnalytics.trackMenuCategoryClick(categoriesPageName)
                        viewModel.getAdapter().clear()
                        pageName = categoriesPageName
                        binding.tvCategories.show()
                        binding.tvCategories.text = pageName
                        viewModel.setPageName(categoriesPageName)
                        viewModel.setPageType(categoriesPageType)
                        firstCategoryCall =false
                        refreshPage()
                    }
                }
            }*/

            homeBottomSheetViewModel.progressListener.observe(viewLifecycleOwner) {
                viewModel.setProgressing(it)
            }

            homeBottomSheetViewModel.errorMessage.observe(viewLifecycleOwner){
                it?.getContentIfNotHandled()?.let { error ->
                    viewModel.setViewModelError(error)
                }
            }
        }
    }

    private fun handleCommonViewModelObservers() {
        commonViewModel?.previouslyUsedMobileNumberResponse?.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                (activity as? LandingActivity)
                    ?.parentalControlSnackbarUtil
                    ?.run {
                        findNavController().navigateSafe(
                            SubFragmentDirections.actionGlobalGuestLoginBottomDialogFragment(
                                isParentalPinSetupRequested = false,
                                isParentalPinVerificaitionRequested = false,
                                isLoggedIn = sharedPrefs.getLoginStatus(),
                                previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                                (activity as? LandingActivity)?.getSourceOrFromScreenName()
                                    ?.takeIf { loginSource -> loginSource == SOURCE_DEEPLINK }
                                    ?: SOURCE_HAMBURGER
                            )
                        )
                    }
            }
        }

        commonViewModel?.previouslyUsedMobileNumberError?.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                (activity as? LandingActivity)
                    ?.parentalControlSnackbarUtil
                    ?.run {
                        findNavController().navigateSafe(
                            SubFragmentDirections.actionGlobalGuestLoginBottomDialogFragment(
                                isParentalPinSetupRequested = false,
                                isParentalPinVerificaitionRequested = false,
                                isLoggedIn = sharedPrefs.getLoginStatus(),
                                previouslyUsedMobileNumbersList = null,
                                (activity as? LandingActivity)?.getSourceOrFromScreenName()
                                    ?.takeIf { loginSource -> loginSource == SOURCE_DEEPLINK }
                                    ?: SOURCE_HAMBURGER
                            )
                        )
                    }
            }
        }

    }

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    private fun doPullToRefreshWithFSChecks() {
        isFirstTime = true
        viewModel.pageOffset = 0
        viewModel.isPullToRefresh = true

        viewModel.subscribed = viewModel.sharedPrefs.isActivePack()  &&
                sharedPrefs.getPartnerIdsList()?.size?:0 > 0
        viewModel.unsubscribed = !viewModel.subscribed
        //FS Dialog checks
        if(sharedPrefs.getLoginStatus() != viewModel.isUserLoggedIn) {
            viewModel.getAdapter().clear()
            (activity as? LandingActivity)?.showFirestickOfferDialogByFrequency()
        }
        viewModel.fetchHierarchyData(true)
        (activity as? LandingActivity)?.handleSubscribeButtonVisibility()
    }

    private fun refreshPage() {
        viewModel.pageOffset = 0
        viewModel.subscribed = viewModel.sharedPrefs.isActivePack()  &&
                sharedPrefs.getPartnerIdsList()?.size?:0 > 0
        viewModel.unsubscribed = !viewModel.subscribed
        e("fetchHomeData", "using refresh inside SubFragment updateInPack")
        viewModel.fetchHierarchyData(true)
    }

    private fun showLanguageBottomSheet(){
        mHomeBottomSheetViewModel?.fetchLanguages ()
    }

    override fun onPause() {
        super.onPause()
//        (activity as? LandingActivity)?.let { landingActivity ->
//            if (landingActivity.tabWasSelected) {
//                Handler(Looper.getMainLooper()).post {
//                    landingActivity.tabWasSelected = false
//                    binding.homeRecyclerView.scrollToPosition(0)
//                }
//            }
//        }
        viewModel.isUserLoggedIn = sharedPrefs.getLoginStatus()
//        viewModel.getAdapter().handleHomeTrailerPlayBack(true)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if((activity as? LandingActivity)?.currentMenuItemId?.value != R.id.home){
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                if(binding.swipeRefresh.isRefreshing){
                    binding.swipeRefresh.isRefreshing=false
                }
                else if((activity as? LandingActivity)?.currentMenuItemId?.value == R.id.home){
                    requireActivity().finish()
                }else{
                    (requireActivity() as LandingActivity).setSelectedTab(R.id.home)
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
//        }
    }

    fun checkManagedAppEligibility(

        sharedPrefs: PrefsRepo,
        activity: FragmentActivity?,
        partnerId: String,
        context: Context?,
        checkFdo: Boolean = false,
        startPackListing: Boolean = false,
        fromScreen: String = SOURCE_NOTIFICATION,
        packName: String? = null,

        ) {

        viewModel.checkForManagedAppEligibility { eligible ->
            if(eligible){
                moveToManagedApp(
                    sharedPrefs,
                    activity,
                    partnerId,
                    context,
                    checkFdo
                )
            }else{
                startActivity(
                    getSubscriptionActivityIntent(
                        activity,
                        fromScreen = fromScreen,
                        startPackListing = startPackListing,
                        packName = packName,
                        partnerId=partnerId,

                        )
                )
            }
        }

    }
}
