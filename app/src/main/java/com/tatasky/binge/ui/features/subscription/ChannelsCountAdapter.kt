package com.tatasky.binge.ui.features.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutChannelCountBinding

class ChannelsCountAdapter(private val listOfChannels: List<PartnerPacks.Channels>) :
    RecyclerView.Adapter<ChannelsCountAdapter.ChannelsViewHolder>() {

    class ChannelsViewHolder(val binding: LayoutChannelCountBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(channelsInfo: PartnerPacks.Channels) {
            binding.channelInfo = channelsInfo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsViewHolder {
        return ChannelsViewHolder(
            LayoutChannelCountBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChannelsViewHolder, position: Int) {
        holder.bind(listOfChannels[position])
    }

    override fun getItemCount(): Int {
        return listOfChannels.size
    }
}