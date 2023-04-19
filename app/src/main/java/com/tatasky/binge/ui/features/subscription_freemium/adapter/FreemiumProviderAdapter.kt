package com.tatasky.binge.ui.features.subscription_freemium.adapter

import android.graphics.Point
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutProviderCurrentSubscriptionBinding
import com.tatasky.binge.databinding.LayoutRotateProviderBinding
import com.tatasky.binge.helper.circularImageApps
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.utils.getRealDisplayPoint


class FreemiumProviderAdapter(
    val list: List<PartnerPacks.PartnerList>,
    var parentWidth: Int = 0,
    var packListing: Boolean = false,
    var fiberDialog: Boolean = false,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var spanCount = 0
    var point: Point? = null
    var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int {
        return if (fiberDialog)
            2
        else if (packListing)
            1
        else
            0
    }


    inner class ViewHolder(val binding: LayoutRotateProviderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sub: PartnerPacks.PartnerList) {
            if (binding.root.layoutParams is FlexboxLayoutManager.LayoutParams) {
                if (parentWidth == 0) {
                    val point = getRealDisplayPoint(binding.root.context)
                    parentWidth = point.x - point.x / 10
                }

                val width = parentWidth / 6
                val height = parentWidth / 6
                val lp = FlexboxLayoutManager.LayoutParams(width, height)
                binding.root.layoutParams = lp
            }
            if (fiberDialog) {
                circularImageApps(binding.imageView1, sub.iconUrl ?: "")
            } else
                imageLoad(binding.imageView1, sub.squareImageUrl ?: "")
        }
    }

    inner class ViewHolderHighlitedPack(val binding: LayoutProviderCurrentSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sub: PartnerPacks.PartnerList) {

            if (sub.starterPackHighlightApp == true) {
                binding.ivPartnerHighlight.show()
                binding.imageViewHighlighted.show()
                binding.imageView1.hide()
                circularImageApps(binding.imageViewHighlighted, sub.iconUrl ?: "")
            } else {
                binding.imageViewHighlighted.hide()
                binding.ivPartnerHighlight.hide()
                binding.imageView1.show()
                circularImageApps(binding.imageView1, sub.iconUrl ?: "")
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> return ViewHolderHighlitedPack(
                LayoutProviderCurrentSubscriptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            2, 0 -> return ViewHolder(
                LayoutRotateProviderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        return ViewHolder(
            LayoutRotateProviderBinding.inflate(
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
        when (getItemViewType(position)) {
            1 -> (holder as ViewHolderHighlitedPack).bind(list[position])
            2, 0 -> (holder as ViewHolder).bind(list[position])
        }
    }
}