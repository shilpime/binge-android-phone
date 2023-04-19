//package com.tatasky.binge.ui.features.home
//
//
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.widget.TextView
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import androidx.viewpager2.widget.ViewPager2
//import com.google.android.material.tabs.TabLayoutMediator
//import com.google.android.material.transition.MaterialSharedAxis
//import com.tatasky.binge.R
//import com.tatasky.binge.analytics.SOURCE_HOME
//import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
//import com.tatasky.binge.analytics.SOURCE_NOTIFICATION_ERROR
//import com.tatasky.binge.data.networking.models.ErrorModel
//import com.tatasky.binge.data.networking.models.response.ContentItem
//import com.tatasky.binge.data.networking.models.response.LeftMenuResponse
//import com.tatasky.binge.databinding.FragmentHomeBinding
//import com.tatasky.binge.interfaces.CommonDialogEventListener
//import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
//import com.tatasky.binge.ui.features.common.CommonSampleViewModel
//import com.tatasky.binge.ui.features.dialog.DialogModel
//import com.tatasky.binge.ui.features.subscription.view.FirestickDialog
//import com.tatasky.binge.utils.*
//import java.util.*
//import javax.inject.Inject
//
//
///**
// * The homePageUseCase screen of the app which displays all the products
// *
// */
//class ParentHomeFragment : CancellationBaseFragment<FragmentHomeBinding, HomeViewModel>() {
//    private var menuResponse: LeftMenuResponse? = null
//    private var isMenuItemFetched: Boolean = false
//    val homeFragmentArgs by
//    navArgs<ParentHomeFragmentArgs>()
//    private var commonViewModel : CommonSampleViewModel?=null
//    private var cancelledUserShownOnce = false
//
//    @Inject
//    lateinit var homeAnalytics: HomeAnalytics
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        binding.vm = viewModel
//        commonViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[CommonSampleViewModel::class.java]
//        commonViewModel?.getReselected()?.observe(viewLifecycleOwner, Observer {
//            if (binding.viewpager.currentItem != 0)
//                it.getContentIfNotHandled()?.let {
//                    binding.viewpager.setCurrentItem(0, true)
//                }
//        })
//        commonViewModel?.homeSelected()?.observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                binding.viewpager.setCurrentItem(0, true)
//            }
//        })
//        commonViewModel?.forceDeviceStatusLogout?.observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                showDeviceStatusLogout()
//            }
//        })
//        commonViewModel?.getSubscriptionResponse()?.observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                if(true == activity?.intent?.extras?.getBoolean("isCancelledUser") && true == sharedPrefs.getSubscribedPack()?.isInactive){
//                    handleDeactivateDialog(SOURCE_HOME)
//                }
//                context?.let {ctx->
//                    if (true == it.data?.atvCancelled) {
//                        localBroadcastHelper.sendBroadcast(ctx, localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH)
//                    }
//                }
//            }
//        })
//        binding.viewpager.adapter =
//            ViewPagerFragmentStateAdapter(
//                viewModel.pageTypeIndexMap,
//                viewModel.pageNameIndexMap,
//                viewModel.searchPageNameIndexMap,
//                childFragmentManager,
//                lifecycle
//            )
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            duration = 250
//        }
//        reenterTransition = backward
//
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            duration = 250
//        }
//        exitTransition = forward
//        try {
//            if (homeFragmentArgs.contentItem != null)
//                findNavController().navigateSafe(
//                    ParentHomeFragmentDirections.actionToDetail(
//                        homeFragmentArgs.contentItem!!
//                    )
//                )
//            if (homeFragmentArgs.id != null && homeFragmentArgs.contentType != null) {
//                findNavController().navigateSafe(
//                    ParentHomeFragmentDirections.actionToDetail(
//                        ContentItem().apply {
//                            id = homeFragmentArgs.id!!
//                            contentType = homeFragmentArgs.contentType!!
//                            provider = ""
//                        }
//                    )
//                )
//            }
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//    }
//
//    override fun toBeCalledOnce() {
//        initViews()
//    }
//
//    private fun onMenuItemFetched(it: LeftMenuResponse?) {
//        if (!it?.data?.items.isNullOrEmpty()) {
//            val items = it?.data?.items!!
//            for ((position, item) in items.withIndex()) {
//                viewModel.pageTypeIndexMap.append(position, item.pageType)
//                viewModel.pageNameIndexMap.append(position, item.pageName)
//                viewModel.searchPageNameIndexMap.append(position, item.searchPageName)
//            }
//            binding.viewpager.adapter =
//                ViewPagerFragmentStateAdapter(
//                    viewModel.pageTypeIndexMap,
//                    viewModel.pageNameIndexMap,
//                    viewModel.searchPageNameIndexMap,
//                    childFragmentManager,
//                    lifecycle
//                )
//            TabLayoutMediator(
//                binding.tabMenu,
//                binding.viewpager
//            ) { tab, position ->
//                tab.setCustomView(R.layout.custom_tab)
//                tab.customView?.findViewById<TextView>(R.id.tabItem)?.text =
//                    viewModel.pageNameIndexMap[position]
//            }.attach()
//
//            binding.viewpager.registerOnPageChangeCallback(object :
//                ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    super.onPageSelected(position)
//                    if(viewModel.isLoggedIn())
//                        homeAnalytics.trackHomeInitiate(viewModel.pageNameIndexMap[position], viewModel.sharedPrefs.isActivePack() && checkSubscription(viewModel.sharedPrefs.getPartnerIdsList()))
//                }
//            })
//        } else {
//            handleNoData()
//        }
//        Handler(Looper.getMainLooper()).postDelayed({
//            if(viewModel.isLoggedIn()){
//                initRechargeNotificationAPI()
//                if (shouldStartCancellationTrigger() && !cancelledUserShownOnce) {
//                    viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
//                }
//                cancelledUserShownOnce = true
//                if(activity?.intent?.getStringExtra(KEY_FROM_SCREEN) == SOURCE_NOTIFICATION_ERROR ){
//                    showToast(context, "Something went wrong!")
//                }
//            }
//        }, 500)
//    }
//
//    private fun handleNoData() {
//        isMenuItemFetched = false
//    }
//
//    private fun initViews() {
//        if(viewModel.isLoggedIn()){
//            viewModel.fetchProfileInfo()
//        }
//        binding.viewpager.isUserInputEnabled = false
//    }
//
//    private fun initRechargeNotificationAPI() {
////        if(true == sharedPrefs.getSubscribedPack()?.isInactive && true == activity?.intent?.getBooleanExtra(
////                "fromSplash",
////                false
////            )){
////            e("HomeFragment","inside handleDeactivateDialog")
////            handleDeactivateDialog()
////        }
//        val lastVistedDateTime: Long = viewModel.getLastVisitedDateTime()
//        val lastDay: Int = Calendar.getInstance().apply {
//            this.timeInMillis = lastVistedDateTime
//        }.get(Calendar.DATE)
//        val today: Int = Calendar.getInstance().get(Calendar.DATE)
//        if (today != lastDay && true == activity?.intent?.getBooleanExtra("fromSplash", false)) {
//            viewModel.fetchRechargeNotification()
//        }
//        if (activity?.intent?.getStringExtra(KEY_FROM_SCREEN) != SOURCE_NOTIFICATION && activity?.intent?.getStringExtra(
//                KEY_FROM_SCREEN
//            ) != SOURCE_NOTIFICATION_ERROR
//        ) {
//            showFirestickOfferDialogByFrequency()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            duration = 250
//        }
//        reenterTransition = backward
//
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            duration = 250
//        }
//        exitTransition = forward
//        if (menuResponse == null) {
//            isMenuItemFetched = true
//            viewModel.fetchMenuItems()
//        }
//    }
//
//    override fun getViewModelClass(): Class<HomeViewModel> = HomeViewModel::class.java
//
//    override fun layoutId(): Int = R.layout.fragment_home
//
//    override fun onNetworkError(errorMessage: String, isRetry: Boolean) {
//        super.onNetworkError(errorMessage, isRetry)
//
//        isMenuItemFetched = false
//    }
//    override fun onError(errorModel: ErrorModel) {
//        super.onError(errorModel)
//        if(viewModel.isLoggedIn()){
//            initRechargeNotificationAPI()
//        }
//        isMenuItemFetched = false
//        handleNoData()
//    }
//
//    override fun setObserver() {
//        super.setObserver()
//        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let { it ->
//                e("pubnub", "inside updateInPack : $it and menuResponse : $menuResponse")
//                if (it && menuResponse != null) {
//                    //reload page
//                    onMenuItemFetched(menuResponse)
//                }
//            }
//        })
//
//        viewModel.getDunningResponse().observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                when (it.code) {
//                    CODE_SUCCESS -> {
//                        viewModel.setLastVisitedDateTime(Calendar.getInstance().timeInMillis)
//                        showDialog(DialogModel(
//                            false,
//                            R.drawable.ic_recharge,
//                            getString(R.string.alert),
//                            getString(R.string.recharge),
//                            getString(R.string.skip),
//                            it.data?.dunningMessage ?: getString(R.string.dunning_message_default)
//                        ), object : CommonDialogEventListener {
//                            override fun onPrimaryButtonClick() {
//                                hideDialog()
//                                activity?.let {
//                                    startActivity(
//                                        getSubscriptionActivityIntent(
//                                            it,
//                                            false,
//                                            null,
//                                            SOURCE_HOME,
//                                            true
//                                        )
//                                    )
//                                }
//                            }
//
//                            override fun onSecondaryButtonClick() {
//                                hideDialog()
//                            }
//
//                            override fun onCloseButtonClick() {
//                                hideDialog()
//                            }
//                        })
//                    }
//                }
//            }
//        })
//
//        viewModel.getLeftMenuItem().observe(this, Observer {
//            it.getContentIfNotHandled()?.let { lrm ->
//                menuResponse = lrm
//                onMenuItemFetched(lrm)
//            }
//        })
//    }
//
//    override fun onDestroyView() {
//        binding.viewpager.adapter = null
//        super.onDestroyView()
//    }
//
//    override fun getViewModelOwner(): ViewModelStoreOwner = activity as LandingActivity
//
//    var recommendationDialog : FirestickDialog? = null
//    /*private fun showFirestickOfferDialog(
//    ) {
//        val eligibleFirestick = viewModel.sharedPrefs.getSubscribedPack()?.eligibleFirestick
//        val firestickDeliverable = viewModel.sharedPrefs.getSubscribedPack()?.ocsFlag.equals("Y", true)
//        val isAlreadytakenFirestick = viewModel.sharedPrefs.isFirestickTaken()
//        val isFirestickDialogAlreadyShown = viewModel.sharedPrefs.isFirestickDialogShown()
//        if (recommendationDialog == null) {
//            if (eligibleFirestick == true && !isAlreadytakenFirestick && !isFirestickDialogAlreadyShown && firestickDeliverable) {
//                viewModel.sharedPrefs.setFirestickDialogShown(true)
//                recommendationDialog = FirestickDialog.newInstance(
//                    object : CommonDialogEventListener {
//                        override fun onPrimaryButtonClick() {
//                            recommendationDialog?.dismiss()
//                            recommendationDialog = null
//                            findNavController().navigateSafe(HomeFragmentDirections.actionHomeFragmentToFirestickJourney())
//                        }
//
//                        override fun onSecondaryButtonClick() {
//                            recommendationDialog?.dismiss()
//                            recommendationDialog = null
//                        }
//
//                        override fun onCloseButtonClick() {
//                        }
//
//                    }
//                )
//                recommendationDialog?.show(parentFragmentManager, DIALOG_TAG)
//            }
//        }
//    }*/
//
//    private fun showFirestickOfferDialogByFrequency(
//    ) {
//        val firestickDialogVisibilityType = viewModel.sharedPrefs.getFirestickDialogVisibilityType()
//        val eligibleFirestick = viewModel.sharedPrefs.getSubscribedPack()?.eligibleFirestick
//        val isFSRequestRaised = viewModel.sharedPrefs.getSubscribedPack()?.isFSRequestRaised ?: false
//        val firestickDeliverable =
//            viewModel.sharedPrefs.getSubscribedPack()?.ocsFlag.equals("Y", true)
//        val isAlreadytakenFirestick = viewModel.sharedPrefs.isFirestickTaken()
//        if (recommendationDialog == null
//            && !isFSRequestRaised
//            && !isAlreadytakenFirestick
//            && eligibleFirestick == true && firestickDeliverable) {
//            when (firestickDialogVisibilityType) {
//                KEY_DIALOG_VISIBILITY_TYPE_TIME -> {
//                    val lastTime = viewModel.sharedPrefs.getFirestickDialogLastVisibleTime()
//                    val timeFrequency = viewModel.sharedPrefs.getFirestickDialogTimeFrequency()
//                    val currentTime = Calendar.getInstance().timeInMillis
//                    if (lastTime == 0L || (lastTime > 0 && timeFrequency > 0 && (currentTime - lastTime) >= (timeFrequency * 3600000))) {
//                        showFirestickDialogOnUI()
//                        viewModel.sharedPrefs.setFirestickDialogLastVisibleTime(currentTime)
//                    }
//                }
//                KEY_DIALOG_VISIBILITY_TYPE_EVENT -> {
//                    val launchValue = viewModel.sharedPrefs.getAppLaunchValue()
//                    val launchFrequency = viewModel.sharedPrefs.getFirestickDialogLaunchFrequency()
//                    if (launchValue == 0 || launchValue % launchFrequency == 0) {
//                        showFirestickDialogOnUI()
//                    }
//                }
//                KEY_DIALOG_VISIBILITY_TYPE_TIME_EVENT -> {
//                    val launchValue = viewModel.sharedPrefs.getAppLaunchValue()
//                    val launchFrequency = viewModel.sharedPrefs.getFirestickDialogLaunchFrequency()
//                    if (viewModel.sharedPrefs.getFirestickDialogFirstVisbileTime() > 0) {
//                        if (launchValue == 0 || launchValue % launchFrequency == 0) {
//                            showFirestickDialogOnUI()
//                        }
//                    } else {
//                        viewModel.sharedPrefs.setFirestickDialogFirstVisbileTime(Calendar.getInstance().timeInMillis)
//                        showFirestickDialogOnUI()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun showFirestickDialogOnUI() {
//            recommendationDialog = FirestickDialog.newInstance(
//                object : CommonDialogEventListener {
//                    override fun onPrimaryButtonClick() {
//                        recommendationDialog?.dismiss()
//                        recommendationDialog = null
//                        findNavController().navigateSafe(ParentHomeFragmentDirections.actionHomeFragmentToFirestickJourney())
//                    }
//
//                    override fun onSecondaryButtonClick() {
//                        recommendationDialog?.dismiss()
//                        recommendationDialog = null
//                    }
//
//                    override fun onCloseButtonClick() {
//                    }
//
//                }
//            )
//            recommendationDialog?.show(parentFragmentManager, DIALOG_TAG)
//    }
//}