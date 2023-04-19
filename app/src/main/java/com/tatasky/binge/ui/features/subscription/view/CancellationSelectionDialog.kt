package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutCancellationSelectionDialogBinding
import com.tatasky.binge.ui.features.subscription.adapter.CancellationSelectionAdapter
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel


/**
 * Created by Srikant Karnani on 20/12/19.
 */
class CancellationSelectionDialog() : CustomDialog() {
    private lateinit var binding: LayoutCancellationSelectionDialogBinding
    lateinit var mSubscriptionViewModel: SubscriptionViewModel
    lateinit var mCurrentPack: PartnerPacks

    private constructor(
        subscriptionViewModel: SubscriptionViewModel
    ) : this() {
        this.mSubscriptionViewModel = subscriptionViewModel
        this.mCurrentPack = mSubscriptionViewModel.getCurrentSubscription()!!
    }

    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutCancellationSelectionDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.selectionDialogVerbiages = mCurrentPack.verbiage?.cancelSubs?.selectionDialogBox
        binding.layoutCancelBingePack.tvPackName.text = mCurrentPack.verbiage?.cancelSubs?.selectionDialogBox?.chkbox1?.takeIf { it.isNotBlank()}?:mCurrentPack.getPackNameWithPrice()
        binding.layoutCancelPrimePack.tvPackName.text =  mCurrentPack.verbiage?.cancelSubs?.selectionDialogBox?.chkbox2?.takeIf { it.isNotBlank()}?:"Amazon Prime"
        binding.layoutCancelPrimePack.cbCancellation.setOnCheckedChangeListener { _, isChecked ->
            binding.btnDialogPrimary.isEnabled =
                isChecked || binding.layoutCancelBingePack.cbCancellation.isChecked
        }
        binding.layoutCancelBingePack.cbCancellation.setOnCheckedChangeListener { _, isChecked ->
            if(mCurrentPack.primeAutoSelect == true) {
                binding.layoutCancelPrimePack.cbCancellation.isChecked = isChecked
                binding.layoutCancelPrimePack.cbCancellation.isClickable = !isChecked
            }
            binding.btnDialogPrimary.isEnabled =
                isChecked || binding.layoutCancelPrimePack.cbCancellation.isChecked
        }
        binding.btnDialogPrimary.setOnClickListener {
            mSubscriptionViewModel.setPacksToCancel(binding.layoutCancelBingePack.cbCancellation.isChecked, binding.layoutCancelPrimePack.cbCancellation.isChecked)
            dismiss()
        }
        binding.btnDialogSecondary.setOnClickListener {
            dismiss()
            mSubscriptionViewModel.subscriptionAnalytics.trackSubscriptionCancelSkip()
        }
    }

    companion object {
        fun newInstance(
            subscriptionViewModel: SubscriptionViewModel
        ): CancellationSelectionDialog =
            CancellationSelectionDialog(
                subscriptionViewModel
            )
    }
}