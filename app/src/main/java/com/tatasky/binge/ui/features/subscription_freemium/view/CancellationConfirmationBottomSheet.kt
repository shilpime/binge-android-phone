package com.tatasky.binge.ui.features.subscription_freemium.view

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentCancellationConfirmationBottomSheetBinding
import com.tatasky.binge.databinding.FragmentCancellationConfirmationBottomSheetBindingImpl
import com.tatasky.binge.ui.base.frameworks.base.BaseBottomSheetDialogFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.features.subscription.model.Cancellation
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*
import dagger.android.support.AndroidSupportInjection
import java.util.*

class CancellationConfirmationBottomSheet : BaseBottomSheetDialogFragment<FragmentCancellationConfirmationBottomSheetBinding,FreemiumSubscriptionViewModel>(tabSupported = true){

    val args by navArgs<CancellationConfirmationBottomSheetArgs>()
//

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

    override fun layoutId(): Int {
        return R.layout.fragment_cancellation_confirmation_bottom_sheet
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return activity ?:findNavController().getViewModelStoreOwner(R.id.freemium_subscription)
    }

    override fun setObserver() {

    }

    override fun toBeCalledOnce() {
        expandBottomSheet(dialog)
        binding.pack = sharedPrefs.getSubscribedPack()
        context?.let {
            if(isTablet(it)){
                binding.pack?.let { partnerPacks ->{
                    if(partnerPacks.planCTADetails?.cancellationOptions?.addOnCancelHeaderText.isNullOrEmpty()
                        && partnerPacks.planCTADetails?.cancellationOptions?.cancelHeaderText.isNullOrEmpty()) {
                        Log.d("TAG", "toBeCalledOnce: PlanCTADETIAL is empty")
                        binding.tvContentTitle.hide()
                    }
                   }
                }

                if(binding.pack!!.planCTADetails?.cancellationOptions?.addOnCancelFooterMessage.isNullOrEmpty()
                    && binding.pack!!.planCTADetails?.cancellationOptions?.cancelFooterMessage.isNullOrEmpty())
                    binding.tvDescription.hide()
            }
        }
        binding.isPrimeCancellation = args.isPrimeCancellation
        binding.btnLater.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanCancelPlanLater(
                sharedPrefs.getSubscribedPack()?.productName?:"",
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                )
            )
            dismiss()
        }
        binding.btnProceed.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanCancelPlanProceed(
                sharedPrefs.getSubscribedPack()?.productName?:"",
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                )
            )
            if (args.isPrimeCancellation) {
                viewModel.requestSubscriptionCancellation(
                    Cancellation(
                        bingeCancel = false,
                        primeCancel = true
                    )
                )
                dismiss()
            }
            else {
                viewModel.requestSubscriptionCancellation(
                    Cancellation(
                        bingeCancel = true,
                        primeCancel = false
                    )
                )
                dismiss()
            }
        }
    }

}


