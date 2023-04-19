package com.tatasky.binge.ui.features.subscription_freemium.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.FREEMIUM
import com.tatasky.binge.analytics.GO_VIP
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.analytics.SOURCE_SUBSCRIPTION
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.EligiblePackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentFreemiumSubscriptionBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.enable
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.setSingleOnClick
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumPackListAdapter
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*

class FreemiumSubscriptionFragment :
    BaseFragment<FragmentFreemiumSubscriptionBinding, FreemiumSubscriptionViewModel>() {
    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_freemium_subscription

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.freemium_subscription)


    override fun setObserver() {
        viewModel.updateInPack.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let {
                if(it){
                    viewModel.fetchEligiblePackList()
                }
            }
        }

        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                findNavController().navigateSafe(
                    FreemiumSubscriptionFragmentDirections.actionSubscriptionStarterFragmentToLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                        loginSource = SOURCE_SUBSCRIPTION,
                        isLoginToHome = true
                    )
                )
            }
        }

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(
                    FreemiumSubscriptionFragmentDirections.actionSubscriptionStarterFragmentToLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = null,
                        loginSource = SOURCE_SUBSCRIPTION,
                        isLoginToHome = true
                    )
                )
            }
        }

        viewModel.getSelectedPack().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled().let { pack ->
                if (pack != null)
                    binding.btnPackProceed.enable()
            }
        })

        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!findNavController().navigateUp())
                    startHomeScreen(activity)
            }
        })

        viewModel.getEligiblePacksResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding.groupPackListing.show()
                var gameInfo = it.data?.getOrNull(0)?.gameZopInfo

                if (gameInfo != null && !(gameInfo.gameZopVerbiage.isNullOrEmpty())) {
                    binding.gameInfoLayout.show()
                    binding.tvGameInfo.text = gameInfo.gameZopVerbiage
                    gameInfo.gameZopIcon?.let { it1 ->
                        transparentImageLoad(
                            binding.ivGameLabel,
                            it1
                        )
                    }
                }

                if (it.data.isNullOrEmpty()) {
                    onError(ErrorModel())
                } else {
                    if (it.data.isNullOrEmpty() || it.data?.size == 1) {
                        binding.comparePlanTV.hide()
                    }

                    activity?.intent?.getStringExtra("packName")?.let { packName ->
                        val scrollPosition = getScrollToPositionViaPackName(packName, it.data!!)
                        binding.recyclerPackOptions.scrollToPosition(scrollPosition)

                        Handler(Looper.getMainLooper()).postDelayed({
                            val holder: RecyclerView.ViewHolder? =
                                binding.recyclerPackOptions.findViewHolderForAdapterPosition(
                                    scrollPosition
                                )
                            holder?.itemView?.callOnClick()
                        }, 100)
                    }

                    activity?.intent?.getStringExtra("partnerId")?.let { partnerId ->
                        val scrollPosition = getScrollToPositionViaPartner(partnerId, it.data!!)
                        binding.recyclerPackOptions.scrollToPosition(scrollPosition)

                        Handler(Looper.getMainLooper()).postDelayed({
                            val holder: RecyclerView.ViewHolder? =
                                binding.recyclerPackOptions.findViewHolderForAdapterPosition(
                                    scrollPosition
                                )
                            holder?.itemView?.callOnClick()
                        }, 100)
                    }


                    activity?.intent?.getStringExtra("packId")?.let { partnerId ->
                        val scrollPosition = getScrollToPositionViaPack(partnerId, it.data!!)
                        binding.recyclerPackOptions.scrollToPosition(scrollPosition)

                        Handler(Looper.getMainLooper()).postDelayed({
                            val holder: RecyclerView.ViewHolder? =
                                binding.recyclerPackOptions.findViewHolderForAdapterPosition(
                                    scrollPosition
                                )
                            holder?.itemView?.callOnClick()
                        }, 100)
                    }

                    /*Pre Selected Pack for Guest Only*/
                    if (!sharedPrefs.getLoginStatus() && it.data?.getOrNull(0)?.preSelectedPackId != null) {
                        val scrollPosition = getScrollToPositionViaPack(
                            it.data?.getOrNull(0)?.preSelectedPackId,
                            it.data!!
                        )
                        binding.recyclerPackOptions.scrollToPosition(scrollPosition)

                        Handler(Looper.getMainLooper()).postDelayed({
                            val holder: RecyclerView.ViewHolder? =
                                binding.recyclerPackOptions.findViewHolderForAdapterPosition(
                                    scrollPosition
                                )
                            holder?.itemView?.callOnClick()
                        }, 100)
                    }
                }
            }
        })

    }


    private fun getScrollToPositionViaPartner(partnerId: String?, data: List<PartnerPacks>): Int{
        //TODO: improve the complexity
        for (i in data.indices) {
            for (j in 0 until data[i].getSelectedComponentAppList.size) {
                if (data[i].getSelectedComponentAppList[j].included == true && data[i].getSelectedComponentAppList[j].partnerId.equals(
                        partnerId
                    )
                ) {
                    return i
                }
            }
        }
        return 0
    }

    private fun getScrollToPositionViaPack(packId: String?, data: List<PartnerPacks>) : Int{
        for(i in data.indices){
            if(data[i].productId.equals(packId)){
                return i
            }
        }
        return 0
    }

    private fun getScrollToPositionViaPackName(packName: String? , data: List<PartnerPacks>) : Int{
        for(i in data.indices){
            if(data[i].productName.equals(packName)){
                return i
            }
        }
        return 0
    }

    override fun toBeCalledOnce() {
        setListeners()

        val source = activity?.intent?.extras?.getString(KEY_FROM_SCREEN) ?: SOURCE_HOME
        val mixpanelSourceToUse =
            if (source.equals(SOURCE_HOME, true))
                GO_VIP
            else
                source

        viewModel.subscriptionAnalytics.trackSubscriptionPageInitiate(mixpanelSourceToUse)

        viewModel.subscriptionAnalytics.trackInitiatePackSelection(
            source,
            activity?.intent?.extras?.getBoolean("isFromNudge") ?: false,
            viewModel.sharedPref.getLoginStatus()
        )

        if (!activity?.intent?.extras?.getString("selectedAppId").isNullOrBlank()) {
            viewModel.setToSubscribePartner(activity?.intent?.extras?.getString("selectedAppId")!!)
        }
/*
        val currentPack = sharedPrefs.getSubscribedPack()
        if(currentPack?.planCTADetails?.changePlanText.isNullOrBlank())
            binding.tvSubLandingTitle.text = getString(R.string.subscribe)
        else {
            binding.tvSubLandingTitle.text = getString(R.string.change_plan)
        }*/
        //viewModel.setMockResponse(readRawFile())

        viewModel.fetchEligiblePackList()
        (binding.recyclerPackOptions.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


//        val snapHelper: SnapHelper = PagerSnapHelper()
//        snapHelper.attachToRecyclerView(binding.recycler)

//        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val visiblePosition = (binding.recycler.layoutManager as CenterLinearLayoutManager).findLastCompletelyVisibleItemPosition()
//                if(visiblePosition != -1) {
//                    viewModel.subscriptionAnalytics.trackPlanSelectionViews(
//                        packName = viewModel.fetchedEligiblePackResponse?.data?.get(
//                            visiblePosition
//                        )?.productName ?: ""
//                    )
//                }
//            }
//        })

        imageLoad(binding.subscriptionBackground,
            sharedPrefs.getConfigResponse()?.data?.config?.freemiumBackgroundPoster?.androidSubscriptionBackgroundPoster?.otherPackPoster
                ?: ""
        )
        handleExistingUserBtnVisibility()

    }

    private fun setListeners() {
        binding.imgBack.setOnClickListener {
            activity?.let {
                findNavController().navigateUpOrOpenHome(
                    it as AppCompatActivity
                )
            }
        }

        binding.tvExistingUser.setOnClickListener {
            viewModel.getPreviouslyUsedMobileNumbers()
        }

        binding.comparePlanTV.setOnClickListener {
            findNavController().navigateSafe(FreemiumSubscriptionFragmentDirections.actionFreemiumSubscriptionFragmentToComparePlanFragment())
        }

        binding.btnPackProceed.setSingleOnClick(1000) {
            (binding.recyclerPackOptions.adapter as? FreemiumPackListAdapter)?.getSelected()?.let {
                //TODO: try to Navigate using NavDirection/Action
                findNavController().navigate(R.id.tenureBottomSheetDialog, Bundle().apply {
                    putParcelable("pack", it)
                    putString(KEY_FROM_SCREEN, "PackListing")
                })
                viewModel.subscriptionAnalytics.trackSubscriptionPageProceed(
                    activity?.intent?.extras?.getString(
                        "fromScreen"
                    ) ?: SOURCE_HOME , it.productName?: FREEMIUM , it.amountValue ?: FREEMIUM
                )

                /*findNavController().navigateSafe(
                    FreemiumSubscriptionFragmentDirections
                        .actionFreemiumSubscriptionFragmentToTenureBottomSheetDialog(
                            it, "PackListing"
                        )
                )*/
            }
        }
    }

    private fun handleExistingUserBtnVisibility(){
        if(sharedPrefs.getLoginStatus()) {
            binding.tvExistingUser.hide()
            binding.ivExistingUserEnd.hide()
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
    }

    private fun readRawFile(): EligiblePackResponse {
        val objectArrayString: String =
            requireContext().resources.openRawResource(R.raw.eligible_pack).bufferedReader()
                .use { it.readText() }
        return Gson().fromJson(objectArrayString, EligiblePackResponse::class.java)
    }

    override fun onResume() {
        super.onResume()
        if(viewModel?.adapterNew?.getSelected() != null){
            binding.btnPackProceed.enable()
        }
        handleExistingUserBtnVisibility()
    }

}