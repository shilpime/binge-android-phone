package com.tatasky.binge.ui.features.subscription_freemium.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.ItemStarterPackBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.disable
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText
import com.tatasky.binge.utils.paintPremiumGradient
import kotlin.math.roundToInt

class FreemiumPackListAdapter(
    private var list: List<PartnerPacks>, val newViewModel: FreemiumSubscriptionViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var recyclerView: RecyclerView? = null
    var mWidth = 0


    // if checkedPosition = -1, there is no default selection
    // if checkedPosition = 0, 1st item is selected by default
    private var checkedPosition = -1

    init {
        checkedPosition = -1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PackViewHolder(
            ItemStarterPackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FreemiumPackListAdapter.PackViewHolder).bind(list[position])

    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class PackViewHolder(private val binding: ItemStarterPackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pack: PartnerPacks) {
            var packExtraInfoText = ""
            //""${pack.getSelectedComponent?.numberOfApps} | ${pack.deviceDetails?.deviceCount}"
            packExtraInfoText = pack.getSelectedComponent?.numberOfApps?:""
            if(!pack.deviceDetails?.deviceCount.isNullOrEmpty()){
                packExtraInfoText += " | ${pack.deviceDetails?.deviceCount}"
            }
            binding.tvExtraInfo.text = packExtraInfoText
            if (!pack.amountValue.isNullOrEmpty() && !pack.amountValue.isNullOrBlank()) {
                binding.tvPackCycle.text = "/${pack.packCycle}"
                binding.tvPackAmountValue.setHtmlText("${pack.rupeesSymbol}${pack.amountValue!!.toDouble().roundToInt()}")
            }
            val partnerList = pack.getSelectedComponentAppList
            val currentPack = newViewModel.sharedPref.getSubscribedPack()
            if (checkedPosition == -1) {
                newViewModel.setPackSelected(null)
            }

            binding.pack = pack
            imageLoad(binding.ivDeviceType,pack.deviceDetails?.iconUrl?:"")

            if(pack.highlightedPack == true)
                paintPremiumGradient(binding.tvPackName,binding.tvPackName.paint.measureText(pack.productName))
            else
                binding.tvPackName.paint.shader = null

            val partnersWithHighlightedPartner = moveHighlightedPartnerToFront(partnerList.filter { it.included == true })

            binding.rvPackPartners.adapter = FreemiumProviderAdapter(
                partnersWithHighlightedPartner,
                mWidth,
                packListing = true
            )

            binding.rvPackPartners.suppressLayout(true)



            if (checkedPosition == -1) {
                deSelectItem(binding)
            } else {
                if (checkedPosition == bindingAdapterPosition) {
                    selectItem(binding)
                } else {
                    deSelectItem(binding)
                }
            }
            if (currentPack?.productId.equals(
                    pack.productId,
                    true
                ) && currentPack?.isInactive != true
            ) {
                binding.llPackTag.show()
                binding.tvPackTag.text = binding.tvPackTag.context.getString(R.string.current_plan)
                binding.rbPackSelector.hide()
                itemView.setOnClickListener {

                }
            }else{
                if(!pack.specialOfferVerbiage.isNullOrEmpty()){
                    binding.llPackTag.show()
                    binding.tvPackTag.text = pack.specialOfferVerbiage
                } else {
                    binding.llPackTag.hide()
                }
                binding.rbPackSelector.show()
                itemView.setOnClickListener {
                    newViewModel.setPackSelected(pack)
                    selectItem(binding)
                    if (checkedPosition != bindingAdapterPosition) {
                        notifyItemChanged(checkedPosition)
                        checkedPosition = bindingAdapterPosition
                    }
                }
            }

        }
    }

    private fun moveHighlightedPartnerToFront(partnerList: List<PartnerPacks.PartnerList>): List<PartnerPacks.PartnerList> {
        val partnersWithHighlightedPartner = mutableListOf<PartnerPacks.PartnerList>()
        partnerList.forEach {it->
            if(it.starterPackHighlightApp == true){
                partnersWithHighlightedPartner.add(0,it)
            } else {
                partnersWithHighlightedPartner.add(it)
            }
        }
        return partnersWithHighlightedPartner
    }

    private fun deSelectItem(binding: ItemStarterPackBinding) {
        binding.rbPackSelector.isChecked = false
        binding.cardView.strokeColor =
            ContextCompat.getColor(binding.cardView.context, R.color.stroke_color)
    }

    private fun selectItem(binding: ItemStarterPackBinding) {
        binding.rbPackSelector.isChecked = true
        binding.cardView.strokeColor =
            ContextCompat.getColor(binding.cardView.context, R.color.darkPrimary)
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


    fun getSelected(): PartnerPacks? {
        return if (checkedPosition != -1 && checkedPosition < list.size) {
            list[checkedPosition]
        } else null
    }


}
