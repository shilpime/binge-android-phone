package com.tatasky.binge.ui.features.home.adapter

import android.graphics.Point
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_BINGE_LIST
import com.tatasky.binge.customviews.EndlessListAdapter
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.requests.ContentIdAndTypeRequest
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.ProviderLogo
import com.tatasky.binge.databinding.ItemGameSquareBinding
import com.tatasky.binge.databinding.LayoutProviderItemBinding
import com.tatasky.binge.databinding.LayoutRailItemBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.interfaces.CommonLoadMoreClickListener
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.utils.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.set


class ItemGridAdapter(
    val listener: CommonDTOClickListener,
    var mList: MutableList<ContentItem>,
    val sectionPosition: Int = 0,
    private val cloudinaryUrl: String?,
    val loadMoreClickListener: CommonLoadMoreClickListener?,
    private val providerLogos: ProviderLogo,
    private val sharedPrefs: PrefsRepo,
    val origin: String? = null,
    private val mSelectedItemsSize: MutableLiveData<SingleEvent<Int>>? = null,
    var railTitle:String? = null,
    var railPosition : String? = null,
    var pageName : String? =null
) : EndlessListAdapter<ContentItem, RecyclerView.ViewHolder>(mList) {
    private var mTotalCount: Int = 0
    private var layoutType: String = ItemLayoutType.LANDSCAPE.name
    private var isSubscribed = false
    private var continueWatching: Boolean = false
    private var isSelectionMode: Boolean = false
    private val mSelectedItems = HashMap<Int,ContentIdAndTypeRequest.ContentIdAndType>()
    private var mNonSubscribedPartnerList = HashSet<String>()
    private var mIsUserLogin  = sharedPrefs.getLoginStatus()
    private var isPackAvailed = false
    init {
        updatePack()
    }
    fun updatePack() {
        isPackAvailed = sharedPrefs.getSubscribedPack() != null&&
                SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true)
        sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
            for (partner in partnerList){
                mNonSubscribedPartnerList.add((partner.partnerName ?: "").toLowerCase())
            }
        }
    }

    fun setAutoUpdate(b: Boolean) {
        autoUpdating = b
    }

    override fun createNormalViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (layoutType.equals(ItemLayoutType.APP_RAIL.name, true))
            return RotateItemViewHolder(
                LayoutProviderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else if (layoutType.equals(ItemLayoutType.SQUARE.name, true))
            return GameViewHolder(
                ItemGameSquareBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else
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
        var contentItem: ContentItem = ContentItem()
        if (holder.itemViewType != VIEW_TYPE_MORE)
            contentItem = mDataList[position]
        contentItem.isPartnerSubscribed = isPackAvailed &&
            !mNonSubscribedPartnerList.contains(contentItem.provider.toLowerCase())
        contentItem.isGuestUser = !mIsUserLogin
        when (holder.itemViewType) {
            PORTRAIT_TYPE -> {
                holder as RailItemViewHolder
                holder.bind(contentItem, sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage)
                point = getPortraitThumbnailDimensionGrid(holder.binding.root.context!!)
                mWidth = point.x
                mHeight = point.y
                val layoutParams =
                    LinearLayout.LayoutParams(mWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

                layoutParams.setMargins(
                    dpToPx(holder.binding.root.context, 4),//left
                    dpToPx(holder.binding.root.context, 4),//top
                    dpToPx(holder.binding.root.context, 4),//right
                    dpToPx(holder.binding.root.context, 8)//bottom
                )
                holder.binding.cardView.layoutParams = layoutParams
                holder.binding.img.layoutParams = FrameLayout.LayoutParams(mWidth, mHeight)
                holder.binding.imgCard.layoutParams = ConstraintLayout.LayoutParams(mWidth, mHeight)
                holder.binding.rlImage.layoutParams = RelativeLayout.LayoutParams(mWidth, mHeight)

                val param = RelativeLayout.LayoutParams(
                    mWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                param.addRule(RelativeLayout.BELOW, holder.binding.rlImage.id)
                param.setMargins(0, 0, 0, dpToPx(holder.binding.root.context, 6))
                holder.binding.commonDetail.root.layoutParams = param

//                holder.binding.imgOverlay.layoutParams =
//                    RelativeLayout.LayoutParams(mWidth, mHeight)
                val url = getCloudinaryUrl(
                    cloudinaryUrl,
                    mWidth, mHeight,
                    contentItem.getImageItem()
                )
                holder.binding.commonDetail.ivBrand.hide()
                holder.binding.commonDetail.expiry.hide()
                if(contentItem.getRental() == null) {
                    if(PROVIDER_TATA_SKY.equals(contentItem.provider, true)){
                        holder.binding.commonDetail.ivBrand.hide()
                    }
                    else {
                        updateProviderImage(
                            holder.binding.commonDetail.ivBrand,
                            contentItem.provider,
                            providerLogos,
                            R.drawable.ic_rail_placeholder
                        )
                        holder.binding.commonDetail.ivBrand.show()
                        holder.binding.commonDetail.expiry.hide()
                    }
                }
                else{
                    holder.binding.commonDetail.expiry.show()
                    holder.binding.commonDetail.ivBrand.hide()
                }
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
                        null,
                        origin = origin
                    )
                }
            }
            LANDSCAPE_TYPE,TITLE -> {
                holder as RailItemViewHolder
                point = getNormalThumbnailDimensionGrid(holder.binding.root.context!!)
                mWidth = point.x
                mHeight = point.y
                handleLandscapeRail(holder,contentItem,mWidth,mHeight,position)
            }
            PROVIDER_TYPE -> {
                holder as RotateItemViewHolder
                holder.bind(contentItem)
                point = getNormalThumbnailDimensionGrid(holder.binding.root.context!!)
//                mWidth = point.x
//                mHeight = point.x - 16
                val layoutParams =
                    RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                if (position % 2 == 0) {
                    layoutParams.setMargins(
                        dpToPx(holder.binding.root.context, 4),
                        dpToPx(holder.binding.root.context, 4),
                        dpToPx(holder.binding.root.context, 8),
                        dpToPx(holder.binding.root.context, 12)
                    )
                } else {
                    layoutParams.setMargins(
                        dpToPx(holder.binding.root.context, 8),
                        dpToPx(holder.binding.root.context, 4),
                        dpToPx(holder.binding.root.context, 4),
                        dpToPx(holder.binding.root.context, 12)
                    )
                }
                holder.binding.cardView.layoutParams = layoutParams

                updateProviderSeeAll(holder.binding.img, contentItem.provider, providerLogos,cloudinaryUrl)

                if (isSubscribed)
                    holder.binding.imgCheck.show()
                else
                    holder.binding.imgCheck.hide()
                holder.binding.root.setOnClickListener {
                    contentItem.isSubscribed = isSubscribed
                    listener.onSubItemClick(
                        contentItem,
                        position,
                        sectionPosition,
                        EventConstants.TYPE_APPS,
                        null,
                        origin = origin
                    )
                }
            }
            GAME_TYPE ->{
                holder as GameViewHolder
//                point = getSquareGameThumnailDimensionForGrid(holder.binding.root.context!!)
//                mWidth = point.x
//                mHeight = point.y
//                handleGameRail(holder,contentItem,mWidth,mHeight,position)
                    handleGameRail(holder,contentItem,0,0,position)

            }
            VIEW_TYPE_MORE -> {
                holder as LoadMoreViewHolder
                holder.itemView.setOnClickListener {
                    if(isAppending)
                        loadMoreClickListener?.onLoadMoreClick((mDataList.size))
                    else
                        removeLoading()
                }
            }
        }
    }

    private fun handleGameRail(
        holder: GameViewHolder,
        contentItem: ContentItem,
        mWidth: Int,
        mHeight: Int,
        position: Int
    ) {
        holder.bind(contentItem)
        val layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(
            dpToPx(holder.binding.root.context, 4),
            dpToPx(holder.binding.root.context, 4),
            dpToPx(holder.binding.root.context, 4),
            dpToPx(holder.binding.root.context, 8)
        )
        holder.binding.clHomeGamingRoot.layoutParams = layoutParams
//        holder.binding.ivItemGameSqaure.layoutParams = ConstraintLayout.LayoutParams(mWidth, mHeight)
        val url = getCloudinaryUrl(
            cloudinaryUrl,
            contentItem.getImageItem()
        )
        imageLoad(holder.binding.ivItemGameSqaure, url)

        holder.binding.root.setOnClickListener {

            listener.onSubItemClick(
                contentItem,
                position,
                sectionPosition,
                EventConstants.TYPE_RAIL,
                transitions = null,
                origin = origin,
                gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                    pageName = pageName?:"", //Page Name
                    railTitle = railTitle?:"",
                    railPosition = railPosition?:"",
                    railCategory = "RAIL",
                    railType = "RAIL",
                    gameGenre = contentItem.getSubTitle(),
                    gamePartner = contentItem.provider,
                    gamePosition = "${position + 1}",
                    gameRating = contentItem.gameRating,
                    releaseYear = contentItem.releaseYear ?: "",
                    source = contentItem.source
                )
            )

        }

    }

    private fun handleLandscapeRail(
        holder: RailItemViewHolder,
        contentItem: ContentItem,
        mWidth: Int,
        mHeight: Int,
        position: Int
    ) {
        holder.bind(contentItem, sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage)
        val layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        if (position % 2 == 0) {
            layoutParams.setMargins(
                dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 8)
            )
        } else {
            layoutParams.setMargins(
                dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 8)
            )
        }
        holder.binding.cardView.layoutParams = layoutParams
        holder.binding.img.layoutParams = FrameLayout.LayoutParams(mWidth, mHeight)
        holder.binding.imgCard.layoutParams = ConstraintLayout.LayoutParams(mWidth, mHeight)
        holder.binding.rlImage.layoutParams = RelativeLayout.LayoutParams(mWidth, mHeight)
        if(isSelectionMode){
            holder.binding.selectOverlay.show()
        }else{
            holder.binding.selectOverlay.hide()
        }

        val param = RelativeLayout.LayoutParams(
            mWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        param.addRule(RelativeLayout.BELOW, holder.binding.rlImage.id)
        param.setMargins(0, 0, 0, dpToPx(holder.binding.root.context, 6))
        holder.binding.commonDetail.root.layoutParams = param

//                holder.binding.imgOverlay.layoutParams =
//                    RelativeLayout.LayoutParams(mWidth, mHeight)
        if(TITLE == holder.itemViewType) {
            holder.binding.rlSportsOverlay.layoutParams =
                FrameLayout.LayoutParams(mWidth, mHeight)
            holder.binding.rlSportsOverlay.show()
        }
        val url = getCloudinaryUrl(
            cloudinaryUrl,
            mWidth, mHeight,
            contentItem.getImageItem()
        )
        imageLoad(holder.binding.img, url)
        holder.binding.commonDetail.ivBrand.hide()
        holder.binding.commonDetail.expiry.hide()
        if(contentItem.getRental() == null) {
            if(PROVIDER_TATA_SKY.equals(contentItem.provider, true)){
                holder.binding.commonDetail.ivBrand.hide()
            }
            else {
                updateProviderImage(
                    holder.binding.commonDetail.ivBrand,
                    contentItem.provider,
                    providerLogos,
                    R.drawable.ic_rail_placeholder
                )
                holder.binding.commonDetail.ivBrand.show()
                holder.binding.commonDetail.expiry.hide()
            }

        }
        else{
            holder.binding.commonDetail.expiry.show()
            holder.binding.commonDetail.ivBrand.hide()
        }

        holder.binding.root.setOnClickListener {
            if (isSelectionMode) {
                toggleItemSelection(position, holder.binding, contentItem)
            } else {
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
                    ),
                    origin = origin,
                    gamesMixpanelInfoModel  = GamesMixpanelInfoModel(
                        pageName = pageName?:"", //Page Name
                        railTitle = railTitle?:"",
                        railPosition = railPosition?:"",
                        railCategory = contentItem.railCategory,
                        railType = "RAIL",
                        gameGenre = contentItem.getSubTitle(),
                        gamePartner = contentItem.provider,
                        gamePosition = "${position + 1}",
                        gameRating = contentItem.gameRating,
                        releaseYear = contentItem.releaseYear ?: "",
                        source = contentItem.source
                    )
                )
            }
        }

        holder.binding.root.setOnLongClickListener {
            if (origin == SOURCE_BINGE_LIST)
                if (!isSelectionMode) {
                    setSelectionMode(true)
                }
            true
        }
        holder.binding.selectOverlay.isSelected = mSelectedItems.contains(position)

    }


    private fun toggleItemSelection(
        position: Int,
        binding: LayoutRailItemBinding,
        contentItem: ContentItem
    ) {
        if (mSelectedItems.contains(position)) {
            binding.selectOverlay.isSelected = false
            mSelectedItems.remove(position)

        } else {
            binding.selectOverlay.isSelected = true
            mSelectedItems[position] =
                ContentIdAndTypeRequest.ContentIdAndType(contentItem.contentId.toInt(),
                    contentItem.contentType)
        }
        mSelectedItemsSize?.value = SingleEvent(mSelectedItems.size)
    }

    fun setSelectionMode(selectionMode: Boolean){
        isSelectionMode = selectionMode
        if (isSelectionMode) {
            mSelectedItemsSize?.value = SingleEvent(0)
        } else {
            mSelectedItemsSize?.value = SingleEvent(-1)
            mSelectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun removeLoading() {
        isAppending = false
    }

    fun addToList(itemsToAdd: List<ContentItem>) {
        this.addTomDataList(itemsToAdd)
        if (mDataList.size < mTotalCount) {
            isAppending = true
        }
    }

    fun updateList(mItems: List<ContentItem>) {
        this.setmDataList(mItems.toMutableList())
        if (mDataList.size < mTotalCount) {
            isAppending = true
        }
    }

    class RailItemViewHolder(val binding: LayoutRailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem, firstEpisodeFreeVerbiage: String?) {
            binding.firstFreeEpisodeVerbiage = firstEpisodeFreeVerbiage
            binding.contentItem = model
            ViewCompat.setTransitionName(binding.img, model.id + "image")
        }
    }

    class GameViewHolder(val binding: ItemGameSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem) {
            binding.contentItem = model
        }
    }

    class RotateItemViewHolder(val binding: LayoutProviderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem) {
            binding.contentItem = model
        }
    }

    public override fun getNormalItemViewType(position: Int): Int {
        return when {
            this.layoutType.equals(ItemLayoutType.PORTRAIT.name, true) -> PORTRAIT_TYPE
            this.layoutType.equals(ItemLayoutType.TITLE_RAIL.name, true) -> TITLE
            this.layoutType.equals(ItemLayoutType.LANDSCAPE.name, true) -> LANDSCAPE_TYPE
            this.layoutType.equals(ItemLayoutType.APP_RAIL.name, true) -> PROVIDER_TYPE
            this.layoutType.equals(ItemLayoutType.SQUARE.name, true) -> GAME_TYPE
            else ->
                LANDSCAPE_TYPE
        }
    }


    fun addLoading() {
        isAppending = true
//        notifyDataSetChanged()
        notifyItemInserted(itemCount)
    }

    fun updateLayoutType(layoutType: String) {
        this.layoutType = layoutType
    }

    fun isSubscribed(isSubscribed: Boolean) {
        this.isSubscribed = isSubscribed
    }

    fun updateCW(continueWatching: Boolean) {
        this.continueWatching = continueWatching
    }

    val LANDSCAPE_TYPE = 1
    val PORTRAIT_TYPE = 2
    val PROVIDER_TYPE = 3
    val GAME_TYPE = 5
    private val TITLE = 4

    fun setTotalItemsCount(totalCount: Int) {
        this.mTotalCount = totalCount
    }

    fun updateListForDiff(
        list: List<ContentItem>, isFilters:Boolean = false
    ) {
        val diffResult = if(isFilters) DiffUtil.calculateDiff(ContentItemFiltersDiffCallback(this.mDataList, list), false) else DiffUtil.calculateDiff(ContentItemDiffCallback(this.mDataList, list), false)
        updateDataWithDiffCallback(list, diffResult)
        if (mDataList.size < mTotalCount) {
            isAppending = true
        }
    }

    fun addToListForDiff(
        list: List<ContentItem>
    ) {
        val tempList = this.mDataList + list
        val diffResult = DiffUtil.calculateDiff(ContentItemDiffCallback(this.mDataList, tempList), false)
        updateDataWithDiffCallback(tempList, diffResult)
        if (tempList.size < mTotalCount) {
            isAppending = true
        }
    }

    fun updateListForDiff(
        list: List<ContentItem>,
        isFilters:Boolean = false,
        continuePaging : Boolean
    ) {
        val diffResult = if(isFilters) DiffUtil.calculateDiff(ContentItemFiltersDiffCallback(this.mDataList, list), false) else DiffUtil.calculateDiff(ContentItemDiffCallback(this.mDataList, list), false)
        updateDataWithDiffCallback(list, diffResult)
        isAppending = continuePaging
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

    fun getSelectedItemArray():Array<ContentIdAndTypeRequest.ContentIdAndType>{
        return mSelectedItems.values.toTypedArray()
    }
}