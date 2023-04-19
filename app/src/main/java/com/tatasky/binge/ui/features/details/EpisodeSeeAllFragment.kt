package com.tatasky.binge.ui.features.details

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.tatasky.binge.R
import com.tatasky.binge.analytics.EVENT_VALUE_SOURCE_DETAIL
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.emptyContentAnalyticsModel
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.DetailsResponse
import com.tatasky.binge.data.networking.models.response.SeriesListResponse
import com.tatasky.binge.databinding.FragmentEpisodeSeeAllBinding
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.EpisodeClickListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.details.adapter.AllEpisodeAdapter
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.model.RailItemsModel
import com.tatasky.binge.utils.*
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class EpisodeSeeAllFragment : BaseFragment<FragmentEpisodeSeeAllBinding, DetailViewModel>(){
    private var isContentSubscribed: Boolean = false
    private val args by navArgs<EpisodeSeeAllFragmentArgs>()
    private var alreadyAddedSeason: Boolean = false
    private var selectedSeriesId: String? = null
    var isLoadingSeries = false
    var seriesLastOffset = 0
    var isAddingToSearch = false
    private var clearSeriesList: Boolean = true
    private var topOffset: Int = 0
    private var mSelectedPosition: Int = 0
    lateinit var endlessRecyclerOnScrollListener:EndlessRecyclerOnScrollListener
    private var pageOffset = 1
    private var episodeSearchPageOffset = 1
    lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    private var prevQuery: String = ""
    private var compositeDisposable = CompositeDisposable()
    private var isQuerySubmitted: Boolean = false
    private var mBackFlag:Boolean = false

    private fun onSeriesFetched(seriesListResponse: SeriesListResponse) {
        isLoadingSeries = false
        var moreContentAvailable = false
        val limit = seriesListResponse.data?.limit ?: 0
        val total = seriesListResponse.data?.total ?: 0
        val offset = seriesListResponse.data?.offset ?: 0

        var isPrepand = true
        if (offset >= seriesLastOffset) {
            isPrepand = false
            seriesLastOffset = offset
            if (limit + offset < total)
                moreContentAvailable = true
        }
        e(
            "DetailsFragment", "onSeriesFetched isPrepand : $isPrepand , offset : $offset " +
                    "seriesLastOffset : $seriesLastOffset topOffset : $topOffset"
        )
        if(seriesListResponse.data != null)
            if (clearSeriesList) {
                topOffset = offset
                binding.seriesRecycler.show()

                (binding.seriesRecycler.adapter as? AllEpisodeAdapter)?.updateList(
                    seriesListResponse.data!!.contentItem,
                    moreContentAvailable,
                    args.contentAnalyticsModel ?: emptyContentAnalyticsModel()
                )
                clearSeriesList = false
            } else if (isPrepand) {
                (binding.seriesRecycler.adapter as? AllEpisodeAdapter)?.prepandToList(
                    seriesListResponse.data!!.contentItem,
                    args.contentAnalyticsModel ?: emptyContentAnalyticsModel()
                )
            } else {
                (binding.seriesRecycler.adapter as? AllEpisodeAdapter)?.addToList(
                    seriesListResponse.data!!.contentItem,
                    moreContentAvailable,
                    args.contentAnalyticsModel ?: emptyContentAnalyticsModel()
                )
            }
    }

    private val mEpisodeClickListener = object : EpisodeClickListener{
        override fun selectedEpisode(
            currentEpisode: ContentItem,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            mSeriesClickListener.onSubItemClick(
                currentEpisode,
                0,
                0,
                EventConstants.TYPE_RAIL,
                null,
                contentAnalyticsModel = contentAnalyticsModel
            )
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {

        }
    }

    private val mEpisodeInfoClickListener = object : CommonDTOClickListener {
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
            iListItem.source = EVENT_VALUE_SOURCE_DETAIL
            iListItem.contentPosition = (iItemPosition+1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            findNavController().navigateSafe(
                EpisodeSeeAllFragmentDirections.actionDetailEpisodeBotttomSheet(
                    iListItem,
                    mEpisodeClickListener,
                    contentAnalyticsModel
                )
            )
        }
    }

    override fun getViewModelClass(): Class<DetailViewModel> {
        return DetailViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_episode_see_all
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return navController().getViewModelStoreOwner(R.id.nav_details)
    }

    override fun setObserver() {
        mSelectedPosition = args.selectedSeason
        isContentSubscribed = args.isContentSubscribed
        viewModel.fetchingEpisodeSearch.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding.progressBarBottom.startProgressAvd(false)
                viewModel.setProgressing(it)
            }
        })

        viewModel.getEpisodeSearchResponse().observe(viewLifecycleOwner , Observer{
            it.getContentIfNotHandled()?.let{ response ->
                if(episodeSearchPageOffset == 1)
                    (binding.episodeSearchRecycler.adapter as AllEpisodeAdapter).clearList()
                if(response.data?.contentItem?.isNotEmpty() == true) {
                    setAdapter(response)
                    response.data?.totalSearchCount?.let { it1 ->
                        endlessScrollListener.setTotalEntries(
                            it1
                        )
                    }
                }
                else
                    binding.tvNoResult.show()
            }
        })


        viewModel.getSeriesList().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->

                binding.seriesRecycler.show()
                if (response.code == CUSTOM_RESPONSE_CODE_SERIES_ADDING) {
                    (binding.seriesRecycler.adapter as AllEpisodeAdapter).addLoading()
                    (binding.seriesRecycler.adapter as AllEpisodeAdapter).notifyItemInserted((binding.seriesRecycler.adapter as AllEpisodeAdapter).itemCount)
                } else {
                    onSeriesFetched(response)
                    binding.progress.startProgressAvd(false)
                    binding.progressBarBottom.startProgressAvd(false)
                    response.data?.let{
                        endlessRecyclerOnScrollListener.setTotalEntries(it.total)
                    }
                }
            }
        })

        viewModel.getVoiceText().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                val query = removeSpecialChar(it)
                binding.searchView.etSearch.setQuery(query,true)
                binding.searchView.ivSpeakNow.hide()
            }
        })
    }

    private fun setSearchedView() {
        binding.searchView.ivClose.show()
        binding.episodeSearchContainer.show()
        binding.tabLayoutSeasons.hide()
        binding.seriesRecycler.hide()
        mBackFlag = true

    }


    private fun setSearchingView(){
        binding.tabLayoutSeasons.hide()
        binding.seriesRecycler.hide()
        binding.episodeSearchContainer.show()
        mBackFlag = true

    }


    private fun resetEpisodeSearchView(clearQuery:Boolean = false) {
        binding.searchView.etSearch.clearFocus()
        binding.searchView.etSearch.setSubmitted(false)
        if(clearQuery) {
            binding.searchView.etSearch.setQuery("", false)
            binding.searchView.ivClose.hide()
            binding.searchView.ivSpeakNow.show()
        }
        (binding.episodeSearchRecycler.adapter as AllEpisodeAdapter).clearList()
        viewModel.episodeSearchQuery = ""
        prevQuery = ""
        binding.episodeSearchContainer.hide()
        binding.tabLayoutSeasons.show()
        binding.seriesRecycler.show()
        mBackFlag = false

    }





    private val mSeriesClickListener = object : CommonDTOClickListener {
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
            iListItem.source = EVENT_VALUE_SOURCE_DETAIL
            iListItem.contentPosition = (iItemPosition+1).toString()
            iListItem.railPosition = iSectionPosition.toString()
            findNavController().navigateSafe(
                EpisodeSeeAllFragmentDirections.actionToDetail1(
                    iListItem,
                    fromGrid = false,
                    playEpisode = true,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            )
        }
    }


    private fun setAdapter(response: SeriesListResponse) {

        binding.tvNoResult.hide()
        setSearchedView()
        response.data?.let{
            if (episodeSearchPageOffset == 1){
                (binding.episodeSearchRecycler.adapter as? AllEpisodeAdapter)?.updateList(
                    it.contentItem,
                    contentAnalyticsModel = args.contentAnalyticsModel
                        ?: emptyContentAnalyticsModel()
                )
            } else {
                (binding.episodeSearchRecycler.adapter as? AllEpisodeAdapter)?.addToList(
                    it.contentItem,
                    contentAnalyticsModel = args.contentAnalyticsModel
                        ?: emptyContentAnalyticsModel()
                )
            }
        }
    }


    private fun handleBrandSeriesRecyclerView(response: DetailsResponse) {

        binding.episodeSearchRecycler.adapter = AllEpisodeAdapter(
            mSeriesClickListener,
            mutableListOf(),
            0,
            PrimaryButtonStateEnum.STATE_PLAY,
            "", "contentItem.id",
            viewModel.getCloudinaryUrl(),
            mEpisodeInfoClickListener,
            isContentSubscribed,
        )

        if (!alreadyAddedSeason) {
            selectedSeriesId =
                args.selectedSeriesId

            if (response.data?.metaDetails?.parentContentType == TYPE_SERIES
                || response.data?.metaDetails?.contentType == TYPE_SERIES
            ) {
                alreadyAddedSeason = true

                binding.seriesRecycler.layoutManager =
                    RVGridLayoutManager(requireContext(),resources.getInteger(R.integer.grid_landscape))
                binding.seriesRecycler.adapter = AllEpisodeAdapter(
                    mSeriesClickListener,
                    mutableListOf<ContentItem>(), 0, PrimaryButtonStateEnum.STATE_PLAY,
                    "", "contentItem.id",
                    viewModel.getCloudinaryUrl(),
                    mEpisodeInfoClickListener,
                    isContentSubscribed
                )
                binding.tabLayoutSeasons.removeAllTabs()
                val tab =
                    LayoutInflater.from(context).inflate(
                        R.layout.custom_tab,
                        null
                    ) as LinearLayout
                val tabItem = tab.findViewById(R.id.tabItem) as TextView
                tabItem.text = getString(R.string.other_episodes)
                binding.tabLayoutSeasons.addTab(
                    binding.tabLayoutSeasons.newTab().setCustomView(
                        tab
                    )
                )
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.tabLayoutSeasons.getTabAt(mSelectedPosition)?.select()
                    }, 100
                )
            } else if (response.data?.metaDetails?.parentContentType == TYPE_BRAND
                || response.data?.metaDetails?.contentType == TYPE_BRAND
            ) {
                alreadyAddedSeason = true
                binding.seriesRecycler.layoutManager =
                    RVGridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_landscape))
                binding.seriesRecycler.adapter = AllEpisodeAdapter(
                    mSeriesClickListener,
                    mutableListOf(),
                    0,
                    PrimaryButtonStateEnum.STATE_PLAY,
                    "", "",
                    viewModel.getCloudinaryUrl(),
                    mEpisodeInfoClickListener,
                    isContentSubscribed,
                )
                binding.tabLayoutSeasons.removeAllTabs()
                response.data?.seriesList?.forEachIndexed { index, season ->
                    val tab =
                        LayoutInflater.from(context).inflate(
                            R.layout.custom_tab,
                            null
                        ) as LinearLayout
                    val tabItem = tab.findViewById(R.id.tabItem) as TextView
                    tabItem.text = season.seriesName
                    if (season.id == selectedSeriesId)
                        mSelectedPosition = index
                    binding.tabLayoutSeasons.addTab(
                        binding.tabLayoutSeasons.newTab().setCustomView(
                            tab
                        )
                    )
                }
//                binding.tabLayoutSeasonsParent.show()
//                binding.rlSeasons.show()
//                binding.btnSecondary.show()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        binding.tabLayoutSeasons.getTabAt(mSelectedPosition)?.select()
                    }, 100
                )
                var id1: String? = null
                if((response.data?.seriesList?.size ?: 0) > mSelectedPosition) {
                    id1 = response.data?.seriesList?.get(mSelectedPosition)?.id.toString()
                    if (id1 != null) {
                        selectedSeriesId = id1
                    }
                }
                binding.tabLayoutSeasons.addOnTabSelectedListener(object :
                    TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let {
                            pageOffset = 1
                            e(
                                "fetchSeries",
                                "inside onTabSelected seriesLastOffset :  $seriesLastOffset"
                            )
                            val id2: String? =
                                response.data?.seriesList?.get(it.position)?.id.toString()
                            if (id2 != null && id2 != id1) {
                                clearSeriesList = true
                                seriesLastOffset = 0
                                id1 = id2
                                selectedSeriesId = id2
                                mSelectedPosition = it.position
                                //(binding.seriesRecycler.adapter as SeriesAdapter).clearList()
                                viewModel.clearSeriesApiCalls()
                                fetchSeries(false, 0, viewModel.SERIES_LIMIT, false, true)
                            }
                        }
                    }
                })
            }
        }
        if (alreadyAddedSeason) {
            clearSeriesList = true
            val pageOffset = 0//viewModel.lastWatched?.episodeId ?:0
            var isLastWatch = false
            if (pageOffset > 0) {
                isLastWatch = true
            }
            val pageLimit = viewModel.SERIES_LIMIT + seriesLastOffset
            fetchSeries(
                loader = false,
                pageOffset = pageOffset, pageLimit = pageLimit,
                isLastWatch = isLastWatch, isAutoScroll = false
            )
        }
    }



    private fun fetchSeries(
        loader: Boolean, pageOffset: Int, pageLimit: Int,
        isLastWatch: Boolean, isAutoScroll: Boolean
    ) {
        selectedSeriesId?.let {
            viewModel.fetchSeriesList(
                it,
                pageOffset,
                pageLimit,
                loader,
                isLastWatch,
                isAutoScroll
            )
        }
    }

    override fun toBeCalledOnce() {
        binding.searchView.etSearch.queryHint = getString(R.string.episode_search_hint)
        endlessScrollListener = object : EndlessRecyclerOnScrollListener(binding.episodeSearchRecycler.layoutManager as RVGridLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                isAddingToSearch = true
                ++episodeSearchPageOffset
                binding.progressBarBottom.startProgressAvd(true)
                viewModel.fetchEpisodeSearchResponse(
                    args.detailResponse.data?.metaDetails,
                    episodeSearchPageOffset,
                    viewModel.episodeSearchQuery,
                    false
                )
            }

        }

        binding.episodeSearchRecycler.addOnScrollListener(endlessScrollListener)

        isContentSubscribed = args.isContentSubscribed
        handleBrandSeriesRecyclerView(args.detailResponse)

        binding.searchView.etSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.etSearch.clearFocus()
                return if (query?.trim()?.length ?: 0 > 0) {
                    isAddingToSearch = false
                    episodeSearchPageOffset = 1
                    isQuerySubmitted = true
                    if (prevQuery.trim() != viewModel.episodeSearchQuery.trim())
                        viewModel.fetchEpisodeSearchResponse(
                            args.detailResponse.data?.metaDetails,
                            episodeSearchPageOffset,
                            viewModel.episodeSearchQuery
                        )
                    prevQuery = viewModel.episodeSearchQuery
                    true
                } else {
                    showToast(
                        context,
                        context?.getString(R.string.search_min_char_text)?:""
                    )
                    false
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                compositeDisposable.clear()
                isQuerySubmitted = false
                viewModel.episodeSearchQuery = (newText ?: "").trim()
                if (viewModel.episodeSearchQuery.isNotEmpty()) {
                    setSearchingView()
                    val di = Completable.timer(1000, TimeUnit.MILLISECONDS).subscribe {
                        isAddingToSearch = false
                        episodeSearchPageOffset = 1
                        if (!isQuerySubmitted && prevQuery.trim() != viewModel.episodeSearchQuery.trim())
                            viewModel.fetchEpisodeSearchResponse(
                                args.detailResponse.data?.metaDetails,
                                episodeSearchPageOffset,
                                viewModel.episodeSearchQuery
                            )
                        prevQuery = viewModel.episodeSearchQuery
                    }
                    compositeDisposable.add(di)
                } else {
                    resetEpisodeSearchView()
                }
                return true
            }

        }
        )

        endlessRecyclerOnScrollListener = object : EndlessRecyclerOnScrollListener(binding.seriesRecycler.layoutManager as RVGridLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                binding.progressBarBottom.startProgressAvd(true)
                isLoadingSeries = true
                seriesLastOffset = pageOffset * viewModel.SERIES_LIMIT
                fetchSeries(
                    loader = true, pageOffset = seriesLastOffset, pageLimit = viewModel.SERIES_LIMIT,
                    isLastWatch = false, isAutoScroll = false
                )
                pageOffset++
            }

        }
        binding.seriesRecycler.addOnScrollListener(endlessRecyclerOnScrollListener)








        binding.searchView.ivSpeakNow.setOnClickListener {
            voiceSearchClick()
        }

        binding.searchView.etSearch.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.searchView.searchViewContainer.strokeColor = Color.parseColor("#A3A6C2")
                binding.searchView.ivSpeakNow.hide()
                binding.searchView.ivClose.show()
            }
            else{
                if(binding.searchView.etSearch.query.isNullOrBlank()){
                    binding.searchView.ivSpeakNow.show()
                    binding.searchView.ivClose.hide()
                }
                binding.searchView.searchViewContainer.strokeColor = Color.parseColor("#444764")
            }
        }


        binding.searchView.ivClose.setOnClickListener {
            binding.tvNoResult.hide()
            resetEpisodeSearchView(clearQuery = true)
        }

        binding.searchView.ivBack.setOnClickListener {
            callback.handleOnBackPressed()
        }
    }

    val callback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            if (isEnabled && mBackFlag) {
                resetEpisodeSearchView(true)
            } else {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }


    private fun voiceSearchClick() {
        checkRuntimePermission(requireActivity())
    }

    private fun checkRuntimePermission(activity: Activity) {
        //check permission at runtime'
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                navigateToVoiceDialog()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    VOICE_SEARCH_DIALOG_RESULT
                )
            }
        }
    }

    private fun navigateToVoiceDialog() {
        binding.searchView.ivSpeakNow.isClickable = false
        if (!isNetworkConnected(requireContext())) {
            showToast(context, getString(R.string.network_error_message))
        } else {
//            searchAnalytics.trackSearchMIC(
//                if (binding.searchContainer.isVisibile()) RESULT_PAGE else SEARCH_PAGE
//            )
            reenterTransition = null
            exitTransition = null
            findNavController().navigateSafe(
                EpisodeSeeAllFragmentDirections.actionEpisodeSeeAllFragmentToEpisodeVoiceSearch()
            )
        }
        binding.searchView.ivSpeakNow.isClickable = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        callback.isEnabled = true
        binding.searchView.etSearch.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).privateImeOptions =
            "nm"
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            VOICE_SEARCH_DIALOG_RESULT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    searchAnalytics.trackSearchMICPermission("Allow")
                    navigateToVoiceDialog()
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showDialog(
                        DialogModel(
                            false,
                            null,
                            getString(R.string.msg_voice_permission),
                            getString(R.string.open_setting_btn),
                            getString(R.string.btn_cancel)
                        ), object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
                                intent.data = uri
                                startActivity(intent)
                                hideDialog()
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }
                        })
                } else {
//                    searchAnalytics.trackSearchMICPermission("Deny")
                    showToast(context, getString(R.string.permission_denied))
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun removeSpecialChar(searchQuery: String): String {
        val re = Regex("[^A-Za-z0-9 ]")
        val query = re.replace(searchQuery, "")
        return if (query.length > 25)
            query.substring(0, 25) // works
        else query
    }

    companion object {
        private const val VOICE_SEARCH_DIALOG_RESULT: Int = 112
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activity?.let {
            if(isTablet(it)){
                (binding.seriesRecycler.adapter as? AllEpisodeAdapter)?.let { adapter->
                        val layoutManager =
                            binding.seriesRecycler.layoutManager as GridLayoutManager
                        layoutManager.spanCount = resources.getInteger(R.integer.grid_landscape)
                        adapter.notifyDataSetChanged()
                    }
            }
        }

    }

}
