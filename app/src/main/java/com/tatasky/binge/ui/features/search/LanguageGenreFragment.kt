package com.tatasky.binge.ui.features.search


import android.content.res.Configuration
import android.widget.RadioGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.transition.Hold
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.emptyContentAnalyticsModel
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentLanguageGenreBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.games.GameAnalytics
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.TabletType
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject


class LanguageGenreFragment : BaseFragment<FragmentLanguageGenreBinding, SearchViewModel>() {

    private var lastCheckedFilterId: Int? = null
    @Inject
    lateinit var searchAnalytics: SearchAnalytics
    @Inject
    lateinit var gamesAnalytics : GameAnalytics
    private var source: String = EVENT_VALUE_SOURCE_GENRE
    private var isNetworkLost: Boolean = false
    private var intentForList: String = ""
    private var genre: String = ""
    private var language: String = ""
    private var pageName : String? = null
    private var isNavigateToOther: Boolean = false

    private lateinit var intent: String
    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    val args by navArgs<LanguageGenreFragmentArgs>()


    private fun handleFreeToggle() {
        val darOnSecondaryColor = ContextCompat.getColor(requireContext(), R.color.darkOnSecondary)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        binding.tvOnSettingsItem.setTextColor(darOnSecondaryColor)
        binding.tvOffSettingsItem.setTextColor(whiteColor)

        if (args.sectionType == ItemLayoutType.GENRE_RAIL_FOR_GAMES.name) {
            context?.let { binding.searchRecyclerView.updatePadding(top = dpToPx(it, 18)) }
            binding.gpFreeToggle.hide()
        }
        else if (sharedPrefs.getConfigResponse()?.data?.config?.freeToggleEnable == true) {
            binding.gpFreeToggle.show()

            binding.switchView.setOnCheckedChangeListener { compoundButton, b ->
                viewModel.freeToggle = b
                viewModel.isFromToggle = true
                endlessScrollListener.refresh()
                viewModel.refreshLangGenreApi(true)
                if (!b) {
                    searchAnalytics.trackFilterToggleClick(source, ALL_CONTENT_STATE,
                        sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM
                        ,args.title)
                    binding.tvOnSettingsItem.setTextColor(darOnSecondaryColor)
                    binding.tvOffSettingsItem.setTextColor(whiteColor)
                } else {
                    searchAnalytics.trackFilterToggleClick(source, FREE_STATE,
                        sharedPrefs.getSubscribedPack()?.productName ?: FREEMIUM,
                        sharedPrefs.getSubscribedPack()?.amountValue ?: FREEMIUM
                        ,args.title)

                    binding.tvOffSettingsItem.setTextColor(darOnSecondaryColor)
                    binding.tvOnSettingsItem.setTextColor(whiteColor)
                }
            }
        } else {
            binding.gpFreeToggle.hide()
        }
    }

    private fun handleLangGenreUI() {
        val sectionType = args.sectionType
        val params: ConstraintLayout.LayoutParams =
            binding.horizontalGuide.layoutParams as ConstraintLayout.LayoutParams
        when {
            sectionType.equals(ItemViewType.GENRE.name, true) -> {
                context?.let { ctx ->
                    when (getTabletType(ctx)) {
                        TabletType.TABLET, TabletType.TABLET_7_INCH -> params.guidePercent = .16f
                        TabletType.TABLET_LANDSCAPE -> params.guidePercent = .28f
                        else -> params.guidePercent = .21f
                    }
                } ?: run { params.guidePercent = .21f }

                binding.apply {
                    groupGenre.show()
                    groupLanguage.hide()
                    tvGenreTitle.text = args.title
                }
            }
            else -> {
                context?.let { ctx ->
                    when (getTabletType(ctx)) {
                        TabletType.TABLET, TabletType.TABLET_7_INCH -> params.guidePercent = .12f
                        TabletType.TABLET_LANDSCAPE -> params.guidePercent = .19f
                        else -> params.guidePercent = .15f
                    }
                } ?: run { params.guidePercent = .15f }

                binding.apply{
                    groupLanguage.show()
                    groupGenre.hide()
                }
            }
        }
    }

    override fun toBeCalledOnce() {
        exitTransition = Hold()
        searchAnalytics.trackLanguageOrGenreScreenView(args.title, args.sectionType, args.pageName)
        binding.apply {
            vm = viewModel
            imgBack.setOnClickListener {
                activity?.onBackPressed()
            }
            imgSearch.setOnClickListener {
                findNavController().navigateSafe(LanguageGenreFragmentDirections.actionToSearch())
            }
            this.tvLangTitle.text = args.title
        }
        intent = INTENT_GENRE
        intentForList = args.sectionType
        viewModel.searchPageName = args.pageName
        handleLangGenreUI()
        if(args.sectionType == ItemLayoutType.GENRE_RAIL_FOR_GAMES.name){
            viewModel.contentType = PROVIDER_GAMEZOP
        }

        //Deeplink case, To show image on the UI
        if (args.bgImage.isBlank() && args.bgBottomImage.isBlank())
            if (args.sectionType == ItemLayoutType.LANGUAGE.name)
                viewModel.fetchSearchRails(
                    INTENT_LANGUAGE,
                    fetchAll = false,
                    true
                )
            else
                viewModel.fetchSearchRails(
                    INTENT_GENRE,
                    fetchAll = false,
                    true
                )

        var gridLayoutManager = RVGridLayoutManager(requireContext(),resources.getInteger(R.integer.grid_landscape))
        if(args.sectionType == ItemLayoutType.GENRE_RAIL_FOR_GAMES.name) {
            gridLayoutManager  = RVGridLayoutManager(requireContext(),resources.getInteger(R.integer.grid_game_landscape))
        }
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == (binding.searchRecyclerView.adapter as ItemGridAdapter).getListSize()) 2 else 1
            }
        }
        binding.searchRecyclerView.layoutManager = gridLayoutManager
        if(args.sectionType == ItemViewType.CHARACTER.name) {
            viewModel.searchQuery = args.title
            val constraintLayout: ConstraintLayout = binding.root
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(
                R.id.searchRecyclerView,
                ConstraintSet.TOP,
                R.id.toolbar_layout,
                ConstraintSet.BOTTOM
            )
            constraintSet.applyTo(constraintLayout)
            viewModel.fetchSearchList(true, "", viewModel.searchQuery,
                "", "", false)
        }
        else {
            if(args.sectionType == ItemLayoutType.GENRE.name || args.sectionType == ItemLayoutType.GENRE_RAIL_FOR_GAMES.name) {
                viewModel.fetchSearchRails(INTENT_LANGUAGE,
                    fetchAll = false)
                intent = INTENT_LANGUAGE
                genre = args.title
                if(args.sectionType.equals(ItemLayoutType.GENRE_RAIL_FOR_GAMES.name,true))
                    transparentImageLoad(binding.ivBgTopImage, args.bgImage)
                else {
                    val point = getDeviceDimension(context)
                    val genreBackdropImage =
                        getCloudinaryUrlByWidthOrHeight(
                            sharedPrefs.getCloudenieryUrl(),
                            args.bgImage,
                            width = point.x
                        )
                    imageLoad(binding.ivGenreBackground, genreBackdropImage)
                    val genreIconUrl =
                        getCloudinaryUrlByWidthOrHeight(
                            sharedPrefs.getCloudenieryUrl(),
                            args.bgBottomImage,
                            height = 64
                        )
                    transparentImageLoad(binding.ivGenreIcon, genreIconUrl)
                }

            }
            else{
                viewModel.fetchSearchRails(INTENT_GENRE,
                    fetchAll = false)
                transparentImageLoad(binding.ivBgTopImage, args.bgImage)
                transparentImageLoad(binding.ivBgTopBottomImage, args.bgBottomImage)
                source = EVENT_VALUE_SOURCE_LANGUAGE
                language = args.title
            }
            fetchFiltersWithLangAndGenreList()
        }
        handleFreeToggle()

    }

    private fun fetchFiltersWithLangAndGenreList() {
        if (::endlessScrollListener.isInitialized) {
            binding.searchRecyclerView.removeOnScrollListener(endlessScrollListener)
        }
        endlessScrollListener = object : EndlessRecyclerOnScrollListener(binding.searchRecyclerView.layoutManager as GridLayoutManager) {
            override fun onLoadMore(current_page: Int) {
                binding.progressBarBottom.startProgressAvd(true)
                viewModel.searchPageOffset++
                viewModel.fetchLanguageGenreList(false,
                    intentForList,
                    genre,
                    language,
                    pageName,
                    true)
            }
        }
        binding.searchRecyclerView.addOnScrollListener(endlessScrollListener)
        viewModel.onlyMessage = false
        filterLanguageGenre(true)
        binding.filters.show()
        binding.tvFilters.show()
        if(args.source == SOURCE_GAMES){
            binding.tvFilters.hide()
            binding.filters.hide()
        }
        binding.languageFilters.filtersRadioGroup.isSingleSelection = true
        binding.languageFilters.filterChipAll.setOnClickListener {
            if (!(binding.languageFilters.filterChipAll.isChecked)) {
                binding.languageFilters.filterChipAll.isChecked = true
                return@setOnClickListener
            }
            binding.languageFilters.filtersRadioGroup.clearCheck()
            val targetView = binding.languageFilters.filterChipAll
            targetView.parent?.requestChildFocus(targetView, targetView)
        }
        binding.languageFilters.filtersRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            /**
             * Getting callback when unchecking the radio btn
             * to avoid the duplicate API call with wrong value
             * return from here while unchecking the radio btn
             */
            if (lastCheckedFilterId == checkedId) return@setOnCheckedChangeListener
            else lastCheckedFilterId = checkedId
            var btn : Chip? = null
            val value = if(radioGroup.checkedChipId!=-1) {
                binding.languageFilters.filterChipAll.isChecked = false
                btn = radioGroup.findViewById<Chip?>(checkedId)
                btn?.text ?: ""
            }
            else {
                binding.languageFilters.filterChipAll.isChecked = true
                val targetView = binding.languageFilters.filterChipAll
                targetView.parent?.requestChildFocus(targetView, targetView)
                ""
            }
            val targetView = radioGroup.findViewById<Chip>(checkedId)
            targetView?.parent?.requestChildFocus(targetView, targetView)
            e("RadioCheck","child at $checkedId tag is : ${value}")
            intentForList = if(value.isBlank()) args.sectionType else INTENT_LANGUAGE_GENRE
            if(intent == INTENT_LANGUAGE)
                language = value.toString()
            else
                genre = value.toString()

            filterLanguageGenre(false)
        }
    }

    private fun filterLanguageGenre(isRetry : Boolean) {
        //Apply api for Genre and Language Filter
        //args.title plus value
        viewModel.searchPageOffset = 0
        viewModel.fetchLanguageGenreList(false,
            intentForList,
            genre,
            language,
            pageName,
            true)
    }

    private fun handleNoData() {
        binding.root.hide()
    }

    override fun getViewModelClass(): Class<SearchViewModel> = SearchViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_language_genre

    override fun onError(errorModel: ErrorModel) {
        super.onError(errorModel)
        handleNoData()
    }

    override fun onNetworkError(errorMessage: String, isRetry: Boolean) {
        super.onNetworkError(errorMessage, isRetry)
        if(!isRetry ) {
            if(viewModel.searchPageOffset == 0) {
                isNetworkLost = true
                binding.searchRecyclerView.hide()
                binding.networkView.show()
            }
            else{
                viewModel.showMoreButton()
            }
        }
    }

    override fun onNetworkAvailable() {
        if(isNetworkLost){
            viewModel.retrySubject.onNext(Any())
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


        viewModel.getBrowseByLangOrGenreResponse.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { responseItemsList ->
                for (item in responseItemsList) {
                    if (item.sectionSource.equals(args.sectionType, true)) {
                        for (contentList in item.contentItem) {
                            if (contentList.title.equals(args.title, true)) {
                                if (args.sectionType == ItemLayoutType.GENRE.name || args.sectionType == ItemLayoutType.GENRE_RAIL_FOR_GAMES.name)
                                    contentList.backgroundImage?.let { it1 ->
                                        transparentImageLoad(
                                            binding.ivGenreBackground,
                                            it1
                                        )
                                    }
                                else {
                                    //User came for Language deeplink
                                    transparentImageLoad(binding.ivBgTopBottomImage, contentList.image)
                                    contentList.backgroundImage?.let { it1 ->
                                        transparentImageLoad(
                                            binding.ivBgTopImage,
                                            it1
                                        )
                                    }
                                }
                                break
                            }
                        }
                        break
                    }
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
            }

        })

        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
            }
        })



        viewModel.getUserPreferredGenreFilter().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                setGenreFilter(response)
            }
        }


        viewModel.getUserPreferredLanguageFilter().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                setLanguageFilter(response)
            }
        }


        viewModel.getSearchResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                viewModel.onlyMessage = true
                if(response.data?.itemCount == 0) {
                    response.data?.itemCount = response.data?.contentItem?.size ?: 0
                }
                setAdapter(
                    response,
                    args.contentAnalyticsModel ?: emptyContentAnalyticsModel()
                )
            }
        })


        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { clickedItem ->
                clickedItem.contentItem.source = source
                clickedItem.gamesMixpanelInfoModel?.railTitle = PARAM_GENRE // TODO
                clickedItem.gamesMixpanelInfoModel?.pageName = (activity as? LandingActivity?)?.getPageName().toString()
                clickedItem.gamesMixpanelInfoModel?.railPosition = "1" // TODO
                clickedItem.gamesMixpanelInfoModel?.source = EVENT_VALUE_SOURCE_GENRE
                clickedItem.contentItem.origin = com.tatasky.binge.utils.EDITORIAL //editorial or recommended
                clickedItem.contentItem.refId = args.refId
                if(clickedItem.contentItem.provider.equals(PROVIDER_GAMEZOP,true)){
                    if(sharedPrefs.getLoginStatus()) {
                        getGamesActivityIntent(requireContext(), clickedItem.contentItem,clickedItem.gamesMixpanelInfoModel)?.let{ intent->
                            startActivity(
                                intent
                            )
                        }
                    } else {
                        clickedItem.gamesMixpanelInfoModel?.let{
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
                }else {
                    findNavController().navigateSafe(
                        LanguageGenreFragmentDirections.actionToDetail(
                            clickedItem.contentItem,
                            contentAnalyticsModel = clickedItem.contentAnalyticsModel
                        ),
                        clickedItem.extras
                    )
                }
            }
        })
    }

    private fun setLanguageFilter(languageResponse: List<ContentItem>) {
        if(languageResponse.isNotEmpty()) {
            binding.root.show()
            val layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 0, 8, 0)

            binding.apply {
                languageFilters.filtersRadioGroup.removeAllViews()
                filters.show()
                tvFilters.show()
            }
            if (args.source == SOURCE_GAMES) {
                binding.tvFilters.hide()
                binding.filters.hide()
            }

            languageResponse.forEach { lang ->
                val chip = layoutInflater.inflate(R.layout.layout_single_filter_chip, binding.languageFilters.filtersRadioGroup, false) as Chip
                chip.apply{
                    text = lang.title
                    tag = lang.title
                }
                binding.languageFilters.filtersRadioGroup.addView(chip)
            }
            viewModel.setLanguageFilterTitle("")

        }
    }

    private fun setGenreFilter(genreResponse: List<ContentItem>) {
        binding.root.show()
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        binding.apply {
            languageFilters.filtersRadioGroup.removeAllViews()
            filters.show()
            tvFilters.show()
        }
        if (args.source == SOURCE_GAMES) {
            binding.tvFilters.hide()
            binding.filters.hide()
        }

        genreResponse.forEach { lang ->
            val chip = layoutInflater.inflate(R.layout.layout_single_filter_chip, binding.languageFilters.filtersRadioGroup, false) as Chip
            chip.apply{
                text = lang.title
                tag = lang.title
            }
            binding.languageFilters.filtersRadioGroup.addView(chip)
        }
        viewModel.setLanguageFilterTitle(
            ""
        )
    }

    private fun setAdapter(
        searchResponse: RecommendationResponse,
        contentAnalyticsModel: ContentAnalyticsModel,
    ) {
        binding.root.show()
        binding.networkView.hide()
        isNetworkLost = false
        var isSquareLayout = false
        if (args.sectionType.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true)) {
            isSquareLayout = true
        }
        viewModel.updateLangGenreList(searchResponse, isSquareLayout, contentAnalyticsModel)
        searchResponse.data?.dthStatus =  sharedPrefs.getDthStatusFreemium()
        if(viewModel.searchPageOffset == 0) {
            if (searchResponse.data?.itemCount ?: 0 > 0 && !searchResponse.data?.filteredContentItems.isNullOrEmpty()) {
                binding.searchRecyclerView.show()
                binding.tvNoData.hide()
            } else {
                binding.searchRecyclerView.hide()
                binding.tvNoData.show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isNavigateToOther = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isNavigateToOther = true
    }

    override fun onResume() {
        super.onResume()
        if (isNavigateToOther) {
            updateUIAdapter()
            isNavigateToOther = false
        }
    }

    override fun getViewModelOwner(): ViewModelStoreOwner = this
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIAdapter()

    }
    private fun updateUIAdapter(){
        activity?.let {
            if(isTablet(it)){
                handleLangGenreUI()
                (binding.searchRecyclerView.adapter as? ItemGridAdapter)?.let { adapter->
                    var layoutManager =
                        binding.searchRecyclerView.layoutManager as GridLayoutManager
                    if(args.sectionType == ItemLayoutType.GENRE_RAIL_FOR_GAMES.name) {
                        layoutManager.spanCount = resources.getInteger(R.integer.grid_game_landscape)
                    }else{
                        layoutManager.spanCount = resources.getInteger(R.integer.grid_landscape)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}
