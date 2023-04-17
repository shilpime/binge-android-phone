package com.tatasky.binge.ui.features.subscription.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutCancellationSelectionItemBinding

class CancellationSelectionAdapter(private val mList: List<PartnerPacks>) :
    RecyclerView.Adapter<CancellationSelectionAdapter.SelectionViewHolder>() {
    private val hashmapOfPartnerPacks = HashMap<String, PartnerPacks>()

    class SelectionViewHolder(val binding: LayoutCancellationSelectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(partnerPacks: PartnerPacks){
            binding.tvPackName.text = partnerPacks.getPackNameWithPrice()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
        return SelectionViewHolder(LayoutCancellationSelectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
        holder.bind(mList[position])
        holder.binding.cbCancellation.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                hashmapOfPartnerPacks[mList[holder.adapterPosition].packId!!] = mList[holder.adapterPosition]
            }else{
                hashmapOfPartnerPacks.remove(mList[holder.adapterPosition].packId)
            }
        }
    }

    fun getCheckedPacks(): List<PartnerPacks>{
        return hashmapOfPartnerPacks.values.toList()
    }
}