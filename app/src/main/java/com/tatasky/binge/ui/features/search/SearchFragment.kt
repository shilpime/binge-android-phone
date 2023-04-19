package com.tatasky.binge.ui.features.search

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.emptyContentAnalyticsModel
import com.tatasky.binge.analytics.util.getSearchResultContentAnalyticsModel
import com.tatasky.binge.analytics.util.getSearchTrendingContentAnalyticsModel
import com.tatasky.binge.analytics.util.replaceRailTitleToSearchSuggestion
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.customviews.RVLinearLayoutManager
import com.tatasky.binge.customviews.ToggleRadioButton
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentSearchNewBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.CommonSeeAllClickListener
import com.tatasky.binge.interfaces.SearchRailScrollListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.coachmark.CoachMark
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.details.DetailAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.games.GameAnalytics
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.SuggestionSuggestors
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.ui.features.search.adapter.SearchLandingAdapter
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.utils.*
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.*
import java.util.*


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class SearchFragment : BaseFragment<FragmentSearchNewBinding, SearchViewModel>() , OrientationManager.OrientationListener{
    private var liveOrientation =
        MutableLiveData<SingleEvent<OrientationManager.ScreenOrientation>>()
    private var commonViewModel: CommonSampleViewModel? = null
    private lateinit var orientationManager: OrientationManager
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

    private lateinit var gestureRecycler : RecyclerView.OnItemTouchListener
    private var hasScrolledDown = false
    private var hasScrolledUp = false


    @Inject
    lateinit var coachMark: CoachMark

    @Inject
    lateinit var searchAnalytics: SearchAnalytics

    @Inject
    lateinit var gamesAnalytics : GameAnalytics

    @Inject
    lateinit var detailAnalytics : DetailAnalytics

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
    private var suggestionEntryTimer:Job? = null

    private val SEARCH_LANDING = 0
    private val SEARCH_SUGGESTIONS = 1
    private val SEARCH_RESULTS = 2

    private var landingVisible = false
    private var suggestionsVisible = false
    private var resultsVisible = false


    override fun layoutId(): Int = R.layout.fragment_search_new
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
        orientationManager = OrientationManager(activity, SensorManager.SENSOR_DELAY_FASTEST, this)
        context?.let {
            if(isTablet(it)) orientationManager.enable()
        }
        callback.isEnabled = true
        binding.searchView.etSearch.findViewById<TextView>(R.id.search_src_text).apply {
            privateImeOptions = "nm"
            hint = viewModel.setVerbiageForSearchPage()?.tvShow
        }
        binding.lifecycleOwner = viewLifecycleOwner
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
        recyclerView.layoutManager = layoutManager
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
        gestureRecycler = object : RecyclerView.OnItemTouchListener {
            private val gestureDetector =
                GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                        if (distanceY > 0 && !hasScrolledDown) {
                            hasScrolledDown = true
                            searchAnalytics.trackSearchSuggestionScrolled(
                                keyword = viewModel.searchQuery,
                                scrollDirection = DOWN,
                                keyboardState = if (binding.searchView.etSearch.hasFocus()) PARA_OPEN else PARA_CLOSED
                            )

                        } else if (distanceY < 0 && !hasScrolledUp) {
                            hasScrolledUp = true
                            //TODO : As per discussion with Chetan only scroll down events are to be captured in phase1
                            /*searchAnalytics.trackSearchSuggestionScrolled(
                                keyword = viewModel.searchQuery,
                                scrollDirection = UP,
                                keyboardState = if (binding.searchView.etSearch.hasFocus()) PARA_OPEN else PARA_CLOSED
                            )*/
                        }
                        return super.onScroll(e1, e2, distanceX, distanceY)
                    }
                })

            override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(motionEvent)
                return false
            }

            override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        }
        binding.searchSuggestionsRecycler.addOnItemTouchListener(gestureRecycler)



        searchAnalytics.trackSearchStart(getSelectedBottomTab(findNavController().currentBackStackEntry!!.destination.id))
        setRecentLayoutManager()
        (binding.searchSuggestionsRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.vm = viewModel
        val lt = LayoutTransition()
        lt.disableTransitionType(LayoutTransition.DISAPPEARING)
        binding.searchContainer.layoutTransition = lt
        rowList = mutableListOf()
        isUserLoggedIn = sharedPrefs.getLoginStatus()

        binding.searchView.ivBack.setSingleOnClick(clickIntervalMillis = 800) {
            callback.handleOnBackPressed()
        }

        binding.searchView.ivSpeakNow.setSingleOnClick {
            voiceSearchClick()
        }

        binding.searchView.ivClose.setSingleOnClick {
            binding.searchView.etSearch.setQuery("", false)
            viewModel.disposeSearch()
            resetSearchView()
        }

        binding.searchView.etSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                suggestionEntryTimer?.cancel()
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
                        context?.getString(R.string.search_min_char_text)?:""
                    )
                    false
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                isQuerySubmitted = false
                viewModel.searchQuery = (newText ?: "").trim()
                if(viewModel.searchQuery.length <= (sharedPrefs.getConfigResponse()?.data?.config?.searchSuggestionThershold?:2)){
                    searchScreensVisibilityHandler(SEARCH_LANDING)
                }
                if (viewModel.searchQuery.isNotEmpty() && !historyClicked) {
                    resetFilters()
                    suggestionEntryTimer?.cancel()
                    suggestionEntryTimer = lifecycleScope.launchWhenResumed {
                        delay(300)
                        viewModel.searchPageOffset = 0
                        if ((viewModel.searchQuery.length) >= (sharedPrefs.getConfigResponse()?.data?.config?.searchSuggestionThershold?:2)) {
                            mResetFilterFlag = false
                            viewModel.fetchAutoSuggestions(
                                true,
                                viewModel.searchQuery
                            )
                        }
                    }
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
            } else {
                binding.filters.hide()
            }
        }
        val gridLayoutManager = RVGridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_landscape))
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == (binding.searchRecyclerView.adapter as ItemGridAdapter).getListSize()) 2 else 1
            }
        }
        binding.searchRecyclerView.layoutManager = gridLayoutManager
        binding.searchView.etSearch.queryHint = getString(R.string.search_hint)
        binding.searchView.etSearch.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.searchView.searchViewContainer.strokeColor = resources.getColor(R.color.darkOnSecondary)
                micVisibilityHandler(false)
                binding.searchView.etSearch.queryHint = ""
                setSearchingView()
            }
            else{
                micVisibilityHandler(true)
                binding.searchView.etSearch.queryHint =  getString(R.string.search_hint)
                if(binding.searchView.etSearch.query.isNullOrBlank()){
                    micVisibilityHandler(true)
                } else{
                    micVisibilityHandler(false)
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
        getGenreWithTimePeriod()
        activity?.let {
            viewModel.isDeviceTablet.postValue(isTablet(it))
        }
    }

    private fun micVisibilityHandler(showMic : Boolean){
        if(showMic) {
            binding.searchView.ivSpeakNow.show()
            binding.searchView.ivClose.hide()
        } else {
            binding.searchView.ivSpeakNow.hide()
            binding.searchView.ivClose.show()
        }
    }

    private fun searchScreensVisibilityHandler(screenToShow: Int) {
        when (screenToShow) {
            SEARCH_LANDING -> {
                if (!landingVisible) {
                    viewModel.mSuggestionAdapter.clearAdapter()
                    landingVisible = true
                    suggestionsVisible = false
                    resultsVisible = false

                    binding.searchSuggestionContainer.hide()
                    binding.searchContainer.hide()
                    binding.landingContainer.show()

                    binding.executePendingBindings()
                    binding.root.invalidate()
                }
            }
            SEARCH_SUGGESTIONS -> {
                if (!suggestionsVisible) {
                    landingVisible = false
                    suggestionsVisible = true
                    resultsVisible = false

                    binding.searchSuggestionContainer.show()
                    binding.searchContainer.hide()
                    binding.landingContainer.hide()

                    binding.executePendingBindings()
                    binding.root.invalidate()
                }
            }
            SEARCH_RESULTS -> {
                if (!resultsVisible) {
                    viewModel.mSuggestionAdapter.clearAdapter()
                    landingVisible = false
                    suggestionsVisible = false
                    resultsVisible = true

                    binding.searchSuggestionContainer.hide()
                    binding.searchContainer.show()
                    binding.landingContainer.hide()

                    binding.executePendingBindings()
                    binding.root.invalidate()
                }
            }
        }
    }


    private fun setupCoachMark(searchScreenMicCoachMarkEnabled: Boolean) {
        if (!searchScreenMicCoachMarkEnabled) return
        viewModel.sharedPrefs.enableSearchScreenMicCoachMark(false)
        val title = viewModel.setVerbiageForSearchPage()?.voiceSearch ?:
        getString(R.string.coach_mark_mic_title)
        val description = viewModel.setVerbiageForSearchPage()?.tapSpeak ?:
        getString(R.string.coach_mark_mic_description)
        coachMark.apply {
            activity?.buildCoachMark(
                coachMarkName = VOICE,
                source = PARA_SEARCH,
                target = binding.searchView.ivSpeakNow,
                title = title,
                description = description,
                icon = R.drawable.voice_assistant_coach_mark,
                iconColor = R.color.white,
                increasePromptBackgroundRadius = 150
            )
        }
    }

    override fun setObserver() {
        fun readRawFile(): RecommendationResponse {
            val objectArrayString: String =
                requireContext().resources.openRawResource(R.raw.eligible_pack).bufferedReader()
                    .use { it.readText() }
            return Gson().fromJson(objectArrayString, RecommendationResponse::class.java)
        }
        activity?.let {
            if(isTablet(it)) {
                liveOrientation.observe(viewLifecycleOwner, Observer {
                    it?.getContentIfNotHandled()?.let { screenOrientation ->
                        when (screenOrientation) {
                            OrientationManager.ScreenOrientation.PORTRAIT, OrientationManager.ScreenOrientation.REVERSED_PORTRAIT -> {
                                commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.PORTRAIT)
                            }
                            OrientationManager.ScreenOrientation.LANDSCAPE, OrientationManager.ScreenOrientation.REVERSED_LANDSCAPE -> {
                                commonViewModel?.saveOrientation(OrientationManager.ScreenOrientation.LANDSCAPE)
                            }

                        }
                    }
                })
            }
        }

        fun checkCommonText(queryText: String, suggestedText: String): SpannableString {
            val spannable = SpannableString(suggestedText)
            val indexStart = suggestedText.indexOf(queryText, ignoreCase = true)
            val indexEnd = indexStart + queryText.length
            if (indexStart != -1) {

                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    indexStart,
                    indexEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return spannable
            } else {
                return SpannableString(suggestedText)
            }
        }

        viewModel.updateInOrientation.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                if (it) updateUIAdapter()
            }
        }

        viewModel.getSearchSuggestionResponse().observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let{


                it.data?.contentItem?.let { it1 ->
                    if (it1.size > 0) {
                        viewModel.mSuggestionAdapter.updateList(it1)
                        binding.searchSuggestionsRecycler.scrollToPosition(0)

                        if (hasScrolledDown && hasScrolledUp) {
                            hasScrolledUp = false
                            hasScrolledDown = false
                            binding.searchSuggestionsRecycler.removeOnItemTouchListener(gestureRecycler)
                        }
                        binding.searchSuggestionsRecycler.addOnItemTouchListener(gestureRecycler)


                        searchScreensVisibilityHandler(SEARCH_SUGGESTIONS)
                    } else {
                        searchScreensVisibilityHandler(SEARCH_LANDING)
                    }

                    searchAnalytics.trackSearchSuggestionInitiated(
                        viewModel.searchQuery,
                        if (it1.size > 0) YES else NO,
                        it1.size
                    )

                } ?: run {
                    searchScreensVisibilityHandler(SEARCH_LANDING)
                }
            }
        }

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
                micVisibilityHandler(false)
                binding.searchView.etSearch.setQuery(query, true)
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
                searchAnalytics.trackSearchHome(
                    findNavController().currentBackStackEntry?.let { it ->
                        getSelectedBottomTab(it.destination.id)
                    } ?: SOURCE_HOME)
            }
        })

        viewModel.getSearchLandingResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                e("SearchRail","inside getSearchLandingResponse trendingResponse: $trendingResponse")
                binding.progressBarBottom.startProgressAvd(false)
                trendingResponse = response
                setTrendingData(response)
            }
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

        viewModel.getSuggestionClickItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{ clickedItem ->
                clickedItem.contentItem.searchKeyword = viewModel.searchQuery
                searchAnalytics.trackSearchSuggestionClicked(
                    viewModel.searchQuery,
                    clickedItem.contentItem.suggestionPosition,
                    clickedItem.contentItem.title,
                    clickedItem.contentItem.suggestorForMixpanel,
                    if (clickedItem.contentItem.id.equals(
                            "0",
                            true
                        )
                    ) "" else clickedItem.contentItem.id
                )

                if(SuggestionSuggestors.TitleSuggestor.name.equals(clickedItem.contentItem.suggestor,true)){
                    if (binding.searchView.etSearch.getSubmitted()) {
                        viewModel.sharedPrefs.saveSearchKeyword(viewModel.searchQuery)
                    }
                    reenterTransition = null
                    exitTransition = null
                    clickedItem.contentItem.isQuerySubmitted = isQuerySubmitted
                    if(clickedItem.contentItem.contentType.equals(TYPE_GAMES,true)){
                        var gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                            pageName = (activity as? LandingActivity)?.getPageName()
                                ?: SOURCE_SEARCH,
                            railTitle = "",
                            railPosition = "",
                            railType = "",
                            railCategory = "",
                            gameGenre = clickedItem.contentItem.getSubTitle(),
                            gamePartner = clickedItem?.contentItem.provider,
                            gamePosition = clickedItem?.contentItem?.railPosition,
                            gameRating = clickedItem?.contentItem.gameRating,
                            releaseYear = "",
                            source = SOURCE_SEARCH
                        )
                        if (sharedPrefs.getLoginStatus())
                            getGamesActivityIntent(
                                context,
                                clickedItem.contentItem,
                                gamesMixpanelInfoModel
                            )?.let {intent ->
                                startActivity(intent)
                            }
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
                                    gameTitle = clickedItem.contentItem.title,
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
                    } else {
                        findNavController().navigateSafe(
                            SubFragmentDirections.actionToDetail(
                                clickedItem.contentItem,
                                true,
                                contentAnalyticsModel = clickedItem
                                    .contentAnalyticsModel
                                    .replaceRailTitleToSearchSuggestion()
                            ),
                            clickedItem.extras
                        )
                    }
                } else if (SuggestionSuggestors.GenreSuggestor.name.equals(clickedItem.contentItem.suggestor,true)) {
                    findNavController().navigateSafe(
                        SearchFragmentDirections.actionSearchLandingFragmentToLanguageGenreFragment(
                            clickedItem.contentItem.title,
                            INTENT_GENRE,
                            bgImage = clickedItem.contentItem.newBackgroundImage?:"",
                            bgBottomImage = clickedItem.contentItem.newImage,
                            contentAnalyticsModel = clickedItem
                                .contentAnalyticsModel
                                .copy(railTitle = clickedItem.contentItem.title)
                        )
                    )
                } else if (SuggestionSuggestors.LanguageSuggestor.name.equals(clickedItem.contentItem.suggestor,true)) {
                    findNavController().navigateSafe(
                        SearchFragmentDirections.actionSearchLandingFragmentToLanguageGenreFragment(
                            clickedItem.contentItem.title,
                            INTENT_LANGUAGE,
                            SOURCE_SEARCH,
                            bgImage = clickedItem.contentItem.backgroundImage?:"",  //TODO BBG REVAMP FOR SUGGESTION NEED CONFIRMATION
                            bgBottomImage = clickedItem.contentItem.image, //TODO BBG REVAMP FOR SUGGESTION NEED CONFIRMATION
                            contentAnalyticsModel = clickedItem
                                .contentAnalyticsModel
                                .copy(railTitle = clickedItem.contentItem.title)
                        )
                    )
                }
                else if (SuggestionSuggestors.ProviderSuggestor.name.equals(clickedItem.contentItem.suggestor,true)) {
                    findNavController().navigateSafe(
                        SearchFragmentDirections.actionSearchFragmentToActionSubHomeLanding(
                            clickedItem.contentItem.pageType,//pageType
                            clickedItem.contentItem.provider,
                            clickedItem.contentItem.image,
                            clickedItem.contentItem.partnerId ?: "",
                            clickedItem.contentItem.title
                        )
                    )
                }
                else {
                    binding.searchView.etSearch.setQuery(clickedItem.contentItem.title,true)
                }
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

        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { clickedItem ->
                if (binding.searchView.etSearch.getSubmitted()) {
                    viewModel.sharedPrefs.saveSearchKeyword(viewModel.searchQuery)
                }
                reenterTransition = null
                exitTransition = null
                clickedItem.contentItem.isQuerySubmitted = isQuerySubmitted
                if (clickedItem.contentItem.contentType.equals(TYPE_GAMES, true)) {
                    val gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                        pageName = (activity as? LandingActivity)?.getPageName()
                            ?: SOURCE_SEARCH,
                        railTitle = "",
                        railPosition = "",
                        railType = "",
                        railCategory = "",
                        gameGenre = clickedItem.contentItem.getSubTitle(),
                        gamePartner = clickedItem?.contentItem.provider,
                        gamePosition = clickedItem?.contentItem?.railPosition,
                        gameRating = clickedItem?.contentItem.gameRating,
                        releaseYear = "",
                        source = SOURCE_SEARCH
                    )
                    if (sharedPrefs.getLoginStatus())
                        getGamesActivityIntent(
                            context, clickedItem.contentItem,
                            gamesMixpanelInfoModel
                        )?.let { intent ->
                            startActivity(intent)
                        }
                    else {
                        gamesMixpanelInfoModel.let {
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
                                gameTitle = clickedItem.contentItem.title,
                                freeGame = YES,
                                releaseYear = it.releaseYear,
                                deviceType = sharedPrefs.getDeviceType()?.uppercase() ?: "",
                                source = it.source,
                                packPrice = FREEMIUM,
                                packName = FREEMIUM
                            )
                        }
                        viewModel.getPreviouslyUsedMobileNumbers()
                    }
                } else {
                    if (clickedItem.contentItem.provider.equals(PROVIDER_PRIME, true)) {
                        val contentAuth = isFreeContent(
                            clickedItem.contentItem.contractName,
                            sharedPrefs.getPartnerIdsList(),
                            clickedItem.contentItem.partnerId ?: "",
                            sharedPrefs.getSubscribedPack()?.subscriptionStatus
                        ) || !PREMIUM.equals(clickedItem.contentItem.partnerSubscriptionType, true)
                        detailAnalytics.trackViewContentDetail(
                            title = clickedItem.contentItem.title,
                            type = clickedItem.contentItem.contentType,
                            genre = clickedItem.contentItem.genres,
                            language = clickedItem.contentItem.language,
                            origin = clickedItem.contentItem.origin.uppercase(Locale.ROOT),
                            railName = clickedItem.contentAnalyticsModel.railTitleForAnalytics ?: "",
                            source = clickedItem.contentItem.source.takeIf { it.isNotEmpty() }
                                ?: SOURCE_DEEPLINK,
                            partnerName = clickedItem.contentItem.provider,
                            parentTitle = clickedItem.contentItem.channelName,
                            isFreeContent = clickedItem.contentItem.partnerSubscriptionType?.contains(
                                FREE,
                                true
                            ) == true,
                            pageName = (activity as? LandingActivity)?.getPageName()
                                ?: EVENT_VALUE_SOURCE_DETAIL,
                            railPosition = clickedItem.contentItem.railPosition,
                            railType = clickedItem.contentItem.railConfigType,
                            railCategory = clickedItem.sectionSource,
                            contentLanguagePrimary = clickedItem?.contentItem.language?.getOrNull(0),
                            contentGenrePrimary = clickedItem.contentItem.genres?.getOrNull(0),
                            contentAuth = if (contentAuth) YES else NO,
                            contentCategory = clickedItem.contentItem.contentType,
                            contentPosition = clickedItem.contentItem.contentPosition,
                            contentRating = clickedItem.contentItem.masterRating,
                            releaseYear = clickedItem.contentItem.releaseYear ?: "",
                            deviceType = sharedPrefs.getDeviceType() ?: "",
                            actors = clickedItem.contentItem.actor,
                            packPrice = sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
                            packName = sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                            autoPlayed = NO,
                            liveContent = if (clickedItem.contentItem.contentType.equals(
                                    TYPE_LIVE,
                                    true
                                )
                            ) YES else NO,
                            contentConfigType = clickedItem.contentItem.contentConfigType,
                            searchKeyword = clickedItem.contentItem.searchKeyword,
                            searchType = clickedItem.contentItem.suggestorForMixpanel
                        )
                    }
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionToDetail(
                            clickedItem.contentItem,
                            true,
                            contentAnalyticsModel = clickedItem.contentAnalyticsModel
                        ),
                        clickedItem.extras
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
                    val contentAnalyticsModel = getSearchResultContentAnalyticsModel()
                    setAdapter(response, contentAnalyticsModel)
                }
            }
        })

        viewModel.getFilterItemClick().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
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
                            it.bgImage, //TODO BBG REVAMP FOR SUGGESTION FOR SUGGESTION NEED CONFIRMATION
                            it.bgBottomImage, //TODO BBG REVAMP FOR SUGGESTION FOR SUGGESTION NEED CONFIRMATION
                            refId = "",
                            contentAnalyticsModel = it.contentAnalyticsModel
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
                genre = value.toString()
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
                language = value.toString()
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

        languageResponse?.let { it ->
            binding.languageFilters.filtersRadioGroup.removeAllViews()
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
                language = value.toString()
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
                genre = value.toString()
                filterLanguageGenre(false)
            }
        }

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
        if(viewModel.searchQuery.isNotEmpty())
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
                packName : String?,
                contentAnalyticsModel: ContentAnalyticsModel
            ) {

            }
        }

        if (binding.searchLandingRecycler.adapter == null) {
            val searchLandingAdapter = SearchLandingAdapter(
                viewModel,
                rowList,
                0,
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

            val gridLayoutManager = RVGridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_landscape))
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
                binding.trendingTitle.show()
                binding.trendingRecyclerView.setHasFixedSize(true)
                binding.trendingRecyclerView.setItemViewCacheSize(20)
                binding.trendingRecyclerView.adapter = searchTrendingAdapter
                binding.trendingRecyclerView.addOnScrollListener(endlessScrollTrendingListener)
            }
            binding.landingContainer.show()
        } else {
            searchResponse?.data?.filteredContentItems?.toMutableList()?.let {
                val contentAnalyticsModel = context?.getSearchTrendingContentAnalyticsModel()
                    ?: emptyContentAnalyticsModel()
                searchTrendingAdapter?.addToList(
                    it,
                    false,
                    contentAnalyticsModel
                )
            }
        }
    }

    private fun resetSearchView() {
        micVisibilityHandler(true)
        prevQuery = ""
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        searchScreensVisibilityHandler(SEARCH_LANDING)
        binding.historySuggestionContainer.hide()
        binding.tvNoData.hide()
        binding.searchView.etSearch.setQuery("", false)
        binding.searchView.etSearch.clearFocus()
        binding.searchView.etSearch.setSubmitted(false)

        viewModel.resetSearchResults(emptyContentAnalyticsModel())
        resetFilters()
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
        micVisibilityHandler(false)
        binding.tvNoData.hide()
        mResetFilterFlag = false
        val searchKeywords = sharedPrefs.getSearchKeywords()
        prevQuery = ""
        viewModel.updateRecentSearchList()
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        if(viewModel.searchQuery.length>2)
            viewModel.fetchAutoSuggestions(true,viewModel.searchQuery)

        if(searchKeywords.isNotEmpty() && searchKeywords[0] != "")
            binding.historySuggestionContainer.show()
        mBackFlag = true
    }

    private fun setNoResultView() {
        resetFilters()
        prevQuery = ""
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }
        binding.historySuggestionContainer.hide()

        searchScreensVisibilityHandler(SEARCH_LANDING)

        binding.tvNoData.show()
        binding.appbar.setExpanded(true)
        Handler(Looper.getMainLooper()).post {
            binding.trendingRecyclerView.scrollToPosition(0)
        }
        binding.searchView.etSearch.clearFocus()
        micVisibilityHandler(true)
        mBackFlag = true
    }

    private fun setSearchedView() {
        micVisibilityHandler(false)
        val transform = MaterialFadeThrough().apply {
            excludeChildren(binding.searchRecyclerView, true)
            excludeChildren(binding.trendingRecyclerView, true)
        }

        searchScreensVisibilityHandler(SEARCH_RESULTS)

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
        if(binding.genreFilters.title == "" && binding.languageFilters.title == ""){
            binding.tvShowFilter.invisible()
        }else{
            binding.tvShowFilter.show()
        }
        mBackFlag = true
    }


    private fun setAdapter(
        searchResponse: RecommendationResponse,
        contentAnalyticsModel: ContentAnalyticsModel,
    ) {
        if (viewModel.searchPageOffset == 0)
            setSearchedView()
        viewModel.updateList(searchResponse, contentAnalyticsModel)
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
                sharedPrefs.getDeviceType() ?: "",
                sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM,
            )
        }

    }

    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation?) {
        liveOrientation.postValue(SingleEvent(screenOrientation ?: OrientationManager.ScreenOrientation.PORTRAIT))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIAdapter()
//        (activity as LandingActivity).isToShowGameAnim()
    }


    private fun updateUIAdapter(){
        activity?.let {
            if(isTablet(it)){
                binding.trendingRecyclerView.adapter?.let { adapter ->
                    val gridLayoutManager = RVGridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_landscape))
                    binding.trendingRecyclerView.layoutManager = gridLayoutManager
                    adapter.notifyDataSetChanged()
                }

                binding.searchRecyclerView.adapter?.let { adapter ->
                    val gridLayoutManager = RVGridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_landscape))
                    binding.searchRecyclerView.layoutManager = gridLayoutManager
                    adapter.notifyDataSetChanged()
                }

                binding.searchLandingRecycler.adapter?.let{ adapter ->
                    val layoutManager = RVLinearLayoutManager(requireContext())
                    binding.searchLandingRecycler.layoutManager = layoutManager
                    (adapter as SearchLandingAdapter).notifyOrientationChange()
                }

            }
        }
    }
}
