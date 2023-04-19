package com.tatasky.binge.ui.features.onboarding.login.bottomsheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.UsedMobileNumber
import com.tatasky.binge.databinding.LayoutPreviouslyUsedMobileItemBinding
import com.tatasky.binge.utils.maskPhoneNumber
import com.tatasky.binge.utils.maskPhoneNumberWithSingleSpace

class GuestLoginPreviouslyUsedMobileAdapter(
    private var mList: MutableList<UsedMobileNumber>,
    val clickListener: (item: UsedMobileNumber) -> Unit,
) :
    RecyclerView.Adapter<GuestLoginPreviouslyUsedMobileAdapter.GuestLoginPreviouslyUsedMobileItemViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): GuestLoginPreviouslyUsedMobileItemViewHolder {
        return GuestLoginPreviouslyUsedMobileItemViewHolder(
            LayoutPreviouslyUsedMobileItemBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false))
    }

    override fun onBindViewHolder(
        holder: GuestLoginPreviouslyUsedMobileItemViewHolder,
        position: Int,
    ) {
        holder.bind(mList[position])
        holder.itemView.setOnClickListener {
            setSelected(holder.bindingAdapterPosition)
        }
    }

    private fun setSelected(position: Int) {
        selectedPosition = position
        clickListener(mList[position])
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mList.size

    fun updateList(updatedList: List<UsedMobileNumber>) {
        mList.clear()
        mList.addAll(updatedList)
        setSelected(DEFAULT_SELECTED_POSITION)
    }

    inner class GuestLoginPreviouslyUsedMobileItemViewHolder(val binding: LayoutPreviouslyUsedMobileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UsedMobileNumber) {
            binding.apply {
                rmnSelectorRB.isChecked = bindingAdapterPosition == selectedPosition
                tvMobPreviouslyUsedMobileItem.text = item.mobileNumber
                    ?.takeIf { it.length == 10 }
                    ?: ""
                ivPrimeCrownGuestLoginPreviouslyUsedMobileItem.visibility =
                    if (item.premiumUser == true) View.VISIBLE else View.GONE
            }
        }
    }

    companion object {
        private const val DEFAULT_SELECTED_POSITION = 0
    }
}