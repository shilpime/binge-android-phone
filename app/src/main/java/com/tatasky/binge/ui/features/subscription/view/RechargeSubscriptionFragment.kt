package com.tatasky.binge.ui.features.subscription.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_ACCOUNT
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.data.networking.models.response.PurchasePackResponse
import com.tatasky.binge.data.networking.models.response.WalletBalanceResponse
import com.tatasky.binge.databinding.FragmentRechargeSubscriptionBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.clearError
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.ChannelsCountAdapter
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription.adapter.ProviderAdapter
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.*
import io.github.inflationx.calligraphy3.CalligraphyTypefaceSpan
import io.github.inflationx.calligraphy3.TypefaceUtils
import java.util.*
import javax.inject.Inject

class RechargeSubscriptionFragment : BaseFragment<FragmentRechargeSubscriptionBinding, SubscriptionViewModel>() {

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    private var rsToPay = 50
    private var recommendedRsToPay = 0
    private var recharged = false
    override fun getViewModelClass(): Class<SubscriptionViewModel> =
        SubscriptionViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = 500
        }
        reenterTransition = backward
    }

    override fun layoutId(): Int = R.layout.fragment_recharge_subscription

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.subscription)

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!findNavController().navigateUp())
                    startHomeScreen(activity)
            }
        })
        viewModel.getCancellationResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.fetchCurrentSubscriptionAfterCancellation()
            }
        })

        viewModel.getCurrentSubscriptionResponseAfterCancellation().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionSummaryFragment())
            }
        })
        viewModel.getWalletBalance().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { walletBalance ->
                when (walletBalance.code) {
                    CODE_SUCCESS -> {
                        val currentPack = viewModel.getCurrentSubscription()
                        if ((currentPack == null || currentPack.doNotConsiderThePack)) {
                            findNavController().graph.startDestination = R.id.subscriptionFragment
                            findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment(),
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.rechargeSubscriptionFragment, true).build()
                            )
                            return@Observer
                        }
                        viewModel.selectedPack = currentPack
                        when {
                            !currentPack.isPaid && currentPack.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.ACTIVE.status, true)->{
                                subscriptionDummyMode(currentPack)
                                binding.clRecharge.show()
                                binding.groupProceed.hide()
                            }
                            currentPack.isInactive -> {
                                subscriptionExpiredMode(currentPack)
                                if ((walletBalance.data?.balanceQueryRespDTO?.balance?.toFloatOrNull() ?: 0f) >= (currentPack.packPrice?.toFloatOrNull() ?: 0f)) {
                                    binding.groupProceed.show()
                                    binding.clRecharge.hide()
                                    binding.btnProceed.setOnClickListener {
                                        viewModel.fetchPackList()
                                    }
                                    binding.btnSkip.setOnClickListener {
                                        startHomeScreen(activity)
                                    }
                                } else {
                                    binding.groupProceed.hide()
                                    binding.clRecharge.show()
                                }
                            }
                            currentPack.isCancelled -> {
                                subscriptionCancelledMode(currentPack)
                                binding.clRecharge.show()
                                binding.groupProceed.hide()
                            }
                            else -> {
                                subscriptionActiveMode(currentPack)
                                if(viewModel.isFromLogin && recharged){
                                    binding.btnStartWatching.show()
                                    binding.clRecharge.hide()
                                } else {
                                    binding.btnStartWatching.hide()
                                    binding.clRecharge.show()
                                }
                                binding.groupProceed.hide()
                            }
                        }
                        binding.tvSubLandingTitle.show()
                        rsToPay = ((walletBalance.data?.minimumRechargeAmount ?: "50").toFloatOrNull()?:0f).toInt()
                        recommendedRsToPay = ((walletBalance.data?.recommendedRechargeAmount ?: "0").toFloatOrNull()?:0f).toInt()
                        binding.ivInfo.setOnClickListener {
                            showDialog(DialogModel(false, R.drawable.ic_info1, getString(R.string.recommended_recharge_msg) , getString(R.string.ok), null), object : CommonDialogEventListener{
                                override fun onPrimaryButtonClick() {
                                    hideDialog()
                                }

                                override fun onSecondaryButtonClick() {
                                }

                                override fun onCloseButtonClick() {
                                }
                            })
                        }
                        binding.etRecharge.setText(recommendedRsToPay.toString())
                    }
                    else -> {
                        onError(ErrorModel(walletBalance.code, walletBalance.message))
                    }
                }
            }
        })

        viewModel.getPacksResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val currentPack = viewModel.getCurrentSubscription()!!
                val packToSelect = it.data?.packsList?.filter { it.packId == currentPack.packId || it.alternatePaidPackId == viewModel.getCurrentSubscription()?.packId }
                if (packToSelect.isNullOrEmpty()) {
                    findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment())
                } else {
                    viewModel.dropPack(currentPack.packId!!)
                    viewModel.addNewPack(packToSelect[0].packId!!)
                    if (!currentPack.isCancelled && currentPack.expiredWithinSixtyDays && currentPack.isDummyUser == false && currentPack.subscriptionDetailInfo?.bingeAccountStatus.equals(SubscriptionPackStatusEnum.DEACTIVE.status, true)) {
                        viewModel.requestSubscriptionCancellation(Cancellation(bingeCancel = true, primeCancel = false))
                    } else {
                        findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionSummaryFragment())
                    }
                }
            }
        })

        viewModel.rechargeResponse().observe(this, Observer {
            it.getContentIfNotHandled()?.data?.let {
                if (it.rechargeUrl.isNullOrBlank()) {
                    onError(ErrorModel(message = "Recharge cannot be processed now please try after some time"))
                    return@Observer
                }
                try {
                    launchRechargeActivity(this, Uri.parse(it.rechargeUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    //refresh Account page for subscription detail
                    setPackInfo()
                }
            }
        })
        viewModel.getCancellationRevokeResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.subscriptionAnalytics.trackSubscriptionRevoke(activity?.intent?.extras?.getString("fromScreen")?: SOURCE_ACCOUNT)
                showDialog(DialogModel(false, R.drawable.ic_success_tick, it.data?.displayMessage?.title?:getString(R.string.revoke_success_title), getString(R.string.proceed), null,it.data?.displayMessage?.message), object : CommonDialogEventListener {
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
        })
    }

    override fun toBeCalledOnce() {
        subscriptionAnalytics.trackViewRechargeScreen()
        viewModel.fetchCurrentSubscription()
    }

    private fun setPackInfo() {
        if (activity?.intent?.extras?.getBoolean("firstTimeLogin") == true) {
            viewModel.firstTimeLogin = true
        }
        if (activity?.intent?.extras?.getBoolean("fromLogin") == true ||
            activity?.intent?.extras?.getString("fromScreen") == SOURCE_NOTIFICATION
        ) {
            binding.llPageIndicator.show()
            viewModel.isFromLogin = true
        } else {
            binding.llPageIndicator.hide()
        }
        binding.firstTimeLogin = viewModel.firstTimeLogin
        binding.btnStartWatching.setOnClickListener {
            startHomeScreen(activity)
        }
        val currentPack = viewModel.getCurrentSubscription()
        viewModel.selectedPack = currentPack
        if (currentPack == null || currentPack.doNotConsiderThePack) {
            findNavController().graph.startDestination = R.id.subscriptionFragment
            findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment(), NavOptions.Builder().setPopUpTo(R.id.rechargeSubscriptionFragment, true).build())
            return
        } else {
            viewModel.fetchBalance()
        }
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

        binding.btnRecharge.setOnClickListener {
            val amount = (binding.etRecharge.text.toString().toFloatOrNull() ?: 0f).toInt()
            if (amount < rsToPay){
                binding.tilRecharge.error = getString(R.string.recharge_error_min, rsToPay)
                return@setOnClickListener
            }
            else if(amount > viewModel.getMaximumRechargeAmount()) {
                binding.tilRecharge.error = getString(R.string.recharge_error_max, viewModel.getMaximumRechargeAmount())
                return@setOnClickListener
            } else {
                viewModel.startRecharge(amount.toString())
            }
        }
        binding.btnCancel.setOnClickListener {
            if(viewModel.isFromLogin)
                startHomeScreen(activity)
            else
                activity?.finish()
        }
    }

    private fun subscriptionCancelledMode(currentPack: PartnerPacks) {
        if(currentPack.isCombo){
            subscriptionCancelledModeNew(currentPack)
            return
        }
        binding.gpActiveSubscriptionNew.hide()
        binding.gpActiveSubscription.show()
        binding.selectedDummyPackCard.root.hide()
        binding.freeTrialExpiryMsg.hide()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        binding.tvRechargeMsg.hide()
        binding.selectedPackCard.tvTag.hide()
        if (true == currentPack.cancelBtn) {
            binding.selectedPackCard.btnModifySubscription.show()
        } else {
            binding.selectedPackCard.btnModifySubscription.hide()
        }
        if (currentPack.isInactive) {
            val color = ContextCompat.getColor(requireContext(), R.color.darkError)
            binding.selectedPackCard.tvRenewalCycle.setTextColor(color)
        }
        binding.selectedPackCard.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            beginRevokeProcess()
        }
        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCard.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        }else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    private fun subscriptionActiveMode(currentPack: PartnerPacks) {
        if(currentPack.isCombo){
            subscriptionActiveModeNew(currentPack)
            return
        }
        binding.gpActiveSubscriptionNew.hide()
        binding.gpActiveSubscription.show()
        binding.selectedDummyPackCard.root.hide()
        binding.freeTrialExpiryMsg.hide()
        binding.selectedPackCard.tvTag.hide()
        binding.tvRechargeMsg.hide()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCard.btnModifySubscription.show()
        } else {
            binding.selectedPackCard.btnModifySubscription.hide()
        }
//        val date = convertPackDate(currentPack.expirationDate, SERVER_DATE_TIME_FORMAT, "dd/MM/yyyy")
//        val expireOn = if (!currentPack.isPaid) {
//            String.format(getString(R.string.free_trial_expiry_date), date)
//        } else {
//            String.format(getString(R.string.expire_on), date)
//        }
//        val spannableExpiredOn = buildSpannedString {
//            append(expireOn).apply { setSpan(CalligraphyTypefaceSpan(TypefaceUtils.load(resources.assets, getString(R.string.medium_font))), expireOn.indexOf(date), expireOn.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE) }
//        }
        binding.selectedPackCard.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCard.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        }else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    private fun subscriptionDummyMode(currentPack: PartnerPacks){
        binding.selectedDummyPackCard.tvTag.hide()
        binding.selectedDummyPackCard.root.show()
        binding.freeTrialExpiryMsg.show()
        binding.gpActiveSubscription.show()
        binding.selectedPackCard.root.hide()
        binding.selectedPack = currentPack
        binding.freeTrialExpiryMsg.text = currentPack.verbiage?.expiryMessage
        binding.selectedDummyPackCard.tvRenewalCycle.text = currentPack.verbiage?.subsTitle
        binding.selectedDummyPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedDummyPackCard.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                if(currentPack.mobileUpgradable) {
                    findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToUpgradeFragment())
                } else {
                    findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment())
                }
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
    }

    private fun subscriptionExpiredMode(currentPack: PartnerPacks) {
        if(currentPack.isCombo){
            subscriptionExpiredModeNew(currentPack)
            return
        }
        binding.gpActiveSubscriptionNew.hide()
        binding.gpActiveSubscription.show()
        binding.selectedDummyPackCard.root.hide()
        binding.freeTrialExpiryMsg.hide()
        binding.selectedPackCard.tvTag.hide()
        binding.tvRechargeMsg.show()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        if(currentPack.isCancelled){
            binding.selectedPackCard.tvTag.text = CANCELLED
            binding.selectedPackCard.tvTag.show()
        }
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCard.btnModifySubscription.show()
        } else {
            binding.selectedPackCard.btnModifySubscription.hide()
        }
        val color = ContextCompat.getColor(requireContext(), R.color.darkError)
        binding.selectedPackCard.tvRenewalCycle.setTextColor(color)
        binding.selectedPackCard.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCard.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCard.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCard.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        }else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    private fun noSubscriptionMode() {
        binding.gpNoSubscription.show()
        binding.gpActiveSubscription.hide()
        binding.selectedPackCard.tvTag.hide()
    }

    private fun beginRevokeProcess() {
        showDialog(DialogModel(false, R.drawable.ic_my_subscription, viewModel.getCurrentSubscription()?.verbiage?.revokeSubs?.title?:getString(R.string.resume_subscription_title), "Yes", getString(R.string.cancel)), object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                viewModel.requestSubscriptionRevokeCancellation()
                hideDialog()
            }

            override fun onSecondaryButtonClick() {
                hideDialog()
                viewModel.subscriptionAnalytics.trackSubscriptionRevokeSkip()
            }

            override fun onCloseButtonClick() {
                hideDialog()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        recharged = true
        viewModel.fetchBalance()
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
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
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
        }
    }

    fun closeRechargeScreen() {
        if (!findNavController().navigateUp())
            if (requireActivity().isTaskRoot) {
                startHomeScreen(activity)
            } else {
                activity?.finish()
            }
    }

    private fun handleModifyBlockedDialog(accountSubStatus : String?, msg : String){
        val dthStatus = viewModel.getDthStatus()
        when (dthStatus.toLowerCase(Locale.getDefault())) {
            AccountStatusEnum.DEACTIVATED.status.toLowerCase(Locale.getDefault()) -> {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.recharge), getString(R.string.skip), msg), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        viewModel.startRecharge()
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
            AccountStatusEnum.TEMP_SUSPENSION.status.toLowerCase(Locale.getDefault()) -> {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.ok), null, msg), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
            AccountStatusEnum.ACTIVE.status.toLowerCase(Locale.getDefault()) -> {
                if (accountSubStatus.equals(AccountStatusEnum.PARTIALLY_DUNNED.status.toLowerCase(Locale.getDefault()), true)) {
                    showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.alert), getString(R.string.recharge), getString(R.string.skip), msg), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            viewModel.startRecharge()
                            hideDialog()
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                        }

                        override fun onCloseButtonClick() {
                        }
                    })
                } else {
                    onError(ErrorModel())
                }
            }
            else -> {
                onError(ErrorModel())
            }
        }
    }

    private fun subscriptionCancelledModeNew(currentPack: PartnerPacks) {
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.show()
        binding.selectedDummyPackCard.root.hide()
        binding.freeTrialExpiryMsg.hide()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        binding.tvRechargeMsg.hide()
        binding.selectedPackCardNew.tvTag.hide()
        if (true == currentPack.cancelBtn) {
            binding.selectedPackCardNew.btnModifySubscription.show()
        } else {
            binding.selectedPackCardNew.btnModifySubscription.hide()
        }
        if (currentPack.isInactive) {
            val color = ContextCompat.getColor(requireContext(), R.color.darkError)
            binding.selectedPackCardNew.tvRenewalCycle.setTextColor(color)
        }
        binding.selectedPackCardNew.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCardNew.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCardNew.btnModifySubscription.setOnClickListener {
            beginRevokeProcess()
        }
        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCardNew.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCardNew.tvEligibility.show()
        }else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCardNew.tvEligibility.show()
        } else {
            binding.selectedPackCardNew.tvEligibility.hide()
        }
        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.selectedPackCardNew.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }
    }

    private fun subscriptionActiveModeNew(currentPack: PartnerPacks) {
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.show()
        binding.selectedDummyPackCard.root.hide()
        binding.freeTrialExpiryMsg.hide()
        binding.selectedPackCardNew.tvTag.hide()
        binding.tvRechargeMsg.hide()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCardNew.btnModifySubscription.show()
        } else {
            binding.selectedPackCardNew.btnModifySubscription.hide()
        }
        binding.selectedPackCardNew.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCardNew.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCardNew.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if(viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCardNew.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCardNew.tvEligibility.show()
        }else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true) || (currentPack.eligibleFirestick && currentPack.fsTaken)){
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCardNew.tvEligibility.show()
        } else {
            binding.selectedPackCardNew.tvEligibility.hide()
        }
        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.selectedPackCardNew.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }
    }

    private fun subscriptionExpiredModeNew(currentPack: PartnerPacks) {
        binding.gpActiveSubscription.hide()
        binding.gpActiveSubscriptionNew.show()
        binding.selectedDummyPackCard.root.hide()
        binding.freeTrialExpiryMsg.hide()
        binding.selectedPackCardNew.tvTag.hide()
        binding.tvRechargeMsg.show()
        binding.selectedPack = currentPack
        binding.gpNoSubscription.hide()
        if(currentPack.isCancelled){
            binding.selectedPackCardNew.tvTag.text = CANCELLED
            binding.selectedPackCardNew.tvTag.show()
        }
        if (true == currentPack.modifyBtn) {
            binding.selectedPackCardNew.btnModifySubscription.show()
        } else {
            binding.selectedPackCardNew.btnModifySubscription.hide()
        }
        val color = ContextCompat.getColor(requireContext(), R.color.darkError)
        binding.selectedPackCardNew.tvRenewalCycle.setTextColor(color)
        binding.selectedPackCardNew.tvRenewalCycle.setText(currentPack.expiryDateToDisplay, TextView.BufferType.SPANNABLE)
        binding.selectedPackCardNew.recyclerPacks.adapter = ProviderAdapter(currentPack.appList)
        binding.selectedPackCardNew.btnModifySubscription.setOnClickListener {
            if(currentPack.modifyBtnMessage.isNullOrBlank())
                findNavController().navigateSafe(RechargeSubscriptionFragmentDirections.actionRechargeSubscriptionFragmentToSubscriptionFragment())
            else
                handleModifyBlockedDialog(currentPack.accountSubStatus, currentPack.modifyBtnMessage?:"")
        }
        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()){
            binding.selectedPackCardNew.tvEligibility.text = currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCardNew.tvEligibility.show()
        }else if(viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true)) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_availed_msg)
            binding.selectedPackCardNew.tvEligibility.show()
        } else if(currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCardNew.tvEligibility.text = getString(R.string.fs_eligible)
            binding.selectedPackCardNew.tvEligibility.show()
        } else {
            binding.selectedPackCardNew.tvEligibility.hide()
        }
        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.selectedPackCardNew.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }
    }
}