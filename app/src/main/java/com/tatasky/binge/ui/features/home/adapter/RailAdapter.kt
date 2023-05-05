package com.tatasky.binge.ui.features.home.adapter

import android.graphics.Point
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.analytics.RAIL
import com.tatasky.binge.analytics.SOURCE_GAMES
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.PartnerData
import com.tatasky.binge.data.networking.models.response.ProviderLogo
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.ItemGameSquareBinding
import com.tatasky.binge.databinding.LayoutCategoryLandscapeBinding
import com.tatasky.binge.databinding.LayoutChannelRailItemBinding
import com.tatasky.binge.databinding.LayoutCharacterBinding
import com.tatasky.binge.databinding.LayoutGenreBinding
import com.tatasky.binge.databinding.LayoutGenreGamesBinding
import com.tatasky.binge.databinding.LayoutGenreLandscapeBinding
import com.tatasky.binge.databinding.LayoutLanguageRailItemBinding
import com.tatasky.binge.databinding.LayoutMidscrollItemBinding
import com.tatasky.binge.databinding.LayoutPortraitRailItemBinding
import com.tatasky.binge.databinding.LayoutProviderPortraitRailItemBinding
import com.tatasky.binge.databinding.LayoutRailItemBinding
import com.tatasky.binge.databinding.LayoutRailItemTempBinding
import com.tatasky.binge.databinding.LayoutRailItemTrailerBinding
import com.tatasky.binge.databinding.LayoutRailTopTenPortraitItemBinding
import com.tatasky.binge.databinding.LayoutRotateItemBinding
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
import com.tatasky.binge.ui.features.home.model.RailItemsModel
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointCharacterGenre
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointGameSquare
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointLargeThumbnail
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointMidScrollCard
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointNormal
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointNormalCategory
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointNormalForGenre
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointPortraitMixedThumbnail
import com.tatasky.binge.ui.features.home.sub.SubFragment.Companion.pointTop10
import com.tatasky.binge.utils.*
import java.util.*

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
    var mNonSubscribedPartnerList: HashSet<String>? = null,
    var isPackAvailed: Boolean = false,
    var viewPortPosition: Int = -1,
    var refId: String,
    val pageName: String = "",
    val railSectionType: String = RAIL.uppercase(),
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
    private val BINGE_CHANNEL = 18
    private val SINGLE_PROVIDER_BANNER_RAIL = 19
    private var mTrailerView: TrailerView? = null
    private var mIsUserLogin = sharedPrefs.getLoginStatus()


    init {
        if (mNonSubscribedPartnerList == null) {
            updateSbscriberList()
        }
        e(
            "RailAdapter", "sectionSource:$sectionSource, layoutType : $layoutType, " +
                    "partnerName: $partnerName"
        )
        continueWatching = false
        if (sectionSource.equals(ItemViewType.BINGE_CHANNEL.name, true))
            layoutType = ItemLayoutType.BINGE_CHANNEL.name
        else if (sectionSource.equals(ItemViewType.DARSHAN_CHANNEL.name, true))
            layoutType = ItemLayoutType.LANDSCAPE.name
        else if (sectionSource.equals(ItemViewType.TITLE_RAIL.name, true))
            layoutType = ItemLayoutType.TITLE_RAIL.name
        else if (sectionSource.equals(ItemLayoutType.POPULAR_CHARACTER.name, true))
            layoutType = ItemLayoutType.POPULAR_CHARACTER.name
        else if (sectionSource.equals(ItemViewType.PROVIDER.name, true)) {
            layoutType = ItemLayoutType.APP_RAIL.name
        } else if (sectionSource.equals(ItemLayoutType.LANGUAGE.name, true)) {
            layoutType = ItemLayoutType.LANGUAGE.name
        } else if (sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true)) {
            layoutType = ItemLayoutType.GENRE_GAMES.name
        } else if (sectionSource.equals(ItemLayoutType.GENRE.name, true)
            && layoutType.equals(ItemLayoutType.LANDSCAPE.name, true)
        ) {
            layoutType = ItemLayoutType.GENRE_LANDSCAPE.name
        } else if (sectionSource.equals(ItemLayoutType.CATEGORY.name, true)) {
            layoutType = ItemLayoutType.CATEGORY.name
        } else if (sectionSource.equals(ItemLayoutType.GENRE.name, true)) {
            layoutType = ItemLayoutType.GENRE.name
        } else if (sectionSource.equals(ItemLayoutType.MID_SCROLL_RAIL.name, true)) {
            layoutType = ItemLayoutType.MID_SCROLL_RAIL.name
        } else if (sectionSource.equals(ItemViewType.MID_BANNER_GAMES.name, true)) {
            layoutType = ItemLayoutType.MID_SCROLL_RAIL.name
        }
        length = list.size
        if (length > 7 && (layoutType.equals(ItemLayoutType.MIXED.name, true)
                    || layoutType.equals(ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name, true))
        )
            calculateLength()
    }

    /** Generate the content related analytics data
     * Pass this data while binding, as sometimes getting
     * data of another rail
     */
    private fun getContentAnalyticsModel() = ContentAnalyticsModel(
        sectionSource,
        railSectionType,
        railTitle
    )

    fun updateSbscriberList() {
//        mNonSubscribedPartnerList = HashSet()
//        mNonSubscribedPartnerList = HashSet()
//        isPackAvailed = sharedPrefs.getSubscribedPack() != null
//        sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
//            e("RailAdapter","partnerList:$partnerList")
//            for (partner in partnerList){
//                mNonSubscribedPartnerList?.add((partner.partnerName ?: "").toLowerCase())
//            }
//        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            layoutType.equals(ItemLayoutType.BINGE_CHANNEL.name, true) -> {
                BINGE_CHANNEL
            }
            layoutType.equals(ItemLayoutType.SQUARE.name, true) -> {
                SQUARE
            }
            layoutType.equals(ItemLayoutType.POPULAR_CHARACTER.name, true) -> {
                POPULAR_CHARACTER
            }
            layoutType.equals(ItemLayoutType.GENRE_LANDSCAPE.name, true) -> {
                GENRE_LANDSCAPE
            }
            layoutType.equals(ItemLayoutType.GENRE_GAMES.name, true) -> {
                GENRE_RAIL_FOR_GAMES
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
            layoutType.equals(ItemLayoutType.SINGLE_PROVIDER_BANNER_RAIL.name, true) -> {
                SINGLE_PROVIDER_BANNER_RAIL
            }
            layoutType.equals(ItemLayoutType.LANDSCAPE.name, true) -> {
                LANDSCAPE
            }
            layoutType.equals(ItemLayoutType.LANGUAGE.name, true) -> {
                LANGUAGE
            }
            layoutType.equals(ItemLayoutType.CIRCULAR.name, true) -> {
                CIRCULAR
            }
            layoutType.equals(ItemLayoutType.APP_RAIL.name, true) -> {
                PROVIDER
            }
            layoutType.equals(ItemLayoutType.MIXED.name, true)
                    || layoutType.equals(ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name, true) -> {
                if (ItemLayoutType.PORTRAIT.name.equals(mixedLayoutType, true)) {
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
            BINGE_CHANNEL -> {
                ChannelItemViewHolder(
                    LayoutChannelRailItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            SQUARE -> GameSquareViewHolder(
                ItemGameSquareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            POPULAR_CHARACTER -> CharacterViewHolder(
                LayoutCharacterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            GENRE_LANDSCAPE -> GenreLandViewHolder(
                LayoutGenreLandscapeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            CATEGORY -> CategoryViewHolder(
                LayoutCategoryLandscapeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            GENRE_RAIL_FOR_GAMES -> GenreGameViewHolder(
                LayoutGenreGamesBinding.inflate(
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
            LANGUAGE -> LanguageItemViewHolder(
                LayoutLanguageRailItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            PORTRAIT -> RailItemPortraitViewHolder(
                LayoutPortraitRailItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            PROVIDER, CIRCULAR -> RotateItemViewHolder(
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
            PROVIDER_PORTRAIT -> RailItemProviderPortraitViewHolder(
                LayoutProviderPortraitRailItemBinding.inflate(
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
            PORTRAIT_TOP10 -> RailItemTop10PortraitViewHolder(
                (LayoutRailTopTenPortraitItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ))
            )
            SINGLE_PROVIDER_BANNER_RAIL -> SingleProviderBannerRailViewHolder(
                LayoutRailItemTempBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> RailItemTempViewHolder(
                LayoutRailItemTempBinding.inflate(
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
                BINGE_CHANNEL -> {
                    if (holder is ChannelItemViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(
                            contentItem,
                            pointCharacterGenre,
                            /** Passing the data while binding,
                             * as sometimes getting data of another rail
                             */
                            RailItemsModel(
                                list,
                                sectionSource,
                                layoutType,
                                railSectionType,
                                railTitle,
                                if (sectionSource.equals(RECOMMENDATION, true))
                                    RECOMMENDATION
                                else
                                    EDITORIAL.uppercase()
                            ),
                            contentAnalyticsModel
                        )
                    }
                }
                SQUARE -> {
                    if (holder is GameSquareViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(contentItem, position, pointGameSquare, contentAnalyticsModel)
                    }
                }
                POPULAR_CHARACTER -> {
                    //Popular character UI
                    if (holder is CharacterViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(
                            contentItem,
                            pointNormalForGenre,
                            position,
                            contentAnalyticsModel
                        )
                    }
                }
                CATEGORY -> {
                    if (holder is CategoryViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(
                            contentItem,
                            pointNormalCategory,
                            position,
                            contentAnalyticsModel
                        )
                    }
                }

                GENRE_LANDSCAPE -> {
                    if (holder is GenreLandViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(
                            contentItem,
                            pointCharacterGenre,
                            position,
                            contentAnalyticsModel
                        )
                    }
                }
                CIRCULAR -> {
                    if (holder is RotateItemViewHolder) {
                        holder.bind(contentItem)

                        val url = getCloudinaryUrl(
                            cloudinaryUrl,
                            holder.binding.rlParent.width, holder.binding.rlParent.height,
                            contentItem.getImageItem()
                        )
                        circularImageApps(holder.binding.imageView1, url)
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.binding.root.setOnClickListener {
                            if (contentItem.refId.isEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                EventConstants.TYPE_RAIL,
                                null,
                                railTitle,
                                sectionSource,
                                contentAnalyticsModel = contentAnalyticsModel
                            )
                        }
                    }
                }
                PROVIDER -> {
                    if (holder is RotateItemViewHolder) {
                        holder.bind(contentItem)
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.binding.root.setOnClickListener {
                            if (contentItem.refId.isNullOrEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                EventConstants.TYPE_APPS,
                                null,
                                railTitle,
                                sectionSource,
                                contentAnalyticsModel = contentAnalyticsModel
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
                    if (holder is GenreItemViewHolder) {
                        holder.bind(contentItem, pointCharacterGenre)
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.binding.root.setOnClickListener {
                            if (contentItem.refId.isNullOrEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                ItemViewType.GENRE.name,
                                null,
                                railTitle,
                                sectionSource,
                                contentAnalyticsModel = contentAnalyticsModel
                            )
                        }
                    }
                }
                LANGUAGE -> {
                    if (holder is LanguageItemViewHolder) {
                        holder.bind(contentItem, pointCharacterGenre)
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.binding.root.setOnClickListener {
                            if (contentItem.refId.isNullOrEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                ItemViewType.LANGUAGE.name, null,
                                railTitle,
                                sectionSource,
                                contentAnalyticsModel = contentAnalyticsModel
                            )
                        }
                    }
                }
                GENRE_RAIL_FOR_GAMES -> {
                    holder as GenreGameViewHolder
                    val contentAnalyticsModel = getContentAnalyticsModel()
                    holder.bind(contentItem, pointCharacterGenre, position, contentAnalyticsModel)
                    holder.binding.root.setOnClickListener {
                        if (contentItem.refId.isEmpty())
                            contentItem.refId = refId
                        listener.onSubItemClick(
                            contentItem,
                            position,
                            sectionPosition,
                            ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                            null,
                            railTitle,
                            sectionSource,
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
                PORTRAIT -> {
                    if (holder is RailItemPortraitViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(contentItem, pointLargeThumbnail, contentAnalyticsModel)
                    }
                }
                PORTRAIT_TOP10 -> {
                    println("Top 10")
                    if (holder is RailItemTop10PortraitViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(contentItem, pointTop10, position, contentAnalyticsModel)
                    }
                }
                SPORTS_RAIL, LANDSCAPE -> {
                    if (holder is RailItemTempViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(
                            contentItem,
                            pointNormal,
                            /** Passing the data while binding,
                             * as sometimes getting data of another rail
                             */
                            if (sectionSource == ItemViewType.DARSHAN_CHANNEL.name)
                                RailItemsModel(
                                    list,
                                    sectionSource,
                                    layoutType,
                                    railSectionType,
                                    railTitle,
                                    if (sectionSource.equals(RECOMMENDATION, true))
                                        RECOMMENDATION
                                    else
                                        EDITORIAL.uppercase()
                                )
                            else
                                null,
                            contentAnalyticsModel
                        )
                    }
                }
                SINGLE_PROVIDER_BANNER_RAIL -> {
                    if(holder is SingleProviderBannerRailViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(contentItem, pointNormal, contentAnalyticsModel)
                    }
                }
                PROVIDER_PORTRAIT -> {
                    //   handleMixedPortrait(holder, contentItem, position)
                    if (holder is RailItemProviderPortraitViewHolder) {
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.bind(contentItem, pointPortraitMixedThumbnail, contentAnalyticsModel)
                    }
                }

                PROVIDER_LANDSCAPE -> {
                    handleMixedLandscape(holder, contentItem, position)
                }

                MID_SCROLL_RAIL -> {
                    if (holder is MidscrollItemViewHolder) {
                        val layoutParams = holder.binding.mcvRoot.layoutParams
                        layoutParams.width = pointMidScrollCard?.x ?: 0
                        layoutParams.height = pointMidScrollCard?.y ?: 0
                        holder.binding.root.layoutParams = layoutParams

                        var sectionType = if (sectionSource.equals(
                                ItemViewType.MID_BANNER_GAMES.name,
                                true
                            )
                        ) EventConstants.TYPE_MID_SCROLL_GAMES else EventConstants.TYPE_MID_SCROLL

                        if (sectionSource.equals(EventConstants.TYPE_MID_SCROLL_BANNER, true)) {
                            sectionType = EventConstants.TYPE_MID_SCROLL_BANNER
                        }
                        val contentAnalyticsModel = getContentAnalyticsModel()
                        holder.binding.root.setOnClickListener {
                            if (contentItem.refId.isNullOrEmpty())
                                contentItem.refId = refId
                            listener.onSubItemClick(
                                contentItem,
                                position,
                                sectionPosition,
                                sectionType,
                                null,
                                railTitle,
                                sectionSource,
                                contentAnalyticsModel = contentAnalyticsModel
                            )
                        }
                        holder.bind(contentItem, sectionType)

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    inner class GameSquareViewHolder(val binding: ItemGameSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            position: Int,
            pointGameSquare: Point?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem

            val layoutParam = binding.ivItemGameSqaure.layoutParams
            layoutParam.width = pointGameSquare?.x ?: dpToPx(binding.root.context, 120)
            layoutParam.height = pointGameSquare?.y ?: dpToPx(binding.root.context, 120)
            binding.ivItemGameSqaure.layoutParams = layoutParam

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                pointGameSquare?.x ?: dpToPx(binding.root.context, 120),
                pointGameSquare?.y ?: dpToPx(binding.root.context, 120),
                contentItem.getImageItem()
            )

            imageLoad(binding.ivItemGameSqaure, url)

            binding.root.setOnClickListener {
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
                    ),
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
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

    inner class RailItemProviderPortraitViewHolder(val binding: LayoutProviderPortraitRailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            /*  contentItem.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
                      SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true) &&
                      (mNonSubscribedPartnerList?.contains(contentItem.provider.toLowerCase(Locale.getDefault())) == false)
              binding.firstFreeEpisodeVerbiage = sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage*/
            binding.contentItem = contentItem

            val layoutParam = binding.mcvTop.layoutParams
            layoutParam.width = point?.x ?: binding.mcvTop.width
            layoutParam.height = point?.y ?: binding.mcvTop.height
            binding.mcvTop.layoutParams = layoutParam

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.x ?: binding.mcvTop.width, point?.y ?: binding.mcvTop.height,
                contentItem.getImageItem()
            )
            imageLoad(binding.img, url)
            //    ViewCompat.setTransitionName(binding.img, model.id + "image")

            if ((partnerName?.length ?: 0) > 1) {
                binding.commonDetail.root.hide()
            } else {
                binding.commonDetail.root.show()
//                holder.binding.commonDetail.ivBrand.show()
            }

            updateProviderImage(
                binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )

            binding.root.setOnClickListener {
                if (sectionSource == ItemViewType.PROVIDER_BROWSE_APPS.name
                    || sectionSource == ItemViewType.SHUFFLE_RAIL.name
                )
                    contentItem.railCategory = sectionSource
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    private fun handleMixedPortrait(
        holder: RecyclerView.ViewHolder,
        contentItem: ContentItem,
        position: Int
    ) {
        if (holder is RailItemViewHolder) {
            val contentAnalyticsModel = getContentAnalyticsModel()
            contentItem.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
                    SubscriptionPackStatusEnum.ACTIVE.status.equals(
                        sharedPrefs.getSubscribedPack()?.subscriptionStatus,
                        true
                    ) &&
                    (mNonSubscribedPartnerList?.contains(contentItem.provider.lowercase(Locale.getDefault())) == false)
            holder.bind(
                contentItem,
                sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage
            )
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
            else*/ if (layoutType == ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name) {
                if (holder.binding.commonDetail.ivBrand.visibility == VISIBLE) {
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(holder.binding.root.context, 14)
                    )
                    lp.topMargin = dpToPx(holder.binding.root.context, 4)
                    lp.bottomMargin = dpToPx(holder.binding.root.context, 4)
                    holder.binding.commonDetail.ivBrand.layoutParams = lp
                }
            }
            if (holder.binding.commonDetail.ivBrand.visibility == VISIBLE) {
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
                if (sectionSource == ItemViewType.PROVIDER_BROWSE_APPS.name
                    || sectionSource == ItemViewType.SHUFFLE_RAIL.name
                )
                    contentItem.railCategory = sectionSource
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    inner class RailItemPortraitViewHolder(val binding: LayoutPortraitRailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            /*  contentItem.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
                      SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true) &&
                      (mNonSubscribedPartnerList?.contains(contentItem.provider.toLowerCase(Locale.getDefault())) == false)
              binding.firstFreeEpisodeVerbiage = sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage*/
            binding.contentItem = contentItem

            val layoutParam = binding.mcvTop.layoutParams
            layoutParam.width = point?.x ?: binding.mcvTop.width
            layoutParam.height = point?.y ?: binding.mcvTop.height
            binding.mcvTop.layoutParams = layoutParam

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.x ?: binding.mcvTop.width, point?.y ?: binding.mcvTop.height,
                contentItem.getImageItem()
            )
            imageLoad(binding.img, url)
            //    ViewCompat.setTransitionName(binding.img, model.id + "image")

            if ((partnerName?.length ?: 0) > 1) {
                binding.commonDetail.root.hide()
            } else {
                binding.commonDetail.root.show()
//                holder.binding.commonDetail.ivBrand.show()
            }

            updateProviderImage(
                binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )

            binding.root.setOnClickListener {
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null, // TODO
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
                    ),
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }

        }
    }

    inner class SingleProviderBannerRailViewHolder(val binding: LayoutRailItemTempBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {

            if (contentItem.provider.equals(PROVIDER_GAMEZOP, true)) {
                binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }
            else {
                binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        binding.tvTitle.context,
                        R.drawable.ic_play_title_rail
                    ), null, null, null
                )
            }

            binding.isSports = true
            binding.contentItem = contentItem
            binding.commonDetail.root.hide()

            val layoutParam = binding.mcvTop.layoutParams
            layoutParam.width = point?.x ?: binding.mcvTop.width
            layoutParam.height = point?.y ?: binding.mcvTop.height
            binding.mcvTop.layoutParams = layoutParam

            updateProviderImage(
                binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.x ?: binding.mcvTop.width, point?.y ?: binding.mcvTop.height,
                contentItem.getImageItem()
            )
            imageLoad(binding.img, url)

            binding.root.setOnClickListener {
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null, // TODO
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
                    ),
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    inner class RailItemTempViewHolder(val binding: LayoutRailItemTempBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            contentItem: ContentItem,
            point: Point?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            /*  contentItem.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
                      SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true) &&
                      (mNonSubscribedPartnerList?.contains(contentItem.provider.toLowerCase(Locale.getDefault())) == false)
              binding.firstFreeEpisodeVerbiage = sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage*/
            binding.contentItem = contentItem

            val layoutParam = binding.mcvTop.layoutParams
            layoutParam.width = point?.x ?: binding.mcvTop.width
            layoutParam.height = point?.y ?: binding.mcvTop.height
            binding.mcvTop.layoutParams = layoutParam

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.x ?: binding.mcvTop.width, point?.y ?: binding.mcvTop.height,
                contentItem.getImageItem()
            )
            imageLoad(binding.img, url)
            /*Need to hide partner logo only for tataplay content*/
            binding.commonDetail.ivBrand.show()
            if(contentItem.getRental() != null ){
                binding.commonDetail.ivBrand.hide()
            }
            else if(PROVIDER_TATA_SKY.equals(contentItem.provider, true)){
                binding.commonDetail.ivBrand.hide()
            }
            else if (ItemLayoutType.TITLE_RAIL.name == layoutType) {
                binding.isSports = true
                if (ItemViewType.BACKGROUND_BANNER_RAIL.name == sectionSource) {
                    if (contentItem.provider.equals(PROVIDER_GAMEZOP, true)) {
                        binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            null,
                            null
                        )
                    }
                    else {
                        binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(
                                binding.tvTitle.context,
                                R.drawable.ic_play_title_rail
                            ), null, null, null
                        )
                    }
                    e("RailAdpater","showPartnerLogo railTitle: $railTitle partnerName: $partnerName" +
                            "contentItem.provider : ${contentItem.provider}")
                }
            }
            else {
                binding.isSports = false
                binding.commonDetail.root.show()
            }
            updateProviderImage(
                binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )
            binding.root.setOnClickListener {
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null, // TODO
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
                    ),
                    railItemsModel = railItemsModel,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }


        }
    }

    inner class RailItemTop10PortraitViewHolder(val binding: LayoutRailTopTenPortraitItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            position: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem

            val layoutParam = binding.clRoot.layoutParams
            val rootWidth = point?.x ?: binding.clRoot.width
            layoutParam.width = rootWidth
            val layoutParamImg = binding.mcvTop.layoutParams
            val layoutParamNumber = binding.tvTrendingNumber.layoutParams
            var w = rootWidth  - rootWidth/3

            if(position >= 9){
                layoutParam.width = rootWidth+ rootWidth/3
                //layoutParam.height = point?.y ?: binding.mcvTop.height
                binding.clRoot.layoutParams = layoutParam
            }
            else{
                layoutParam.width = rootWidth - dpToPx(binding.root.context, 8)
                //layoutParam.height = point?.y ?: binding.mcvTop.height
            }
            layoutParamImg.width = w
            layoutParamNumber.width = w
            layoutParamImg.height = (w * THUMBNAIL_RATIO_LARGE_GRID).toInt()
            binding.mcvTop.layoutParams = layoutParamImg
            //binding.tvTrendingNumber.layoutParams = layoutParamImg
            binding.clRoot.layoutParams = layoutParam

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.y ?: binding.mcvTop.width, point?.x ?: binding.mcvTop.height,
                contentItem.getImageItem()
            )
            imageLoad(binding.img, url)
            val drawableName = "ic_top_" + ((position % 10) + 1)
            val drawableResourceId: Int = binding.root.context.resources
                .getIdentifier(drawableName, "drawable", binding.root.context.packageName)
            binding.tvTrendingNumber.setImageResource(drawableResourceId)
            binding.tvTrendingNumber.show()
            binding.imgOverlay.show()
            binding.contentItem?.isTop10 = true

            updateProviderImage(
                binding.commonDetail.ivBrand,
                contentItem.provider,
                providerLogos,
                R.drawable.ic_rail_placeholder
            )

            binding.root.setOnClickListener {
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null, // TODO
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
                    ),
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }


        }
    }

    inner class RotateItemViewHolder(val binding: LayoutRotateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contentItem: ContentItem) {
            binding.contentItem = contentItem
        }
    }

    inner class GenreItemViewHolder(val binding: LayoutGenreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contentItem: ContentItem, point: Point?) {
            binding.contentItem = contentItem

            val layoutParams = binding.frame.layoutParams
            layoutParams.width = point?.x ?: binding.frame.width
            layoutParams.height = point?.x ?: binding.frame.height
            binding.frame.layoutParams = layoutParams

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.x ?: binding.frame.width,
                point?.x ?: binding.frame.height,
                contentItem.image
            )
            transparentImageLoad(binding.ivGenre, url)
        }
    }

    inner class LanguageItemViewHolder(val binding: LayoutLanguageRailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contentItem: ContentItem, point: Point?) {
            binding.contentItem = contentItem

            val layoutParams = binding.frame.layoutParams
            layoutParams.width = point?.x ?: binding.frame.width
            layoutParams.height = point?.x ?: binding.frame.height
            binding.frame.layoutParams = layoutParams

            val url = getCloudinaryUrl(
                cloudinaryUrl,
                point?.x ?: binding.frame.width,
                point?.x ?: binding.frame.height,
                contentItem.image
            )
            transparentImageLoad(binding.ivGenre, url)
        }
    }

    inner class ChannelItemViewHolder(val binding: LayoutChannelRailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            railItemsModel: RailItemsModel?,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem

//            val layoutParams = binding.frame.layoutParams
//            layoutParams.width = point?.x ?: binding.frame.width
//            layoutParams.height = point?.x ?: binding.frame.height
//            binding.frame.layoutParams = layoutParams
            val w = dpToPx(binding.cardView.context,92)
            val h = dpToPx(binding.cardView.context,84)
            val url = getCloudinaryUrl(
                cloudinaryUrl,
                w,
                h,
                contentItem.image
            )
            imageLoad(binding.img, url)

            binding.root.setOnClickListener {
                contentItem.railCategory = sectionSource
                if (contentItem.refId.isNullOrEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    bindingAdapterPosition,
                    sectionPosition,
                    ItemViewType.BINGE_CHANNEL.name,
                    null,
                    railTitle,
                    sectionSource,
                    railItemsModel = railItemsModel,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }
    inner class CategoryViewHolder(val binding: LayoutCategoryLandscapeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            position: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem

            val layoutParams = binding.cardView.layoutParams
            layoutParams.width = point?.x ?: binding.cardView.width
            layoutParams.height = point?.y ?: binding.cardView.height
            binding.cardView.layoutParams = layoutParams

            val url =
                getCloudinaryUrl(
                    cloudinaryUrl,
                    point?.x ?: binding.cardView.width, point?.y ?: binding.cardView.height,
                    contentItem.image
                )
            imageLoad(binding.img, url)

            binding.root.setOnClickListener {
                if (contentItem.refId.isNullOrEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    ItemViewType.CATEGORY.name,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }
    inner class GenreLandViewHolder(val binding: LayoutGenreLandscapeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            position: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem
            val url = getCloudinaryUrlByWidthOrHeight(
                cloudinaryUrl,
                contentItem.newImage,
                height = 46
            )
            transparentImageLoad(binding.img, url)
            binding.root.setOnClickListener {
                if (contentItem.refId.isNullOrEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    ItemViewType.GENRE.name,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    inner class GenreGameViewHolder(val binding: LayoutGenreGamesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            position: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem
            val layoutParams = binding.cardView.layoutParams
            layoutParams.width = point?.x ?: 0
            layoutParams.height = point?.y ?: 0
            binding.cardView.layoutParams = layoutParams
            val url =
                getCloudinaryUrl(
                    cloudinaryUrl,
                    contentItem.image
                )
            imageLoad(binding.img, url)

            binding.root.setOnClickListener {
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    inner class CharacterViewHolder(val binding: LayoutCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            contentItem: ContentItem,
            point: Point?,
            position: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding.contentItem = contentItem

            val layoutParams = binding.llRoot.layoutParams
            layoutParams.width = point?.x ?: 0
            layoutParams.height = point?.y ?: 0
            binding.llRoot.layoutParams = layoutParams

            val drawableName = "character" + (position % 5)
            val drawableResourceId: Int = binding.root.context.resources
                .getIdentifier(drawableName, "drawable", binding.root.context.packageName)
            binding.imgCharacter.setImageResource(drawableResourceId)

            binding.root.setOnClickListener {
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    ItemViewType.CHARACTER.name,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
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
        fun bind(
            contentItem: ContentItem,
            position: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            if (railPoint.landscapePoint == null || railPoint.mLandscapeHeight == null || railPoint.mLandscapeWidth == null) {
                railPoint.landscapePoint =
                    getNormalThumbnailDimension(binding.root.context!!)
                railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
                railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
            }
            var layoutParams =
                ConstraintLayout.LayoutParams(
                    railPoint.mLandscapeWidth!! + 20,
                    railPoint.mLandscapeHeight!! + dpToPx(binding.root.context, 35)
                )
            var clTrailerLayoutParams= ConstraintLayout.LayoutParams(
                railPoint.mLandscapeWidth!! + 20,
                railPoint.mLandscapeHeight!!
            )
            layoutParams.setMargins(0, 0, 0, dpToPx(binding.root.context, 2))

            if(isTablet(binding.root.context) && !isLandTablet(binding.root.context)){
                layoutParams =
                    ConstraintLayout.LayoutParams(
                        railPoint.mLandscapeWidth!! + 50,
                        railPoint.mLandscapeHeight!! + dpToPx(binding.root.context, 30)
                    )

                clTrailerLayoutParams= ConstraintLayout.LayoutParams(
                    railPoint.mLandscapeWidth!! + 50,
                    railPoint.mLandscapeHeight!! + dpToPx(binding.root.context, 30)
                )
                layoutParams.setMargins(0, 0, 10, dpToPx(binding.root.context, 2))
                clTrailerLayoutParams.setMargins(0, 0, 10, dpToPx(binding.root.context, 2))
            }
            if(isLandTablet(binding.root.context)){
                layoutParams =
                    ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        railPoint.mLandscapeHeight!! + dpToPx(binding.root.context, ADD_HEIGHT_LANDSCAPE)
                    )

                clTrailerLayoutParams= ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    railPoint.mLandscapeHeight!! + dpToPx(binding.root.context, ADD_HEIGHT_LANDSCAPE)
                )
                layoutParams.setMargins(0, 0, 10, dpToPx(binding.root.context, 2))
                clTrailerLayoutParams.setMargins(0, 0, 10, dpToPx(binding.root.context, 2))
            }

            binding.clItemTrailer.layoutParams = layoutParams
            binding.cvTrailer.layoutParams = clTrailerLayoutParams
            binding.tvTrailerName.text = contentItem.title
            showTrailerImage(contentItem)


            mTrailerView = binding.itemTrailer
            /*Need to add this for QoE Probe Mitigation*/
            mTrailerView?.setPlayerModel(contentItem.id,contentItem.contentType, contentItem.provider, contentItem.title, sharedPrefs.getOriginalSubscriberId())
            /*End*/
            mTrailerView?.setTrailerUrl(contentItem.trailerUrl!!)
            d("TrailerView", " Url is ==>" + contentItem.trailerUrl)
            mTrailerView?.setTrailerStartLambda {
                binding.imgTrailer.visibility = View.GONE
                binding.itemTrailer.visibility = View.VISIBLE
            }
            mTrailerView?.setTrailerFinishLambda {
                d("TrailerView", "inside finish")
                showTrailerImage(contentItem)
            }

            if (!TextUtils.isEmpty(contentItem.trailerUrl))
                trailerExchanger?.invoke(mTrailerView, true, viewPortPosition)


            binding.root.setOnClickListener {
                contentItem.railCategory = sectionSource
                if (contentItem.refId.isEmpty())
                    contentItem.refId = refId
                listener.onSubItemClick(
                    contentItem,
                    position,
                    sectionPosition,
                    EventConstants.TYPE_RAIL,
                    null,
                    railTitle,
                    sectionSource,
                    contentAnalyticsModel = contentAnalyticsModel
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


    private fun handleMixedLandscape(
        holder: RecyclerView.ViewHolder,
        contentItem: ContentItem,
        position: Int
    ) {
        if (holder is RailItemTrailerViewHolder) {
            val contentAnalyticsModel = getContentAnalyticsModel()
            holder.bind(contentItem, position, contentAnalyticsModel)
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
        list.forEach {contentItem->
            crownCalculation(contentItem)
        }
        notifyDataSetChanged()
        for (content in list) {
            if (!TextUtils.isEmpty(content.trailerUrl)) {
                mTrailerView?.playUrl()
                break

            }
        }
    }
    private fun crownCalculation(it : ContentItem) {
        val mNonSubscribedPartnerList = sharedPrefs.getNonSubscribedPartnerList()
        it.isPartnerSubscribed = sharedPrefs.getSubscribedPack() != null &&
            SubscriptionPackStatusEnum.ACTIVE.status.equals(sharedPrefs.getSubscribedPack()?.subscriptionStatus, true) &&
            (mNonSubscribedPartnerList?.contains(it.provider.toLowerCase(Locale.getDefault())) == false)
        it.firstFreeEpisodeVerbiage = sharedPrefs.getConfigResponse()?.data?.config?.firstEpisodeFreeVerbiage?:"1st Episode Free"
    }


    private fun calculateLength() {
        length = if (ItemLayoutType.PORTRAIT.name.equals(mixedLayoutType, true)) {
            8
        } else {
            7
        }
    }

}
