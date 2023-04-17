package com.tatasky.binge.ui.features.details.adapter

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.customviews.EndlessListAdapter
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.LayoutRelatedFragmentBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.details.PrimaryButtonStateEnum
import com.tatasky.binge.ui.features.home.adapter.ContentItemDiffCallback
import com.tatasky.binge.utils.*


class AllEpisodeAdapter(
    val listener: CommonDTOClickListener,
    val mList: MutableList<ContentItem> = mutableListOf(),
    private val sectionPosition: Int,
    var state: PrimaryButtonStateEnum,
    val contentType: String,
    val id: String,
    private val cloudinaryUrl: String?,
    val episodeInfoClickListener: CommonDTOClickListener,
    val isContenSubscribed : Boolean
) : EndlessListAdapter<ContentItem, RecyclerView.ViewHolder>(mList) {
    init {
        autoUpdating = false
    }

    override fun createNormalViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return RailItemViewHolder(
            LayoutRelatedFragmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getNoContentVisibility(): Int {
        return View.GONE
    }

    fun updateList(mItems: MutableList<ContentItem>, moreContentAvailable: Boolean = false) {

        val diffResult = DiffUtil.calculateDiff(
            ContentItemDiffCallback(this.mDataList, mItems),
            false
        )
        updateDataWithDiffCallback(mItems, diffResult)

//        this.mDataList.clear()
//        this.mDataList.addAll(0, mItems)
//        notifyDataSetChanged()
//        notifyItemRangeInserted(0, this.mDataList.size)
//        if (moreContentAvailable)
//            addLoading()
    }

    fun addLoading() {
        isAppending = true
    }

    private fun removeLoading() {
        isAppending = false
    }

    fun addToList(mItems: List<ContentItem>, moreContentAvailable: Boolean) {
        removeLoading()
        this.addTomDataList(mItems)
//        if (moreContentAvailable)
//            addLoading()
    }

    fun addToList(mItems: List<ContentItem>) {
        removeLoading()
        this.addTomDataList(mItems)
//        if (moreContentAvailable)
//            addLoading()
    }


    fun prepandToList(mItems: List<ContentItem>) {
//        val initialCount = this.mDataList.size
        this.mDataList.addAll(0, mItems)
        notifyItemRangeInserted(0, mItems.size)
        //this.addTomDataList(mItems)
    }

    @SuppressLint("SetTextI18n")
    override fun bindNormalViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RailItemViewHolder) {
            val railPoint = RailPoint()
            if(railPoint.landscapePoint==null || railPoint.mLandscapeHeight==null || railPoint.mLandscapeWidth==null) {
                railPoint.landscapePoint = getEpisodeThumbnailDimension2(holder.binding.root.context!!)
                railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
            }
            val width = railPoint.mLandscapeWidth ?: 0
            val height = railPoint.mLandscapeHeight ?: 0


            val layoutParams = ConstraintLayout.LayoutParams(width, height)
            if (position % 2 == 0) {
                layoutParams.setMargins(
                    dpToPx(holder.binding.root.context, 0),//left
                    dpToPx(holder.binding.root.context, 6),//top
                    dpToPx(holder.binding.root.context, 4),//right
                    dpToPx(holder.binding.root.context, 6)//bottom
                )
            } else {
                layoutParams.setMargins(
                    dpToPx(holder.binding.root.context, 4),//left
                    dpToPx(holder.binding.root.context, 6),//top
                    dpToPx(holder.binding.root.context, 0),//right
                    dpToPx(holder.binding.root.context, 6)//bottom
                )
            }
            holder.binding.root.layoutParams = layoutParams

            val contentItem = mDataList[position]

            contentItem.isPartnerSubscribed = isContenSubscribed
            holder.bind(contentItem, state)
            val index = position + 1
            holder.binding.title.text = "Ep.${contentItem.episodeId} ${contentItem.title}"
            val url = getCloudinaryUrl(
                cloudinaryUrl,
                width,height,
                contentItem.getImageItem()
            )
            imageLoad(holder.binding.image, url)
            holder.binding.root.setOnClickListener {
                listener.onSubItemClick(
                    contentItem, position, sectionPosition, EventConstants.TYPE_RAIL, listOf(
                        Pair(
                            holder.binding.image,
                            ViewCompat.getTransitionName(holder.binding.image) ?: "fd"
                        )
                    )
                )
            }
            holder.binding.ivMore.setOnClickListener {
                episodeInfoClickListener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null
                )
            }
        } else if (holder is LoadMoreViewHolder) {
            if (getItemViewType(position) == VIEW_TYPE_MORE) {
                holder.itemView.setOnClickListener {
                    removeLoading()
                }
            }
        }
    }

    fun clearList() {
        mDataList.clear()
        notifyDataSetChanged()
    }

    fun updatePrimaryButtonState(primaryButtonState: PrimaryButtonStateEnum) {
        state = primaryButtonState
        notifyDataSetChanged()
    }

    class RailItemViewHolder(val binding: LayoutRelatedFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem, state: PrimaryButtonStateEnum) {
            binding.contentItem = model
//            if (state == PrimaryButtonStateEnum.STATE_SUBSCRIBE) {
//                binding.ivPlayOverlay.hide()
//            }
//            else
//                binding.ivPlayOverlay.show()
            var watchedSeconds = model.secondsWatched
            val totalDuration = model.durationInSeconds
            val isReplay = checkWatchedReplay(totalDuration, watchedSeconds )
            if(watchedSeconds > totalDuration)
                watchedSeconds = totalDuration

            if(watchedSeconds in 1..totalDuration) {
                binding.cwProgress.show()
                binding.cwProgress.max = model.durationInSeconds
                binding.cwProgress.progress = model.secondsWatched
//                if(isReplay){
//                    model.secondsWatched = 0
////                    binding.ivPlayOverlay.setImageDrawable(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_series_replay))
//                }
//                else{
////                    binding.ivPlayOverlay.setImageDrawable(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_series_play))
//                }
            }
            else {
//                binding.ivPlayOverlay.setImageDrawable(ContextCompat.getDrawable(binding.root.context, R.drawable.ic_series_play))
                binding.cwProgress.hide()
            }
            binding.descToggle.setOnCheckedChangeListener { _, isChecked ->
                val lt = LayoutTransition()
                lt.disableTransitionType(LayoutTransition.DISAPPEARING)
                binding.rlAnimated.layoutTransition = lt
                if (isChecked) {
                    binding.desc.show()
                } else {
                    binding.desc.hide()
                }
            }
        }
    }

}
