package com.tatasky.binge.ui.features.subscription_freemium

import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.databinding.FragmentSubscriptionTypeSelectorBinding
import com.tatasky.binge.helper.loadSubscriptionBottomSheetBanner
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumProviderAdapter
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.ManagedAppViewModel
import com.tatasky.binge.utils.*
import javax.inject.Inject


class SubscriptionTypeSelectorFragment :
    BaseBottomSheetDialogFragment<FragmentSubscriptionTypeSelectorBinding, FreemiumSubscriptionViewModel>() {

    private lateinit var managedAppViewModel: ManagedAppViewModel
    private val mHandler = Handler(Looper.getMainLooper())

    private var showBothTypes = false
    var navigationToManagedApp = false

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    private val args by navArgs<SubscriptionTypeSelectorFragmentArgs>()
    private val TAG = this.javaClass.simpleName
    private var source: String? = null
    private var landingActivity: LandingActivity? = null

    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subscription_type_selector


    override fun getViewModelOwner(): ViewModelStoreOwner = requireActivity()


    override fun setObserver() {

        if (navigationToManagedApp)
            return
        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->

                findNavController().navigateSafe(
                    SubscriptionTypeSelectorFragmentDirections.actionSubscriptionTypeSelectorFragmentToGuestLoginBottomDialogFragment(
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
                    SubscriptionTypeSelectorFragmentDirections.actionSubscriptionTypeSelectorFragmentToGuestLoginBottomDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = null,
                        loginSource = SOURCE_MANAGED_APPS,
                        isLoginToHome = false
                    )
                )
            }
        }

        managedAppViewModel.progressListener.observe(viewLifecycleOwner) {
            if (it) {

                showProgress()

                binding.root.closeKeyboard()


                mHandler.postDelayed(
                    showProgress, loaderDelayTime
                )
            } else {
                mHandler.removeCallbacks(showProgress)
                hideProgress()
            }
        }

        managedAppViewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateUp()
            }
        })
        managedAppViewModel.getManagedAppResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { managedAppResponse ->
                managedAppResponse.data?.href?.let {
                    findNavController().navigateSafe(
                        SubscriptionTypeSelectorFragmentDirections.actionSubscriptionTypeSelectorFragmentToManagedAppFragment(
                            journeySource = landingActivity?.currentJourneyRef ?: "",
                            accessToken = managedAppResponse.data?.accessToken ?: "",
                            pageUrl = it
                        )
                    )
                } ?: run { onError(ErrorModel()) }
            }
        })
    }


    private fun showProgress() {
        binding.progressBar.startProgressAvd(true)
        binding.cyopProgressBar.startProgressAvd(true)
    }

    private fun hideProgress() {
        binding.progressBar.startProgressAvd(false)
        binding.progressBar.visibility = View.GONE
        binding.cyopProgressBar.startProgressAvd(false)
        binding.cyopProgressBar.visibility = View.GONE
    }


    override fun toBeCalledOnce() {
        sharedPrefs.getConfigResponse()?.data?.config?.enableTickTickJourney?.let {
            showBothTypes = it
        }

        managedAppViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            )[ManagedAppViewModel::class.java]


        source = args.source
        if (activity is LandingActivity)
            landingActivity = activity as LandingActivity
        navigationToManagedApp = !TextUtils.isEmpty(args.journeySource) || args.skipDrawer==true
        //Navigate to ManagedApps Webview as this is the start destination of navgraph
        if (navigationToManagedApp) {
//            mHandler.postDelayed(object : Runnable{
//                override fun run() {
                    findNavController().navigateSafe(
                        SubscriptionTypeSelectorFragmentDirections.actionSubscriptionTypeSelectorFragmentToManagedAppFragment(
                            journeySource = args.journeySource,
                            journeySourceRefId = args.journeySourceRefId,
                            skipDrawer = args.skipDrawer
                        )
                    )
//                }
//
//            },5000)

            return
        }
        //Todo cross wcheck if code after this is executing


        d(TAG, source)
        subscriptionAnalytics.trackSubscriptionDrawerInitiate(
            source
                ?: ""/*, viewModel.sharedPref.getLoginStatus(), sourceScreenForAppsFlyer = getSourceOrFromScreenName()*/,
            sourceScreenForAppsFlyer = ""
        )

        setUiElements()
        setClickListeners()
    }

    private fun setUiElements() {

        binding.parent.visibility=View.VISIBLE
        expandBottomSheet(dialog)
        if (!showBothTypes) {
            binding.clRoot.visibility = View.GONE
            binding.cyopClRoot.visibility = View.VISIBLE
            sharedPrefs.getConfigResponse()?.data?.config?.tickTickFixedPlanDrawerScreen?.let {
                val coloredValue=it.colorTitleValue
                it.colorTitleValue = " " + Html.fromHtml(it.colorTitleValue).toString()
                binding.drawerData = it
                it.partnersImage?.let {
                    binding.cyopRvHeaderApps.adapter = FreemiumProviderAdapter(it)
                }
                binding.cyopTvMarkettingAmount.text= Html.fromHtml(" " +coloredValue)
                paintPremiumGradient(
                    binding.cyopTvMarkettingAmount,
                    binding.cyopTvMarkettingAmount.paint.measureText(
                        Html.fromHtml(coloredValue).toString()
                    )
                )


            }
            if (sharedPrefs.getLoginStatus()) {
                binding.cyopTvExistingUser.hide()
                binding.arrow.hide()

            }
            loadSubscriptionBottomSheetBanner(
                binding.cyopIvSubscriptionBsBg,
                sharedPrefs.getConfigResponse()?.data?.config?.freemiumBackgroundPoster?.androidSubscriptionBackgroundPoster?.otherPackPoster
                    ?: ""
            )
        } else {
            binding.clRoot.visibility = View.VISIBLE
            binding.cyopClRoot.visibility = View.GONE

            sharedPrefs.getConfigResponse()?.data?.config?.tickTickDrawerScreen?.let {
                it.colorTitleValue = " " + Html.fromHtml(it.colorTitleValue).toString()
                binding.drawerData = it
                it.partnersImage?.let {
                    binding.rvHeaderApps.adapter = FreemiumProviderAdapter(it)
                }
                paintPremiumGradient(
                    binding.tvMarkettingAmount,
                    binding.tvMarkettingAmount.paint.measureText(
                        Html.fromHtml(it.colorTitleValue).toString()
                    )
                )


            }
            if (sharedPrefs.getLoginStatus())
                binding.tvExistingUser.hide()

            loadSubscriptionBottomSheetBanner(
                binding.ivSubscriptionBsBg,
                sharedPrefs.getConfigResponse()?.data?.config?.freemiumBackgroundPoster?.androidSubscriptionBackgroundPoster?.otherPackPoster
                    ?: ""
            )
        }


        this.dialog?.setOnDismissListener {

            subscriptionAnalytics.trackSubscriptionDrawerClose(source ?: "", "")
            findNavController().navigateUp()
        }
    }

    private fun myopClickAction() {
        if (NetworkUtil.checkInternetBeforeNavigate()) {
            subscriptionAnalytics.trackSubscriptionDrawerSelectMYOP(source ?: "")
            landingActivity?.currentJourneyRef = DRAWER_MYOP
            managedAppViewModel.fetchManagedAppsUrl(DRAWER_MYOP, "", source)

        }
    }

    private fun cyopClickAction() {
        if (NetworkUtil.checkInternetBeforeNavigate()) {
            if (showBothTypes)
                subscriptionAnalytics.trackSubscriptionDrawerSelectCuratedMYOP(source ?: "")
            else
                subscriptionAnalytics.trackSubscriptionDrawerSelectExplore(source ?: "")

            landingActivity?.currentJourneyRef = DRAWER_CYOP
            managedAppViewModel.fetchManagedAppsUrl(DRAWER_CYOP, "", source)
        }
    }

    private fun btnLaterClickAction() {
        subscriptionAnalytics.trackSubscrptionDrawerLater(source ?: "")
        dismiss()
    }

    private fun tvExistingUserClickAction() {
        viewModel.getPreviouslyUsedMobileNumbers()
        subscriptionAnalytics.trackExistingUserLogin(source ?: "")
    }

    private fun setClickListeners() {

        binding.selectorCreatePlanCard.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                myopClickAction()
            }
        })

        binding.selectorValuePlanCard.setOnClickListener (object : SingleClickListener() {
            override fun onClicked(v: View?) {
                cyopClickAction()
            }
        })

        binding.btnLater.setOnClickListener {
            btnLaterClickAction()
        }

        binding.tvExistingUser.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                tvExistingUserClickAction()
            }
        })

        binding.cyopSelectorCreatePlanCard.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                cyopClickAction()
            }
        })


        binding.cyopBtnLater.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                btnLaterClickAction()
            }
        })

        binding.cyopTvExistingUser.setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                tvExistingUserClickAction()
            }
        })
    }
}