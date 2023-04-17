package com.tatasky.binge.ui.features.subscription.adapter

import android.R.attr.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.OfferEligiblePacks
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutAdditionalOptionsBinding
import com.tatasky.binge.databinding.LayoutPacksBinding
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription.viewmodel.SubscriptionViewModel
import com.tatasky.binge.utils.EXPIRED
import com.tatasky.binge.utils.SUBSCRIBED
import com.tatasky.binge.utils.dpToPx


class CombinedAdapter(
    private var list: List<PartnerPacks>,
    private var offersList: List<OfferEligiblePacks>,
    private var commonApps: Set<String> = emptySet<String>(),
    val newViewModel: SubscriptionViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var selected_index = 0
    var default_selected_index = 0
    var recyclerView: RecyclerView? = null
    var subscribedPackIdWithStatus: Pair<String, String?>? = null
    var alternateSubscribedPackIdWithStatus: Pair<String, String?>? = null
    var defaultTagMsg : String? = null
    var isFDR : Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PackViewHolder(
            LayoutPacksBinding.inflate(
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
        var status: String? = if(default_selected_index == holder.adapterPosition) defaultTagMsg else null
        if (!list[position].packId.isNullOrBlank() && !list[position].alternatePaidPackId.isNullOrBlank() && (list[position].packId == subscribedPackIdWithStatus?.first || list[position].packId == alternateSubscribedPackIdWithStatus?.first
            || list[position].alternatePaidPackId == subscribedPackIdWithStatus?.first || list[position].alternatePaidPackId == alternateSubscribedPackIdWithStatus?.first )) {
            status = subscribedPackIdWithStatus?.second ?: alternateSubscribedPackIdWithStatus?.second
        }
        (holder as PackViewHolder).bind(list[position], status)
        (holder as PackViewHolder).itemView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_UP -> holder.itemView.performClick()
                else -> {
                }
            }
            true
        }
        (holder as PackViewHolder).itemView.setOnClickListener {
            if (selected_index != holder.adapterPosition) {
                selected_index = holder.adapterPosition
                newViewModel.setSelectedPosition(holder.adapterPosition)
                notifyDataSetChanged()
            } else {
                recyclerView?.post {
                    try {
                        val y = (recyclerView?.y ?: 0f) + (recyclerView?.getChildAt(selected_index)?.y ?: 0f)
                        (recyclerView?.parent?.parent as NestedScrollView?)?.smoothScrollTo(0, y.toInt())
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    inner class PackViewHolder(private val binding: LayoutPacksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pack: PartnerPacks, status: String?) {
            if(list.size == 1){
                binding.framelayout.hide()
                binding.tvKnowMore.hide()
                binding.tvTotalApps.hide()
                binding.tvPackName.hide()
                binding.tvPackPrice.hide()
            }
            binding.pack = pack
            binding.status = status
            binding.isExpired = status == EXPIRED
            if (binding.recyclerOffers.childCount != 1) {
                val ll = LinearLayout(itemView.context)
                ll.orientation = LinearLayout.VERTICAL
                pack.offersList?.forEachIndexed { index, offer ->
                    val bindingOffer = LayoutAdditionalOptionsBinding.inflate(LayoutInflater.from(itemView.context))
                    bindingOffer.ivIcon.alpha = if (offer.isAvailable) 1f else .3f
                    bindingOffer.tvText.alpha = if (offer.isAvailable) 1f else .3f
                    bindingOffer.ivIsAvailable.alpha = if (offer.isAvailable) 1f else .3f
                    bindingOffer.ivIsAvailable.setImageResource(if (offer.isAvailable) R.drawable.ic_tick_available else R.drawable.ic_cross)
                    Glide.with(bindingOffer.ivIcon.context).load(offer.imgUrl).override(Target.SIZE_ORIGINAL).into(bindingOffer.ivIcon)
                    bindingOffer.tvText.text = offer.title
                    bindingOffer.viewBarrier.visibility = if (pack.offersList?.size ?: -1 == index + 1) View.GONE else View.VISIBLE
                    ll.addView(bindingOffer.root)
                }
                binding.recyclerOffers.addView(ll)
            }
            if (selected_index == adapterPosition) {
                if(list.size != 1) {
                    binding.framelayout.show()
                }
                if(!isFDR)
                    recyclerView?.post {
                        try {
                            val y = (recyclerView?.y ?: 0f) + (recyclerView?.getChildAt(adapterPosition)?.y ?: 0f)
                            (recyclerView?.parent?.parent as NestedScrollView?)?.smoothScrollTo(0, y.toInt())
                        } catch (e: Exception) {
                        }
                    }
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!status.isNullOrBlank()) {
                        val bitmap: Bitmap = Bitmap.createBitmap(binding.root.width,
                            binding.root.height, Bitmap.Config.ARGB_8888)
                        val c = Canvas(bitmap)
                        binding.framelayout.draw(c)
                        val startColor: Int = bitmap.getPixel((binding.tvTag.x - 2).toInt(), (binding.tvTag.y + binding.tvTag.height).toInt() / 2)
                        val endColor: Int = bitmap.getPixel((binding.tvTag.x + binding.tvTag.width + 2).toInt(), (binding.tvTag.y + binding.tvTag.height).toInt() / 2)
                        binding.tvTag.background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(startColor, endColor)).apply { this.cornerRadius = dpToPx(binding.root.context, 16).toFloat() }
                    }
                }, 200)
            } else {
                binding.framelayout.hide()
            }
            binding.recyclerPacks.adapter = ProviderAdapter(pack.appList.filter { it.providerId in commonApps })
            if (pack.appList.any { it.providerId !in commonApps }) {
                val totalExtraPacks = pack.appList.filter { it.providerId !in commonApps }
                binding.ivPlus.show()
                binding.extraRecyclerPacks.adapter = ProviderAdapter(totalExtraPacks)
                binding.extraRecyclerPacks.show()
            } else {
                binding.ivPlus.hide()
                binding.extraRecyclerPacks.hide()
            }
            if(pack.knowMoreDetails!=null && list.size > 1){
                binding.tvKnowMore.show()
                binding.tvKnowMore.setOnClickListener { newViewModel.setKnowMoreClickedDetail(pack.knowMoreDetails!!) }
            } else {
                binding.tvKnowMore.setOnClickListener(null)
                binding.tvKnowMore.hide()
            }
            binding.recyclerPacks.suppressLayout(true)
            binding.extraRecyclerPacks.suppressLayout(true)
//            binding.recyclerOffers.suppressLayout(true)
        }
    }

    fun updateList(
        list: List<PartnerPacks>,
        offers: List<OfferEligiblePacks>,
        selectedPackId: String?,
        alternatePackId: String?,
        commonApps: Set<String>,
        isActive: Boolean?,
        selectedIndex: Int,
        defaultSelectedIndex : Int?,
        defaultTagMsg : String?,
        isFDR : Boolean = false
    ) {
        this.list = list
        this.offersList = offers
        val value = if(isActive == true) SUBSCRIBED else if(isActive == false) EXPIRED else defaultTagMsg
        this.commonApps = commonApps
        this.defaultTagMsg = defaultTagMsg
        selectedPackId?.let {
            this.subscribedPackIdWithStatus = Pair(it, value)
        }
        alternatePackId?.let {
            this.alternateSubscribedPackIdWithStatus = Pair(it, value)
        }
        selected_index = selectedIndex
        default_selected_index = defaultSelectedIndex?:-1
        this.isFDR = isFDR
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
}