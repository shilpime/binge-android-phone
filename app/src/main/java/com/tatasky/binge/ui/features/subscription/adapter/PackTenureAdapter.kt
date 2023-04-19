package com.tatasky.binge.ui.features.subscription.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.Tenure
import com.tatasky.binge.databinding.ItemTenureBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText

class PackTenureAdapter(
    private var list: List<Tenure>, private var mViewModel : FreemiumSubscriptionViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // if checkedPosition = -1, there is no default selection
    // if checkedPosition = 0, 1st item is selected by default
    private var checkedPosition = -1

    init {
        for(i in list.indices){
            if(list[i].enable == true && list[i].currentTenure == false){
                checkedPosition = i
                break
            }
        }
//        checkedPosition = if((list[0].currentTenure == true || list[0].enable == false) && list.size > 1){
//            1
//        } else {
//            0
//        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PackViewHolder(
            ItemTenureBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class PackViewHolder(private val binding: ItemTenureBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tenure: Tenure) {

            if(tenure.currentTenure == true){
                binding.tvCurrentTenure.show()
                binding.rbTenure.hide()
                itemView.isClickable = false
                binding.root.alpha = 0.5f
            }
            if(tenure.enable == false){
                binding.rbTenure.hide()
                itemView.isClickable = false
                binding.root.alpha = 0.5f
            }

            binding.tvTenureTitle.text = tenure.tenureType
            binding.tvTenureSubtitle.text = tenure.tenureDuration
            binding.tvTenurePriceOffered.setHtmlText(tenure.offeredPrice)
            binding.tvTenurePrice.setHtmlText(tenure.mrp)
            if(tenure.discountedPercentage.isNullOrBlank()){
                binding.llSaveTag.hide()
            }
            binding.tvSaveTag.text = tenure.discountedPercentage
            tenure.tenureImage?.let { imageLoad(binding.ivTenure, it) }

            if(tenure.currentTenure == false && (tenure.enable == true || tenure.enable == null)){
                if (checkedPosition == -1 || checkedPosition != bindingAdapterPosition) {
                    deSelectItem(binding)
                } else {
                    selectItem(binding)
                }

                itemView.setOnClickListener {
                    mViewModel.setTenureSelected(tenure)
                    selectItem(binding)
                    if (checkedPosition != bindingAdapterPosition) {
                        notifyItemChanged(checkedPosition)
                        checkedPosition = bindingAdapterPosition
                    }
                }
            }
        }
    }

    private fun deSelectItem(binding: ItemTenureBinding) {
        binding.rbTenure.isChecked = false
        binding.cardView.strokeColor =
            ContextCompat.getColor(binding.cardView.context, R.color.stroke_color)
    }
    private fun selectItem(binding: ItemTenureBinding) {
        binding.rbTenure.isChecked = true
        binding.cardView.strokeColor =
            ContextCompat.getColor(binding.cardView.context, R.color.darkPrimary)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PackViewHolder).bind(list[position])

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getSelected(): Tenure? {
        return if (checkedPosition != -1) {
            list.get(checkedPosition)
        } else null
    }


}