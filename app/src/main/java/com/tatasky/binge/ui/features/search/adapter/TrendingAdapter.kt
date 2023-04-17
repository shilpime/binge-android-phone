package com.tatasky.binge.ui.features.search.adapter

import android.graphics.Point
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.customviews.EndlessListAdapter
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.ProviderLogo
import com.tatasky.binge.databinding.LayoutRailItemBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.interfaces.CommonLoadMoreClickListener
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.utils.*


class TrendingAdapter(
    val listener: CommonDTOClickListener,
    var list: MutableList<ContentItem> = mutableListOf(),
    val sectionPosition: Int = 0,
    private val cloudinaryUrl: String?,
    val loadMoreClickListener : CommonLoadMoreClickListener?,
    val providerLogos : ProviderLogo,
    val continuePaging : Boolean = false,
    private val sharedPrefs: PrefsRepo
    ) : EndlessListAdapter<ContentItem, RecyclerView.ViewHolder>(mutableListOf()) {
    init {
        e("SearchLandingAdapter","inside TrendingAdaptersize : ${list.size}")
        autoUpdating = false
        addToList(list, continuePaging)
    }

    private var layoutType: String = ItemLayoutType.LANDSCAPE.name
    override fun createNormalViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RailItemViewHolder(
            LayoutRailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun bindNormalViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val point: Point
        var mWidth: Int
        val mHeight: Int
        if (holder is RailItemViewHolder) {
            val contentItem = list[position]
            when (holder.itemViewType) {
                PORTRAIT_TYPE -> {
                    holder as RailItemViewHolder
                    holder.bind(contentItem, sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage)
                    point = getLargeThumbnailDimensionGrid(holder.binding.root.context!!)
                    mWidth = point.x
                    mHeight = point.y
                    val layoutParams =
                        RelativeLayout.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                    if (position % 2 == 0) {
                        layoutParams.setMargins(
                            dpToPx(holder.binding.root.context, 8),
                            dpToPx(holder.binding.root.context, 4),
                            dpToPx(holder.binding.root.context, 4),
                            dpToPx(holder.binding.root.context, 8)
                        )
                    } else {
                        layoutParams.setMargins(
                            dpToPx(holder.binding.root.context, 4),
                            dpToPx(holder.binding.root.context, 4),
                            dpToPx(holder.binding.root.context, 8),
                            dpToPx(holder.binding.root.context, 8)
                        )
                    }
                    holder.binding.cardView.layoutParams = layoutParams
                    holder.binding.img.layoutParams = ConstraintLayout.LayoutParams(mWidth, mHeight)
                    holder.binding.rlImage.layoutParams = RelativeLayout.LayoutParams(mWidth, mHeight)

                    val param = RelativeLayout.LayoutParams(
                        mWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    param.addRule(RelativeLayout.BELOW, holder.binding.rlImage.id)
                    param.setMargins(0, 0, 0, dpToPx(holder.binding.root.context, 8))
                    holder.binding.commonDetail.root.layoutParams = param

                    val url = getCloudinaryUrl(
                        cloudinaryUrl,
                        mWidth, mHeight,
                        contentItem.getImageItem()
                    )
                    imageLoad(holder.binding.img, url)
                    updateProviderImage(
                        holder.binding.commonDetail.ivBrand,
                        contentItem.provider,
                        providerLogos,
                        R.drawable.ic_rail_placeholder
                    )

                    holder.binding.root.setOnClickListener {
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            EventConstants.TYPE_RAIL,
                            listOf(
                                Pair(
                                    holder.binding.img,
                                    ViewCompat.getTransitionName(holder.binding.img)!!
                                )
                            )
                        )
                    }
                }
                LANDSCAPE_TYPE -> {
                    holder as RailItemViewHolder
                    holder.bind(contentItem, sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage)
                    point = getNormalThumbnailDimensionGrid(holder.binding.root.context!!)
                    mWidth = point.x
                    mHeight = point.y
                    val layoutParams =
                        RelativeLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    if (position % 2 == 0) {
                        mWidth -= dpToPx(holder.binding.root.context, 12)
                        layoutParams.setMargins(
                            dpToPx(holder.binding.root.context, 8),
                            dpToPx(holder.binding.root.context, 4),
                            dpToPx(holder.binding.root.context, 6),
                            dpToPx(holder.binding.root.context, 8)
                        )
                    } else {
                        mWidth -= dpToPx(holder.binding.root.context, 12)
                        layoutParams.setMargins(
                            dpToPx(holder.binding.root.context, 6),
                            dpToPx(holder.binding.root.context, 4),
                            dpToPx(holder.binding.root.context, 8),
                            dpToPx(holder.binding.root.context, 8)
                        )
                    }
                    holder.binding.cardView.layoutParams = layoutParams
                    holder.binding.img.layoutParams = FrameLayout.LayoutParams(mWidth, mHeight)
                    holder.binding.rlImage.layoutParams = RelativeLayout.LayoutParams(mWidth, mHeight)

                    val param = RelativeLayout.LayoutParams(
                        mWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    param.addRule(RelativeLayout.BELOW, holder.binding.rlImage.id)
                    param.setMargins(0, 0, 0, dpToPx(holder.binding.root.context, 8))
                    holder.binding.commonDetail.root.layoutParams = param

                    val url = getCloudinaryUrl(
                        cloudinaryUrl,
                        mWidth, mHeight,
                        contentItem.getImageItem()
                    )
                    imageLoad(holder.binding.img, url)
                    updateProviderImage(
                        holder.binding.commonDetail.ivBrand,
                        contentItem.provider,
                        providerLogos,
                        R.drawable.ic_rail_placeholder
                    )

                    holder.binding.root.setOnClickListener {
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            EventConstants.TYPE_RAIL,
                            listOf(
                                Pair(
                                    holder.binding.img,
                                    ViewCompat.getTransitionName(holder.binding.img)!!
                                )
                            )
                        )
                    }
                }

            }
        } else if (holder is LoadMoreViewHolder) {
            if (getItemViewType(position) == VIEW_TYPE_MORE) {
                holder.itemView.setOnClickListener {
                    if(isAppending) {
                        loadMoreClickListener?.onLoadMoreClick((mDataList.size))
                    }
                    else
                        removeLoading()
                    /*addToList(mDataList.size / 12)*/
                }
            }
        }
    }


    fun removeLoading() {
        isAppending = false
    }

    private fun addToList() {
        this.addTomDataList(list)
//        addLoading()
        if (continuePaging) {
            addLoading()
        }
    }


    fun updateList(mItems: List<ContentItem>) {
        list.clear()
        list.addAll(mItems)
        this.setmDataList(list)
    }

    class RailItemViewHolder(val binding: LayoutRailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem, firstEpisodeFreeVerbiage: String?) {
            binding.firstFreeEpisodeVerbiage = firstEpisodeFreeVerbiage
            binding.contentItem = model
            ViewCompat.setTransitionName(binding.img, model.id + "image")
        }
    }

    public override fun getNormalItemViewType(position: Int): Int {
        return when {
            this.layoutType.equals(ItemLayoutType.PORTRAIT.name, true) -> PORTRAIT_TYPE
            this.layoutType.equals(ItemLayoutType.LANDSCAPE.name, true) -> LANDSCAPE_TYPE
            else -> 0
        }
    }


    fun addLoading() {
        isAppending = true
//        notifyItemInserted(itemCount)
    }

    fun addToList(itemsToAdd: List<ContentItem>,
                  continuePaging : Boolean) {
        this.addTomDataList(itemsToAdd)
        isAppending = continuePaging
    }

    fun updateList(mItems: List<ContentItem>,
                   continuePaging : Boolean) {
        this.setmDataList(mItems.toMutableList())
        isAppending = continuePaging
    }

    val LANDSCAPE_TYPE = 1
    val PORTRAIT_TYPE = 2
}