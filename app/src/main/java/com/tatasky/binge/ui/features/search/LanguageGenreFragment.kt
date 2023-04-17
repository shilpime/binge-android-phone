package com.tatasky.binge.ui.features.search


import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.customviews.ToggleRadioButton
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentLanguageGenreBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.MiscAnalytics
import com.tatasky.binge.ui.features.games.GameAnalytics
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
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
    private var state: String = FREE_STATE
    private var pageName : String? = null

    private lateinit var intent: String
    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    val args by navArgs<LanguageGenreFragmentArgs>()

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
                findNavController().navigateSafe(LanguageGenreFragmentDirections.actionToSearch())
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }


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

    override fun toBeCalledOnce() {
        exitTransition = Hold()
        searchAnalytics.trackLanguageOrGenreScreenView(args.title, args.sectionType, args.pageName)
        binding.vm = viewModel
        intent = INTENT_GENRE
        intentForList = args.sectionType
        viewModel.searchPageName = args.pageName

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

        val gridLayoutManager = RVGridLayoutManager(requireContext())
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == (binding.searchRecyclerView.adapter as ItemGridAdapter).getListSize()) 2 else 1
            }
        }
        binding.searchRecyclerView.layoutManager = gridLayoutManager
        if(args.sectionType == ItemViewType.CHARACTER.name) {
            viewModel.searchQuery = args.title
            binding.groupFilters.hide()
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
                transparentImageLoad(binding.ivBgTopImage, args.bgImage)

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
//        viewModel.fetchFiltersList(intent, false)
        filterLanguageGenre(true)
        binding.groupFilters.show()
        binding.filters.show()
        binding.tvFilters.show()
        if(args.source == SOURCE_GAMES){
            binding.tvFilters.hide()
            binding.filters.hide()
        }
        binding.languageFilters.filtersRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            /**
             * Getting callback when unchecking the radio btn
             * to avoid the duplicate API call with wrong value
             * return from here while unchecking the radio btn
             */
            if (lastCheckedFilterId == checkedId) return@setOnCheckedChangeListener
            else lastCheckedFilterId = checkedId
            var btn : RadioButton? = null
            val value = if(radioGroup.checkedRadioButtonId!=-1) {
                btn = radioGroup.findViewById<RadioButton?>(checkedId)
                btn?.text ?: ""
            }
            else
                ""
            val targetView = radioGroup.findViewById<RadioButton>(checkedId)
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
//                binding.tvNoData.show()
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
                                            binding.ivBgTopImage,
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
//        viewModel.getSearchFiltersResponse().observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let { hr ->
//                //viewModel.fetchSearchLandingList(true)
//                onFiltersResponseFetched(hr)
//            }
//        })


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
                setAdapter(response)
            }
        })


        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer
        {
            it.getContentIfNotHandled()?.let { contentItem ->
                contentItem.contentItem.source = source
//                contentItem.contentItem.railName = if(intent != INTENT_GENRE) EVENT_VALUE_SOURCE_GENRE.toUpperCase() else EVENT_VALUE_SOURCE_LANGUAGE.toUpperCase()
                contentItem.gamesMixpanelInfoModel?.railTitle = PARAM_GENRE // TODO
                contentItem.gamesMixpanelInfoModel?.pageName = (activity as? LandingActivity?)?.getPageName().toString()
                contentItem.gamesMixpanelInfoModel?.railPosition = "1" // TODO
                contentItem.gamesMixpanelInfoModel?.source = EVENT_VALUE_SOURCE_GENRE
                contentItem.contentItem.origin = com.tatasky.binge.utils.EDITORIAL //editorial or recommended
                contentItem.contentItem.refId = args.refId
                if(contentItem.contentItem.provider.equals(PROVIDER_GAMEZOP,true)){
                    if(sharedPrefs.getLoginStatus()) {
                        getGamesActivityIntent(requireContext(), contentItem.contentItem,contentItem.gamesMixpanelInfoModel)?.let{ intent->
                            startActivity(
                                intent
                            )
                        }
                    } else {
                        contentItem.gamesMixpanelInfoModel?.let{
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
                }else {
                    findNavController().navigateSafe(
                        LanguageGenreFragmentDirections.actionToDetail(
                            contentItem.contentItem
                        ), contentItem.extras
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
            languageResponse?.let { it ->
                binding.languageFilters.filtersRadioGroup.removeAllViews()
                binding.groupFilters.show()
                binding.filters.show()
                binding.tvFilters.show()
                if(args.source == SOURCE_GAMES){
                    binding.tvFilters.hide()
                    binding.filters.hide()
                }
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
                    ""
                )
            }
        }
    }

    private fun setGenreFilter(genreResponse: List<ContentItem>) {
        binding.root.show()
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        genreResponse?.let { it ->
            binding.languageFilters.filtersRadioGroup.removeAllViews()
            binding.groupFilters.show()
            binding.filters.show()
            binding.tvFilters.show()
            if(args.source == SOURCE_GAMES){
                binding.tvFilters.hide()
                binding.filters.hide()
            }
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

                binding.languageFilters.filtersRadioGroup.addView(radioButton)
            }
            viewModel.setLanguageFilterTitle(
                ""
            )
        }
    }

    private fun setAdapter(searchResponse: RecommendationResponse) {
        binding.root.show()
        binding.networkView.hide()
        isNetworkLost = false
        var isSquareLayout = false
        if (args.sectionType.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true)) {
            isSquareLayout = true
        }
        viewModel.updateLangGenreList(searchResponse,isSquareLayout)
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

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    private fun onFiltersResponseFetched(filterResponse: HashMap<String, RecommendationResponse>) {
        binding.root.show()
        val languageResponse = filterResponse.get(intent)
        val layoutParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.WRAP_CONTENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        languageResponse?.let { it ->
            binding.languageFilters.filtersRadioGroup.removeAllViews()
            binding.groupFilters.show()
            binding.filters.show()
            binding.tvFilters.show()
            if(args.source == SOURCE_GAMES){
                binding.tvFilters.hide()
                binding.filters.hide()
            }
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
                ""
            )
        }

    }

}
