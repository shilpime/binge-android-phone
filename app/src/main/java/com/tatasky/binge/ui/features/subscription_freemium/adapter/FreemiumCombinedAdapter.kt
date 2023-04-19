package com.tatasky.binge.ui.features.subscription_freemium.adapter

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.tatasky.binge.data.networking.models.response.EligiblePackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.LayoutFreemiumPacksBinding
import com.tatasky.binge.ui.base.frameworks.extensions.disable
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.getPackCardDimension
import com.tatasky.binge.utils.paintPremiumGradient
import java.lang.Exception


class FreemiumCombinedAdapter(
    private var list: List<PartnerPacks>, val newViewModel: FreemiumSubscriptionViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var recyclerView: RecyclerView? = null
    var mWidth = 0
    private val railPoint = RailPoint()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PackViewHolder(
            LayoutFreemiumPacksBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(railPoint.landscapePoint==null || railPoint.mLandscapeHeight==null || railPoint.mLandscapeWidth==null) {
            railPoint.landscapePoint = getPackCardDimension(holder.itemView.context)
            railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
            railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
        }
        val width = railPoint.mLandscapeWidth ?: 0
        val height = railPoint.mLandscapeHeight ?: 0
        mWidth = width
        (holder as FreemiumCombinedAdapter.PackViewHolder).bind(list[position])
        holder.setIsRecyclable(false)
        (holder as PackViewHolder).itemView.rootView.layoutParams =
            ConstraintLayout.LayoutParams(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }

    inner class PackViewHolder(private val binding: LayoutFreemiumPacksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pack: PartnerPacks) {
            val partnerList = pack.getSelectedComponentAppList
            var currentPack = newViewModel.sharedPref.getSubscribedPack()
            if (currentPack?.productId.equals(
                    pack.productId,
                    true
                ) && currentPack?.isInactive != true
            ) {
                binding.cardViewTag.show()
                binding.btnProceed.disable()
            }
            binding.pack = pack
            binding.tvTotalApps.text = pack.componentList[0].numberOfApps
            if(pack.highlightedPack == true) {
                paintPremiumGradient(binding.tvPackName,binding.tvPackName.paint.measureText(pack.productName))
                paintPremiumGradient(binding.ivPlus,binding.ivPlus.paint.measureText(pack.appHeaderMessage))
            }


                val layoutManager = FlexboxLayoutManager(binding.recyclerPacks.context)
                layoutManager.flexDirection = FlexDirection.ROW
                layoutManager.justifyContent = JustifyContent.CENTER
                layoutManager.alignItems = AlignItems.CENTER
                binding.recyclerPacks.layoutManager = layoutManager

                val layoutManagerExtra = FlexboxLayoutManager(binding.recyclerPacks.context)
                layoutManagerExtra.flexDirection = FlexDirection.ROW
                layoutManagerExtra.justifyContent = JustifyContent.CENTER
                layoutManagerExtra.alignItems = AlignItems.CENTER
                binding.extraRecyclerPacks.layoutManager = layoutManagerExtra

                binding.recyclerPacks.adapter = FreemiumProviderAdapter(
                    partnerList.filter { it.included == true && it.premiumPartner == false },
                    mWidth
                )

                if(partnerList.any { it.included == false || it.premiumPartner == true }){
                    val premiumPartnerList = partnerList.filter {
                        it.premiumPartner == true && it.included == true
                    }
                    if(premiumPartnerList.isEmpty()){
                        val includedPartnerList = partnerList.filter {
                            it.included == false
                        }
                        binding.extraRecyclerPacks.alpha = .5f
                        binding.extraRecyclerPacks.adapter = FreemiumProviderAdapter(includedPartnerList, mWidth)
                    } else {
                        binding.extraRecyclerPacks.alpha = 1f
                        binding.extraRecyclerPacks.adapter = FreemiumProviderAdapter(premiumPartnerList, mWidth)
                    }
                    binding.ivPlus.text = pack.appHeaderMessage
                    binding.ivPlus.show()
                    binding.extraRecyclerPacks.show()
                }

                (binding.recyclerPacks.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                (binding.extraRecyclerPacks.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false




            binding.btnProceed.setOnClickListener {
                newViewModel.setPackProceedClicked(pack)
            }

        }
    }


    fun updateList1(
        list: List<PartnerPacks>
    ) {
        this.list = list
        notifyDataSetChanged()
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
}