package com.tatasky.binge.ui.features.subscription.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.COMMON_ERROR_MSG
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.CommonSubscriptionDetailsModel
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.data.networking.models.response.WalletBalanceResponse
import com.tatasky.binge.databinding.FragmentSubscriptionSummaryBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.clearError
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.ProviderAdapter
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.*
import java.util.*
import javax.inject.Inject
import kotlin.math.absoluteValue

class SubscriptionSummaryFragment :
    BaseFragment<FragmentSubscriptionSummaryBinding, SubscriptionViewModel>() {
    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    var isModification = true
    var rsToPay = 0
    var recommendedRsToPay = 0
    var minimumRsToPay = 0
    var walletBalanceSufficient = true
    override fun getViewModelClass(): Class<SubscriptionViewModel> =
        SubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_subscription_summary

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.subscription)

    override fun setObserver() {
        viewModel.getPurchasedPackResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                when (response.code) {
                    CODE_SUCCESS -> {
                        if(isModification && true == viewModel.previousPackType?.equals("Free", true) && true == response.data?.packType?.equals("paid" , true)){
                            subscriptionAnalytics.trackPurchaseEvent()
                        }
//                        if(!isModification && true == response.data?.packType?.equals("paid", true)){
//                            subscriptionAnalytics.trackSubscribeEvent()
//                        }
                        viewModel.saveSubscription(response.data)

                        /*subscriptionAnalytics.trackSubscribeSuccess(
                            viewModel.selectedPack?.packType ?: "PAID",
                            activity?.intent?.extras?.getString("fromScreen")?: SOURCE_HOME,
                            viewModel.selectedPack?.packName ?: "",
                            viewModel.selectedPack?.packPrice ?: "",
                            activity?.intent?.extras?.getBoolean("isFromNudge")?: false,
                            response.data?.isFDRRaised == true,
                            if(response.data?.isFDRRaised == true) viewModel.getCurrentSubscription()?.nudges?.paidPackSelectionNudge?.let { (it.totalFreeTrialDuration - it.availableDays) + 1}?.toString()?:"" else "",
                            if(isModification) modType else "",
                            "",
                            ""
                        )*/
                        /*if(isModification){
                            subscriptionAnalytics.trackModifyPackSuccess(activity?.intent?.extras?.getString("fromScreen") ?: SOURCE_HOME, viewModel.selectedPack?.packName ?: "", viewModel.selectedPack?.packPrice ?: "", modType)
                        }*/
                        when {
                            !isModification && response.data?.packType.equals("free", true) -> {
                                subscriptionAnalytics.trackStartTrialEvent()
                                handleAfterPackPurchaseNavigation()
                            }
                            response.data?.isFDRRaised == true -> {
                                handleAfterPackPurchaseNavigation()
                            }
                            response.data?.isDowngrade == true -> {
                                showDialog(
                                    DialogModel(
                                        false,
                                        R.drawable.ic_success_tick,
                                        getString(R.string.request_success),
                                        getString(R.string.proceed),
                                        null,
                                        response.data?.modificationMessage
                                    ), object : CommonDialogEventListener {
                                        override fun onPrimaryButtonClick() {
                                            hideDialog()
                                            handleAfterPackPurchaseNavigation()
                                        }

                                        override fun onSecondaryButtonClick() {
                                        }

                                        override fun onCloseButtonClick() {
                                        }
                                    })
                            }
                            else -> {
                                showDialog(
                                    DialogModel(
                                        false,
                                        R.drawable.ic_success_tick,
                                        getString(R.string.payment_success),
                                        getString(R.string.proceed),
                                        null,
                                        response.data?.modificationMessage
                                    ), object : CommonDialogEventListener {
                                        override fun onPrimaryButtonClick() {
                                            hideDialog()
                                            handleAfterPackPurchaseNavigation()
                                        }

                                        override fun onSecondaryButtonClick() {
                                        }

                                        override fun onCloseButtonClick() {
                                        }
                                    })
                            }
                        }
                    }
                    RESPONSE_CODE_LOW_BALANCE_ERROR -> {
//                        subscriptionAnalytics.trackSubscribeFailure(
//                            response.message?:"",
//                            response.data?.packName ?: viewModel.selectedPack?.packName ?: "",
//                            response.data?.packType ?: viewModel.selectedPack?.packType ?: "PAID",
//                            "",
//                            ""
//                        )
                        showDialog(DialogModel(
                            false,
                            R.drawable.ic_subscription_error,
                            getString(R.string.low_balance),
                            getString(R.string.recharge),
                            getString(R.string.skip),
                            response.message
                        ),
                            object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    viewModel.startRecharge()
                                }

                                override fun onSecondaryButtonClick() {
                                    hideDialog()
                                    if (!viewModel.isFromLogin) {
                                        activity?.finish()
                                    } else {
                                        startHomeScreen(activity)
                                    }
                                }

                                override fun onCloseButtonClick() {
                                    hideDialog()
                                }
                            })
                    }
                    else -> {
                        var title = COMMON_ERROR_MSG
//                        subscriptionAnalytics.trackSubscribeFailure(
//                            response.message ?: title,
//                            response.data?.packName ?: viewModel.selectedPack?.packName ?: "",
//                            response.data?.packType ?: viewModel.selectedPack?.packType ?: "PAID",
//                        "",
//                            "")
                        if (response.code == RESPONSE_CODE_DOWNGRADE_ERROR || response.code == RESPONSE_CODE_DOWNGRADE_ERROR2) {
                            title = getString(R.string.cannot_downgrade)
                        } else if(response.code == RESPONSE_CODE_DOWNGRADE_ERROR3){
                            title = getString(R.string.already_downgraded)
                        }
                        showDialog(
                            DialogModel(
                                false,
                                R.drawable.ic_subscription_error,
                                title,
                                getString(R.string.ok),
                                null,
                                response.message
                            ), object : CommonDialogEventListener {
                                override fun onPrimaryButtonClick() {
                                    hideDialog()
                                    if (findNavController().graph.startDestination == R.id.currentSubscriptionFragment)
                                        findNavController().popBackStack(R.id.currentSubscriptionFragment, false)
                                    else if (findNavController().graph.startDestination == R.id.rechargeSubscriptionFragment)
                                        findNavController().popBackStack(R.id.rechargeSubscriptionFragment, false)
                                    else
                                        if (requireActivity().isTaskRoot) {
                                            startHomeScreen(activity)
                                        } else {
                                            activity?.finish()
                                        }
                                }

                                override fun onSecondaryButtonClick() {
                                }

                                override fun onCloseButtonClick() {
                                }
                            })
                    }
                }
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateUp()
            }
        })
        viewModel.getWalletBalance().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { walletBalance ->
                when (walletBalance.code) {
                    CODE_SUCCESS -> {
                        binding.groupViews.show()
                        binding.btnSkip.setOnClickListener {
                            if (!viewModel.isFromLogin) {
                                activity?.finish()
                            } else {
                                startHomeScreen(activity)
                            }
                            subscriptionAnalytics.trackAddPackSkip()
                        }
                        if (viewModel.selectedPack?.packType.equals("free", true) || (viewModel.getCurrentSubscription()?.isDummyUser == true && true == viewModel.getCurrentSubscription()?.subscriptionDetailInfo?.bingeAccountStatus?.equals(SubscriptionPackStatusEnum.ACTIVE.status, true))) {
                            binding.btnSkip.invisible()
                            binding.btnSkip.setOnClickListener(null)
                        }
                        else
                            binding.btnSkip.show()

                        rsToPay = (walletBalance.data?.totalAmount ?: "0").toFloat().toInt()
                        recommendedRsToPay = ((walletBalance.data?.recommendedRechargeAmount ?: "0").toFloatOrNull()?:0f).toInt()
                        minimumRsToPay = ((walletBalance.data?.minimumRechargeAmount ?: "50").toFloatOrNull()?:0f).toInt()
                        val selectedPackPrice = viewModel.selectedPack?.getFormattedPrice()
                        if (viewModel.selectedPack!!.isPaid && ((viewModel.getCurrentSubscription()?.isDummyUser == false || viewModel.getCurrentSubscription()?.isDummyUser == null) || (viewModel.getCurrentSubscription()?.isDummyUser == true && viewModel.getCurrentSubscription()?.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.WRITTEN_OFF.status, true)))) {
                            if (rsToPay == selectedPackPrice) {
                                // Free to Paid
                                binding.rowToTotalPriceDetails.infoIcon.setOnClickListener {
                                    showDialog(DialogModel(false, R.drawable.ic_info1, walletBalance.data?.message?:"", getString(R.string.ok), null), object : CommonDialogEventListener{
                                        override fun onPrimaryButtonClick() {
                                            hideDialog()
                                        }

                                        override fun onSecondaryButtonClick() {
                                            hideDialog()
                                        }

                                        override fun onCloseButtonClick() {
                                            hideDialog()
                                        }
                                    })
                                }
                                walletBalance.data?.retrofitAmount?.toFloatOrNull()?.takeIf { it != 0f }?.let {
                                    binding.rowCurrentPackPriceDetails.nPrice = it.toInt().absoluteValue.toString()
                                    binding.rowCurrentPackPriceDetails.hide = false
                                    binding.groupTotalPrice.show()
                                    binding.rowToTotalPriceDetails.infoIcon.show()
                                }?: binding.run {
                                    rowCurrentPackPriceDetails.hide = true
                                    groupTotalPrice.hide()
                                }
                                binding.rowToTotalPriceDetails.price = rsToPay.toString()
                                binding.rowToBuyPackPriceDetails.price = selectedPackPrice.toString()
                                if (rsToPay <= (walletBalance.data?.balanceQueryRespDTO?.balance ?: "0").toFloat().toInt()) {
                                    //binding.groupTotalPrice.show()
                                    walletBalanceSufficientMode()
                                } else {
                                    walletBalanceInsufficientMode()
                                    binding.ivInfo.setOnClickListener {
                                        showRecommendationBreakDownDialog(viewModel.selectedPack?.packName ?: "", walletBalance)
                                    }
                                    binding.etRecharge.setText(recommendedRsToPay.toString())
                                }
                            } else if (rsToPay != 0 && rsToPay > 0) {
                                // Upgrade Case
                                binding.selectedPackCard.proRataValue = maxOf(0, rsToPay)
                                binding.rowToTotalPriceDetails.infoIcon.setOnClickListener {
                                    showDialog(DialogModel(false, R.drawable.ic_info1, getString(R.string.subscription_change), getString(R.string.ok), null, walletBalance.data?.message?:""), object : CommonDialogEventListener{
                                        override fun onPrimaryButtonClick() {
                                            hideDialog()
                                        }

                                        override fun onSecondaryButtonClick() {
                                            hideDialog()
                                        }

                                        override fun onCloseButtonClick() {
                                            hideDialog()
                                        }
                                    })
                                }
                                walletBalance.data?.retrofitAmount?.toFloatOrNull()?.takeIf { it != 0f }?.let {
                                    binding.rowCurrentPackPriceDetails.nPrice = it.toInt().absoluteValue.toString()
                                    binding.rowCurrentPackPriceDetails.hide = false
                                    binding.groupTotalPrice.show()
                                    binding.rowToTotalPriceDetails.infoIcon.show()
                                }
                                binding.rowToTotalPriceDetails.price = rsToPay.toString()
                                binding.rowToBuyPackPriceDetails.price = selectedPackPrice.toString()
                                if (rsToPay <= (walletBalance.data?.balanceQueryRespDTO?.balance ?: "0").toFloat().toInt()) {
                                    //binding.groupTotalPrice.show()
                                    walletBalanceSufficientMode()
                                } else {
                                    walletBalanceInsufficientMode()
                                    binding.ivInfo.setOnClickListener {
                                        showRecommendationBreakDownDialog(viewModel.selectedPack?.packName ?: "", walletBalance)
                                    }
                                    binding.etRecharge.setText(recommendedRsToPay.toString())
                                }
                            } else if (rsToPay == 0) {
                                // Downgrade Case
                                binding.groupTotalPrice.hide()
                                walletBalanceSufficientMode()
                            }
                        } else {
                            // Free Trial Case
                            binding.tvMsg.show()
                        }
                        binding.tvMsg.text = viewModel.selectedPack?.footerVerbiage
                    }
                    else -> {
                        onError(ErrorModel(walletBalance.code, walletBalance.message))
                    }
                }
            }
        })

        viewModel.rechargeResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.data?.let {
                if (it.rechargeUrl.isNullOrBlank()) {
                    onError(ErrorModel(message = "Recharge Cannot be processed please try after some time"))
                    return@Observer
                }
                try {
                    launchRechargeActivity(this, Uri.parse(it.rechargeUrl))
                } catch (e: Exception) {
                    onError(ErrorModel(message = "Recharge Cannot be processed please try after some time"))
                }
            }
        })
    }

    private fun walletBalanceInsufficientMode() {
        binding.tvMsg.hide()
        binding.clRecharge.show()
        walletBalanceSufficient = false
        binding.btnProceed.text = getString(R.string.recharge)
    }

    private fun walletBalanceSufficientMode() {
        binding.clRecharge.hide()
        binding.tvMsg.show()
        walletBalanceSufficient = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selectedPack", Gson().toJson(viewModel.selectedPack))
    }
    var modType = UPGRADE
    override fun toBeCalledOnce() {
        binding.lifecycleOwner = viewLifecycleOwner
        if (!viewModel.isFromLogin)
            binding.llPageIndicator.invisible()
        else
            binding.llPageIndicator.show()
        viewModel.fetchBalance()
        binding.viewModel = viewModel
        binding.selectedPack = viewModel.selectedPack?.apply { subscriptionDetailInfo = CommonSubscriptionDetailsModel().apply { migrated = true } }
        if (true == viewModel.selectedPack?.eligibleFirestick) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_eligible_not_availed)
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }

        binding.selectedPackCard.recyclerPacks.adapter =
            ProviderAdapter(viewModel.selectedPack?.appList ?: emptyList())
        //Prevent leading zeros in recharge edit text
        binding.etRecharge.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length > 1 && s.toString().startsWith("0")) {
                    s!!.delete(0, 1)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilRecharge.clearError()
            }
        })

        binding.btnProceed.setOnClickListener {
            if (!walletBalanceSufficient) {
                val amountToRecharge =
                    (binding.etRecharge.text.toString().toFloatOrNull() ?: 0f).toInt()
                if (amountToRecharge < minimumRsToPay){
                    binding.tilRecharge.error = getString(R.string.recharge_error_min, minimumRsToPay)
                    return@setOnClickListener
                }
                else if(amountToRecharge > viewModel.getMaximumRechargeAmount()) {
                    binding.tilRecharge.error =
                        getString(R.string.recharge_error_max, viewModel.getMaximumRechargeAmount())
                    return@setOnClickListener
                } else {
                    viewModel.startRecharge(amountToRecharge.toString())
                }
            } else {
                val currentPack = viewModel.getCurrentSubscription()
                viewModel.previousPackType = currentPack?.packType
                if (currentPack != null) {
                    val currPrice =
                        currentPack.packPrice?.toFloatOrNull() ?: 0f
                    val selectedPrice = viewModel.selectedPack?.packPrice?.toFloatOrNull() ?: 0f
                    modType = if (selectedPrice - currPrice == 0f) {
                        RENEW
                    } else if (selectedPrice - currPrice > 0f) {
                        UPGRADE
                    } else {
                        DOWNGRADE
                    }
                }
                if (currentPack == null) {
                    isModification = false
                    viewModel.requestSubscriptionCreation()
                } else if ((currentPack.isCancelled && currentPack.expiredWithinSixtyDays && (currentPack.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.DEACTIVE.status, true) || currentPack.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.WRITTEN_OFF.status, true))) || (currentPack.isCancelled && currentPack.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.WRITTEN_OFF.status, true) && currentPack.isFDRRaised == true && currentPack.isDummyUser == true)) {
                    viewModel.requestSubscriptionReactivation()
                } else {
                    if(currentPack.isCancelled)
                        viewModel.subscriptionAnalytics.trackSubscriptionRevoke(activity?.intent?.extras?.getString("fromScreen")?: SOURCE_ACCOUNT)
                    viewModel.requestSubscriptionModification()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.getString("selectedPack")?.let {
            viewModel.selectedPack = Gson().fromJson(it, PartnerPacks::class.java)
            toBeCalledOnce()
        }
        getDateRange()
    }

    private fun getDateRange() {
        val calendar = Calendar.getInstance()
        val format =
            if (!viewModel.selectedPack!!.isPaid)
                "dd/MM/yyyy"
            else
                "dd MMM yyyy"
        val startDate = convertPackDate(
            viewModel.selectedPack!!.packCreatedDate,
            SERVER_DATE_TIME_FORMAT, format
        )
        val endDate = convertPackDate(
            viewModel.selectedPack!!.expirationDate,
            SERVER_DATE_TIME_FORMAT, format
        )
        val endDateProRata = convertPackDate(
            viewModel.selectedPack!!.expirationDate,
            SERVER_DATE_TIME_FORMAT, "dd MMM"
        )
        val expiryDate = if (!viewModel.selectedPack!!.isPaid) {
            getString(R.string.free_trial_expiry_date, endDate)
        } else {
            binding.selectedPackCard.tvTotalApps.show()
            getString(R.string.billing_period, getString(R.string.date_range, startDate, endDate))
        }

//        val spannableExpiredOn = buildSpannedString {
//            append(expiryDate).apply { setSpan(CalligraphyTypefaceSpan(TypefaceUtils.load(resources.assets, getString(R.string.medium_font))), expiryDate.indexOf(endDate), expiryDate.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE) }
//        }
        binding.selectedPackCard.tvRenewalCycle.setText(expiryDate, TextView.BufferType.SPANNABLE)
    }

    private fun showRecommendationBreakDownDialog(
        packName: String,
        walletBalanceResponse: WalletBalanceResponse
    ) {
        val recommendationDialog = RecommendationDialog.newInstance(packName, walletBalanceResponse)
        recommendationDialog.show(parentFragmentManager, DIALOG_TAG)
    }

    override fun onError(errorModel: ErrorModel) {
        if (errorModel.statusCode == RESPONSE_CODE_SUCCESS || errorModel.statusCode == CODE_SUCCESS) {
            showDialog(
                DialogModel(false, null, errorModel.message, "Ok", null),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        findNavController().navigateUp()
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        } else {
            showDialog(
                DialogModel(false,
                    R.drawable.ic_subscription_error,
                    COMMON_ERROR_TITLE,
                    "Ok", null,
                    com.tatasky.binge.utils.COMMON_ERROR_MSG, errorModel.statusCode),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        findNavController().navigateUp()
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                        binding.groupViews.hide()
                        binding.clRecharge.hide()
                        viewModel.fetchBalance()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            else {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.payment_failure), getString(R.string.ok), null, null), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                        binding.groupViews.hide()
                        binding.clRecharge.hide()
                        viewModel.fetchBalance()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
        }
    }

    private fun handleAfterPackPurchaseNavigation() {
        if (!activity?.intent?.extras?.getString("selectedAppId").isNullOrBlank()) {
            activity?.setResult(Activity.RESULT_OK)
            activity?.finish()
        } else if (viewModel.isFromLogin || viewModel.getCurrentSubscription()?.isFDRRaised == true) {
            startHomeScreen(activity)
        } else {
            activity?.let {
                startActivity(
                    getSubscriptionActivityIntent(it, fromLogin = false, selectedAppId = null, fromScreen = SOURCE_HOME).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                it.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
            }
        }
    }
}