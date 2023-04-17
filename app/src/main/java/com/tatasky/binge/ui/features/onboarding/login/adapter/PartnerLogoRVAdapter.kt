package com.tatasky.binge.ui.features.onboarding.login.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.Providers
import com.tatasky.binge.databinding.LayoutRvPartnerLogoBinding
import com.tatasky.binge.helper.transparentImageLoad

class PartnerLogoRVAdapter :
    ListAdapter<Providers, RecyclerView.ViewHolder>(PartnerLogoDiffCallback()) {

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