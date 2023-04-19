package com.tatasky.binge.ui.features.subscription.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.database.model.SubscriptionInfoModel
import com.tatasky.binge.data.networking.models.response.Tenure
import com.tatasky.binge.databinding.FragmentSubscriptionTenureBottomSheetDialogBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.base.frameworks.extensions.*
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.PackTenureAdapter
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity.Companion.PAYMENT_ACTIVITY_REQUEST_CODE
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SubscriptionTenureBottomSheetDialogFragment :
    BaseBottomSheetDialogFragment<FragmentSubscriptionTenureBottomSheetDialogBinding, FreemiumSubscriptionViewModel>() {

    private var source: String? = null
    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var showProgressBar: Runnable

    private val args by navArgs<SubscriptionTenureBottomSheetDialogFragmentArgs>()
    private var selectedTenure: Tenure? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> {
        return FreemiumSubscriptionViewModel::class.java

    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return activity ?:findNavController().getViewModelStoreOwner(R.id.freemium_subscription)
    }

    override fun layoutId(): Int = R.layout.fragment_subscription_tenure_bottom_sheet_dialog

    private fun getSourceOrFromScreenName(): String {
        source = activity?.intent?.extras?.get(KEY_FROM_SCREEN) as String? ?: viewModel.source.takeIf {
            !it.isNullOrBlank()
        }?.also { viewModel.source = null /*After consumption, make it null again*/ }
        ?: SOURCE_HOME

        if (source.equals("home", true)) {
            val currentPack = sharedPrefs.getSubscribedPack()
            if(args.fromScreen == "PackListing") {
                return if (currentPack?.planCTADetails?.changePlanText.isNullOrBlank())
                    GO_VIP
                else
                    PARA_CHANGE_PLAN
            }
        }
        return source ?: SOURCE_HOME
    }


    override fun toBeCalledOnce() {
        activity?.let {
            when(it) {
                is LandingActivity -> it.fetchPayloadAndInitiateJuspay()
                is FreemiumSubscriptionActivity -> it.fetchPayloadAndInitiateJuspay(false)
            }
        }
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        binding.root.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().navigateUp()
            } else false
        }
        if(MINI_DRAWER == args.fromScreen){
            binding.tvGoBack.show()
        }
        binding.tvGoBack.setOnClickListener{
            findNavController().navigateUp()
        }

        viewModel.subscriptionAnalytics.trackPackTenureView()
        showProgressBar = Runnable {
            binding.progressBar.startProgressAvd(true)
        }
        expandBottomSheet(dialog)
        val tenureList = mutableListOf<Tenure>()
        args.pack?.tenure?.let { it1 -> tenureList.addAll(it1) }
        if(args.pack?.highlightedPack == true){
            paintPremiumGradient(binding.tvPackTitle,binding.tvPackTitle.paint.measureText(args.pack?.productName))
        }
        if (tenureList.isNotEmpty()) {
            val tenureAdapter = PackTenureAdapter(tenureList, viewModel)
            selectedTenure = tenureAdapter.getSelected()
            if(selectedTenure?.enable == false || selectedTenure?.currentTenure == true || selectedTenure == null){
                binding.btnProceed.disable()
            }
            val currentPack = sharedPrefs.getSubscribedPack()
            binding.tenureMessage = currentPack?.tenureMessage?:""
            if(
                currentPack != null &&
                !(currentPack.subscriptionStatus.equals(SubscriptionPackStatusEnum.DEACTIVE.status, true))
                && currentPack.freeTrialStatus != true && selectedTenure !=null
            ) {
                args.pack?.let {
                    if(sharedPrefs.getLoginStatus()) {
                        viewModel.fetchProRatedData(
                            pack = it,
                            tenure = selectedTenure!!
                        )
                    }
                }
            }
            (binding.recyclerTenureOptions.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
                false
            binding.recyclerTenureOptions.adapter = tenureAdapter
        }
        binding.pack = args.pack

        if(!sharedPrefs.getLoginStatus())
            binding.btnProceed.text = resources.getString(R.string.proceed)
        binding.btnProceed.setSingleOnClick(1000) {
            if (!sharedPrefs.getLoginStatus()) {
                selectedTenure?.let { selectedTenure ->
                    trackPackSelectionEvent(selectedTenure, true)
                }
                viewModel.getPreviouslyUsedMobileNumbers()
            } else {
                selectedTenure?.let { selectedTenure ->
                    trackPackSelectionEvent(selectedTenure)
                    activity?.startActivityForResult(
                        getPaymentActivityIntent(
                            context = requireContext(),
                            packId = args.pack?.productId,
                            selectedTenureId = selectedTenure.tenureId,
                            selectedTenureAmount = selectedTenure.offeredPriceValue,
                            isMigrated = args.pack?.migrated,
                            migratedVerbiage = args.pack?.migratedVerbiage,
                            proratedAmount = viewModel.getProRateResponse().value?.peekContent()?.data?.amount
                                ?: selectedTenure.offeredPriceValue,
                            fromScreen = getSourceOrFromScreenName(),
                            sharedPrefs = sharedPrefs
                        ).apply {
                            putExtra(
                                KEY_ACTUAL_PRORATED_AMOUNT_FROM_API,
                                //Passing actual prorated amount from API to identify modification is upgrade or downgrade
                                //If it is null then its downgrade otherwise upgrade
                                viewModel.getProRateResponse().value?.peekContent()?.data?.amount
                            )
                            putExtra(KEY_PACK_PRICE, args.pack?.amountValue)
                            putExtra(KEY_APPSFLYER_SOURCE, source)
                            putExtra(KEY_PACK_NAME, args.pack?.productName)
                            putExtra(
                                KEY_SELECTED_TENURE_PACK_PRICE,
                                selectedTenure.offeredPriceValue
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                                selectedTenure.tenureDurationInDaysWithDSuffix
                            )
                            putExtra(KEY_SELECTED_TENURE_TYPE, selectedTenure.tenureType)
                        }, PAYMENT_ACTIVITY_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun trackPackSelectionEvent(selectedTenure: Tenure, trackOnlyInAppsFlyer: Boolean = false) {
        val currentPack = sharedPrefs.getSubscribedPack()
        subscriptionAnalytics.trackPackSelection(
            args.pack?.productName ?: "",
            if(currentPack == null) FRESH else UPGRADE,
            args.pack?.amountValue ?: "",
            packPaymentMethod = "",
            packDuration = selectedTenure.tenureDuration ?: "",
            packChangePlan = if(currentPack?.productId != args.pack?.productId) YES else NO,
            packChangeTenure = if(currentPack?.productId != this.selectedTenure?.tenureId) YES else NO,
            packPayableAmount = viewModel.getProRateResponse().value?.peekContent()?.data?.amount
                ?: selectedTenure.offeredPriceValue ?:"",
            selectedTenure.tenureDurationInDaysWithDSuffix ?: "",
            args.pack?.productId ?: "",
            viewModel.sharedPref.getLoginStatus(),
            trackOnlyInAppsFlyer,
            selectedTenure.offeredPriceValue ?: "",
            selectedTenure.tenureType ?: "",
            getSourceOrFromScreenName(),
            PACK_TYPE_PAID,
            null,
            currentPack?.productName,
            currentPack?.let { if (true == it.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID },
            currentPack?.amountValue,
            currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
            if (sharedPrefs.getLoginStatus())
                PaymentUtility.isFirstPaidPack(
                    sharedPrefs.getSubscribedPack(),
                    sharedPrefs.getPreviousSubscribedPack(),
                    sharedPrefs.getSubscribedPack()?.firstPaidPackSubscriptionDate
                )
            else
                null
        )
    }

    private fun dismissSubscriptionDialog(){
        if (activity is LandingActivity) {
            (activity as LandingActivity).subscriptionBottomSheetDialog?.dismiss()
        } else {
            dialog?.dismiss()
            activity?.onBackPressed()
        }
    }

    override fun setObserver() {
        viewModel.previouslyUsedMobileNumberResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                val subscriptionInfo = SubscriptionInfoModel(
                    packId = args.pack?.productId ?:"",
                    selectedTenureId = selectedTenure?.tenureId ?:"",
                    selectedTenureAmount = selectedTenure?.offeredPriceValue?:"",
                    proratedAmount = viewModel.getProRateResponse().value?.peekContent()?.data?.amount
                        ?: selectedTenure?.offeredPriceValue ?:"",
                    selectedTenure = selectedTenure,
                    selectedPack = args.pack
                )
                dismiss()
                findNavController().navigateSafe(
                    SubscriptionTenureBottomSheetDialogFragmentDirections.actionSubscriptionTenureBottomSheetDialogFragmentToLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = response.data?.mobileNUmberList?.toTypedArray(),
                        loginSource = SOURCE_SUBSCRIPTION,
                        pack = args.pack,
                        selectedTenure = selectedTenure,
                        subscriptionInfo = subscriptionInfo
                    )
                )
            }
        }

        viewModel.previouslyUsedMobileNumberError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                val subscriptionInfo = SubscriptionInfoModel(
                    packId = args.pack?.productId ?:"",
                    selectedTenureId = selectedTenure?.tenureId ?:"",
                    selectedTenureAmount = selectedTenure?.offeredPriceValue?:"",
                    proratedAmount = viewModel.getProRateResponse().value?.peekContent()?.data?.amount
                        ?: selectedTenure?.offeredPriceValue ?:"",
                    selectedTenure = selectedTenure,
                    selectedPack = args.pack
                )
                dismiss()
                findNavController().navigateSafe(
                    SubscriptionTenureBottomSheetDialogFragmentDirections.actionSubscriptionTenureBottomSheetDialogFragmentToLoginBottomSheetDialogFragment(
                        isParentalPinSetupRequested = false,
                        isParentalPinVerificaitionRequested = false,
                        isLoggedIn = false,
                        previouslyUsedMobileNumbersList = null,
                        loginSource = SOURCE_SUBSCRIPTION,
                        pack = args.pack,
                        selectedTenure = selectedTenure,
                        subscriptionInfo = subscriptionInfo
                    )
                )
            }
        }

        viewModel.progressListener.observe(viewLifecycleOwner) {
            if(it) {
                dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                mHandler.postDelayed(
                    showProgressBar,loaderDelayTime
                )

            } else {
                mHandler.removeCallbacks(showProgressBar)
                binding.progressBar.startAvd(false)
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        viewModel.getSelectedTenure().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                selectedTenure = it
                if(selectedTenure?.enable == false){
                    binding.btnProceed.disable()
                } else {
                    binding.btnProceed.enable()
                }
                val currentPack = sharedPrefs.getSubscribedPack()
                if(currentPack != null && !(currentPack.subscriptionStatus.equals(
                        SubscriptionPackStatusEnum.DEACTIVE.status,
                        true
                    )) && currentPack.freeTrialStatus != true
                ) {
                    args.pack?.let { it1 ->
                        if(sharedPrefs.getLoginStatus()) {
                            viewModel.fetchProRatedData(
                                pack = it1,
                                tenure = selectedTenure!!
                            )
                        }
                    }
                }
            }
        })

        viewModel.getProRateResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { proRatedResponse ->
                if (proRatedResponse.data == null || (proRatedResponse.data?.currentBalance == null && proRatedResponse.data?.payableAmount == null)) {
                    binding.priceBreakup.proRatedRoot.hide()
                    binding.tvTenureDisc.show()
                } else {
                    if (proRatedResponse.data?.payableAmount?.toDoubleOrNull() ?: 0.0 != selectedTenure?.offeredPriceValue?.toDoubleOrNull() ?: 0.0) {
                        binding.priceBreakup.proRatedRoot.show()
                        dialog?.let{
                            val standardBottomSheetBehavior = (dialog as BottomSheetDialog).behavior
                            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            standardBottomSheetBehavior.skipCollapsed = true
                        }
                        binding.tvTenureDisc.hide()
                        binding.priceBreakup.proRatedResponse = proRatedResponse
                    }
                }
            }
        })
    }

}
