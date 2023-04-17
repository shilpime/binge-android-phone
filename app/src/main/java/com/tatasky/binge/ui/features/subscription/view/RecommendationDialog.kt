package com.tatasky.binge.ui.features.subscription.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tatasky.binge.R
import com.tatasky.binge.customviews.CustomDialog
import com.tatasky.binge.data.networking.models.response.WalletBalanceResponse
import com.tatasky.binge.databinding.LayoutRecommendedBreakdownDialogBinding
import com.tatasky.binge.databinding.LayoutRecommendedBreakdownItemBinding
import com.tatasky.binge.ui.base.frameworks.extensions.show


/**
 * Created by Srikant Karnani on 20/12/19.
 */
class RecommendationDialog() : CustomDialog() {
    private lateinit var binding: LayoutRecommendedBreakdownDialogBinding
    private var mWalletBalanceResponse: WalletBalanceResponse? = null

    private constructor(walletBalanceResponse: WalletBalanceResponse) : this() {
        this.mWalletBalanceResponse = walletBalanceResponse
    }

    override fun getRootViewLayout(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = LayoutRecommendedBreakdownDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        isCancelable = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        mWalletBalanceResponse?.data?.mapOfBingeSubscription?.forEach { map ->
//            map?.forEach { name, amount ->
//                val firstRow =
//                    LayoutRecommendedBreakdownItemBinding.inflate(LayoutInflater.from(context), null, false)
//                firstRow.tvChargesFor.text = name
//                firstRow.tvCharges.text = getString(
//                    R.string.rupees,
//                    (amount ?: "0").toFloat().toInt()
//                        .toString()
//                )
//                binding.tableLayout.addView(firstRow.root)
//            }
//        }
//        val totalRow =
//            LayoutRecommendedBreakdownItemBinding.inflate(LayoutInflater.from(context), null, false)
//        totalRow.tvChargesFor.text = getString(R.string.total_payment)
//        totalRow.ivDivider.show()
//        totalRow.tvCharges.text = getString(
//            R.string.rupees,
//            (mWalletBalanceResponse?.data?.recommendedRechargeAmount ?: "0").toFloat().toInt()
//                .toString()
//        )
//        binding.tableLayout.addView(totalRow.root)
        binding.btnDialogPrimary.setOnClickListener { dismiss() }
    }

    companion object {
        fun newInstance(
            packName: String,
            walletBalanceResponse: WalletBalanceResponse
        ): RecommendationDialog =
            RecommendationDialog(
                walletBalanceResponse
            )
    }
}