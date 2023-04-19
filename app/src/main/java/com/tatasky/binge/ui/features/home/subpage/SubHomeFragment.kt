package com.tatasky.binge.ui.features.home.subpage

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentSubpageBinding
import com.tatasky.binge.interfaces.AddPackListener
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.SingleEventParcelizeWrapper
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.setSingleOnClick
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.adapter.HomeAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.ui.features.home.sub.SubViewModel
import com.tatasky.binge.utils.*

class SubHomeFragment : CancellationBaseFragment<FragmentSubpageBinding, SubViewModel>() {

    private var partnerIds: Set<String>? = null
    private var providerId: String = ""
    private var isSubscribed: Boolean = false
    private var isFirstTime: Boolean = true
    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    private val REQUEST_FOR_PACK_SELECTION: Int = 1012
    val args: SubHomeFragmentArgs by navArgs<SubHomeFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
//        startPostponedEnterTransition()
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

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            (activity as? AppCompatActivity)?.let { findNavController().navigateUpOrOpenHome(it) }
        }
    }

    private val onAddPackClickListener = object : AddPackListener {
        override fun onAddPack(providerId: String, providerName: String) {
            if (!shouldStartCancellationTrigger(true)) {
                val subscribedEntitlements = viewModel.sharedPrefs.getPartnerIdsList()
                val subscribedPack = sharedPrefs.getSubscribedPack()

                if (!subscribedEntitlements.isNullOrEmpty()
                    && subscribedEntitlements.contains(providerId)
                ) {
                    activity?.let {
                        startActivityForResult(
                            getSubscriptionActivityIntent(
                                it,
                                false,
                                null,
                                SOURCE_PARTNER_HOME,
                                initiateRecharge = true,
                                fromDialog = true
                            ), REQUEST_FOR_PACK_SELECTION
                        )
                    }
                } else if (!subscribedEntitlements.isNullOrEmpty()
                    && !subscribedEntitlements.contains(providerId)
                    && subscribedPack?.isCancelled == true && !subscribedPack.subscriptionDetailInfo?.bingeAccountStatus.equals(
                        SubscriptionPackStatusEnum.WRITTEN_OFF.status,
                        true
                    )
                ) {
                    showDialog(
                        DialogModel(
                            false,
                            R.drawable.ic_subscribe,
                            subscribedPack.verbiage?.contentSubs?.title
                                ?: getString(R.string.upgrade_subscription),
                            getString(R.string.upgrade),
                            getString(R.string.cancel),
                            subscribedPack.verbiage?.contentSubs?.message
                        ), object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                activity?.let {
                                    startActivityForResult(
                                        getSubscriptionActivityIntent(
                                            it,
                                            fromLogin = false,
                                            selectedAppId = providerId,
                                            fromScreen = SOURCE_PARTNER_HOME
                                        ), REQUEST_FOR_PACK_SELECTION
                                    )
                                }
                                hideDialog()
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                            }

                        }
                    )
                } else {
                    activity?.let {
                        startActivityForResult(
                            getSubscriptionActivityIntent(
                                it,
                                fromLogin = false,
                                selectedAppId = providerId,
                                fromScreen = SOURCE_PARTNER_HOME
                            ), REQUEST_FOR_PACK_SELECTION
                        )
                    }
                }
            } else {
                viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
            }
        }
    }

    override fun toBeCalledOnce() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner, // LifecycleOwner
            onBackPressedCallback
        )
        binding.vm = viewModel
        viewModel.onlyMessage = false
        partnerIds = viewModel.sharedPrefs.getPartnerIdsList()
        initViews()
    }

    private fun handleNoData() {
        binding.swipeRefresh.isRefreshing = false
        binding.homeRecyclerView.hide()
        binding.tvNoData.visibility = View.VISIBLE
    }

    private fun initViews() {
        binding.rlToolbar.show()
        binding.tvCategories.invisible()
        if(args.provider.isEmpty() || args.provider.isBlank()){
            binding.ivLogo.hide()
            binding.tvCategoryTitle.text = args.categoryTitle
            binding.tvCategoryTitle.show()
            viewModel.setPageNameDrp(args.categoryTitle?:"")
        } else {
            viewModel.setPageNameDrp(args.provider)
        }
        val source =
            if ((findNavController().previousBackStackEntry?.destination?.arguments?.keys?.size ?: 0) < 1) {
                SOURCE_SEARCH
            } else {
                ((activity as? LandingActivity)?.getPageName()) ?: SOURCE_HOME
            }

        viewModel.miscAnalytics.trackMixPanelHomePageView(
            name = viewModel.getPageNameDrp(),
            source = source,
            drpEnabled = if (viewModel.checkDRPpages()) YES else NO
        )
        providerId = args.providerId
        isSubscribed = sharedPrefs.isActivePack() && sharedPrefs.getPartnerIdsList()
            ?.contains(providerId) == true
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.searchIcon.setSingleOnClick(1000) {
            findNavController().navigateSafe(
                SubFragmentDirections.actionGlobalSearch()
            )
        }
        viewModel.searchPageName = args.provider
        viewModel.setPageName(SOURCE_PARTNER_HOME)
        updateProviderImage(
            binding.ivLogo,
            args.provider,
            sharedPrefs.getProviderLogo(),
            R.drawable.ic_banner_placeholder
        )
        viewModel.setPageType(args.pageType)
        viewModel.provider = args.provider
        if (args.provider.isNotEmpty()) {
            isSubscribed = sharedPrefs.isActivePack() &&
                    sharedPrefs.getPartnerIdsList()?.contains(providerId) == true
            viewModel.subscribed = isSubscribed
            viewModel.unsubscribed = !isSubscribed
        } else {
            viewModel.subscribed = checkSubscription(sharedPrefs.getPartnerIdsList())
            viewModel.unsubscribed = !viewModel.subscribed
        }
        viewModel.fetchHierarchyData(true, context?.let { it1 -> isTablet(it1) })
        binding.swipeRefresh.setColorSchemeColors(Color.BLUE, Color.MAGENTA, Color.RED)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.pageOffset = 0
            viewModel.fetchHierarchyData(false, context?.let { it1 -> isTablet(it1) })
        }
        val animator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(@NonNull viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.homeRecyclerView.itemAnimator = animator
        val linearLayoutManager = binding.homeRecyclerView.layoutManager as LinearLayoutManager
        binding.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (viewModel.getAdapter() as HomeAdapter).handleHomeTrailerScrolling(
                    linearLayoutManager.findFirstVisibleItemPosition(),
                    linearLayoutManager.findLastVisibleItemPosition(),
                    false
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isContinueWatching)
            viewModel.refreshContinueWatching()
        if(viewModel.isGameFav)
                viewModel.refreshGameFav()
        if(viewModel.isGameRP)
                viewModel.refreshGameFav()
        (viewModel.getAdapter() as HomeAdapter).handleHomeTrailerPlayBack(false)
    }

    private fun onHomeResponseFetched(homeResponse: HomeResponse) {
        isFirstTime = false
        if (homeResponse.data == null) {
            homeResponse.data = HomeResponse.Data()
            homeResponse.data!!.items = mutableListOf()
        }
        if (homeResponse.data?.total == 0 && homeResponse.data?.offset ?: 0 == 0) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.homeRecyclerView.hide()
        } else {

            if (homeResponse.data?.offset ?: 0 == 0) {
                if (::endlessScrollListener.isInitialized) {
                    binding.homeRecyclerView.removeOnScrollListener(endlessScrollListener)
                }
                endlessScrollListener = object :
                    EndlessRecyclerOnScrollListener(binding.homeRecyclerView.layoutManager as LinearLayoutManager) {
                    override fun onLoadMore(current_page: Int) {
                        if(!viewModel.isPullToRefresh) {
                            viewModel.pageOffset += viewModel.PAGELIMIT
                            viewModel.fetchHomeData(false)
                        }
                    }
                }
                binding.homeRecyclerView.scrollToPosition(0)
                binding.homeRecyclerView.addOnScrollListener(endlessScrollListener)
            }
            binding.homeRecyclerView.show()
            binding.tvNoData.visibility = View.GONE
        }
        viewModel.updateList(homeResponse)

    }

    override fun getViewModelClass(): Class<SubViewModel> = SubViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subpage

    override fun onError(errorModel: ErrorModel) {
        super.onError(errorModel)
        handleNoData()
    }

    override fun setObserver() {
        super.setObserver()

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(KEY_REFRESH_GAME_FAV)
            ?.observe(viewLifecycleOwner, Observer {
                if(it)
                    viewModel.refreshGameFav()
            })

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(KEY_REFRESH_GAME_CW)
            ?.observe(viewLifecycleOwner, Observer {
                if(it)
                    viewModel.refreshGameCW()
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
                        //User's pack details has changed...Refresh the data fro Crown visibility
                        d(this.javaClass.simpleName, "UPDATE_IN_PACK $isPackUpdated")
                        //refresh Page
                        doPullToRefresh()
                    }
                }
            })
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    initViews()
                }
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
            }
        })
        viewModel.getHomeData().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { hr ->
                if (binding.swipeRefresh.isRefreshing)
                    binding.swipeRefresh.isRefreshing = false

                viewModel.onlyMessage = true
                onHomeResponseFetched(hr)
            }
        })
        viewModel.getChangedCount().observe(viewLifecycleOwner, Observer {
            if (::endlessScrollListener.isInitialized) {
                it.getContentIfNotHandled()?.let { changedCount ->
                    endlessScrollListener.setTotalEntries(changedCount)
                }
            }
        })

        viewModel.getAfterAllUpdate().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { changedCount ->
                if (isSubscribed && changedCount == 0) {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.homeRecyclerView.hide()
                } else {
                    binding.homeRecyclerView.show()
                    binding.tvNoData.visibility = View.GONE
                }
            }
        })

        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { clickedItem ->
                if (clickedItem.sectionSource == EventConstants.TYPE_SELECT_PAID_PACK
                    || clickedItem.sectionSource == EventConstants.TYPE_START_FREE_TRIAL
                    || clickedItem.sectionSource == EventConstants.TYPE_FREE_TRIAL_UPGRADE
                ) {
                    activity?.let { activity ->
                        startActivityForResult(
                            getSubscriptionActivityIntent(
                                activity,
                                false,
                                providerId,
                                SOURCE_PARTNER_HOME,
                                initiateRecharge = false,
                                fromDialog = true
                            ), REQUEST_FOR_PACK_SELECTION
                        )
                    }
                } else if (clickedItem.sectionSource == EventConstants.TYPE_SELECT_LANGUAGE_POP_UP) {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionSelectLanguageBottomSheetDialog()
                    )
                }
                else if (clickedItem.sectionSource.equals(
                        ItemViewType.LANGUAGE.name,
                        true
                    ) ||
                    clickedItem.sectionSource.equals(ItemViewType.GENRE.name, true)
                ) {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionHomeFragmentToLanguageGenreFragment(
                            clickedItem.contentItem.title,
                            clickedItem.sectionSource,
                            viewModel.searchPageName ?: "",
                            clickedItem.contentItem.getLangGenreBackDrop(clickedItem.sectionSource)?:"",
                            clickedItem.contentItem.getLangGenreIcon(clickedItem.sectionSource),
                            refId = clickedItem.contentItem.refId,
                            contentAnalyticsModel = clickedItem.contentAnalyticsModel
                        )
                    )
                    (activity as? LandingActivity)?.parentalControlSnackbarUtil?.hideParentalControlSnackbar()
                }
                else {
                    findNavController().navigateSafe(
                        SubFragmentDirections.actionToDetail(
                            clickedItem.contentItem,
                            railItemsModel = clickedItem.railItemsModel,
                            contentAnalyticsModel = clickedItem.contentAnalyticsModel
                        ),
                        clickedItem.extras
                    )
                }
            }
        })
        viewModel.getSeeAllClickedRail().observe(viewLifecycleOwner, Observer
        {
            val seeAllTransition = it.getContentIfNotHandled()
            if (seeAllTransition != null) {
                if (seeAllTransition.sectionSource.equals(ItemViewType.LANGUAGE.name, true)
                    || seeAllTransition.sectionSource.equals(ItemViewType.GENRE.name, true)
                ) {
                } else {
                    val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                        this.duration = 250
                    }
                    exitTransition = forward
                    var railResponse: RecommendationResponse? = null
                    if (!seeAllTransition.placeHolder.isNullOrEmpty()) {
                        railResponse = RecommendationResponse()
                        railResponse.data = seeAllTransition.item
                    }
                    findNavController().navigateSafe(
                        SubHomeFragmentDirections.actionActionHomeLandingToSeeAllFragment(
                            seeAllTransition.railIdName.first.toString(),
                            seeAllTransition.railIdName.second,
                            seeAllTransition.sectionSource,
                            false,
                            null,
                            viewModel.provider,
                            seeAllTransition.placeHolder,
                            seeAllTransition.source,
                            seeAllTransition.configType,
                            seeAllTransition.isMixedRail,
                            seeAllTransition.isPrepand,
                            taContentResponse = railResponse,
                            backgroundImage = seeAllTransition.backgroundImage,
                            railPosition = seeAllTransition.railPosition.toString(),
                            refId = seeAllTransition.refId,
                            contentAnalyticsModel = seeAllTransition.contentAnalyticsModel
                        )
                    )
                }
            }
        })
    }

    private fun doPullToRefresh() {
        viewModel.pageOffset = 0
        viewModel.fetchHierarchyData(false, context?.let { it1 -> isTablet(it1) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FOR_PACK_SELECTION && resultCode == Activity.RESULT_OK) {
            updateUIWithDeeplink()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun updateUIWithDeeplink() {
        val uri = getString(
            R.string.deeplink_app_page,
            BuildConfig.hostName,
            args.pageType,
            args.provider,
            args.providerId
        )
        val notificationIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(uri),
                activity?.applicationContext,
                LandingActivity::class.java
            )
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(notificationIntent)
    }

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun onPause() {
        super.onPause()
        (viewModel.getAdapter() as HomeAdapter).handleHomeTrailerPlayBack(true)
    }

    private fun refreshOnlyAdapter() {
        viewModel.setPackInAdapter()
        viewModel.notifyAdapter()
        (activity as? LandingActivity)?.handleSubscribeButtonVisibility()
    }
}
