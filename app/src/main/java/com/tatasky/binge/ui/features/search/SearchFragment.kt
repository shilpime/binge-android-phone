package com.tatasky.binge.ui.features.search

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.transition.TransitionManager
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.customviews.ToggleRadioButton
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentSearchBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.CommonSeeAllClickListener
import com.tatasky.binge.interfaces.SearchRailScrollListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.coachmark.CoachMark
import com.tatasky.binge.ui.features.coachmark.CoachMarkAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.games.GameAnalytics
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.ui.features.search.adapter.SearchLandingAdapter
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.utils.*
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class SearchFragment : BaseFragment<FragmentSearchBinding, SearchViewModel>() {
    private var lastCheckedLanguageFilterId: Int? = null
    private var lastCheckedGenreFilterId: Int? = null
    private var shouldListenForSwitchViewChange: Boolean = true
    private var searchResultSource: String = MANUAL
    private var trendingResponse: RecommendationResponse? = null
    private var isQuerySubmitted: Boolean = false
    private var prevQuery: String = ""
    private var filteredData: HashMap<String, RecommendationResponse> = HashMap()
    private val genreHrs: Int = 3
    private var language: String = ""
    private var genre: String = ""
    private var mResetFilterFlag:Boolean = false

    @Inject
    lateinit var coachMark: CoachMark

    @Inject
    lateinit var searchAnalytics: SearchAnalytics

    @Inject
    lateinit var gamesAnalytics : GameAnalytics

    var historyClicked = false
    private val VOICE_SEARCH_DIALOG_RESULT: Int = 112
    private lateinit var rowList: MutableList<HomeResponse.Items>
    private var compositeDisposable = CompositeDisposable()
    private var searchType = TEXT
    override fun getViewModelClass(): Class<SearchViewModel> = SearchViewModel::class.java

    private var searchTrendingAdapter: ItemGridAdapter? = null

    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    private lateinit var endlessScrollTrendingListener: EndlessRecyclerOnScrollListener
    private var mBackFlag:Boolean = false
    private var mGenreLanguageApiCount = 0

    override fun layoutId(): Int = R.layout.fragment_search
    //    override fun getViewModelOwner(): ViewModelStoreOwner = this
    override fun getViewModelOwner(): ViewModelStoreOwner = try {
        e("getViewModelOwner","inside Try")
        navController().getViewModelStoreOwner(R.id.search)
    } catch (e:Exception){
        e.printStackTrace()
        e("getViewModelOwner","inside catch")
        this
    }

    private fun getSelectedBottomTab(navID: Int): String {
        return when(navID) {
            // User is trying to login directly
            R.id.home -> SOURCE_HOME
            R.id.movies -> SOURCE_MOVIES
            R.id.shows -> SOURCE_TV_SHOWS
            R.id.gametab -> SOURCE_GAMES
            R.id.sports -> SOURCE_SPORTS
//            R.id.others -> SOURCE_CATEGORY
            // User came to login screen when tried to play content without login
            else -> findNavController().currentBackStackEntry?.destination?.parent?.parent?.id?.let {
                getSelectedBottomTab(it)
            }?: SOURCE_HOME
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        callback.isEnabled = true
        binding.searchView.etSearch.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).privateImeOptions =
            "nm"
        binding.lifecycleOwner = viewLifecycleOwner
        getGenreWithTimePeriod()
    }

    var isUserLoggedIn : Boolean = false
    override fun onResume() {
        super.onResume()
        setRecentLayoutManager()
        if(!binding.searchView.etSearch.hasFocus())
            viewModel.historyVisible.postValue(false)
        if (trendingResponse == null) {
            viewModel.isSearchTrendingFetched = true
            mGenreLanguageApiCount = 2
            viewModel.fetchSearchRails(INTENT_LANGUAGE,false)
            viewModel.fetchSearchRails(INTENT_GENRE,false)
            viewModel.fetchSearchRails(null,false)
        }
        else if(sharedPrefs.getLoginStatus() != isUserLoggedIn)
            refreshPage()
    }

    private fun refreshPage() {
        viewModel.getSearchAdapter().notifyDataSetChanged()
        searchTrendingAdapter?.notifyDataSetChanged()
        isUserLoggedIn = sharedPrefs.getLoginStatus()
    }

    private fun setRecentLayoutManager() {
        val recyclerView: RecyclerView = binding.historyRecyclerView as RecyclerView
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.setLayoutManager(layoutManager)
    }


    override fun onError(errorModel: ErrorModel) {
        super.onError(errorModel)
        binding.progressBarBottom.startProgressAvd(false)
    }


    private fun handleFreeToggle() {
        val darOnSecondaryColor = ContextCompat.getColor(requireContext(), R.color.darkOnSecondary)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        binding.tvOnSettingsItem.setTextColor(darOnSecondaryColor)
        binding.tvOffSettingsItem.setTextColor(whiteColor)

        if (sharedPrefs.getConfigResponse()?.data?.config?.freeToggleEnable == true) {
            binding.gpFreeToggle.show()
            binding.switchView.setOnCheckedChangeListener { compoundButton, b ->
                viewModel.freeToggle = b
                if (!b) {

                    searchAnalytics.trackFilterToggleClick(
                        PARA_SEARCH, ALL_CONTENT_STATE,
                        sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM
                        ,"")

                    binding.tvOnSettingsItem.setTextColor(darOnSecondaryColor)
                    binding.tvOffSettingsItem.setTextColor(whiteColor)
                } else {

                    searchAnalytics.trackFilterToggleClick(
                        PARA_SEARCH, FREE_STATE,
                        sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM
                        ,"")


                    binding.tvOffSettingsItem.setTextColor(darOnSecondaryColor)
                    binding.tvOnSettingsItem.setTextColor(whiteColor)
                }
                if (!shouldListenForSwitchViewChange) return@setOnCheckedChangeListener
                if (binding.genreFilters.filtersRadioGroup?.checkedRadioButtonId == -1
                    &&
                    binding.languageFilters.filtersRadioGroup?.checkedRadioButtonId == -1){
                    viewModel.refreshApi()
                } else {
                    viewModel.refreshLangGenreApi()
                }
            }
        } else {
            binding.gpFreeToggle.hide()
        }
    }


    override fun toBeCalledOnce() {
        searchAnalytics.trackSearchStart(getSelectedBottomTab(findNavController().currentBackStackEntry!!.destination.id))
        setRecentLayoutManager()
        binding.vm = viewModel
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        binding.searchContainer.layoutTransition = lt
        rowList = mutableListOf()
        isUserLoggedIn = sharedPrefs.getLoginStatus()
        binding.searchView.ivBack.setOnClickListener {
            callback.handleOnBackPressed()
        }
        binding.searchView.ivSpeakNow.setOnClickListener {
            voiceSearchClick()
        }

        binding.searchView.ivClose.setOnClickListener {
            binding.searchView.etSearch.setQuery("", false)
            viewModel.disposeSearch()
            resetSearchView()
        }

        binding.searchView.etSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.etSearch.clearFocus()
                return if ((query?.trim()?.length ?: 0) > 0) {
                    viewModel.searchPageOffset = 0
                    viewModel.sharedPrefs.saveSearchKeyword(query!!)
                    isQuerySubmitted = true
                    if (prevQuery.trim() != viewModel.searchQuery.trim()) {
                        resetFilters()
                        viewModel.fetchSearchList(true, "", viewModel.searchQuery, "", "", false)
                        mResetFilterFlag = false
                    }
                    prevQuery = viewModel.searchQuery
                    searchType = TEXT

                    searchAnalytics.trackSearch(
                        query,
                        MANUAL,
                        if (binding.searchContainer.isVisibile()) RESULT_PAGE else SEARCH_PAGE,
                        filterLanguage = language,
                        filterGenre = genre
                    )
                    true
                } else {
                    showToast(
                        context,
                        "Please enter atleast 1 character to search"
                    )
                    false
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                compositeDisposable.clear()
                isQuerySubmitted = false
                viewModel.searchQuery = (newText ?: "").trim()
                if (viewModel.searchQuery.isNotEmpty() && !historyClicked) {
                    resetFilters()
                    val di = Completable.timer(1000, TimeUnit.MILLISECONDS).subscribe {
                        viewModel.searchPageOffset = 0
                        if (!isQuerySubmitted && prevQuery.trim() != viewModel.searchQuery.trim()) {
                            mResetFilterFlag = false
                            viewModel.fetchSearchList(
                                true,
                                "",
                                viewModel.searchQuery,
                                "",
                                "",
                                false
                            )
                        }
                        prevQuery = viewModel.searchQuery
                    }
                    compositeDisposable.add(di)
                }
                historyClicked = false
                return true
            }
        })
        binding.tvShowFilter.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.filters.show()
                if(binding.genreFilters.title == ""){
                    binding.genreFilters.root.hide()
                }
                if(binding.languageFilters.title == ""){
                    binding.languageFilters.root.hide()
                }
//                binding.tvShowFilter.text = getString(R.string.hide_filter)
            } else {
                binding.filters.hide()
//                binding.tvShowFilter.text = getString(R.string.show_filter)
            }
        }
        val gridLayoutManager = RVGridLayoutManager(requireContext())
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == (binding.searchRecyclerView.adapter as ItemGridAdapter).getListSize()) 2 else 1
            }
        }
        binding.searchRecyclerView.layoutManager = gridLayoutManager
        binding.searchView.etSearch.queryHint = getString(R.string.search_hint)
        binding.searchView.etSearch.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.searchView.searchViewContainer.strokeColor = resources.getColor(R.color.darkOnSecondary)//Color.parseColor("#A3A6C2")
//                val porterDuffColorFilter = PorterDuffColorFilter(
//                    Color.WHITE,
//                    PorterDuff.Mode.SRC_ATOP
//                )
//
//                binding.searchView.ivSearch.setColorFilter(porterDuffColorFilter)
                binding.searchView.ivClose.show()
                binding.searchView.etSearch.queryHint = ""
                setSearchingView()
            }
            else{
//                val porterDuffColorFilter = PorterDuffColorFilter(
//                    resources.getColor(R.color.darkOnSecondary),
//                    PorterDuff.Mode.SRC_ATOP
//                )
//                binding.searchView.ivSearch.setColorFilter(porterDuffColorFilter)

                binding.searchView.etSearch.queryHint =  getString(R.string.search_hint)
                if(binding.searchView.etSearch.query.isNullOrBlank()){
                    binding.searchView.ivClose.hide()
                    binding.searchView.ivSpeakNow.show()
                } else{
                    binding.searchView.ivClose.show()
                }
                viewModel.historyVisible.postValue(false)
                binding.searchView.searchViewContainer.strokeColor = Color.parseColor("#444764")
            }
        }

        handleFreeToggle()
        lifecycleScope.launchWhenResumed {
            delay(500)
            setupCoachMark(viewModel.sharedPrefs.isSearchScreenMicCoachMarkEnabled())
        }
    }

    private fun setupCoachMark(searchScreenMicCoachMarkEnabled: Boolean) {
        if (!searchScreenMicCoachMarkEnabled) return
        viewModel.sharedPrefs.enableSearchScreenMicCoachMark(false)
        coachMark.apply {
            activity?.buildCoachMark(
                coachMarkName = VOICE,
                source = PARA_SEARCH,
                target = binding.searchView.ivSpeakNow,
                title = getString(R.string.coach_mark_mic_title),
                description = getString(R.string.coach_mark_mic_description),
                icon = R.drawable.voice_assistant_coach_mark,
                iconColor = R.color.white,
                increasePromptBackgroundRadius = 150
            )
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
                        SOURCE_SEARCH
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
                        SOURCE_SEARCH
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
                        searchTrendingAdapter?.updatePack()
                        searchTrendingAdapter?.notifyDataSetChanged()
                    }
                }
            })

        viewModel.getChangedCount().observe(viewLifecycleOwner, Observer {
            if (::endlessScrollListener.isInitialized) {
                endlessScrollListener.setTotalEntries(it)
            }
        })

        viewModel.fetchingSearch.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding.progressBarBottom.startProgressAvd(false)
                viewModel.setProgressing(it)
            }

        })
        viewModel.getVoiceText().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                val query = removeSpecialChar(it)
                binding.searchView.ivSpeakNow.hide()
                binding.searchView.ivClose.show()
                binding.searchView.etSearch.setQuery(query, false)
                viewModel.sharedPrefs.saveSearchKeyword(query)
                searchType = VOICE_SEARCH
                searchResultSource = VOICE_SEARCH
                searchAnalytics.trackSearchMIC(
                    if (binding.searchContainer.isVisibile()) RESULT_PAGE else SEARCH_PAGE,
                    query,
                    searchType
                )
                searchAnalytics.trackSearch(
                    query,
                    VOICE_SEARCH,
                    if (binding.searchContainer.isVisibile()) RESULT_PAGE else SEARCH_PAGE,
                    filterLanguage = language,
                    filterGenre = genre,
                )
            }
        })
        viewModel.getSearchFiltersResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { hr ->
                filteredData = hr
                viewModel.fetchSearchLandingList(true)

            }
        })

        viewModel.getUserPreferredLanguageFilter().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                mGenreLanguageApiCount --
                for (item in rowList){
                    if (ItemViewType.LANGUAGE.name.equals(item.sectionSource, true)) {
                        if (response.isNotEmpty()) {
                            item.contentItem = response as ArrayList<ContentItem>
                        }
                    }
                }
                if (mGenreLanguageApiCount < 1) {
                    viewModel.fetchSearchLandingList(true)
                }
                setLanguageFilter(response)
            }
        }

        viewModel.getUserPreferredGenreFilter().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{ response ->
                mGenreLanguageApiCount --
                for (item in rowList){
                    if(ItemViewType.GENRE.name.equals(item.sectionSource,true) ){
                        if (response.isNotEmpty())
                            item.contentItem = response as ArrayList<ContentItem>
                    }
                }
                if (mGenreLanguageApiCount < 1) {
                    viewModel.fetchSearchLandingList(true)
                }
                setGenreFilter(response)

            }
        }


        viewModel.getSearchRailResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {response ->
                e("SearchRail","inside getSearchRailResponse")
                setSearchRails(response)
            }
        })

        viewModel.getSearchLandingResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                e("SearchRail","inside getSearchLandingResponse trendingResponse: $trendingResponse")
                binding.progressBarBottom.startProgressAvd(false)
                trendingResponse = response
                setTrendingData(response)
            }
//            setTrendingData(trendingResponse)
        })

        viewModel.getClickedHistory().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { query ->
                historyClicked = true
                binding.searchView.etSearch.setQuery(query, true)
                searchType = TEXT
                searchResultSource = RECENT_SEARCH
                searchAnalytics.trackSearch(
                    query,
                    RECENT_SEARCH, "Search Page",
                    filterLanguage = language,
                    filterGenre = genre
                )
            }
        })


        viewModel.getAppClickedItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { contentItem ->
                searchAnalytics.trackSearchHomeClick(contentItem.sectionSource)
                findNavController().navigateSafe(
                    SearchFragmentDirections.actionSearchFragmentToActionSubHomeLanding(
                        contentItem.contentItem.pageType,//pageType
                        contentItem.contentItem.provider,
                        contentItem.contentItem.image,
                        contentItem.contentItem.partnerId ?: "",
                        contentItem.contentItem.title
                    )
                )
                (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
            }
        })

        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer
        {
            it.getContentIfNotHandled()?.let { contentItem ->
                if (binding.searchView.etSearch.getSubmitted()) {
                    viewModel.sharedPrefs.saveSearchKeyword(viewModel.searchQuery)
                }
                reenterTransition = null
                exitTransition = null
                contentItem.contentItem.isQuerySubmitted = isQuerySubmitted
                if(contentItem.contentItem?.contentType.equals(TYPE_GAMES,true)){
                    var gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                        pageName = (activity as? LandingActivity)?.getPageName()
                            ?: SOURCE_SEARCH,
                        railTitle = "",
                        railPosition = "",
                        railType = "",
                        railCategory = "",
                        gameGenre = contentItem.contentItem.getSubTitle(),
                        gamePartner = contentItem?.contentItem.provider,
                        gamePosition = contentItem?.contentItem?.railPosition,
                        gameRating = contentItem?.contentItem.gameRating,
                        releaseYear = "",
                        source = SOURCE_SEARCH
                    )
                    if(sharedPrefs.getLoginStatus())
                        getGamesActivityIntent(context,contentItem.contentItem,
                            gamesMixpanelInfoModel
                        )?.let{startActivity(it) }
                    else {
                        gamesMixpanelInfoModel.let{
                            gamesAnalytics.trackGameClick(
                                pageName = it.pageName,
                                railTitle = it.railTitle,
                                railPosition = it.railPosition,
                                railType = it.railType,
                                railCategory = it.railCategory,
                                gameGenre = it.gameGenre,
                                gamePartner = it.gamePartner,
                                gamePosition = it.gamePosition,
                                gameRating = it.gameRating,
                                gameTitle = contentItem.contentItem.title,
                                freeGame = YES,
                                releaseYear = it.releaseYear,
                                deviceType = PLATFORM_ANDROID_CAPS,
                                source = it.source,
                                packPrice = FREEMIUM,
                                packName = FREEMIUM
                            )
                        }
                        viewModel.getPreviouslyUsedMobileNumbers()
                    }
                } else {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionToDetail(
                            contentItem.contentItem,
                            true
                        ), contentItem.extras
                    )
                }

            }
        })
        viewModel.getSearchResponse().observe(viewLifecycleOwner, Observer {
            binding.progressBarBottom.startProgressAvd(false)
//            prevQuery = ""
            it.getContentIfNotHandled()?.let { response ->
                e("SearchFragment","viewModel.searchPageOffset:${viewModel.searchPageOffset}")
                if (viewModel.searchPageOffset == 0 &&
                    (response.data?.itemCount ?: 0 == 0 || response.data?.filteredContentItems.isNullOrEmpty())) {
                    e("SearchFragment","viewModel.searchPageOffset:${viewModel.searchPageOffset}")
                    searchAnalytics.trackSearchNoResult(
                        viewModel.getLastSearchedQuery(),
                        searchType,
                        language,
                        genre,
                        searchResultSource,
                        "0"
                    )
                    setNoResultView()
                } else {
                    response.data?.dthStatus =  sharedPrefs.getDthStatusFreemium()
                    if (viewModel.searchPageOffset == 0)
                        if (viewModel.getLastSearchedQuery().length>=3){
                            searchAnalytics.trackSearchResult(
                                viewModel.getLastSearchedQuery(),
                                response.data?.totalCount!!,
                                searchResultSource,
                                searchType,
                                language,
                                genre
                            )
                        }
                    setAdapter(response)
                }
            }
        })

        viewModel.getFilterItemClick().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                //                it.contentItem, it.ty
                val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                    duration = 250
                }
                reenterTransition = backward

                val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                    duration = 250
                }
                exitTransition = forward
                searchAnalytics.trackSearchHomeClick(it.type)


                //TODO CATEOGRIES
                if (it.type.equals(ItemViewType.CATEGORY.name, true)) {
                    findNavController().navigateSafe(
                        SearchFragmentDirections.actionSearchFragmentToActionSubHomeLanding(
                            it.categoryPageType,
                            "",
                            "",
                            "",
                            it.contentItem
                        )
                    )
                } else {
                    findNavController().navigateSafe(
                        SearchFragmentDirections.actionSearchLandingFragmentToLanguageGenreFragment(
                            it.contentItem,
                            it.type,
                            SOURCE_SEARCH,
                            it.bgImage,
                            it.bgBottomImage,
                            refId = ""
                        )
                    )
                }

            }
        })
    }

    private fun setGenreFilter(genreResponse: List<ContentItem>) {
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        if(genreResponse.isEmpty()){
            binding.genreFilters.title = ""
        }
        else{
            genreResponse.let { it ->
                binding.genreFilters.filtersRadioGroup.removeAllViews()
//            binding.groupFilters.show()
                val genreList = it
                genreList.forEach { lang ->
                    val radioButton = ToggleRadioButton(
                        ContextThemeWrapper(
                            context,
                            R.style.SearchFilterRadioButtonStyle
                        ), null, 0
                    )
                    radioButton.text = lang.title
                    radioButton.tag = lang.title
                    radioButton.layoutParams = layoutParams

                    binding.genreFilters.filtersRadioGroup.addView(radioButton)
                }
                viewModel.setGenreFilterTitle(
                    "Filter By Genre"
                )
            }
        }

        binding.genreFilters.filtersRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if(!mResetFilterFlag) {
                /**
                 * Getting callback when unchecking the radio btn
                 * to avoid the duplicate API call with wrong value
                 * return from here while unchecking the radio btn
                 */
                if (lastCheckedGenreFilterId == checkedId) return@setOnCheckedChangeListener
                else lastCheckedGenreFilterId = checkedId
                var btn: RadioButton? = null
                val value = if (radioGroup.checkedRadioButtonId != -1) {
                    btn = radioGroup.findViewById<RadioButton?>(checkedId)
                    btn?.text ?: ""
                } else
                    ""
                val targetView = radioGroup.findViewById<RadioButton>(checkedId)
                targetView?.parent?.requestChildFocus(targetView, targetView)
                e("RadioCheck", "child at $checkedId tag is : ${value}")
//            intentForList = if(value.isBlank()) args.sectionType else INTENT_LANGUAGE_GENRE
                genre = value.toString()
//                if (checkedId != -1)
                filterLanguageGenre(false)
            }
        }


    }

    private fun setLanguageFilter(languageResponse: List<ContentItem>) {
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        if(languageResponse.isEmpty()){
            binding.languageFilters.title = ""
        }
        else{
            languageResponse.let { it ->
                binding.languageFilters.filtersRadioGroup.removeAllViews()
//            binding.groupFilters.show()
                val langList = it
                langList.forEach { lang ->
                    val radioButton = ToggleRadioButton(
                        ContextThemeWrapper(
                            context,
                            R.style.SearchFilterRadioButtonStyle
                        ), null, 0
                    )
                    radioButton.text = lang.title
                    radioButton.tag = lang.title
                    radioButton.layoutParams = layoutParams

                    binding.languageFilters.filtersRadioGroup.addView(radioButton)
                }
                viewModel.setLanguageFilterTitle(
                    "Filter By Language"
                )
            }
        }


        binding.languageFilters.filtersRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if(!mResetFilterFlag) {
                /**
                 * Getting callback when unchecking the radio btn
                 * to avoid the duplicate API call with wrong value
                 * return from here while unchecking the radio btn
                 */
                if (lastCheckedLanguageFilterId == checkedId) return@setOnCheckedChangeListener
                else lastCheckedLanguageFilterId = checkedId
                var btn: RadioButton? = null
                val value = if (radioGroup.checkedRadioButtonId != -1) {
                    btn = radioGroup.findViewById<RadioButton?>(checkedId)
                    btn?.text ?: ""
                } else
                    ""
                val targetView = radioGroup.findViewById<RadioButton>(checkedId)
                targetView?.parent?.requestChildFocus(targetView, targetView)
                e("RadioCheck", "child at $checkedId tag is : ${value}")
//            intentForList = if(value.isBlank()) args.sectionType else INTENT_LANGUAGE_GENRE
                language = value.toString()
//                if (checkedId != -1)
                filterLanguageGenre(false)
            }

        }

    }

    private fun getGenreWithTimePeriod() {
        val oldApiHitDate = viewModel.sharedPrefs.getGenreAPITime()
        if (oldApiHitDate.isNullOrEmpty())
            viewModel.getPrefLangGenre(USER_PREFERRED_GENRE_TYPE)
        else {
            if (compareHrsWithOldTime(oldApiHitDate, genreHrs)) {
                viewModel.getPrefLangGenre(USER_PREFERRED_GENRE_TYPE)
            }
        }
    }

    private fun onFiltersResponseFetched(filterResponse: HashMap<String, RecommendationResponse>) {
        val languageResponse = filterResponse.get(INTENT_LANGUAGE)
        val genreResponse = filterResponse.get(INTENT_GENRE)
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

//        Log.d("TAG111", "onFiltersResponseFetched: ${languageResponse?.data?.contentItem?.get(0)!!.title}")
        languageResponse?.let { it ->
            binding.languageFilters.filtersRadioGroup.removeAllViews()
//            binding.groupFilters.show()
            val langList = it.data?.contentItem ?: ArrayList()
            langList.forEach { lang ->
                val radioButton = ToggleRadioButton(
                    ContextThemeWrapper(
                        context,
                        R.style.SearchFilterRadioButtonStyle
                    ), null, 0
                )
                radioButton.text = lang.title
                radioButton.tag = lang.title
                radioButton.layoutParams = layoutParams

                binding.languageFilters.filtersRadioGroup.addView(radioButton)
            }
            viewModel.setLanguageFilterTitle(
                "Filter By Language"
            )
        }

        genreResponse?.let { it ->
            binding.genreFilters.filtersRadioGroup.removeAllViews()
//            binding.groupFilters.show()
            val genreList = it.data?.contentItem ?: ArrayList()
            genreList.forEach { lang ->
                val radioButton = ToggleRadioButton(
                    ContextThemeWrapper(
                        context,
                        R.style.SearchFilterRadioButtonStyle
                    ), null, 0
                )
                radioButton.text = lang.title
                radioButton.tag = lang.title
                radioButton.layoutParams = layoutParams

                binding.genreFilters.filtersRadioGroup.addView(radioButton)
            }
            viewModel.setGenreFilterTitle(
                "Filter By Genre"
            )
        }

        binding.languageFilters.filtersRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if(!mResetFilterFlag) {
                var btn: RadioButton? = null
                val value = if (radioGroup.checkedRadioButtonId != -1) {
                    btn = radioGroup.findViewById<RadioButton?>(checkedId)
                    btn?.text ?: ""
                } else
                    ""
                val targetView = radioGroup.findViewById<RadioButton>(checkedId)
                targetView?.parent?.requestChildFocus(targetView, targetView)
                e("RadioCheck", "child at $checkedId tag is : ${value}")
//            intentForList = if(value.isBlank()) args.sectionType else INTENT_LANGUAGE_GENRE
                language = value.toString()
//                if (checkedId != -1)
                filterLanguageGenre(false)
            }

        }


        binding.genreFilters.filtersRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if(!mResetFilterFlag) {
                var btn: RadioButton? = null
                val value = if (radioGroup.checkedRadioButtonId != -1) {
                    btn = radioGroup.findViewById<RadioButton?>(checkedId)
                    btn?.text ?: ""
                } else
                    ""
                val targetView = radioGroup.findViewById<RadioButton>(checkedId)
                targetView?.parent?.requestChildFocus(targetView, targetView)
                e("RadioCheck", "child at $checkedId tag is : ${value}")
//            intentForList = if(value.isBlank()) args.sectionType else INTENT_LANGUAGE_GENRE
                genre = value.toString()
//                if (checkedId != -1)
                filterLanguageGenre(false)
            }
        }





//        context?.let { context1 ->
//            languageResponse?.let { it ->
//                //binding.tvShowFilter.show()
//                val langList = it.data?.contentItem ?: ArrayList()
////                if (langList.isNotEmpty())
////                    rowList.add(
////                        LanguageModel(
////                            it.data?.title ?: context1.getString(R.string.browse_language),
////                            langList
////                        )
////                    )
//            }
//            genreResponse?.let { it ->
//                //binding.tvShowFilter.show()
//                val genreList = it.data?.contentItem ?: ArrayList()
////                if (genreList.isNotEmpty())
////                    rowList.add(
////                        GenreModel(
////                            it.data?.title ?: context1.getString(R.string.browse_genre), genreList
////                        )
////                    )
//            }
//        }
    }
    private fun filterLanguageGenre(isRetry : Boolean) {
        //Apply api for Genre and Language Filter
        //args.title plus value
        viewModel.searchPageOffset = 0
        viewModel.fetchLanguageGenreList(true,
            "LANGUAGE",
            genre,
            language,
            "",
            isRetry)
    }

    private fun setSearchRails(response: HomeResponse){
        rowList = response.data?.items!!
    }

    private fun setTrendingData(searchResponse: RecommendationResponse?) {
        resetSearchView()

        val maxCount = searchResponse?.data?.totalCount ?: 0
        val itemCount = searchResponse?.data?.itemCount ?: 0
        val totalItems = searchTrendingAdapter?.itemCount ?: 0 + (itemCount)
        searchResponse?.data?.continuePagination = false//itemCount > 0 && totalItems < maxCount
        searchResponse?.data?.dthStatus =  sharedPrefs.getDthStatusFreemium()

        val mSeeAllClickListener = object : CommonSeeAllClickListener{
            override fun onSeeAllClick(
                railIdName: Pair<Int, String>,
                sectionType: String,
                railPosition: Int?,
                placeHolder: String,
                configType: String?,
                provider: String?,
                isMixedRail: Boolean,
                isPrepand: Boolean,
                item: HomeResponse.Items?,
                backgroundImage: String?,
                layoutType: String?,
                refId : String,
                packName : String?
            ) {

            }
        }

        if (binding.searchLandingRecycler.adapter == null) {
//            onFiltersResponseFetched(filteredData)
            val searchLandingAdapter = SearchLandingAdapter(
                viewModel,
                rowList, 0,
                viewModel.sharedPrefs.getCloudenieryUrl(), requireContext(),
                false,
                null,
                mSeeAllClickListener,
                viewModel.mFilterClick,
                sharedPrefs.getProviderLogo(),
                mSearchRailScrollListener
            )
            searchLandingAdapter?.setContinuePaging(
                searchResponse?.data?.continuePagination ?: false
            )
            if (::endlessScrollTrendingListener.isInitialized) {
                binding.trendingRecyclerView.removeOnScrollListener(endlessScrollTrendingListener)
            }

            val gridLayoutManager = RVGridLayoutManager(requireContext())
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == searchTrendingAdapter?.getListSize()) 2 else 1
                }
            }
            binding.trendingRecyclerView.layoutManager = gridLayoutManager

            endlessScrollTrendingListener = object : EndlessRecyclerOnScrollListener(gridLayoutManager) {
                override fun onLoadMore(current_page: Int) {
                    viewModel.trendingOffset++
                    e("Trending Offset", viewModel.trendingOffset.toString())
                    binding.progressBarBottom.startProgressAvd(true)
                    viewModel.fetchSearchLandingList(false)
                }
            }
            endlessScrollTrendingListener.setTotalEntries(maxCount)
            binding.searchLandingRecycler.adapter = searchLandingAdapter
            binding.searchLandingRecycler.descendantFocusability =
                ViewGroup.FOCUS_BEFORE_DESCENDANTS
            if(searchResponse?.data?.filteredContentItems != null) {
                searchTrendingAdapter = ItemGridAdapter(
                    listener = viewModel.mTrendingBannerClick,
                    mList = searchResponse.data?.filteredContentItems!!.toMutableList(),
                    cloudinaryUrl = viewModel.sharedPrefs.getCloudenieryUrl(),
                    loadMoreClickListener = null,
                    providerLogos = viewModel.sharedPrefs.getProviderLogo(),
                    sharedPrefs = sharedPrefs,
                    origin = ""
                )
                searchTrendingAdapter?.autoUpdating = true
                binding.railTitle.show()
                binding.trendingRecyclerView.setHasFixedSize(true)
                binding.trendingRecyclerView.setItemViewCacheSize(20)
                binding.trendingRecyclerView.adapter = searchTrendingAdapter
                binding.trendingRecyclerView.addOnScrollListener(endlessScrollTrendingListener)
            }
            binding.scrollingContent.show()
        } else {
            searchResponse?.data?.filteredContentItems?.toMutableList()?.let {
                searchTrendingAdapter?.addToList(
                    it,
                    false
                )
            }
        }
    }

    private fun resetSearchView() {
        binding.searchView.ivClose.hide()
        binding.searchView.ivSpeakNow.show()
        prevQuery = ""
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        TransitionManager.beginDelayedTransition(binding.searchAllContainer as ViewGroup, transform)
        binding.historySuggestionContainer.hide()
        binding.searchContainer.hide()
        binding.landingContainer.show()
        binding.tvNoData.hide()
        binding.searchView.etSearch.setQuery("", false)
        binding.searchView.etSearch.clearFocus()
        binding.searchView.etSearch.setSubmitted(false)

        viewModel.resetSearchResults()
        resetFilters()
//        binding.searchView.clSearchview.setState(CustomSearchConstraintLayout.STATE_IDLE)
        mBackFlag = false
    }

    private fun resetFilters() {
        mResetFilterFlag = true
        language = ""
        genre = ""
        binding.languageFilters.filtersRadioGroup.clearCheck()
        binding.genreFilters.filtersRadioGroup.clearCheck()
        binding.languageFilters.scrollView.scrollTo(0,0)
        binding.genreFilters.scrollView.scrollTo(0,0)
        binding.tvShowFilter.isChecked = false
        resetFreeToggle()
    }

    private fun resetFreeToggle() {
        /**
         * Stopping listener callback from listening to the change
         * as callback will again hit the API on switch change
         */
        shouldListenForSwitchViewChange = false
        binding.switchView.isChecked = false
        lifecycleScope.launch {
            delay(500)
            shouldListenForSwitchViewChange = true
        }
    }

    private fun setSearchingView() {
        binding.searchView.ivSpeakNow.hide()
        binding.tvNoData.hide()
        mResetFilterFlag = false
        val searchKeywords = sharedPrefs.getSearchKeywords()
        prevQuery = ""
        viewModel.updateRecentSearchList()
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        TransitionManager.beginDelayedTransition(binding.searchAllContainer as ViewGroup, transform)
//        binding.landingContainer.hide()
        binding.searchContainer.hide()
        if(searchKeywords.isNotEmpty() && searchKeywords[0] != "")
            binding.historySuggestionContainer.show()

//        binding.searchView.clSearchview.setState(CustomSearchConstraintLayout.STATE_SEARCHING)
        mBackFlag = true
    }

    private fun setNoResultView() {
        resetFilters()
        prevQuery = ""
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        TransitionManager.beginDelayedTransition(binding.searchAllContainer as ViewGroup, transform)
        binding.landingContainer.show()
        binding.searchContainer.hide()
        binding.historySuggestionContainer.hide()
        binding.tvNoData.show()
        binding.appbar.setExpanded(true)
        Handler(Looper.getMainLooper()).post {
            binding.trendingRecyclerView.scrollToPosition(0)
        }
        binding.searchView.etSearch.clearFocus()
        binding.searchView.ivClose.show()
//        binding.searchView.clSearchview.setState(CustomSearchConstraintLayout.STATE_NO_RESULT)
        mBackFlag = true
    }

    private fun setSearchedView() {
        binding.searchView.ivClose.show()
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        TransitionManager.beginDelayedTransition(binding.searchAllContainer as ViewGroup, transform)
        binding.historySuggestionContainer.hide()
        binding.landingContainer.hide()
        binding.searchContainer.show()
        binding.searchRecyclerView.scrollToPosition(0)
        if (::endlessScrollListener.isInitialized) {
            binding.searchRecyclerView.removeOnScrollListener(endlessScrollListener)
        }
        endlessScrollListener = object : EndlessRecyclerOnScrollListener(binding.searchRecyclerView.layoutManager as GridLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                binding.progressBarBottom.startProgressAvd(true)
                viewModel.searchPageOffset++
                if(TextUtils.isEmpty(genre) && TextUtils.isEmpty(language))
                    viewModel.fetchSearchList(false, "", viewModel.searchQuery, "", "", false)
                else
                    viewModel.fetchLanguageGenreList(true,
                        "LANGUAGE",
                        genre,
                        language,
                        "",
                        false)
                searchAnalytics.trackSearchResultSwipe(viewModel.searchQuery)
            }
        }
        binding.searchRecyclerView.addOnScrollListener(endlessScrollListener)
        binding.tvNoData.hide()
//        binding.searchView.etSearch.clearFocus()
//        binding.searchView.clSearchview.setState(CustomSearchConstraintLayout.STATE_SEARCHED)
        if(binding.genreFilters.title == "" && binding.languageFilters.title == ""){
            binding.tvShowFilter.invisible()
        }else{
            binding.tvShowFilter.show()
        }
        mBackFlag = true
    }


    private fun setAdapter(searchResponse: RecommendationResponse) {
        if (viewModel.searchPageOffset == 0)
            setSearchedView()
        viewModel.updateList(searchResponse)
    }

    val callback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            if (isEnabled && mBackFlag) {
                resetSearchView()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            VOICE_SEARCH_DIALOG_RESULT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchAnalytics.trackSearchMICPermission("Allow")
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
                    searchAnalytics.trackSearchMICPermission("Deny")
                    showToast(context, getString(R.string.permission_denied))
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun voiceSearchClick() {
        mResetFilterFlag = false
        checkRuntimePermission(requireActivity())
    }

    private fun navigateToVoiceDialog() {
        binding.searchView.ivSpeakNow.isClickable = false
        if (!isNetworkConnected(requireContext())) {
            showToast(context, getString(R.string.network_error_message))
        } else {
            reenterTransition = null
            exitTransition = null
            findNavController().navigateSafe(
                SearchFragmentDirections.actionSearchLandingFragmentToVoiceFragment()
            )
        }
        binding.searchView.ivSpeakNow.isClickable = true
    }

    private fun checkRuntimePermission(activity: Activity) {
        //check permission at runtime'
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
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

    private fun removeSpecialChar(searchQuery: String): String {
        val re = Regex("[^A-Za-z0-9 ]")
        val query = re.replace(searchQuery, "")
        return if (query.length > 25)
            query.substring(0, 25) // works
        else query
    }

    private val mSearchRailScrollListener = object : SearchRailScrollListener {
        override fun onSearchRailScrolled(
            railName: String,
            position: Int,
            railType: String,
            railCategory: String,
            provider: String
        ) {
            searchAnalytics.trackSearchRailWatched(
                railName,
                position.toString(),
                pageName = SOURCE_SEARCH,
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

}