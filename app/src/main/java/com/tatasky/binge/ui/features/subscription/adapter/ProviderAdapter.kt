package com.tatasky.binge.ui.features.subscription.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.Providers
import com.tatasky.binge.databinding.LayoutProviderBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.e
import com.tatasky.binge.utils.getRealDisplayPoint


class ProviderAdapter(
    val list: List<Providers>
) : RecyclerView.Adapter<ProviderAdapter.ViewHolder>() {

    var spanCount = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if(recyclerView.layoutManager is GridLayoutManager){
            spanCount = (recyclerView.layoutManager as GridLayoutManager).spanCount
        }
    }
    inner class ViewHolder(val binding: LayoutProviderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sub: Providers) {
            val lp = binding.root.layoutParams as GridLayoutManager.LayoutParams
            val margin = dpToPx(binding.root.context, if(spanCount<=3) 4 else 2)
            lp.leftMargin = margin
            lp.rightMargin = margin
            lp.topMargin = margin
            lp.bottomMargin = margin
            binding.root.layoutParams = lp
            imageLoad(binding.ivAppImage, sub.iconUrl ?: "")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutProviderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}