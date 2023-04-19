package com.tatasky.binge.ui.features.onboarding.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.Providers
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.LayoutRvPartnerLogoBinding
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.getEpisodeThumbnailDimension
import com.tatasky.binge.utils.getLoginDialogProviderDimension
import kotlinx.android.synthetic.main.row_price.view.*

class PartnerLogoRVAdapter :
    ListAdapter<Providers, RecyclerView.ViewHolder>(PartnerLogoDiffCallback()) {

    private val railPoint = RailPoint()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PartnerLogoRVAdapter.PartnerLogoViewHolder {
        return PartnerLogoViewHolder(
            LayoutRvPartnerLogoBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as? PartnerLogoViewHolder


        if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
            railPoint.landscapePoint = getLoginDialogProviderDimension(holder.itemView.context)
            railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
            railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
        }
        val width = railPoint.mLandscapeWidth ?: 0
        val height = railPoint.mLandscapeHeight ?: 0

        val layoutParams = ConstraintLayout.LayoutParams(width, height)

        itemViewHolder?.binding?.partnerLogoIV?.layoutParams = layoutParams


        itemViewHolder?.bind(getItem(position))
    }

    inner class PartnerLogoViewHolder(val binding: LayoutRvPartnerLogoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(provider: Providers?) {
            provider?.squareImageUrl?.let { transparentImageLoad(binding.partnerLogoIV, it) }
        }
    }
}

class PartnerLogoDiffCallback : DiffUtil.ItemCallback<Providers>() {
    override fun areItemsTheSame(oldItem: Providers, newItem: Providers): Boolean =
        oldItem.squareImageUrl == newItem.squareImageUrl

    override fun areContentsTheSame(oldItem: Providers, newItem: Providers): Boolean =
        oldItem.providerId == newItem.providerId
}