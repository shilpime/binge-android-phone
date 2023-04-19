package com.tatasky.binge.ui.features.subscription_freemium

import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.analytics.SOURCE_SUBSCRIPTION
import com.tatasky.binge.analytics.YES
import com.tatasky.binge.data.networking.models.response.EligiblePackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentSubscriptionStarterBinding
import com.tatasky.binge.helper.loadSubscriptionBottomSheetBanner
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.disableWithoutAlpha
import com.tatasky.binge.ui.base.frameworks.extensions.enable
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumPackListAdapter
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject


class SubscriptionStarterFragment :
    BaseFragment<FragmentSubscriptionStarterBinding, FreemiumSubscriptionViewModel>() {

    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subscription_starter


    override fun getViewModelOwner(): ViewModelStoreOwner = requireActivity()

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    private var source: String = ""

    private var selectedPack: PartnerPacks? = null

    private fun getSourceOrFromScreenName() =
        activity?.intent?.extras?.get(KEY_FROM_SCREEN) as String? ?: viewModel.source.takeIf {
            !it.isNullOrBlank()
        }?.also { viewModel.source = null /*After consumption, make it null again*/ }
        ?: SOURCE_HOME

    override fun setObserver() {

        viewModel.getSelectedPack().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled().let { pack ->
                if (pack == null) {
                    binding.btnPackProceed.disableWithoutAlpha()
                    selectedPack = null
                } else {
                    selectedPack = pack
                    binding.btnPackProceed.enable()
                }
            }
        }
        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                findNavController().navigateSafe(
                    SubscriptionStarterFragmentDirections.actionSubscriptionStarterFragmentToLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                        loginSource = SOURCE_SUBSCRIPTION,
                        isLoginToHome = false
                    )
                )
            }
        }

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(
                    SubscriptionStarterFragmentDirections.actionSubscriptionStarterFragmentToLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = null,
                        loginSource = SOURCE_SUBSCRIPTION,
                        isLoginToHome = false
                    )
                )
            }
        }

    }

    override fun toBeCalledOnce() {
        source = arguments?.getString("source") ?: ""
        e("SubscriptionStarterFragment", source)
        subscriptionAnalytics.trackSubscriptionDrawerInitiate(source, viewModel.sharedPref.getLoginStatus(), sourceScreenForAppsFlyer = getSourceOrFromScreenName())

        val packList = (activity as? LandingActivity)?.packList
        packList?.let{ it->
            var gameInfo = it.data?.getOrNull(0)?.gameZopInfo

            if (gameInfo != null && !(gameInfo.gameZopVerbiage.isNullOrEmpty())) {
                binding.gameInfoLayout.show()
                binding.tvGameInfo.text = gameInfo.gameZopVerbiage
                gameInfo.gameZopIcon?.let { it1 -> transparentImageLoad(binding.ivGameLabel, it1) }
            }
            it.data?.let { it1 ->
                binding.recyclerPackOptions.adapter = FreemiumPackListAdapter(it1,viewModel)
            }
        }

        loadSubscriptionBottomSheetBanner(binding.ivSubscriptionBsBg,
            sharedPrefs.getConfigResponse()?.data?.config?.freemiumBackgroundPoster?.androidSubscriptionBackgroundPoster?.otherPackPoster
                ?: ""
        )

        setUpBottomSheetHeight()
        setClickListeners()
        handleExistingUserBtnVisibility()
        packList?.data?.let { data ->
            data.getOrNull(0)?.preSelectedPackId?.let { preSelectedPackId ->
                val scrollPosition = getScrollToPositionViaPack(preSelectedPackId, data)
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


        (activity as? LandingActivity)?.subscriptionBottomSheetDialog?.dialog?.setOnDismissListener(object : DialogInterface.OnDismissListener{
            override fun onDismiss(p0: DialogInterface?) {
                p0?.let{
                    (activity as? LandingActivity)?.subscriptionBottomSheetDialog?.onDismiss(p0)
                }
                subscriptionAnalytics.trackSubscriptionDrawerClose(
                    source,
                    selectedPack?.productName ?: ""
                )
            }

        })

    }

    private fun getScrollToPositionViaPack(packId: String?, data: List<PartnerPacks>) : Int{
        for(i in data.indices){
            if(data[i].productId.equals(packId)){
                return i
            }
        }
        return 0
    }

    private fun setClickListeners() {

        binding.tvComparePlan.setOnClickListener {
            //Todo check for managedApp
            startActivity(
                getSubscriptionActivityIntent(
                    context,
                    startComparePlan = true
                )
            )
        }

        binding.btnPackProceed.setOnClickListener {
            (binding.recyclerPackOptions.adapter as FreemiumPackListAdapter).getSelected()?.let {
                subscriptionAnalytics.trackSubscriptionDrawerProceed(source,it.productName?:"")
                findNavController().navigateSafe(
                    SubscriptionStarterFragmentDirections
                        .actionSubscriptionStarterFragmentToSubscriptionTenureBottomSheetDialog(
                            it,
                            fromScreen = MINI_DRAWER
                        )
                )
            }
        }
        binding.btnPackLater.setOnClickListener {
            subscriptionAnalytics.trackSubscrptionDrawerLater(source)
            if (activity is LandingActivity) {
                (activity as LandingActivity).subscriptionBottomSheetDialog?.dismiss()
            } else {
                activity?.onBackPressed()
            }
        }

        binding.tvExistingUser.setOnClickListener {
            subscriptionAnalytics.trackExistingUserLogin(source)
            viewModel.getPreviouslyUsedMobileNumbers()
        }

    }

    private fun handleExistingUserBtnVisibility(){
        if(sharedPrefs.getLoginStatus()) {
            binding.tvExistingUser.hide()
            binding.ivExistingUserEnd.hide()
        }
    }

    private fun setUpBottomSheetHeight() {
        val displayMetrics: DisplayMetrics = requireActivity().resources.displayMetrics
        val height: Int = displayMetrics.heightPixels
//        val offsetFromTop = (height * 0.20).toInt()
        val maxHeight = (height * 0.85).toInt()

        val mConstrainLayout = binding.clRoot
        val lp = mConstrainLayout.layoutParams as FrameLayout.LayoutParams
        lp.height = maxHeight
        mConstrainLayout.layoutParams = lp
        (binding.recyclerPackOptions.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

    }

    private fun readRawFile(): EligiblePackResponse {
        val objectArrayString: String =
            requireContext().resources.openRawResource(R.raw.eligible_pack).bufferedReader()
                .use { it.readText() }
        return Gson().fromJson(objectArrayString, EligiblePackResponse::class.java)
    }

    override fun onResume() {
        super.onResume()
        handleExistingUserBtnVisibility()
        if(binding.recyclerPackOptions.adapter !=null &&
            (binding.recyclerPackOptions.adapter as FreemiumPackListAdapter).getSelected() != null){
            binding.btnPackProceed.enable()
        }

    }
}