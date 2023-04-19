package com.tatasky.binge.ui.features.home.subpage

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentSeeAllBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.RECOMMENDATION
import kotlinx.android.synthetic.main.layout_rail_item_trailer.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 2/12/19.
 */
class SeeAllFragment : BaseFragment<FragmentSeeAllBinding, SeeAllViewModel>() {

    private var packName: String?=null

    @Inject
    lateinit var seeAllAnalytics: SeeAllAnalytics
    private var isPositionWithOffset: Boolean = false
    private var lastItemPosition: Int = 0
    private var isFirstTime: Boolean = true
    val subPageGridFragmentArgs by navArgs<SeeAllFragmentArgs>()
    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    private var filteredContentItems: List<ContentItem> = ArrayList()

    override fun getViewModelClass(): Class<SeeAllViewModel> = SeeAllViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_see_all

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.let {
                        findNavController().navigateUpOrOpenHome(it as AppCompatActivity)
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        startPostponedEnterTransition()
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            this.duration = 250
//        }
//        enterTransition = forward
//
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            this.duration = 250
//        }
//        returnTransition = backward
//        reenterTransition = backward
//        exitTransition = forward
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_icon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_menu -> {
                findNavController().navigateSafe(SeeAllFragmentDirections.actionGlobalSearch())
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun toBeCalledOnce() {
        binding.viewModel = viewModel
        viewModel.onlyMessage = false
        var pageName = (activity as? LandingActivity?)?.getPageName()
        viewModel?.mAdapter?.pageName = pageName
        e("SeeAllFragment","MixedRail title:${subPageGridFragmentArgs.title}" +
                ", isPrepand : ${subPageGridFragmentArgs.isPrepand}")
        e("SeeAllFragment","item.sectionType : ${subPageGridFragmentArgs.sectionType}")
        e("SeeAllFragment","railPosition : ${subPageGridFragmentArgs.railPosition}")
        packName = subPageGridFragmentArgs.packName ?: ""
        viewModel.configType = subPageGridFragmentArgs.origin ?: EventConstants.TYPE_EDITORIAL
        viewModel.refId = subPageGridFragmentArgs.refId
//        viewModel.source = subPageGridFragmentArgs.source ?: "Deeplink"
        viewModel.railName = subPageGridFragmentArgs.title?.replace("_", " ") ?: ""
        viewModel?.mAdapter?.railTitle = viewModel.railName
        //As of now, For AppsFlyer
        seeAllAnalytics.trackSeeAll(viewModel.railName, subPageGridFragmentArgs.source ?: "Deeplink")
        viewModel.onlyMessage = true
        val response = subPageGridFragmentArgs.taContentResponse
        if(response?.data != null) {
            setAdapterWithResponse(response)
        }
        else
            fetchData(true)

        if (!subPageGridFragmentArgs.backgroundImage.isNullOrBlank()) {
            binding.ivSeeAllBanner.show()
            context?.let { binding.subpageRecycler.updatePadding(top = dpToPx(it, 18)) }
            imageLoad(binding.ivSeeAllBanner, subPageGridFragmentArgs.backgroundImage!!)
        }
    }

    private fun setAdapterWithResponse(response: RecommendationResponse) {
        viewModel.onlyMessage = true

        if(viewModel.railName.isNotBlank()){
            response.data?.title = viewModel.railName
        }
        if(!response.data?.title.isNullOrBlank()) {
            findNavController().currentDestination?.label =response.data?.title
            (activity as LandingActivity).supportActionBar?.title = response.data?.title
        }
        viewModel.pagingState = response.data?.pagingState
        binding.progressBarBottom.startProgressAvd(false)
        updateSbscriberList()
        isHideRailWithPackName(response.data,
            sharedPrefs.getSubscribedPack(), packName,
            mNonSubscribedPartnerList, sharedPrefs.getLoginStatus()
        )

        try {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(200L)
                setAdapter(response)
            }

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private fun fetchData(isShowLoader: Boolean) {
        if(viewModel.pageOffset > 0)
            binding.progressBarBottom.startProgressAvd(true)
        if(subPageGridFragmentArgs.isMixedRail){
            viewModel.provider = subPageGridFragmentArgs.provider
            viewModel.fetchMixedTARails(isShowLoader, subPageGridFragmentArgs.placeHolder ?: "",
                subPageGridFragmentArgs.railId, subPageGridFragmentArgs.isPrepand, subPageGridFragmentArgs.isRecommendation)
        }
        else
            if (subPageGridFragmentArgs.isRecommendation) {
                val data = subPageGridFragmentArgs.detailType?.split("-".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

                if(subPageGridFragmentArgs.placeHolder.isNullOrEmpty()) {
                    viewModel.fetchRecommendations(
                        isShowLoader,
                        subPageGridFragmentArgs.railId,
                        subPageGridFragmentArgs.detailType!!
                    )
                }
                else if(data != null){

                    val showType = if (data[0] == "CatchupEPG") "EPG" else "VOD"
                    val contentType = data[1]
                    viewModel.getTARecommendations(isShowLoader,
                        subPageGridFragmentArgs.placeHolder ?: "",
                        subPageGridFragmentArgs.railId,
                        contentType,
                        showType,
                        subPageGridFragmentArgs.provider ?: ""
                    )
                }
            }
            else if (subPageGridFragmentArgs.sectionType.equals(CONTINUE_WATCHING, ignoreCase = true)) {
                viewModel.provider = subPageGridFragmentArgs.provider
                viewModel.getContinueWatchingData(isShowLoader)
            }
            else if (subPageGridFragmentArgs.sectionType.equals(ItemViewType.FAVOURITES.name,true)){
                if(subPageGridFragmentArgs.layoutType.equals(ItemLayoutType.SQUARE.name,true)){
                    viewModel.layoutType = ItemLayoutType.SQUARE.name
                } else {
                    viewModel.layoutType = ItemLayoutType.LANDSCAPE.name
                }
                viewModel.fetchGameFavs()
            }
            else if (subPageGridFragmentArgs.sectionType.equals(ItemViewType.GAMEZOP_CONTINUE_PLAYING.name,true)){
                if(subPageGridFragmentArgs.layoutType.equals(ItemLayoutType.SQUARE.name,true)){
                    viewModel.layoutType = ItemLayoutType.SQUARE.name
                } else {
                    viewModel.layoutType = ItemLayoutType.LANDSCAPE.name
                }
                viewModel.fetchGameCw() //TODO GAME RP : use game rp API
            }
            else if (subPageGridFragmentArgs.sectionType.equals(WATCHLIST, ignoreCase = true)) {
                viewModel.provider = subPageGridFragmentArgs.provider
                viewModel.fetchWatchList(isShowLoader)
            }
            else if (subPageGridFragmentArgs.sectionType.equals(RECOMMENDATION, ignoreCase = true)) {
                viewModel.provider = subPageGridFragmentArgs.provider
                viewModel.fetchTARails(isShowLoader, subPageGridFragmentArgs.placeHolder ?: "",subPageGridFragmentArgs.isRecommendation)
            }
            else if (subPageGridFragmentArgs.sectionType.equals(TVOD, ignoreCase = true)) {
                viewModel.fetchTVoD(isShowLoader, false)
            }
            else
                viewModel.fetchRailData(subPageGridFragmentArgs.railId, isShowLoader, null,subPageGridFragmentArgs.sectionType)
    }

    override fun onResume() {
        super.onResume()
        if (!isFirstTime) {
            viewModel.pageOffset = 0
            viewModel.pagingState = null
            if(viewModel.watchlistRail)
                viewModel.fetchWatchList(false)
            else if(viewModel.continueWatching)
                viewModel.getContinueWatchingData(false)
        }
        if(!isFirstTime && viewModel.gameFav){
            viewModel.apply {
                pageOffset = 0
                pagingState = null
                fetchGameFavs()
            }
        }
        if(!isFirstTime && viewModel.gameCw){
            viewModel.apply {
                pageOffset = 0
                pagingState = null
                fetchGameCw()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isFirstTime = false
        if (viewModel.continueWatching|| viewModel.watchlistRail || viewModel.gameFav || viewModel.gameCw) {
            val manager = binding.subpageRecycler.layoutManager as GridLayoutManager
            lastItemPosition = manager.findLastVisibleItemPosition()
            isPositionWithOffset = false
            lastItemPosition = manager.findLastVisibleItemPosition()
            if (manager.findFirstVisibleItemPosition() == 0)
                lastItemPosition = 0

        }
    }

    override fun onError(errorModel: ErrorModel) {
        //super.onError(errorMessage)
        when {
            viewModel.pageOffset == 0 -> context?.let { ctx ->
                if(!viewModel.watchlistRail && !viewModel.gameFav && !viewModel.gameCw)
                    showToast(ctx, errorModel.message ?: getString(R.string.no_content_available))
                findNavController().navigateUp()
            }
            else -> viewModel.hideAdapterLoader()
        }
    }

    override fun setObserver() {

        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                findNavController().navigateSafe(
                    SubFragmentDirections.actionGlobalGuestLoginBottomDialogFragment(
                        false,
                        false,
                        false,
                        response.data?.mobileNUmberList?.toTypedArray(),
                        SOURCE_SEE_ALL
                    )
                )
            }
        }

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(
                    SubFragmentDirections.actionGlobalGuestLoginBottomDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = sharedPrefs.getLoginStatus(),
                        previouslyUsedMobileNumbersList = null,
                        SOURCE_SEE_ALL
                    )
                )
            }
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<SingleEventParcelizeWrapper>(UPDATE_IN_PACK)
            ?.observe(viewLifecycleOwner, Observer {
                it.booleanEventValue.getContentIfNotHandled()?.let { isPackUpdated ->
                    if (isPackUpdated) {
                        findNavController().previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(UPDATE_IN_PACK, SingleEventParcelizeWrapper(SingleEvent(isPackUpdated)))
                        //User's pack details has changed...Refresh the data fro Crown visibility
                        d(this.javaClass.simpleName, "UPDATE_IN_PACK $isPackUpdated")
                        //refresh Page
                        viewModel?.mAdapter?.updatePack()
                        viewModel?.mAdapter?.notifyDataSetChanged()
                    }
                }
            })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
            }
        })

        viewModel.getAnyResponseHideLoader().observe(viewLifecycleOwner, Observer {
            binding.progressBarBottom.startProgressAvd(false)
        })

        viewModel.getRailResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                setAdapterWithResponse(response)
            }
        })
        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer
        {
            val contentIfNotHandled = it.getContentIfNotHandled()
            if (contentIfNotHandled != null) {
                /*if(contentIfNotHandled.extras.sharedElements.isEmpty()){
                    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                        this.duration = 250
                    }
                }else{
                    exitTransition = Hold()
                }*/
                if (contentIfNotHandled.contentItem.id == "0") {
                    contentIfNotHandled.contentItem.id = contentIfNotHandled.contentItem.contentId
                }
                if(contentIfNotHandled.contentItem.contentType.equals(TYPE_GAMES)) {
                    contentIfNotHandled.gamesMixpanelInfoModel?.railPosition = subPageGridFragmentArgs?.railPosition?:""
                    contentIfNotHandled.gamesMixpanelInfoModel?.source = EVENT_VALUE_SEE_ALL
                    if(sharedPrefs.getLoginStatus()) {
                        getGamesActivityIntent(
                            context,
                            contentIfNotHandled.contentItem,
                            contentIfNotHandled.gamesMixpanelInfoModel
                        )?.let { startActivity(it) }
                    }
                    else {
                        contentIfNotHandled.gamesMixpanelInfoModel?.let{
                            seeAllAnalytics.trackGameClick(
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
                                deviceType = sharedPrefs.getDeviceType()?.uppercase()?:"",
                                source = it.source,
                                packPrice = FREEMIUM,
                                packName = FREEMIUM
                            )
                        }
                        viewModel.getPreviouslyUsedMobileNumbers()
                    }
                }
                else {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionToDetail(
                            contentItem = contentIfNotHandled.contentItem,
                            fromGrid = true,
                            contentAnalyticsModel = contentIfNotHandled.contentAnalyticsModel
                        ),
                        contentIfNotHandled.extras
                    )
                }
            }
        })
        viewModel.getChangedCount().observe(viewLifecycleOwner, Observer {
            if (::endlessScrollListener.isInitialized) {
                it.getContentIfNotHandled()?.let { changedCount ->
                    endlessScrollListener.setTotalEntries(changedCount)
                }
            }
        })

        viewModel.updateInOrientation.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                if (it) updateUIAdapter()
            }
        }
    }

    private var mNonSubscribedPartnerList = HashSet<String>()
    fun updateSbscriberList() {
        try {
            sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
                mNonSubscribedPartnerList = HashSet<String>()
                e("SeeAllFragment","partnerList:$partnerList")
                for (partner in partnerList){
                    mNonSubscribedPartnerList.add((partner.partnerName ?: "").toLowerCase())
                }
            }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }


    private fun setAdapter(railResponse: RecommendationResponse) {
        railResponse.data?.dthStatus =  sharedPrefs.getDthStatusFreemium()
        if (viewModel.pageOffset == 0) {
            if(ItemLayoutType.PORTRAIT.name.equals(railResponse.data?.layoutType, true)) {
                val manager = binding.subpageRecycler.layoutManager as GridLayoutManager
                manager.spanCount = resources.getInteger(R.integer.grid_portrait)
            }
            else if(ItemLayoutType.SQUARE.name.equals(railResponse.data?.layoutType, true)) {
                val manager = binding.subpageRecycler.layoutManager as GridLayoutManager
                manager.spanCount = resources.getInteger(R.integer.grid_game_square)
            }
            if(subPageGridFragmentArgs.sectionType.equals(ItemViewType.FAVOURITES.name,true)){
                val manager = binding.subpageRecycler.layoutManager as GridLayoutManager
                manager.spanCount = resources.getInteger(R.integer.grid_game_square)
            }
            if (railResponse.data?.filteredContentItems?.size == 0) {
                binding.tvNoData.visibility = View.VISIBLE
                binding.subpageRecycler.hide()
                onError(ErrorModel(message = getString(R.string.no_content_available)))
            } else {
                binding.subpageRecycler.show()
                binding.tvNoData.visibility = View.GONE
            }
            if (::endlessScrollListener.isInitialized) {
                binding.subpageRecycler.removeOnScrollListener(endlessScrollListener)
            }
            endlessScrollListener = object :
                EndlessRecyclerOnScrollListener(binding.subpageRecycler.layoutManager as GridLayoutManager) {
                override fun onLoadMore(current_page: Int) {
                    loadMoreContent()
                }
            }

            if(!viewModel.continueWatching && !viewModel.watchlistRail && !viewModel.gameFav && !viewModel.gameCw)
                binding.subpageRecycler.scrollToPosition(0)
            binding.subpageRecycler.addOnScrollListener(endlessScrollListener)

        }
        if ((railResponse.data?.filteredContentItems?.size ?: 0) >= 0
            || viewModel.continueWatching
            || viewModel.watchlistRail
            || viewModel.gameFav
            || viewModel.gameCw) {
            viewModel.updateList(
                railResponse,
                subPageGridFragmentArgs.contentAnalyticsModel?.copy(
                    railTitle = viewModel.railName.takeIfNotEmptyOrNull()
                        ?: subPageGridFragmentArgs.title.takeIfNotEmptyOrNull()
                        ?: railResponse.data?.title
                ) ?: ContentAnalyticsModel(
                    null,
                    subPageGridFragmentArgs.sectionType,
                    viewModel.railName.takeIfNotEmptyOrNull()
                        ?: subPageGridFragmentArgs.title.takeIfNotEmptyOrNull()
                        ?: railResponse.data?.title
                )
            )
        }
        if(viewModel.pageOffset == 0 && (viewModel.continueWatching
                    || viewModel.watchlistRail
                    ||viewModel.gameFav || viewModel.gameCw)) {
            val isNewItemAdded = checkNewItem(railResponse.data)
            if (isNewItemAdded)
                binding.subpageRecycler.scrollToPosition(0)
            filteredContentItems = railResponse.data?.filteredContentItems ?: ArrayList()
        }
        e("SeeAllFragment","railResponse.data?.totalCount : ${railResponse.data?.totalCount}, " +
                "viewModel.pageOffset: ${viewModel.pageOffset}, " +
                "viewModel.PAGELIMIT : ${viewModel.PAGELIMIT}")
        if((railResponse.data?.filteredContentItems?.size ?: 0) == 0 &&
            railResponse.data?.totalCount?:0 > ((viewModel.pageOffset) + viewModel.PAGELIMIT)){
            loadMoreContent()
        }
    }

    private fun loadMoreContent() {
        if(viewModel.continueWatching || viewModel.watchlistRail)
            viewModel.pageOffset++
        else if(viewModel.gameFav)
            viewModel.pageOffset++
        else if (viewModel.gameCw)
            viewModel.pageOffset++
        else
            viewModel.pageOffset += viewModel.PAGELIMIT
        fetchData(false)
    }

    private fun checkNewItem(data: HomeResponse.Items?): Boolean {
        if(filteredContentItems.isNotEmpty() && filteredContentItems.size > 0
            && data?.filteredContentItems?.size ?: 0 > 0
            && filteredContentItems[0].contentId != data?.filteredContentItems!![0].contentId)
            return true
        return false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIAdapter()

    }
    private fun updateUIAdapter(){
        activity?.let {
            if(isTablet(it)){
                (binding.subpageRecycler.adapter as? ItemGridAdapter)?.let { adapter ->
                    val layoutManager =
                        binding.subpageRecycler.layoutManager as GridLayoutManager

                    when {
                        subPageGridFragmentArgs.layoutType.equals(
                            ItemLayoutType.LANDSCAPE.name,
                            true
                        ) ->
                            layoutManager.spanCount = resources.getInteger(R.integer.grid_landscape)

                        subPageGridFragmentArgs.layoutType.equals(
                            ItemLayoutType.PORTRAIT.name,
                            true
                        ) ->
                            layoutManager.spanCount = resources.getInteger(R.integer.grid_portrait)

                        subPageGridFragmentArgs.layoutType.equals(
                            ItemLayoutType.SQUARE.name,
                            true
                        ) ->
                            layoutManager.spanCount =
                                resources.getInteger(R.integer.grid_game_square)
                        else ->
                            layoutManager.spanCount = resources.getInteger(R.integer.grid_landscape)

                    }

                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}

