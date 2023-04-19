package com.tatasky.binge.ui.features.parentalcontrol.sidemenu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.AgeRatingsResponse
import com.tatasky.binge.databinding.LayoutParentalControlRatingItemBinding

class ParentalControlRatingAdapter(
    private var mList: MutableList<AgeRatingsResponse.AgeRatings>,
    private val mRatingSelector: RatingSelector
) :
    RecyclerView.Adapter<ParentalControlRatingAdapter.ParentalControlRatingItemViewHolder>() {
    private var selectedPosition = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ParentalControlRatingItemViewHolder {
        return ParentalControlRatingItemViewHolder(
            LayoutParentalControlRatingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ParentalControlRatingItemViewHolder, position: Int) {
        holder.bind(mList[position])
        holder.binding.root.setOnClickListener {
            if (holder.adapterPosition >= 0) {
                mRatingSelector.onRatingSelect(mList[holder.adapterPosition])
                setSelected(holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = mList.size

    fun updateList(updatedList: List<AgeRatingsResponse.AgeRatings>, selPosition: Int?) {
        mList.clear()
        mList.addAll(updatedList)
        if (selPosition != null && selPosition > -1) {
            selectedPosition = selPosition
            mRatingSelector.onRatingSelect(mList[selPosition])
        }
        notifyDataSetChanged()
    }

    private fun setSelected(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    inner class ParentalControlRatingItemViewHolder(val binding: LayoutParentalControlRatingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: AgeRatingsResponse.AgeRatings) {
            binding.cbRating.isChecked = selectedPosition == adapterPosition
            binding.tvRatingTitle.text = itemData.ageRatingName
        }
    }
}