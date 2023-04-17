package com.tatasky.binge.ui.features.subscription.adapter

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.ItemComparePlanBinding
import com.tatasky.binge.helper.circularImageApps
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText

class ComparePlanAdapter(
    var isHeader : Boolean= false, var subscriptionAnalytics: SubscriptionAnalytics,private var mainList: List<PartnerPacks>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // if checkedPosition = -1, there is no default selection
    // if checkedPosition = 0, 1st item is selected by default
    private var checkedPosition = 0
    private val railPoint = RailPoint()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ComparePlanViewHolder(
            ItemComparePlanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    inner class ComparePlanViewHolder(private val binding: ItemComparePlanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pack: PartnerPacks,x:List<String>,position:Int) {
            val border = binding.compareItemBorder
            val card = binding.compareItemCard
            val cardRoot = binding.llComparePlans
            var hideFooterFlag = false
            binding.tvPackName.text = pack.productName
            binding.tvPackPrice.setHtmlText(pack.amount)
            binding.tvAppCount.text = pack.getSelectedComponent?.numberOfApps
            if(pack.comparePlanFooterMessage == null) {
                binding.tvBottomText.hide()
                binding.ivLargeScreen.invisible()
                hideFooterFlag = true
            }
            if(isHeader) {
                if (hideFooterFlag) {
                    binding.tvBottomText.hide()
                    binding.ivLargeScreen.invisible()
                }
                pack.largeScreenImage?.let {
                    binding.ivLargeScreen.invisible()
                    circularImageApps(binding.ivLargeScreen, it)
                }
            }
            else {
                binding.tvDevices.text = pack.deviceDetails?.platformName
                binding.tvDevicesCount.text = pack.deviceDetails?.deviceCount
                binding.tvDevices.setTextColor(ContextCompat.getColor(binding.tvDevices.context,R.color.purple_25))
                binding.tvDevicesCount.setTextColor(ContextCompat.getColor(binding.tvDevicesCount.context,R.color.purple_25))
                binding.tvBottomText.text = pack.comparePlanFooterMessage
            }
            if(mainList[position].highlightedPack == true)
            {
                val paint = binding.tvPackName.paint
                val width = paint.measureText(binding.tvPackName.text.toString())

                paintPremiumGradient(binding.tvPackName,width)
                paintPremiumGradient(binding.tvPackPrice,width)
                paintPremiumGradient(binding.tvAppCount,width)
                paintPremiumGradient(binding.tvBottomText,width)



            }

            if(pack.getSelectedComponentAppList.isEmpty()){
                binding.tvMiddleText.adapter = CompareItemAdapter(x, mainList[position].componentList[0].partnerList  ,isHeader,hideFooterFlag,pack.componentList[0].partnerList)
            } else {
                binding.tvMiddleText.adapter =
                    CompareItemAdapter(x, mainList[position].getSelectedComponentAppList, isHeader,hideFooterFlag,  pack.getSelectedComponentAppList)
            }

            binding.tvMiddleText.suppressLayout(true)

            if (isHeader) {
                binding.tvBottomText.hide()
                border.isSelected = false
                binding.tvTopText.invisible()
                card.strokeWidth = dpToPx(itemView.context, 0)
                border.isSelected = false
                border.isSelected = false
                cardRoot.setBackgroundResource(
                    R.drawable.searchview_background_transparent
                )
                card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.transparent
                    )
                )
            } else {
                if (checkedPosition == -1) {
                    border.isSelected = false
                    card.strokeWidth = dpToPx(itemView.context, 1)
                    cardRoot.setBackgroundResource(
                        R.drawable.searchview_background_transparent
                    )
                    card.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.transparent
                        )
                    )
                } else {
                    if (checkedPosition == bindingAdapterPosition) {
                        border.isSelected = true
                        card.strokeWidth = 0
                        cardRoot.setBackgroundResource(
                            R.drawable.bg_bottom_gradient
                        )
                    } else {
                        border.isSelected = false
                        card.strokeWidth = dpToPx(itemView.context, 1)
                        card.setCardBackgroundColor(
                            ContextCompat.getColor(
                                itemView.context,
                                R.color.transparent
                            )
                        )
                        cardRoot.setBackgroundResource(
                            R.drawable.searchview_background_transparent
                        )
                    }
                }
                itemView.setOnClickListener {
                    border.isSelected = true
                    card.strokeWidth = 0
                    cardRoot.setBackgroundResource(
                        R.drawable.bg_bottom_gradient
                    )

                    if (checkedPosition != bindingAdapterPosition) {
                        notifyItemChanged(checkedPosition)
                        subscriptionAnalytics.trackComparePlanSelection(pack.productName?:"")
                        checkedPosition = bindingAdapterPosition
                    }
                }
            }
        }
    }

    fun getSelected(): PartnerPacks? {
        return if (checkedPosition != -1) {
            mainList[checkedPosition]
        } else null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val x = mainList[0].componentList[0].partnerList.map {
            it.partnerId!!
        }
        if(isHeader) {
            (holder as ComparePlanViewHolder).itemView.rootView.layoutParams =
                ConstraintLayout.LayoutParams(
                    dpToPx(holder.itemView.context, 80),
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
        } else {
            if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
                railPoint.landscapePoint = getComparePlanCardWidth((holder as ComparePlanViewHolder).itemView.rootView.context)
                railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
            }
            val width = railPoint.mLandscapeWidth ?: 0
            val height = railPoint.mLandscapeHeight ?: 0

            val layoutParams = ConstraintLayout.LayoutParams(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)


            layoutParams.setMargins(
                dpToPx(holder.itemView.context, if(position==0) 2 else 0),//left
                dpToPx(holder.itemView.context, 0),//top
                dpToPx(holder.itemView.context, 6),//right
                dpToPx(holder.itemView.context, 0)//bottom
            )

            (holder as ComparePlanViewHolder).itemView.rootView.layoutParams =
                layoutParams

        }

        (holder as ComparePlanViewHolder).bind(mainList[position],x,position)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

}