package com.tatasky.binge.ui.features.home.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.provider.MediaStore.Video.VideoColumns.CATEGORY
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_GAMES
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.PartnerData
import com.tatasky.binge.data.networking.models.response.ProviderLogo
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.circularImageApps
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.interfaces.CommonDTOClickListener
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.home_trailer.TrailerView
import com.tatasky.binge.utils.*

class RailAdapter(
    val listener: CommonDTOClickListener,
    var list: List<ContentItem>,
    var layoutType: String,
    val sectionPosition: Int,
    private val cloudinaryUrl: String?,
    val sectionSource: String,
    var continueWatching: Boolean,
    val railTitle: String,
    val providerLogos: ProviderLogo,
    val railPoint: RailPoint,
    val sharedPrefs: PrefsRepo,
    var mixedLayoutType: String = "",
    var partnerName: String? = null,
    var mNonSubscribedPartnerList: HashSet<String> ?= null,
    var isPackAvailed : Boolean = false,
    var viewPortPosition : Int = -1,
    var refId : String,
    val pageName : String = "",
    val trailerExchanger: ((TrailerView?, Boolean, Int) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var length: Int = 0
    private val PORTRAIT = 0
    private val LANDSCAPE = 1
    private val CIRCULAR = 2
    private val GENRE = 3
    private val LANGUAGE = 4
    private val PROVIDER = 5
    private val GENRE_LANDSCAPE = 6
    private val PORTRAIT_TOP10 = 7
    private val POPULAR_CHARACTER = 8
    private val PROVIDER_PORTRAIT = 9
    private val PROVIDER_LANDSCAPE = 10
    private val SPORTS_RAIL = 11
    private val MID_SCROLL_RAIL = 12
    private val SQUARE = 14
    private val GENRE_RAIL_FOR_GAMES = 15
    private val MID_SCROLL_RAIL_GAME = 16
    private val CATEGORY = 17
    private var mTrailerView: TrailerView? = null
    private var mIsUserLogin = sharedPrefs.getLoginStatus()


    init {
        if(mNonSubscribedPartnerList == null){
            updateSbscriberList()
        }
        e("RailAdapter","sectionSource:$sectionSource, layoutType : $layoutType, " +
                "partnerName: $partnerName")
        e(
            "RailAdapter", "sectionSource:$sectionSource, layoutType : $layoutType, " +
                    "partnerName: $partnerName"
        )
        continueWatching = false

        if(sectionSource.equals(ItemViewType.TITLE_RAIL.name, true))
            layoutType = ItemLayoutType.TITLE_RAIL.name
        else if(sectionSource.equals(ItemLayoutType.POPULAR_CHARACTER.name, true))
            layoutType = ItemLayoutType.POPULAR_CHARACTER.name
        else if (sectionSource.equals(ItemViewType.PROVIDER.name, true)) {
            layoutType = ItemLayoutType.APP_RAIL.name
        }
        else if(sectionSource.equals(ItemLayoutType.LANGUAGE.name, true)){
            layoutType = ItemLayoutType.LANGUAGE.name
        }
        else if (sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true)) {
            layoutType = ItemLayoutType.GENRE_LANDSCAPE.name
        }
        else if(sectionSource.equals(ItemLayoutType.GENRE.name, true)
            && layoutType.equals(ItemLayoutType.LANDSCAPE.name, true)){
            layoutType = ItemLayoutType.GENRE_LANDSCAPE.name
        }
        else if(sectionSource.equals(ItemLayoutType.CATEGORY.name, true)){
            layoutType = ItemLayoutType.CATEGORY.name
        }
        else if(sectionSource.equals(ItemLayoutType.GENRE.name, true)){
            layoutType = ItemLayoutType.GENRE.name
        }
        else if(sectionSource.equals(ItemLayoutType.MID_SCROLL_RAIL.name, true)){
            layoutType = ItemLayoutType.MID_SCROLL_RAIL.name
        }
        else if(sectionSource.equals(ItemViewType.MID_BANNER_GAMES.name, true)){
            layoutType = ItemLayoutType.MID_SCROLL_RAIL.name
        }
//        else if(sectionSource.equals(ItemViewType.GAMES.name, true))
//            layoutType = ItemLayoutType.SQUARE.name
        length = list.size
        if (length > 7 && (layoutType.equals(ItemLayoutType.MIXED.name, true)
                    || layoutType.equals(ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name, true))
        )
            calculateLength()
    }

    fun updateSbscriberList() {
        mNonSubscribedPartnerList = HashSet()
        mNonSubscribedPartnerList = HashSet()
        isPackAvailed = sharedPrefs.getSubscribedPack() != null
        sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
            e("RailAdapter","partnerList:$partnerList")
            for (partner in partnerList){
                mNonSubscribedPartnerList?.add((partner.partnerName ?: "").toLowerCase())
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            layoutType.equals(ItemLayoutType.SQUARE.name, true) -> {
                SQUARE
            }
            layoutType.equals(ItemLayoutType.POPULAR_CHARACTER.name, true) -> {
                POPULAR_CHARACTER
            }
            layoutType.equals(ItemLayoutType.GENRE_LANDSCAPE.name, true) -> {
                GENRE_LANDSCAPE
            }
            layoutType.equals(ItemLayoutType.GENRE.name, true) -> {
                GENRE
            }
            layoutType.equals(ItemLayoutType.CATEGORY.name, true) -> {
                CATEGORY
            }
            layoutType.equals(ItemLayoutType.TOP_PORTRAIT.name, true) -> {
                PORTRAIT_TOP10
            }
            layoutType.equals(ItemLayoutType.PORTRAIT.name, true) -> {
                PORTRAIT
            }
            layoutType.equals(ItemLayoutType.TITLE_RAIL.name, true) -> {
                SPORTS_RAIL
            }
            layoutType.equals(ItemLayoutType.LANDSCAPE.name, true) -> {
                LANDSCAPE
            }
            layoutType.equals(ItemLayoutType.LANGUAGE.name, true) -> {
                LANGUAGE
            }
            layoutType.equals(ItemLayoutType.GENRE_RAIL_FOR_GAMES.name, true) -> {
                GENRE_RAIL_FOR_GAMES
            }
            layoutType.equals(ItemLayoutType.CIRCULAR.name, true) -> {
                CIRCULAR
            }
            layoutType.equals(ItemLayoutType.APP_RAIL.name, true) -> {
                PROVIDER
            }
            layoutType.equals(ItemLayoutType.MIXED.name, true)
                    || layoutType.equals(ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name, true) -> {
                if ( ItemLayoutType.PORTRAIT.name.equals(mixedLayoutType,true)) {
                    return PROVIDER_PORTRAIT
                }
                return when (position) {
                    0 -> PROVIDER_LANDSCAPE
                    else -> PROVIDER_PORTRAIT
                }
            }
            layoutType.equals(ItemLayoutType.MID_SCROLL_RAIL.name, true) -> {
                MID_SCROLL_RAIL
            }
            else -> LANDSCAPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SQUARE -> GameSquareViewHolder(
                ItemGameSquareBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            POPULAR_CHARACTER -> CharacterViewHolder(
                LayoutCharacterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )
            GENRE_LANDSCAPE -> GenreLandViewHolder(
                LayoutGenreLandscapeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )
            CATEGORY -> CategoryViewHolder(
                LayoutCategoryLandscapeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            GENRE -> GenreItemViewHolder(
                LayoutGenreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            LANGUAGE -> GenreItemViewHolder(
                LayoutGenreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            GENRE_RAIL_FOR_GAMES -> GenreItemViewHolder(
                LayoutGenreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            PORTRAIT -> RailItemViewHolder(
                LayoutRailItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            PROVIDER -> RotateItemViewHolder(
                LayoutRotateItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            CIRCULAR -> RotateItemViewHolder(
                LayoutRotateItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            PROVIDER_LANDSCAPE -> RailItemTrailerViewHolder(
                LayoutRailItemTrailerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MID_SCROLL_RAIL -> MidscrollItemViewHolder(
                LayoutMidscrollItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> RailItemViewHolder(
                LayoutRailItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return length
//        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val contentItem = list[position]
            when (getItemViewType(position)) {
                SQUARE -> {
                    if (holder is GameSquareViewHolder) {
                        if (railPoint.gameSquarePoint == null) {
                            railPoint.gameSquarePoint =
                                getSquareGameThumbnailDimension(holder.binding.root.context!!)
                        }
                        val width =
                            railPoint.gameSquarePoint?.x ?: dpToPx(holder.binding.root.context, 120)
                        val layoutParams =
                            ConstraintLayout.LayoutParams(
                                width,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        layoutParams.setMargins(0, 0, dpToPx(holder.binding.root.context, 8), 0)
                        holder.binding.clHomeGamingRoot.layoutParams = layoutParams
                        ConstraintLayout.LayoutParams(width, width)
                            .also { holder.binding.ivItemGameSqaure.layoutParams = it }

                        holder.bind(contentItem)
                        holder.binding.root.setOnClickListener {
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                EventConstants.TYPE_RAIL,
                                null,
                                railTitle,
                                sectionSource,
                                GamesMixpanelInfoModel(
                                    pageName = pageName,
                                    railTitle = railTitle,
                                    railPosition = "$sectionPosition",
                                    railType = contentItem.origin,
                                    railCategory = ItemViewType.RAIL.name,
                                    gameGenre = contentItem.getSubTitle(),
                                    gamePartner = contentItem.provider,
                                    gamePosition = "${position + 1}",
                                    gameRating = contentItem.gameRating,
                                    releaseYear = "",
                                    source = if (pageName.equals(
                                            PROVIDER_GAMEZOP,
                                            true
                                        )
                                    ) SOURCE_GAMES else pageName
                                )
                            )
                        }

                        val url = getCloudinaryUrl(
                            cloudinaryUrl,
                            width, width,
                            contentItem.getImageItem()
                        )
                        val favBtn = holder.binding.lvGameFav

                        // TODO: set the initial state of the fav button first then perofrm click listener
//                    if (contentItem.isGameFavourite) {
//                        favBtn.progress = 1F
//                    } else {
//                        favBtn.progress = 0F
//                    }


                        favBtn.setOnClickListener {
                            if (!favBtn.isAnimating) {
                                if (favBtn.progress == 0F) {
                                    //TODO: Add to favourite
                                    favBtn.playAnimation()
                                } else {
                                    //TODO: Remove from favourite
                                    favBtn.progress = 0F
                                }
                            }
                        }


                        imageLoad(holder.binding.ivItemGameSqaure, url)
                    }
                }
                POPULAR_CHARACTER -> {
                    //Popular character UI
                    holder as CharacterViewHolder
                    if (railPoint.landscapeGenrePoint == null || railPoint.mLandscapeGenreWidth == null) {
                        railPoint.landscapeGenrePoint =
                            getNormalThumbnailForGenreDimension(holder.binding.root.context)
                        railPoint.mLandscapeGenreWidth = railPoint.landscapeGenrePoint?.x
                        railPoint.mLandscapeGenreHeight = railPoint.landscapeGenrePoint?.y
                    }
                    holder.bind(contentItem, cloudinaryUrl, railPoint, position)
                    holder.binding.root.setOnClickListener {

                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            ItemViewType.CHARACTER.name, null, railTitle,
                            sectionSource
                        )
                    }
                }
                CATEGORY -> {
                    holder as CategoryViewHolder
                    if (railPoint.landscapeCategoryPoint == null || railPoint.mLandscapeCategoryWidth == null){
                        railPoint.landscapeCategoryPoint =
                            getNormalThumbnailForCategoryDimension(holder.binding.root.context)
                        railPoint.mLandscapeCategoryWidth = railPoint.landscapeCategoryPoint?.x
                        railPoint.mLandscapeCategoryHeight = railPoint.landscapeCategoryPoint?.y
                    }


                    holder.bind(contentItem,cloudinaryUrl,railPoint,pageName)

                    holder.binding.root.setOnClickListener {
                        if(contentItem.refId.isNullOrEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            ItemViewType.CATEGORY.name,
                            null,
                            railTitle,
                            sectionSource
                        )
                    }
                }


                GENRE_LANDSCAPE -> {
                    holder as GenreLandViewHolder
                    if (railPoint.landscapeGenrePoint == null || railPoint.mLandscapeGenreWidth == null) {
                        railPoint.landscapeGenrePoint =
                            getNormalThumbnailForGenreDimension(holder.binding.root.context)
                        railPoint.mLandscapeGenreWidth = railPoint.landscapeGenrePoint?.x
                        railPoint.mLandscapeGenreHeight = railPoint.landscapeGenrePoint?.y
                    }
                    var isGameGenre = false
                    if (sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true)) {
                        isGameGenre = true
                        holder.binding.tvContentTitle.hide()
                    } else {
                        holder.binding.tvContentTitle.show()
                    }
                    holder.bind(contentItem, cloudinaryUrl, railPoint, pageName, isGameGenre)
                    holder.binding.root.setOnClickListener {
                        if(contentItem.refId.isNullOrEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            if (sectionSource.equals(
                                    ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                                    true
                                )
                            ) ItemViewType.GENRE_RAIL_FOR_GAMES.name else ItemViewType.GENRE.name,
                            null,
                            railTitle,
                            sectionSource
                        )
                    }
                }
                CIRCULAR -> {
                    if (holder is RotateItemViewHolder) {
                        holder.bind(contentItem)
                        if (railPoint.circularPoint == null) {
                            railPoint.circularPoint =
                                getCircularProviderIconPoint(holder.binding.root.context)
                        }
                        val wH =
                            railPoint.circularPoint?.x ?: dpToPx(holder.binding.root.context, 64)
                        val url = getCloudinaryUrl(
                            cloudinaryUrl,
                            wH, wH,
                            contentItem.getImageItem()
                        )
                        val layoutParams =
                            RelativeLayout.LayoutParams(wH, wH)
                        layoutParams.setMargins(0, 0, dpToPx(holder.binding.root.context, 10), 0)
                        holder.binding.rlParent.layoutParams = layoutParams

                        holder.binding.imageView1.layoutParams =
                            RelativeLayout.LayoutParams(wH, wH)
                        circularImageApps(holder.binding.imageView1, url)

                        holder.binding.root.setOnClickListener {
                            if(contentItem.refId.isNullOrEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                EventConstants.TYPE_RAIL,
                                null,
                                railTitle,
                                sectionSource

                            )
                        }
                    }
                }
                PROVIDER -> {
                    if (holder is RotateItemViewHolder) {
                        holder.bind(contentItem)
                        if (railPoint.circularPoint == null) {
                            railPoint.circularPoint =
                                getCircularProviderIconPoint(holder.binding.root.context)
                        }
                        val wH =
                            railPoint.circularPoint?.x ?: dpToPx(holder.binding.root.context, 64)
                        val layoutParams =
                            RelativeLayout.LayoutParams(wH, wH)
                        layoutParams.setMargins(0, 0, dpToPx(holder.binding.root.context, 10), 0)
                        holder.binding.rlParent.layoutParams = layoutParams

                        val rl = RelativeLayout.LayoutParams(wH, wH)
                        /*rl.setMargins( dpToPx(holder.binding.root.context, 8),
                        dpToPx(holder.binding.root.context, 12),
                        dpToPx(holder.binding.root.context, 8),
                        dpToPx(holder.binding.root.context, 12))*/
                        holder.binding.imageView1.layoutParams = rl

                        holder.binding.root.setOnClickListener {
                            if(contentItem.refId.isNullOrEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                EventConstants.TYPE_APPS, null, railTitle,
                                sectionSource
                            )
                        }

                        updateProviderLogo(
                            holder.binding.imageView1,
                            contentItem.provider,
                            providerLogos,
                            R.drawable.ic_detail_placeholder,
                            cloudinaryUrl
                        )
                    }
                }
                GENRE -> {
                    holder as GenreItemViewHolder
                    holder.bind(contentItem, cloudinaryUrl, railPoint, pageName)
                    holder.binding.root.setOnClickListener {
                        if(contentItem.refId.isNullOrEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            ItemViewType.GENRE.name, null, railTitle,
                            sectionSource
                        )
                    }
                }
                LANGUAGE -> {
                    holder as GenreItemViewHolder
                    holder.bind(contentItem, cloudinaryUrl, railPoint, pageName)
                    holder.binding.root.setOnClickListener {
                        if(contentItem.refId.isNullOrEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            ItemViewType.LANGUAGE.name, null,
                            railTitle,
                            sectionSource

                        )
                    }
                }
                GENRE_RAIL_FOR_GAMES -> {
                    holder as GenreItemViewHolder
                    holder.bind(contentItem, cloudinaryUrl, railPoint, pageName)
                    holder.binding.root.setOnClickListener {
                        if(contentItem.refId.isNullOrEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                            null,
                            railTitle,
                            sectionSource
                        )
                    }
                }
                PORTRAIT -> {
                    if (holder is RailItemViewHolder) {
                        if (railPoint.portraitPoint == null || railPoint.mPortraitWidth == null || railPoint.mPortraitHeight == null) {
                            railPoint.portraitPoint =
                                getLargeThumbnailDimension(holder.binding.root.context!!)
                            railPoint.mPortraitWidth =
                                railPoint.portraitPoint?.x//dpToPx(holder.binding.root.context, 137)//
                            railPoint.mPortraitHeight =
                                railPoint.portraitPoint?.y//dpToPx(holder.binding.root.context, 205)//
                        }
                        val width = railPoint.mPortraitWidth ?: 0
                        val height = railPoint.mPortraitHeight ?: 0
                        handleRailItemLayout(
                            holder,
                            contentItem,
                            position,
                            width,
                            height
                        )
                    }
                }
                PORTRAIT_TOP10 -> {
                    if (holder is RailItemViewHolder) {
                        holder.binding.contentItem?.isTop10 = true
                        if (railPoint.portraitTop10Point == null
                            || railPoint.mPortraitTop10Width == null
                            || railPoint.mPortraitTop10Height == null
                        ) {
                            railPoint.portraitTop10Point =
                                getLargeThumbnailTop10Dimension(holder.binding.root.context!!)
                            railPoint.mPortraitTop10Width =
                                railPoint.portraitTop10Point?.x//dpToPx(holder.binding.root.context, 137)//
                            railPoint.mPortraitTop10Height =
                                railPoint.portraitTop10Point?.y//dpToPx(holder.binding.root.context, 205)//
                        }
                        val width = railPoint.mPortraitTop10Width ?: 0
                        val height = railPoint.mPortraitTop10Height ?: 0
                        handleRailItemLayout(
                            holder,
                            contentItem,
                            position,
                            width,
                            height
                        )
                    }
                }
                SPORTS_RAIL -> {
                    if (holder is RailItemViewHolder) {
                        if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
                            railPoint.landscapePoint =
                                getNormalThumbnailDimension(holder.binding.root.context!!)
                            railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                            railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
                        }
                        val width = railPoint.mLandscapeWidth ?: 0
                        val height = railPoint.mLandscapeHeight ?: 0
                        handleRailItemLayout(
                            holder,
                            contentItem,
                            position,
                            width,
                            height
                        )
                    }
                }
                LANDSCAPE -> {
                    if (holder is RailItemViewHolder) {
                        if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
                            railPoint.landscapePoint =
                                getNormalThumbnailDimension(holder.binding.root.context!!)
                            railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                            railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
                        }
                        val width = railPoint.mLandscapeWidth ?: 0
                        val height = railPoint.mLandscapeHeight ?: 0
                        handleRailItemLayout(
                            holder,
                            contentItem,
                            position,
                            width,
                            height
                        )
                    }
                }
                PROVIDER_PORTRAIT -> {
                    handleMixedPortrait(holder, contentItem, position)
                }

                PROVIDER_LANDSCAPE -> {
                    handleMixedLandscape(holder, contentItem, position)
                }

                MID_SCROLL_RAIL -> {
                    holder as MidscrollItemViewHolder
                    val railPoint = RailPoint()
                    if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
                        railPoint.landscapePoint =
                            getMidscrollCardDimension(holder.binding.root.context!!)
                        railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                        railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
                    }
                    val width = railPoint.mLandscapeWidth ?: 0
                    val height = railPoint.mLandscapeHeight ?: 0
                    val layoutParams = ConstraintLayout.LayoutParams(width, height)
                    layoutParams.setMargins(
                        dpToPx(holder.binding.root.context, 4),//left
                        dpToPx(holder.binding.root.context, 0),//top
                        dpToPx(holder.binding.root.context, 4),//right
                        dpToPx(holder.binding.root.context, 0)//bottom
                    )
                    holder.binding.root.layoutParams = layoutParams

                    var sectionType = if (sectionSource.equals(
                            ItemViewType.MID_BANNER_GAMES.name,
                            true
                        )
                    ) EventConstants.TYPE_MID_SCROLL_GAMES else EventConstants.TYPE_MID_SCROLL

                    if(sectionSource.equals(EventConstants.TYPE_MID_SCROLL_BANNER,true)){
                        sectionType = EventConstants.TYPE_MID_SCROLL_BANNER
                    }

                    holder.binding.root.setOnClickListener {
                        if(contentItem.refId.isNullOrEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            sectionType,
                            null,
                            railTitle,
                            sectionSource
                        )
                    }
                    holder.bind(contentItem,sectionType)


                }
            }
        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }


    class GameSquareViewHolder(val binding: ItemGameSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem) {
            binding.contentItem = model
//            ViewCompat.setTransitionName(binding.img, model.id + "image")
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

    class RotateItemViewHolder(val binding: LayoutRotateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem) {
            binding.contentItem = model
        }
    }

    class GenreItemViewHolder(val binding: LayoutGenreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ContentItem, cloudinaryUrl: String?, railPoint: RailPoint, pageName: String) {
            binding.contentItem = model
//            if(pageName.equals(SOURCE_KIDS,true))
//                binding.tvContentTitle.hide()
            val layoutParams =
                LinearLayout.LayoutParams(railPoint.mCircularWidth!!, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, 0, dpToPx(binding.root.context, 6), 0)
            binding.rlParent.layoutParams = layoutParams

            binding.frame.layoutParams =
                LinearLayout.LayoutParams(railPoint.mCircularWidth!!, railPoint.mCircularWidth!!)
            val url = getCloudinaryUrl(
                cloudinaryUrl,
                model.image
            )
            transparentImageLoad(binding.ivGenre, url)
        }
    }

    class CategoryViewHolder(val binding: LayoutCategoryLandscapeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(
            model: ContentItem,
            cloudinaryUrl: String?,
            railPoint : RailPoint,
            pageName: String
        ) {
            binding.contentItem = model
            val layoutParams =
                LinearLayout.LayoutParams(railPoint.mLandscapeCategoryWidth!!, railPoint.mLandscapeCategoryHeight!!)
            layoutParams.setMargins(0, 0, dpToPx(binding.root.context, 6), 0)
            binding.cardView.layoutParams = layoutParams
            binding.img.layoutParams =
                ConstraintLayout.LayoutParams(railPoint.mLandscapeCategoryWidth!!, railPoint.mLandscapeCategoryHeight!!)
             binding.rlImage.layoutParams = LinearLayout.LayoutParams(railPoint.mLandscapeCategoryWidth!!,
                railPoint.mLandscapeCategoryHeight!!)


            val url =
                    getCloudinaryUrl(
                        cloudinaryUrl,
                        railPoint.mLandscapeCategoryWidth!!, railPoint.mLandscapeCategoryHeight!!,
                        model.image
                    )
            imageLoad(binding.img, url)
        }
    }

    class GenreLandViewHolder(val binding: LayoutGenreLandscapeBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(
            model: ContentItem,
            cloudinaryUrl: String?,
            railPoint: RailPoint,
            pageName: String,
            isGameGenre: Boolean
        ) {
            binding.contentItem = model
            val layoutParams =
                LinearLayout.LayoutParams(railPoint.mCircularWidth!!, railPoint.mCircularWidth!!)
            layoutParams.setMargins(0, 0, dpToPx(binding.root.context, 6), 0)
            binding.cardView.layoutParams = layoutParams
            binding.img.layoutParams =
                RelativeLayout.LayoutParams(railPoint.mCircularWidth!!, railPoint.mCircularWidth!!)
            binding.imgOverlay.layoutParams =
                RelativeLayout.LayoutParams(railPoint.mCircularWidth!!, railPoint.mCircularWidth!!)
            binding.rlImage.layoutParams = LinearLayout.LayoutParams(railPoint.mCircularWidth!!,
                railPoint.mCircularWidth!!)

            val url =
                if(isGameGenre)
                    getCloudinaryUrl(
                        cloudinaryUrl,
                        model.image
                    )
                else
                    getCloudinaryUrl(
                        cloudinaryUrl,
                        railPoint.mLandscapeGenreWidth!!, railPoint.mLandscapeGenreHeight!!,
                        model.image
                    )
            e("GlideHelperGenre","url : $url"+"scale"+binding.img.scaleType)
            imageLoad(binding.img, url)
        }
    }

    class CharacterViewHolder(val binding: LayoutCharacterBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(model: ContentItem, cloudinaryUrl: String?, railPoint: RailPoint, position: Int) {
            binding.contentItem = model
            val layoutParams =
                LinearLayout.LayoutParams(railPoint.mCircularWidth!!, railPoint.mCircularWidth!!)
            layoutParams.setMargins(0, 0, dpToPx(binding.root.context, 4), 0)
            binding.llRoot.layoutParams = layoutParams
            binding.imgCharacter.layoutParams =
                LinearLayout.LayoutParams(railPoint.mCircularWidth!!, railPoint.mCircularWidth!!)
            val drawableName = "character"+(position%5)
            val drawableResourceId: Int = binding.root.context.resources
                .getIdentifier(drawableName, "drawable", binding.root.context.packageName)
            binding.imgCharacter.setImageResource(drawableResourceId)
        }
    }


    inner class MidscrollItemViewHolder(val binding: LayoutMidscrollItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contentItem: ContentItem, sectionType: String? = null) {
            imageLoad(binding.ivMidScroll, contentItem.image)
//            if (sectionType.equals(EventConstants.TYPE_MID_SCROLL_BANNER)) {
//                binding.ivGradient.show()
//                val gd = GradientDrawable(
//                    GradientDrawable.Orientation.TOP_BOTTOM,
//                    intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK)
//                )
//                gd.cornerRadius = 0f
//                binding.ivGradient.setImageDrawable(gd)
//            } else {
//                binding.ivGradient.hide()
//            }

//            if (contentItem.screenName?.equals(MID_SCROLL_DETAIL_SCREEN, true) == true) {
//                binding.tvContentTitle.show()
//                binding.ivProvider.show()
//                updateCircularProviderLogo(
//                    binding.ivProvider,
//                    contentItem.provider,
//                    providerLogos,
//                    R.drawable.ic_rail_placeholder,
//                    null
//                )
//                binding.tvContentTitle.text = contentItem.title
//                imageLoad(binding.ivMidScroll, contentItem.image)
//            } else {
//                binding.tvContentTitle.hide()
//                binding.ivProvider.hide()
//                imageLoad(binding.ivMidScroll, contentItem.image)
//            }
        }
    }

    inner class RailItemTrailerViewHolder(val binding: LayoutRailItemTrailerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contentItem: ContentItem, position: Int) {
            if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
                railPoint.landscapePoint =
                    getNormalThumbnailDimension(binding.root.context!!)
                railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
            }
            val layoutParams =
                ConstraintLayout.LayoutParams(
                    railPoint.mLandscapeWidth!! + 30,
                    railPoint.mLandscapeHeight!! + dpToPx(binding.root.context, 35)
                )
            layoutParams.setMargins(0, 0, 0, dpToPx(binding.root.context, 2))
            binding.clItemTrailer.layoutParams = layoutParams
            binding.cvTrailer.layoutParams = ConstraintLayout.LayoutParams(
                railPoint.mLandscapeWidth!! + 30,
                railPoint.mLandscapeHeight!!
            )

            binding.tvTrailerName.text = contentItem.title
            showTrailerImage(contentItem)


            mTrailerView = binding.itemTrailer
            mTrailerView?.setTrailerUrl(contentItem.trailerUrl!!)
            d("TrailerView"," Url is ==>"+contentItem.trailerUrl)
            mTrailerView?.setTrailerStartLambda {
                binding.imgTrailer.visibility = View.GONE
                binding.itemTrailer.visibility = View.VISIBLE
            }
            mTrailerView?.setTrailerFinishLambda {
                d("TrailerView","inside finish")
                showTrailerImage(contentItem)
            }

            if(!TextUtils.isEmpty(contentItem.trailerUrl))
                trailerExchanger?.invoke(mTrailerView, true, viewPortPosition)


            binding.root.setOnClickListener {
                contentItem.railCategory = sectionSource
                if(contentItem.refId.isNullOrEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null,
                    railTitle,
                    sectionSource
                )
            }


        }

        private fun showTrailerImage(contentItem: ContentItem) {
            binding.imgTrailer.apply {
                visibility = View.VISIBLE
                setImageDrawable(this.context.resources.getDrawable(R.drawable.shp_placeholder))
            }


            binding.itemTrailer.visibility = View.GONE
            val url = getCloudinaryUrl(
                cloudinaryUrl,
                railPoint.mLandscapeWidth!! + 30,
                railPoint.mLandscapeHeight!!,
                contentItem.getImageItem()
            )
            imageLoad(binding.imgTrailer, url)
        }
    }


    private fun handleMixedPortrait(
        holder: RecyclerView.ViewHolder,
        contentItem: ContentItem,
        position: Int
    ) {
        if (holder is RailItemViewHolder) {
            contentItem.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
                    SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true) &&
                    (mNonSubscribedPartnerList?.contains(contentItem.provider.toLowerCase()) == false)
            holder.bind(contentItem, sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage)
            if (railPoint.portraitMixedPoint == null) {
                railPoint.portraitMixedPoint =
                    getPortraitMixedThumbnailDimension(holder.binding.root.context!!)
            }
            val mPortraitWidth =
                railPoint.portraitMixedPoint?.x
            val mPortraitHeight =
                railPoint.portraitMixedPoint?.y
            val layoutParams =
                RelativeLayout.LayoutParams(
                    mPortraitWidth!!,
                    mPortraitHeight!! + dpToPx(holder.binding.root.context, 26)
                )
//                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(
                dpToPx(holder.binding.root.context, 4), 0, dpToPx(holder.binding.root.context, 4),
                dpToPx(holder.binding.root.context, 0)
            )
            holder.binding.cardView.layoutParams = layoutParams
            holder.binding.tvEpisodeFree.textSize = 8.0f
            holder.binding.img.layoutParams =
                FrameLayout.LayoutParams(mPortraitWidth, mPortraitHeight)
            holder.binding.imgCard.layoutParams = ConstraintLayout.LayoutParams(
                mPortraitWidth,
                mPortraitHeight
            )
            holder.binding.rlImage.layoutParams =
                RelativeLayout.LayoutParams(mPortraitWidth, mPortraitHeight)

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                mPortraitWidth,
                mPortraitHeight,
                contentItem.getImageItem()
            )
            imageLoad(holder.binding.img, url)
            if (partnerName?.length ?: 0 > 1) {
                holder.binding.commonDetail.root.hide()
            } else {
                holder.binding.commonDetail.root.show()
//                holder.binding.commonDetail.ivBrand.show()
            }
            /*

            if(layoutType.equals(ItemLayoutType.MIXED.name, true)){
                val lp2 = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(holder.binding.root.context, 14)
                )
                lp2.topMargin = dpToPx(holder.binding.root.context, 2)
                lp2.addRule(RelativeLayout.ALIGN_PARENT_END)
                holder.binding.commonDetail.ivCrownSmall.layoutParams = lp2
            }
            else*/ if (layoutType == ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name){
                if(holder.binding.commonDetail.ivBrand.visibility == VISIBLE) {
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(holder.binding.root.context, 14)
                    )
                    lp.topMargin = dpToPx(holder.binding.root.context, 4)
                    lp.bottomMargin = dpToPx(holder.binding.root.context, 4)
                    holder.binding.commonDetail.ivBrand.layoutParams = lp
                }
            }
            if(holder.binding.commonDetail.ivBrand.visibility == VISIBLE) {
                val lp = RelativeLayout.LayoutParams(
                    dpToPx(holder.binding.root.context, 54),
                    dpToPx(holder.binding.root.context, 14)
                )
                lp.topMargin = dpToPx(holder.binding.root.context, 4)
                lp.bottomMargin = dpToPx(holder.binding.root.context, 4)
                holder.binding.commonDetail.ivBrand.layoutParams = lp
            }
            updateProviderImage(
                holder.binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )
            holder.binding.root.setOnClickListener {
                if(sectionSource == ItemViewType.PROVIDER_BROWSE_APPS.name
                    ||sectionSource == ItemViewType.SHUFFLE_RAIL.name)
                    contentItem.railCategory = sectionSource
                if(contentItem.refId.isNullOrEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null, railTitle,
                    sectionSource
                )
            }
        }
    }

    private fun handleMixedLandscape(
        holder: RecyclerView.ViewHolder,
        contentItem: ContentItem,
        position: Int
    ) {
        if (holder is RailItemTrailerViewHolder) {
            holder.bind(contentItem,position)
        }
    }

    fun updateListForApps(partnerData: PartnerData, mNonSubscribedPartnerList: HashSet<String>) {
        this.mNonSubscribedPartnerList = mNonSubscribedPartnerList
        this.partnerName = partnerData.provider
        this.mixedLayoutType = partnerData.layoutType
        mTrailerView?.pausePlayer()
        trailerExchanger?.invoke(null, false, sectionPosition)
        if (partnerData.filteredContentItems.size > 7) {
            calculateLength()
        } else {
            length = partnerData.filteredContentItems.size
        }
        list = partnerData.filteredContentItems
        notifyDataSetChanged()
        for(content in list){
            if(!TextUtils.isEmpty(content.trailerUrl))
            {
                mTrailerView?.playUrl()
                break

            }
        }
    }


    private fun handleRailItemLayout(
        holder: RailItemViewHolder, contentItem: ContentItem,
        position: Int, width: Int, height: Int
    ){
        contentItem.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
                SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true) &&
                (mNonSubscribedPartnerList?.contains(contentItem.provider.toLowerCase()) == false)
        holder.bind(contentItem, sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage)
        val layoutParams =
            RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, 0, dpToPx(holder.binding.root.context, 8), 0)
        holder.binding.cardView.layoutParams = layoutParams
        FrameLayout.LayoutParams(width, height).also { holder.binding.img.layoutParams = it }
        holder.binding.imgCard.layoutParams = ConstraintLayout.LayoutParams(width, height)
        holder.binding.rlImage.layoutParams = RelativeLayout.LayoutParams(width, height)

        holder.binding.imgOverlay.layoutParams =
            FrameLayout.LayoutParams(
                width,
                height
            )
        val param = RelativeLayout.LayoutParams(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        param.addRule(RelativeLayout.BELOW, holder.binding.rlImage.id)
        if(ItemLayoutType.TOP_PORTRAIT.name == layoutType){
            param.setMargins(0, 0, 0, dpToPx(holder.binding.root.context, 8))
        }
        holder.binding.commonDetail.root.layoutParams = param

        if(ItemLayoutType.TITLE_RAIL.name == layoutType) {
            holder.binding.rlSportsOverlay.layoutParams =
                FrameLayout.LayoutParams(width, height)
            holder.binding.rlSportsOverlay.show()
            if(ItemViewType.BACKGROUND_BANNER_RAIL.name == sectionSource) {
                if (contentItem.provider.equals(PROVIDER_GAMEZOP, true)) {
                    holder.binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                } else {
                    holder.binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            holder.binding.tvTitle.context,
                            R.drawable.ic_play_title_rail
                        ), null, null, null
                    )
                }
                holder.binding.commonDetail.root.hide()
            }
            else
                holder.binding.commonDetail.root.show()
        }
        holder.binding.commonDetail.ivBrand.show()
        if(contentItem.getRental() != null ){
            holder.binding.commonDetail.ivBrand.hide()
        }
        else if(PROVIDER_TATA_SKY.equals(contentItem.provider, true)){
            holder.binding.commonDetail.ivBrand.hide()
        }
        else if(ItemLayoutType.TOP_PORTRAIT.name == layoutType){
            val drawableName = "ic_top_"+((position%10)+1)
            val drawableResourceId: Int = holder.binding.root.context.resources
                .getIdentifier(drawableName, "drawable", holder.binding.root.context.packageName)
            holder.binding.tvTrendingNumber.setImageResource(drawableResourceId)
            holder.binding.tvTrendingNumber.show()
//            holder.binding.viewRange.show()
            holder.binding.imgOverlay.show()
            holder.binding.contentItem?.isTop10 = true
        }

        val url = getCloudinaryUrl(
            cloudinaryUrl,
            width, height,
            contentItem.getImageItem()
        )

        imageLoad(holder.binding.img, url)
        if (holder.binding.commonDetail.root.visibility == View.VISIBLE) {
            updateProviderImage(
                holder.binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )
        }
        var transition: List<Pair<View, String>>? = null
        //in case of portrait transition should be null
        if (ItemLayoutType.PORTRAIT.name != layoutType && ItemLayoutType.TOP_PORTRAIT.name != layoutType)
            transition = listOf(
                Pair(
                    holder.binding.img,
                    ViewCompat.getTransitionName(holder.binding.img)!!
                )
            )
        holder.binding.root.setOnClickListener {
            if(contentItem.refId.isNullOrEmpty())
                contentItem.refId = refId
            listener.onSubItemClick(
                contentItem,
                position,
                sectionPosition,
                EventConstants.TYPE_RAIL,
                transition,
                railTitle,
                sectionSource,
                GamesMixpanelInfoModel(
                    pageName = pageName,
                    railTitle = railTitle,
                    railPosition = "$sectionPosition",
                    railType = contentItem.origin,
                    railCategory = ItemViewType.RAIL.name,
                    gameGenre = contentItem.getSubTitle(),
                    gamePartner = contentItem.provider,
                    gamePosition = "${position + 1}",
                    gameRating = contentItem.gameRating,
                    releaseYear = "",
                    source = if (pageName.equals(
                            PROVIDER_GAMEZOP,
                            true
                        )
                    ) SOURCE_GAMES else pageName
                )
            )
        }
    }

    private fun calculateLength() {
        length = if (ItemLayoutType.PORTRAIT.name.equals(mixedLayoutType, true)) {
            8
        } else {
            7
        }
    }

}