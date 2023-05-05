package com.tatasky.binge.ui.features.home.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_GAMES
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.emptyContentAnalyticsModel
import com.tatasky.binge.analytics.util.getContentAnalyticsModel
import com.tatasky.binge.customviews.CustomRecyclerView
import com.tatasky.binge.customviews.EndlessListAdapter
import com.tatasky.binge.customviews.OnDoubleTapListener
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.*
import com.tatasky.binge.databinding.*
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.interfaces.*
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.invisible
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.TabletType
import com.tatasky.binge.ui.features.home.home_trailer.TrailerView
import com.tatasky.binge.ui.features.home.model.RailsModel
import com.tatasky.binge.utils.*
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.listener.CarouselOnScrollListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem


class HomeAdapter(
    mList: MutableList<HomeResponse.Items>,
    private val mBannerClick: CommonDTOClickListener,
    private val cloudinaryUrl: String?,
    private val mSeeAllClickListener: CommonSeeAllClickListener,
    private val mContentViewListener: CommonContentViewListener,
    private val mRailScrollListener: RailScrollListener,
    val providerLogos: ProviderLogo,
    private val freeTrialAvailed: Boolean?,
    private val freeTrialStartupNudgeData: ConfigResponse.FreeTrialStartupNudge?,
    private val sharedPrefs: PrefsRepo,
    private val homeAnalytics: HomeAnalytics?,
) : EndlessListAdapter<HomeResponse.Items, RecyclerView.ViewHolder>(
    mList,
    emptyContentAnalyticsModel()
) {

    private var ignorePositions: HashSet<Int> = hashSetOf()
    private var manager: LinearLayoutManager? = null
    private var reset: Boolean = false

    private var mHBStartPosition: Int? = null
    private var mHBPosition: Int? = null

    private var mBanner: ViewHolderBanner? = null

    private val MID_SCROLL = 1000
    private val ALLCHANNELS = 2000
    private val RAIL = 3000
    private val WIDGET = 5000
    private val PROVIDER = 6000
    private val PROVIDER_UNSUBSCRIBED = 7000
    private val STARTFREETRIALSTARTNUDGE = 8000
    private val SELECTPAIDPACKNUDGE = 9000
    private val FREETRIALUPGRADENUDGE = 10000
    private val TITTLE_RAIL_WITH_BACKGROUND_IMAGE = 13000
    private val SELECT_LANGUAGE_WIDGET = 20000
    private val PROVIDER_WITH_CONTENTS = 21000
    private val SHUFFLE_RAIL = 22000
    private val HEROBANNER = 23000
    private val NEWLY_ADDED_GAMES = 24000
    private val GAME_WEEK_BANNER = 25000
    private val MID_SCROLL_GAME = 26000
    private val GAME_NUDGE = 27000
    private val MID_SCROLL_BANNER = 28000 //Mixed midscroll
    private val CATEGORY = 29000
    private val LIVE_BANNER = 30000
    private val GENRE_RAIL = 31000
    private val MERGE_GAME_RAIL = 14000


    private val viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private val railPoint = RailPoint()
    private var onAddPackClickListener: AddPackListener? = null
    private var isUpgradePack: CharSequence? = null
    private var positionOfNudges = hashSetOf<Int>()
    private var dthStatus: String? = null
    private var partnerPacks: PartnerPacks? = null
    private var languageWidgetVisible = true
    private var gameWidgetVisible = true
    private var lastProviderIndex = ""
    private var mCurrentTrailerPair: Pair<Int, TrailerView?>? = null
    private var mTrailerMap: HashMap<Int, TrailerView?> = HashMap()
    private var pageName: String? = ""
    private var isRefresh: Boolean = false
    private var isPackUpdated: Boolean = false
    private var orientationChanged = false


    fun setDthStatus(dthStatus: String?) {
        this.dthStatus = dthStatus
        val length = mDataList.size
        notifyItemRangeChanged(0, length)
    }

    fun updateIsRefresh(isRefreshed:Boolean){
        isRefresh = isRefreshed
    }
    fun updateIsPackUpdated(isUpdated:Boolean){
        isPackUpdated = isUpdated
    }
    fun setPageName(pageName: String) {
        this.pageName = pageName
    }
//    private var lastPosition = -1


    fun setPartnerPack(partnerPacks: PartnerPacks?) {
        try {
            if (!Gson().toJson(this.partnerPacks).equals(partnerPacks)) {
                this.partnerPacks = partnerPacks
                positionOfNudges.forEach {
                    notifyItemChanged(it)
                }
            }
        } catch (e: Exception) {
        }
    }

    fun setGameWidgetVisibility(visibility: Boolean) {
        gameWidgetVisible = visibility
        val length = mDataList.size
        notifyItemRangeChanged(0, length)
    }

    fun setLanguageWidgetVisibility(visibility: Boolean) {
        if (languageWidgetVisible != visibility) {
            languageWidgetVisible = visibility
            val length = mDataList.size
            notifyItemRangeChanged(0, length)
        }
    }

    fun getLanguageWidgetVisibility(): Boolean {
        return languageWidgetVisible
    }

    fun setHeroPosition(position: Int){
        mHBStartPosition = position
        if(mHBPosition!=null) {
            reset = true
            notifyItemChanged(mHBPosition!!)
        }
    }


    fun setAddPackListener(onAddPackClickListener: AddPackListener, isUpgrade: CharSequence) {
        this.onAddPackClickListener = onAddPackClickListener
        isUpgradePack = isUpgrade
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        calculateWidthAndHeight(recyclerView.context)
    }

    private fun calculateWidthAndHeight(context: Context) {
        railPoint.circularPoint = getCircularProviderIconPoint(context)
        railPoint.genreCharPoint = getCharcterGenrePoint(context)
        railPoint.mCircularWidth = railPoint.genreCharPoint!!.x
        railPoint.landscapePoint = getNormalThumbnailDimension(context)
        railPoint.portraitPoint = getLargeThumbnailDimension(context)
        railPoint.mLandscapeWidth = railPoint.landscapePoint?.x
        railPoint.mLandscapeHeight = railPoint.landscapePoint?.y
        railPoint.mPortraitWidth = railPoint.portraitPoint?.x
        railPoint.mPortraitHeight = railPoint.portraitPoint?.y
        railPoint.landscapeGenrePoint = getNormalThumbnailForGenreDimension(context)
        railPoint.mLandscapeGenreWidth = railPoint.landscapeGenrePoint?.x
        railPoint.mLandscapeGenreHeight = railPoint.landscapeGenrePoint?.y
        railPoint.providerPoint = getProviderIconLandscapePoint(context)
        railPoint.mProviderWidth = railPoint.providerPoint?.x
        railPoint.mProviderHeight = railPoint.providerPoint?.y
    }

    override fun getNoContentVisibility(): Int {
        return View.GONE
    }

    override fun createNormalViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            HEROBANNER -> {
                view = HomeHeroView(parent.context)
                mBanner = ViewHolderBanner(view)
                viewHolder = mBanner
                parent.tag = null
            }
            PROVIDER -> {
                view = inflater.inflate(R.layout.home_recyclerview, parent, false)
                parent.tag = null
                viewHolder = ViewHolderAllChannel(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(viewPool)
            }
            PROVIDER_WITH_CONTENTS -> {
                parent.tag = null
                view = inflater.inflate(R.layout.layout_provider_with_content, parent, false)
                viewHolder = ViewHolderProviderContents(view)
                viewHolder.binding?.rvProviderContents?.setRecycledViewPool(viewPool)
            }
//            GAME_RAIL -> {
//                view = inflater.inflate(R.layout.home_recyclerview, parent, false)
//                viewHolder = ViewHolderRails(view)
//                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(
//                    viewPool
//                )
//            }
            GAME_WEEK_BANNER -> {
                view = inflater.inflate(R.layout.home_gaming_week_banner,parent,false)
                viewHolder = ViewHolderGameWeekBanner(view)

            }
            LIVE_BANNER -> {
                view = inflater.inflate(R.layout.layout_live_banner,parent,false)
                viewHolder = ViewHolderLiveBanner(view)
            }

            NEWLY_ADDED_GAMES -> {
                view = inflater.inflate(R.layout.home_new_games, parent, false)
                viewHolder = ViewHolderGameNew(view)
            }
            MERGE_GAME_RAIL -> {
                view = inflater.inflate(R.layout.merge_games_row, parent, false)
                viewHolder = MergedViewHolder(view)
            }
            GENRE_RAIL -> {
                view = inflater.inflate(R.layout.genre_home_recycler_view,parent,false)
                viewHolder = ViewHolderGenreNew(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(
                    viewPool
                )
            }
            RAIL -> {
                view = inflater.inflate(R.layout.home_recyclerview, parent, false)
                viewHolder = ViewHolderRails(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(
                    viewPool
                )
            }
            WIDGET -> {
                view = inflater.inflate(R.layout.home_recyclerview, parent, false)
                viewHolder = ViewHolderRails(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(viewPool)
            }
            PROVIDER_UNSUBSCRIBED -> {
                view = inflater.inflate(R.layout.layout_unsubscribed_banner, parent, false)
                parent.tag = null
                viewHolder = ViewHolderUnsubscribedProvider(view)
            }
            STARTFREETRIALSTARTNUDGE -> {
                view = inflater.inflate(R.layout.layout_start_free_trial_nudge, parent, false)
                parent.tag = null
                viewHolder = StartFreeTrialViewHolder(view)
            }
            SELECTPAIDPACKNUDGE -> {
                view = inflater.inflate(R.layout.layout_select_paid_pack_nudge, parent, false)
                parent.tag = null
                viewHolder = SelectPaidPackViewHolder(view)
            }
            FREETRIALUPGRADENUDGE -> {
                view = inflater.inflate(R.layout.layout_select_paid_pack_nudge, parent, false)
                parent.tag = null
                viewHolder = UpgradeFreeTrialViewHolder(view)
            }
            SELECT_LANGUAGE_WIDGET -> {
                view = inflater.inflate(R.layout.layout_select_language_widget, parent, false)
                parent.tag = null
                viewHolder = SelectLanguageWidgetViewHolder(view)
            }
            GAME_NUDGE -> {
                view = inflater.inflate(R.layout.layout_game_subscribe_widget,parent,false)
                parent.tag = null
                viewHolder = GameNudgeViewHolder(view)
            }
            SHUFFLE_RAIL -> {
                parent.tag = null
                view = inflater.inflate(R.layout.layout_shuffle_rail, parent, false)
                viewHolder = ShuffleRailViewHolder(view)
                viewHolder.binding?.rvShuffleContents?.setRecycledViewPool(viewPool)
            }
            TITTLE_RAIL_WITH_BACKGROUND_IMAGE -> {
                parent.tag = null
                view = inflater.inflate(R.layout.layout_sports_with_background, parent, false)
                viewHolder = TittleRailWithBackgroundImageViewHolder(view)
                viewHolder.binding?.rvSportsRail?.setRecycledViewPool(viewPool)
            }
            MID_SCROLL ->{
                view = inflater.inflate(R.layout.home_midscroll,parent,false)
                viewHolder = ViewHolderMidScroll(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(viewPool)
                parent.tag = null
            }
            MID_SCROLL_GAME ->{
                view = inflater.inflate(R.layout.home_midscroll,parent,false)
                viewHolder = ViewHolderMidScrollGames(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(viewPool)
                parent.tag = null
            }
            MID_SCROLL_BANNER ->{
                view = inflater.inflate(R.layout.home_midscroll,parent,false)
                viewHolder = ViewHolderMidScrollBanner(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(viewPool)
                parent.tag = null
            }
            else -> {
                view = inflater.inflate(R.layout.home_recyclerview, parent, false)
                viewHolder = ViewHolderRails(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(
                    viewPool
                )
            }
        }

        return viewHolder!!
    }

    override fun clear() {
        super.clear()
        ignorePositions.clear()
        reset = true
        pageName=""
    }

    override fun bindNormalViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is LoadMoreViewHolder) {
            val item = mDataList[position]
            item.dthStatus = sharedPrefs.getDthStatusFreemium()
            when (holder.itemViewType) {
                HEROBANNER -> {
                    ignorePositions.add(position)
                    val bannerViewHolder = holder as ViewHolderBanner
                    when {
                        bannerViewHolder.banner.len == 0 -> bannerViewHolder.banner.initData(
                            item,
                            mBannerClick,
                            position,
                            cloudinaryUrl,
                            providerLogos,
                            mContentViewListener,
                            dthStatus,
                            mHBStartPosition,
                            sharedPrefs,
                            contentAnalyticsModel = item.getContentAnalyticsModel()
                        )
                        reset -> {
                            bannerViewHolder.banner.reset()
                            bannerViewHolder.banner.initData(
                                item,
                                mBannerClick,
                                position,
                                cloudinaryUrl,
                                providerLogos,
                                mContentViewListener,
                                dthStatus,
                                mHBStartPosition,
                                sharedPrefs,
                                contentAnalyticsModel = item.getContentAnalyticsModel()
                            )
                            reset = false
                        }
                        else -> bannerViewHolder.banner.startSlide(true)
                    }

                }
                GAME_WEEK_BANNER -> {
                    val gameWeekBannerViewHolder = holder as ViewHolderGameWeekBanner
                    val contentList = item.filteredContentItems
                    if (contentList.isEmpty()) {
                        gameWeekBannerViewHolder.binding?.root?.hide()
                        ignorePositions.add(position)
                    } else {
                        ignorePositions.remove(position)
                        gameWeekBannerViewHolder.binding?.root?.show()
                    }
                    val contentAnalyticsModel = item.getContentAnalyticsModel()
                    gameWeekBannerViewHolder.bind(
                        position,
                        item.title,
                        contentList,
                        contentAnalyticsModel
                    )
                }

                LIVE_BANNER -> {
                    val liveBannerViewHolder = holder as ViewHolderLiveBanner
                    val contentList = item.filteredContentItems
                    if (contentList.isEmpty()) {
                        liveBannerViewHolder.binding?.root?.hide()
                        ignorePositions.add(position)
                    } else {
                        ignorePositions.remove(position)
                        liveBannerViewHolder.binding?.root?.show()
                    }
                    val contentAnalyticsModel = item.getContentAnalyticsModel()
                    liveBannerViewHolder.bind(
                        position,
                        item.title,
                        contentList,
                        contentAnalyticsModel
                    )
                }

                NEWLY_ADDED_GAMES -> {
                    val gameNewViewHolder = holder as ViewHolderGameNew
                    val contentList = item.filteredContentItems
                    val contentAnalyticsModel = item.getContentAnalyticsModel()
                    gameNewViewHolder.bind(
                        position,
                        item.title,
                        contentList,
                        contentAnalyticsModel
                    )
                }
                MERGE_GAME_RAIL -> {
                    val gameNewViewHolder = holder as MergedViewHolder
                    val contentList = item.filteredContentItems
                    val contentAnalyticsModel = item.getContentAnalyticsModel()
                    gameNewViewHolder.mergeBindRows(
                        position,
                        item,
                        contentAnalyticsModel
                    )
                }
                GENRE_RAIL -> {
                    val genreNewViewHolder = holder as ViewHolderGenreNew
                    val contentList = item.contentItem
                    genreNewViewHolder.bind(
                        RailsModel(
                            item.title,
                            RailAdapter(
                                mBannerClick,
                                contentList,
                                item.layoutType,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                item.sectionSource,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                pageName = pageName?:"",
                                refId = item.refId,
                                railSectionType = item.sectionType
                            )
                        )
                    )
                }

                RAIL -> {
                    val railsViewHolder = holder as ViewHolderRails
                    var contentList = item.filteredContentItems
                    railsViewHolder.binding?.homeRecyclerViewTitle?.show()
                    e("BINGE_CHANNEL_RAIL","contentList : ${contentList.size}")
                    railsViewHolder.binding?.ivLiveIndicator?.hide()
                    if (item.sectionSource.equals(
                            ItemViewType.BINGE_CHANNEL.name,
                            true
                        ) || item.sectionSource.equals(
                            ItemViewType.DARSHAN_CHANNEL.name,
                            true
                        )
                    ) {
                        railsViewHolder.binding?.ivLiveIndicator?.show()
                        e("BINGE_CHANNEL_RAIL","inside contentList : ${contentList.size}")
                    }
                    if (item.sectionSource.equals(ItemViewType.LANGUAGE.name, true) ||
                        item.sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true) ||
                        item.sectionSource.equals(ItemLayoutType.POPULAR_CHARACTER.name, true) ||
                        item.sectionSource.equals(ItemViewType.GAMES.name,true) ||
                        item.sectionSource.equals(ItemViewType.CATEGORY.name,true)
                    ) {
                        contentList = item.contentItem
                    }
                    else if(!item.sectionSource.equals(ItemViewType.PRIME.name, true) ){
                        if(isHideRailWithPackName(item,
                                sharedPrefs.getSubscribedPack(), item.packName,
                                mNonSubscribedPartnerList, sharedPrefs.getLoginStatus()
                            ))
                            contentList = item.filteredContentItems
                    }

                    if (contentList.isEmpty() ||
                        (item.sectionSource.equals(ItemViewType.PRIME.name, true) &&
                                (dthStatus.isNullOrEmpty() || NON_DTH_USER.equals(
                                    dthStatus,
                                    true
                                )))
                    ) {
                        railsViewHolder.binding?.clHomeRoot?.hide()
                        ignorePositions.add(position)
                    } else {
                        ignorePositions.remove(position)
                        railsViewHolder.binding?.clHomeRoot?.show()
                        if (item.sectionSource.equals(ItemViewType.PRIME.name, true) &&
                            railsViewHolder.binding != null
                        ) {
                            railsViewHolder.binding.providerLogo.hide()
                            railsViewHolder.binding.homeSeeAll.show()
                        }/* else if(item.provider != null && railsViewHolder.binding != null){
                            updateProviderLogo(
                                railsViewHolder.binding.providerLogo,
                                item.provider!!,
                                providerLogos,
                                R.drawable.ic_detail_placeholder,
                                cloudinaryUrl
                            )
                            railsViewHolder.binding.providerLogo.show()
                            railsViewHolder.binding.homeSeeAll.hide()
                        }*/
                        else if (item.sectionSource.equals(ItemViewType.LANGUAGE.name, true) ||
                            item.sectionSource.equals(ItemViewType.GENRE_RAIL_FOR_GAMES.name, true) ||
                            item.sectionSource.equals(
                                ItemLayoutType.POPULAR_CHARACTER.name,
                                true
                            )
                            //|| item.layoutType.equals(ItemLayoutType.TOP_PORTRAIT.name, true)
                            || item.sectionSource.equals(ItemViewType.CATEGORY.name,true)
                            || item.sectionSource.equals(ItemViewType.BINGE_CHANNEL.name,true)
                            || item.sectionSource.equals(ItemViewType.DARSHAN_CHANNEL.name,true)
                        ) {
                            if (item.sectionSource.equals(
                                    ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                                    true
                                )
                            ) {
                                railsViewHolder.binding?.homeRecyclerViewTitle?.hide()
                            } else {
                                railsViewHolder.binding?.homeRecyclerViewTitle?.show()
                            }
                            railsViewHolder.binding?.homeSeeAll?.hide()
                            railsViewHolder.binding?.providerLogo?.hide()
                        } else {
                            railsViewHolder.binding?.providerLogo?.hide()
                            railsViewHolder.binding?.homeSeeAll?.show()
                        }
                    }
                    val isPrepand = !"APPEND".equals(item.recommendationPosition, true)
                    val isMixedRail =
                        !item.recommendationPosition.isNullOrEmpty() && !item.sectionSource.equals(
                            TYPE_GAMES, true
                        )
                    val contentAnalyticsModel = item.getContentAnalyticsModel()
                    railsViewHolder.bind(
                        RailsModel(
//                            "${item.id}",
                            item.title,
                            RailAdapter(
                                mBannerClick,
                                contentList,
                                item.layoutType,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                item.sectionSource,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                pageName = pageName?:"",
                                refId = item.refId,
                                railSectionType = item.sectionType
                            )
                        ),
                        mSeeAllClickListener,
                        item.id, item.title,
                        item.sectionSource,
                        item.lastPosition,
                        item.placeHolder,
                        item.configType,
                        item.trendingProvider,
                        isMixedRail,
                        isPrepand,
                        item,
                        item.backgroundImage,
                        item.layoutType,
                        item.refId,
                        packName = item.packName,
                        contentAnalyticsModel
                    )

                    if (item.continueWatching) {
                        manager =
                            railsViewHolder.binding?.homeRecyclerView!!.layoutManager as LinearLayoutManager
                    }

                }

                PROVIDER -> {
                    val railsViewHolder = holder as ViewHolderAllChannel
                    holder.itemView.visibility = View.VISIBLE
                    holder.itemView.layoutParams =
                        RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    railsViewHolder.binding?.homeSeeAll?.hide()
                    val contentAnalyticsModel = item.getContentAnalyticsModel()
                    railsViewHolder.bind(
                        RailsModel(
//                            "${item.id}",
                            item.title,
                            RailAdapter(
                                mBannerClick,
                                item.filteredContentItems,
                                item.layoutType,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                item.sectionSource,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                refId = item.refId,
                                railSectionType = item.sectionType
                            )
                        ),
                        mSeeAllClickListener,
                        item.id,
                        item.title,
                        item.sectionSource,
                        item.placeHolder,
                        item.configType,
                        item.backgroundImage,
                        item.layoutType,
                        item.refId,
                        contentAnalyticsModel
                    )
                }
                PROVIDER_WITH_CONTENTS -> {

                    val railsViewHolder = (holder as ViewHolderProviderContents)
                    railsViewHolder.viewPortPosition=position
                    if(item.filteredProvider.isEmpty()){
                        railsViewHolder.binding?.clHomeRoot?.hide()
                        ignorePositions.add(position)
                    }
                    else {
                        railsViewHolder.binding?.clHomeRoot?.show()
                        val firstProviderData = PartnerData()
                        firstProviderData.layoutType = item.filteredProvider[0].layoutType
                        firstProviderData.contentList = item.filteredProvider[0].contentItem
                        firstProviderData.provider = item.filteredProvider[0].provider

                        railsViewHolder.bind(
                            RailsModel(
//                                "${item.id}",
                                item.title,
                                adapter = RailAdapter(
                                    mBannerClick,
                                    firstProviderData.contentList,
                                    ItemLayoutType.MIXED.name,
                                    (position - ignorePositions.filter { it < position }.size + 1),
                                    cloudinaryUrl,
                                    item.sectionSource,
                                    false,
                                    item.title,
                                    providerLogos,
                                    railPoint,
                                    sharedPrefs = sharedPrefs,
                                    firstProviderData.layoutType,
                                    firstProviderData.provider,
                                    mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                    isPackAvailed = isPackAvailed,
                                    viewPortPosition = position,
                                    refId = item.refId,
                                    railSectionType = item.sectionType
                                )
                                { trailerFragment, addTrailer, current_position ->
                                    handleTrailerExchanger(
                                        trailerFragment,
                                        addTrailer,
                                        railsViewHolder.viewPortPosition
                                    )
                                }
                            ),
                            item,
                            providerLogos,
                            railPoint,
                            mBannerClick,
                            firstProviderData.layoutType
                        )
                    }
                }
                PROVIDER_UNSUBSCRIBED -> {
                    val viewHolderUnsubscribedProvider = holder as ViewHolderUnsubscribedProvider
                    viewHolderUnsubscribedProvider.bind(item)
                }
                STARTFREETRIALSTARTNUDGE -> {
                    val viewHolderStartFreeTrialNudge = holder as StartFreeTrialViewHolder
                    if (freeTrialAvailed == true || freeTrialStartupNudgeData == null || partnerPacks != null || freeTrialAvailed == null) {
                        viewHolderStartFreeTrialNudge.binding?.clStartFreeTrialRoot?.hide()
                        ignorePositions.add(position)
                    } else {
                        val contentAnalyticsModel = item.getContentAnalyticsModel()
                        viewHolderStartFreeTrialNudge.bind(position, contentAnalyticsModel)
                    }
                }
                SELECTPAIDPACKNUDGE -> {
                    val viewHolderSelectPaidPackNudge = holder as SelectPaidPackViewHolder
                    partnerPacks?.let {
                        val availableDays = (it.getPaidPackSelectionNudge()?.availableDays ?: 0)
                        val startDay = it.getPaidPackSelectionNudge()?.startDay ?: Int.MIN_VALUE
                        val totalFreeTrialDuration =
                            (it.getPaidPackSelectionNudge()?.totalFreeTrialDuration ?: 0)
                        val progress: Double =
                            ((totalFreeTrialDuration.toDouble() - availableDays.toDouble()) / totalFreeTrialDuration.toDouble()) * 100
                        val currentDay = totalFreeTrialDuration - availableDays + 1
                        if (!isHideSelectPackButton(it) &&
                            it.isDummyUser == true &&
                            currentDay >= startDay && availableDays >= 0
                        ) {
                            val contentAnalyticsModel = item.getContentAnalyticsModel()
                            it.getPaidPackSelectionNudge()?.availableDays = availableDays
                            viewHolderSelectPaidPackNudge.bind(
                                position,
                                progress,
                                currentDay,
                                availableDays,
                                contentAnalyticsModel
                            )
                            viewHolderSelectPaidPackNudge.binding?.clSelectPaidPackRoot?.show()
                            ignorePositions.remove(position)
                        } else {
                            viewHolderSelectPaidPackNudge.binding?.clSelectPaidPackRoot?.hide()
                            ignorePositions.add(position)
                        }
                    } ?: run {
                        viewHolderSelectPaidPackNudge.binding?.clSelectPaidPackRoot?.hide()
                        ignorePositions.add(position)
                    }
                }
                SELECT_LANGUAGE_WIDGET -> {
                    val viewHolderSelectLanguageWidget = holder as SelectLanguageWidgetViewHolder
                    if (!languageWidgetVisible) {
                        viewHolderSelectLanguageWidget.binding?.clRoot?.hide()
                        ignorePositions.add(position)
                    } else {
                        ignorePositions.remove(position)
                        viewHolderSelectLanguageWidget.binding?.clRoot?.show()
                        val contentAnalyticsModel = item.getContentAnalyticsModel()
                        viewHolderSelectLanguageWidget.bind(position, contentAnalyticsModel)
                    }
                }
                GAME_NUDGE -> {
                    val viewHolderGameNudge = holder as GameNudgeViewHolder
                    if (!gameWidgetVisible) {
                        viewHolderGameNudge.binding?.cvRoot?.hide()
                        ignorePositions.add(position)
                    } else {
                        ignorePositions.remove(position)
                        viewHolderGameNudge.binding?.cvRoot?.show()
                        val contentAnalyticsModel = item.getContentAnalyticsModel()
                        viewHolderGameNudge.bind(position, contentAnalyticsModel)
                    }
                }
                SHUFFLE_RAIL -> {
                    if (item.filteredShuffleList.isEmpty()) {
                        (holder as ShuffleRailViewHolder).binding?.clProviderSpecificRail?.hide()
                        ignorePositions.add(position)
                    }
                    else {
                        (holder as ShuffleRailViewHolder).binding?.clProviderSpecificRail?.show()
                        var layoutType = ItemLayoutType.MIXED.name
                        if (item.provider.isNullOrEmpty()) {
                            layoutType = ItemLayoutType.MIXED_WITH_PROVIDER_DATA.name
                        }
                        (holder as ShuffleRailViewHolder).let {
                            it.viewPortPosition=position
                            it.bind(
                                RailsModel(
                                    adapter = RailAdapter(
                                        mBannerClick,
                                        item.filteredShuffleList[item.shuffleIndex].filteredContentItems,
                                        layoutType,
                                        (position - ignorePositions.filter { it < position }.size + 1),
                                        cloudinaryUrl,
                                        item.sectionSource,
                                        item.continueWatching,
                                        item.title,
                                        providerLogos,
                                        railPoint,
                                        sharedPrefs = sharedPrefs,
                                        item.filteredShuffleList[item.shuffleIndex].layoutType,
                                        item.provider,
                                        mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                        isPackAvailed = isPackAvailed,
                                        viewPortPosition = position,
                                        refId = item.refId,
                                        railSectionType = item.sectionType
                                    ) { trailerFragment, addTrailer, currentPosition ->
                                        handleTrailerExchanger(
                                            trailerFragment,
                                            addTrailer,
                                            it.viewPortPosition
                                        )
                                    }
                                ),
                                item,
                                providerLogos,
                            )
                        }
                    }
                }
                TITTLE_RAIL_WITH_BACKGROUND_IMAGE -> {
                    (holder as TittleRailWithBackgroundImageViewHolder).bind(
                        RailsModel(
                            adapter = RailAdapter(
                                mBannerClick,
                                item.filteredContentItems,
                                ItemLayoutType.TITLE_RAIL.name,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                item.sectionSource,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                pageName = pageName?:"",
                                refId = item.refId,
                                partnerName = item.provider,
                                railSectionType = item.sectionType
                            )
                        ),
                        item,
                        providerLogos
                    )
                }
                FREETRIALUPGRADENUDGE -> {
                    val viewHolderUpgradeTrailNudge = holder as UpgradeFreeTrialViewHolder
                    partnerPacks?.let {
                        val availableDays = (it.getPaidPackSelectionNudge()?.availableDays ?: 0)
                        val startDay = it.getPaidPackSelectionNudge()?.startDay ?: Int.MIN_VALUE
                        val totalFreeTrialDuration =
                            (it.getPaidPackSelectionNudge()?.totalFreeTrialDuration ?: 0)
                        val progress: Double =
                            ((totalFreeTrialDuration.toDouble() - availableDays.toDouble()) / totalFreeTrialDuration.toDouble()) * 100
                        val currentDay = totalFreeTrialDuration - availableDays + 1
                        if (
                            it.subscriptionDetailInfo?.bingeAccountStatus.equals(
                                SubscriptionPackStatusEnum.ACTIVE.status,
                                ignoreCase = true
                            ) &&
                            it.mobileUpgradable &&
                            it.packType.equals("free", true) &&
                            currentDay >= startDay &&
                            it.isDummyUser != true &&
                            availableDays >= 0
                        ) {
                            val contentAnalyticsModel = item.getContentAnalyticsModel()
                            it.getPaidPackSelectionNudge()?.availableDays = availableDays
                            viewHolderUpgradeTrailNudge.bind(
                                position,
                                progress,
                                currentDay,
                                availableDays,
                                contentAnalyticsModel
                            )
                            viewHolderUpgradeTrailNudge.binding?.clSelectPaidPackRoot?.show()
                            ignorePositions.remove(position)
                        } else {
                            viewHolderUpgradeTrailNudge.binding?.clSelectPaidPackRoot?.hide()
                            ignorePositions.add(position)
                        }
                    } ?: run {
                        viewHolderUpgradeTrailNudge.binding?.clSelectPaidPackRoot?.hide()
                        ignorePositions.add(position)
                    }
                }
                MID_SCROLL -> {
                    ignorePositions.add(position)
                    val midscrollViewHolder = holder as ViewHolderMidScroll
                    val contentlist = item.contentItem.filter{
                        return@filter if(it.screenName?.equals(MID_SCROLL_DETAIL_SCREEN,true) == true)
                            isValidRentalContent(it,item.dthStatus,item.sectionSource,item.sectionType, item.refId )
                        else
                            true
                    }


                    /**
                     * Aman/ashutosh check here with Shilpi for item.contentItem
                     */
                    midscrollViewHolder.bind(
                        RailsModel(
                            "",
                            RailAdapter(
                                mBannerClick,
                                contentlist,
                                ItemLayoutType.MID_SCROLL_RAIL.name,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                ItemLayoutType.MID_SCROLL_RAIL.name,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                refId = item.refId,
                                railSectionType = item.sectionType
                            )
                        )
                    )
                }


                MID_SCROLL_BANNER -> {
                    ignorePositions.add(position)
                    val midscrollViewHolder = holder as ViewHolderMidScrollBanner
                    val contentlist = item.contentItem.filter{
                        return@filter if(it.screenName?.equals(MID_SCROLL_DETAIL_SCREEN,true) == true)
                            isValidRentalContent(it,item.dthStatus,item.sectionSource,item.sectionType, item.refId)
                        else
                            true
                    }


                    midscrollViewHolder.bind(
                        RailsModel(
                            "",
                            RailAdapter(
                                mBannerClick,
                                contentlist,
                                ItemLayoutType.MID_SCROLL_RAIL.name,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                item.sectionSource,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                refId = item.refId,
                                railSectionType = item.sectionType
                            )
                        )
                    )
                }

                MID_SCROLL_GAME -> {
                    ignorePositions.add(position)
                    val midscrollViewHolder = holder as ViewHolderMidScrollGames
                    val contentlist = item.contentItem.filter{
                        return@filter if(it.screenName?.equals(MID_SCROLL_DETAIL_SCREEN,true) == true)
                            isValidRentalContent(it,item.dthStatus,item.sectionSource,item.sectionType, item.refId)
                        else
                            true
                    }


                    /**
                     * Aman/ashutosh check here with Shilpi for item.contentItem
                     */
                    midscrollViewHolder.bind(
                        RailsModel(
                            "",
                            RailAdapter(
                                mBannerClick,
                                contentlist,
                                ItemLayoutType.MID_SCROLL_RAIL.name,
                                (position - ignorePositions.filter { it < position }.size + 1),
                                cloudinaryUrl,
                                item.sectionSource,
                                item.continueWatching,
                                item.title,
                                providerLogos,
                                railPoint,
                                sharedPrefs = sharedPrefs,
                                mNonSubscribedPartnerList = mNonSubscribedPartnerList,
                                isPackAvailed = isPackAvailed,
                                refId = item.refId,
                                railSectionType = item.sectionType
                            )
                        )
                    )
                }


            }
        }
    }


    private inner class ViewHolderUnsubscribedProvider internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: LayoutUnsubscribedBannerBinding? = DataBindingUtil.bind(itemView)
        fun bind(items: HomeResponse.Items) {
            binding?.contentItem = items
            val url = getCloudinaryUrl(
                cloudinaryUrl,
                items.providerImg
            )
//            binding?.partnerBanner?.setBackgroundResource(R.drawable.banner_img)
            binding?.btnActivatePartner?.setOnClickListener {
                //handle subscription part under development
                onAddPackClickListener?.onAddPack(items.providerId, items.title)
            }
            if (isUpgradePack != null)
                binding?.btnActivatePartner?.text = isUpgradePack
            if (items.isBanner)
                binding?.partnerBanner?.show()
            else
                binding?.partnerBanner?.hide()
            updateProviderBanner(binding?.partnerBanner!!, items.title, providerLogos)
        }
    }


    private inner class ViewHolderMidScrollGames(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: HomeMidscrollBinding? = DataBindingUtil.bind(itemView)
        fun bind(model: RailsModel) {
            if (binding?.homeRecyclerView?.adapter != null) {
                binding.homeRecyclerView.swapAdapter(model.adapter, false)
            } else {
                binding?.railsModel = model
                val snapHelper: SnapHelper = PagerSnapHelper()
                binding?.homeRecyclerView?.onFlingListener = null
                snapHelper.attachToRecyclerView(binding?.homeRecyclerView)
            }
        }
    }


    private inner class ViewHolderMidScrollBanner(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: HomeMidscrollBinding? = DataBindingUtil.bind(itemView)
        fun bind(model: RailsModel) {
            if (binding?.homeRecyclerView?.adapter != null) {
                binding.homeRecyclerView.swapAdapter(model.adapter, false)
            } else {
                binding?.railsModel = model
                val snapHelper: SnapHelper = PagerSnapHelper()
                binding?.homeRecyclerView?.onFlingListener = null
                snapHelper.attachToRecyclerView(binding?.homeRecyclerView)
            }
        }
    }

    private inner class ViewHolderMidScroll(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: HomeMidscrollBinding? = DataBindingUtil.bind(itemView)
        fun bind(model: RailsModel) {
            if (binding?.homeRecyclerView?.adapter != null) {
                binding.homeRecyclerView.swapAdapter(model.adapter, false)
            } else {
                binding?.railsModel = model
                val snapHelper: SnapHelper = PagerSnapHelper()
                binding?.homeRecyclerView?.onFlingListener = null
                snapHelper.attachToRecyclerView(binding?.homeRecyclerView)
            }
        }
    }

    /*View Holder for all type of rails*/
    inner class ViewHolderRails(itemView: View) :
        RecyclerView.ViewHolder /*implements View.OnClickListener */(itemView) {
        val binding: HomeRecyclerviewBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            railsModel: RailsModel,
            mSeeAllClickListener: CommonSeeAllClickListener,
            railId: Int,
            railName: String,
            sectionSource: String,
            lastIndex: Int,
            placeHolder: String,
            configType: String?,
            provider: String?,
            isMixedRail: Boolean,
            isPrepand: Boolean,
            item: HomeResponse.Items,
            backgroundImage: String?,
            layoutType: String?,
            refId: String,
            packName: String,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            //TODO: May have performance impact
            //For games rail background is different on home
            if ((sectionSource.equals(ItemViewType.GAMES.name, true)
                        || layoutType?.equals(ItemLayoutType.SQUARE.name, true) == true)
                && pageName.equals(
                    KEY_HOME,
                    true
                )
            ) {
                binding?.clHomeRoot?.setBackground(
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_bg_home_game_rail
                    )
                )
            } else {
                binding?.clHomeRoot?.setBackgroundResource(0)
            }
            if (binding?.homeRecyclerView?.adapter != null) {
                binding.homeRecyclerViewTitle.text = railsModel.title
                binding.homeRecyclerView.swapAdapter(railsModel.adapter, false)
            } else {
                binding?.railsModel = railsModel
            }
            binding?.homeRecyclerView?.clearOnScrollListeners()
            binding?.homeRecyclerView?.addOnScrollListener(CustomScrollListener {
                mRailScrollListener.onRailScrolled(
                    railName,
                    (adapterPosition - ignorePositions.filter { it < adapterPosition }.size + 1),
                    item.configType?.uppercase()?: EDITORIAL,
                    item.sectionType
                )
            })
            binding?.homeSeeAll?.setOnClickListener {
                mSeeAllClickListener.onSeeAllClick(
                    Pair(
                        railId,
                        railName
                    ),
                    sectionSource,
                    (adapterPosition - ignorePositions.filter { it < adapterPosition }.size + 1),
                    placeHolder,
                    configType,
                    provider,
                    isMixedRail,
                    isPrepand,
                    item,
                    backgroundImage,
                    layoutType,
                    refId,
                    packName,
                    contentAnalyticsModel
                )
            }
        }

    }

    private inner class ViewHolderAllChannel internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: HomeRecyclerviewBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            railsModel: RailsModel,
            mSeeAllClickListener: CommonSeeAllClickListener,
            railId: Int,
            railName: String,
            sectionSource: String,
            placeHolder: String,
            configType: String?,
            backgroundImage: String?,
            layoutType: String?,
            refId: String,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.railsModel = railsModel

            binding?.homeRecyclerView?.clearOnScrollListeners()
            binding?.homeRecyclerView?.addOnScrollListener(CustomScrollListener {
                mRailScrollListener.onRailScrolled(
                    railName,
                    (adapterPosition - ignorePositions.filter { it < adapterPosition }.size + 1),
                    configType?.uppercase()?: EDITORIAL,
                    sectionSource
                )
            })

            binding?.homeSeeAll?.setOnClickListener {
                mSeeAllClickListener.onSeeAllClick(
                    Pair(
                        railId,
                        railName
                    ),
                    sectionSource,
                    (adapterPosition - ignorePositions.filter { it < adapterPosition }.size + 1),
                    placeHolder,
                    configType,
                    backgroundImage = backgroundImage,
                    layoutType = layoutType,
                    refId = refId,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    private inner class ViewHolderBanner(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val banner: HomeHeroView = itemView as HomeHeroView
    }

    private inner class ViewHolderProviderContents(val itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var viewPortPosition : Int = -1
        val binding: LayoutProviderWithContentBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            railsModel: RailsModel,
            item: HomeResponse.Items,
            providerLogos: ProviderLogo,
            railPoint: RailPoint,
            mBannerClick: CommonDTOClickListener,
            mixedLayoutType: String
        ) {
            binding?.let { it ->


                if (isPackUpdated) {
                    it.viewpagerProviders.currentVirtualPosition = calculateCarouselScrollPosition(
                        it.viewpagerProviders.currentPosition,
                        it.viewpagerProviders.currentVirtualPosition,
                        0,
                        item.filteredProvider.size
                    )
                }
                if (it.rvProviderContents.adapter == null) {
                    it.railsModel = railsModel

                    (it.rvProviderContents.layoutManager as GridLayoutManager).spanSizeLookup =
                        handleMixedRailSpanSize(mixedLayoutType)

                    if (railPoint.providerPoint == null || railPoint.mProviderWidth == null || railPoint.mProviderHeight == null) {
                        railPoint.providerPoint = getProviderIconLandscapePoint(itemView.context)
                        railPoint.mProviderWidth = railPoint.providerPoint?.x
                        railPoint.mProviderHeight = railPoint.providerPoint?.y
                    }
                    val lp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        railPoint.mProviderHeight!!
                    )
                    lp.addRule(RelativeLayout.BELOW, R.id.home_recycler_view_title)
//                    lp.setMargins(0, dpToPx(itemView.context, 8), 0, 0)
                    it.viewpagerProviders.layoutParams = lp
                    if (it.viewpagerProviders.onScrollListener == null) {
                        it.viewpagerProviders.onScrollListener =
                            object : CarouselOnScrollListener {
                                override fun onScrolled(
                                    recyclerView: RecyclerView,
                                    dx: Int,
                                    dy: Int,
                                    position: Int,
                                    carouselItem: CarouselItem?
                                ) {
                                    recyclerView.post {
                                        try {
                                            handleProviderCircularViewAlpha(recyclerView)
                                        } catch (e: Exception) {

                                        }
                                    }
                                    e(
                                        "CarouselItem", "onScrolled $position," +
                                                "it.viewpagerProviders?.currentPosition: ${it.viewpagerProviders.currentPosition}"
                                    )

                                    super.onScrolled(recyclerView, dx, dy, position, carouselItem)
                                }

                                override fun onScrollStateChanged(
                                    recyclerView: RecyclerView,
                                    newState: Int,
                                    position: Int,
                                    carouselItem: CarouselItem?
                                ) {

                                    if (newState == 0) {

                                        carouselItem?.caption?.let {
                                            if (lastProviderIndex == it) {
                                                return
                                            }
                                            lastProviderIndex = it
                                        }


                                        if (it.rvProviderContents.adapter is RailAdapter) {
                                            val lastProvider =
                                                item.filteredProvider[lastProviderIndex.toInt()]
                                            item.lastPosition = lastProviderIndex.toInt()
                                            val lastProviderData = PartnerData()
                                            lastProviderData.layoutType = lastProvider.layoutType
                                            lastProviderData.contentList = lastProvider.contentItem
                                            lastProviderData.provider = lastProvider.provider
                                            (it.rvProviderContents.layoutManager as GridLayoutManager).spanSizeLookup =
                                                handleMixedRailSpanSize(lastProviderData.layoutType)
                                            val adapter: RailAdapter =
                                                it.rvProviderContents.adapter as RailAdapter
                                            updateSbscriberList()
                                            adapter.updateListForApps(lastProviderData, mNonSubscribedPartnerList)
                                        }
                                    }
                                    e(
                                        "CarouselItem",
                                        "onScrollStateChanged position: $position," +
                                                "currentPosition: , " +
                                                "it.viewpagerProviders?.currentPosition: ${it.viewpagerProviders.currentPosition}"
                                    )

                                    super.onScrollStateChanged(
                                        recyclerView,
                                        newState,
                                        position,
                                        carouselItem
                                    )
                                }
                            }

                    }
                    if (it.viewpagerProviders.carouselListener == null) {
                        it.viewpagerProviders.carouselListener = object : CarouselListener {
                            override fun onBindViewHolder(
                                mBinding: ViewBinding,
                                carouselItem: CarouselItem,
                                position: Int
                            ) {
                                val layoutParams =
                                    ConstraintLayout.LayoutParams(
                                        railPoint.mProviderWidth!!,
                                        railPoint.mProviderHeight!!
                                    )
                                mBinding.root.layoutParams =
                                    layoutParams
                                transparentImageLoad(
                                    mBinding.root.findViewById<ImageView>(R.id.iv_app_image),
                                    carouselItem.imageUrl ?: ""
                                )

                                mBinding.root.findViewById<ImageView>(R.id.iv_app_image)
                                    .setOnClickListener(object : OnDoubleTapListener() {
                                        override fun onDoubleClick(v: View?) {
                                            e(
                                                "onDoubleClick", " $position," +
                                                        "it.viewpagerProviders?.currentPosition: ${it.viewpagerProviders.currentPosition}"
                                            )
                                            val position = it.viewpagerProviders.currentPosition
                                            clickOnProviderSeeAllPage(
                                                position,
                                                item,
                                                railsModel.adapter.sectionPosition
                                            )
                                        }

                                        override fun onClick(v: View?) {
                                            e(
                                                "CarouselItem", " $position," +
                                                        "it.viewpagerProviders?.currentPosition: ${it.viewpagerProviders.currentPosition}"
                                            )
                                            it.viewpagerProviders.currentVirtualPosition =
                                                calculateCarouselScrollPosition(
                                                    it.viewpagerProviders.currentPosition,
                                                    it.viewpagerProviders.currentVirtualPosition,
                                                    position,
                                                    item.filteredProvider.size
                                                )
                                            item.lastPosition = it.viewpagerProviders.currentPosition
                                            super.onClick(v)
                                        }
                                    })
                            }


                            override fun onCreateViewHolder(
                                layoutInflater: LayoutInflater,
                                parent: ViewGroup
                            ): ViewBinding? {
                                return LayoutProviderCurvedBinding.inflate(
                                    LayoutInflater.from(parent.context),
                                    parent,
                                    false
                                )
                            }

                        }
                    }

                    it.viewpagerProviders.imagePlaceholder =
                        itemView.context.getDrawable(R.drawable.carousel_default_placeholder)
                    if (it.viewpagerProviders.getData().isNullOrEmpty()) {
                        val carouselList = mutableListOf<CarouselItem>()
                        item.filteredProvider.mapIndexed { index, contentItem ->
                            if (contentItem.contentItem.size > 0) {
                                val appName = getAppForProvider(
                                    contentItem.provider,
                                    providerLogos
                                )
                                var url = appName?.logoCircular
                                if(appName == null){
                                    url = cloudinaryUrl + ProvidersCache.availableProviders[contentItem.provider.lowercase()]?.logoCircular
                                }
                                carouselList.add(
                                    CarouselItem(
                                        url,
                                        index.toString()
                                    )
                                )
                            }
                        }
                        it.viewpagerProviders.setData(carouselList)
                    }

                }
                else if(isPackUpdated){
                    val firstProviderData = PartnerData()
                    firstProviderData.layoutType = item.filteredProvider[item.lastPosition].layoutType
                    firstProviderData.contentList = item.filteredProvider[item.lastPosition].contentItem
                    firstProviderData.provider = item.filteredProvider[item.lastPosition].provider

                    updateSbscriberList()
                    (it.rvProviderContents.adapter as RailAdapter).updateListForApps(
                        firstProviderData,
                        mNonSubscribedPartnerList
                    )
                }

                isPackUpdated = false
                /* Fixed for https://jira.tothenew.com/browse/TSF-13653 */
                it.executePendingBindings()
                it.root.invalidate()
                /*----*/
                it.homeSeeAll.setOnClickListener { _ ->
                    val position = it.viewpagerProviders.currentPosition
                    clickOnProviderSeeAllPage(position, item, railsModel.adapter.sectionPosition)
                }
            }
        }

        private fun calculateCarouselScrollPosition(
            currentPosition: Int,
            currentVirtualPosition: Int,
            position: Int,
            providerListSize: Int
        ): Int {
            var carouselScrollingIndex = position
            when {
                (currentPosition == 0
                        || currentPosition == 1)
                        && position > 3 -> {
                    carouselScrollingIndex -= providerListSize
                }
                (currentPosition == providerListSize - 1
                        || currentPosition == providerListSize - 2)
                        && position < 2 -> {
                    carouselScrollingIndex += providerListSize
                }
            }


            carouselScrollingIndex -= currentPosition
            if (carouselScrollingIndex > 0) {
                carouselScrollingIndex += 2
            } else {
                carouselScrollingIndex -= 2
            }
            return currentVirtualPosition + carouselScrollingIndex
        }

        private fun handleProviderCircularViewAlpha(recyclerView: RecyclerView) {
            recyclerView[6].apply {
                this.alpha = 0.5f
                this.isSelected = false
                this.findViewById<ImageView>(R.id.imgOverlay).show()
            }
            recyclerView[5].apply {
                this.alpha = 0.5f
                this.isSelected = false
                this.findViewById<ImageView>(R.id.imgOverlay).show()

                if (isLandTablet(context)) {
                    this.alpha = 1f
                    this.isSelected = true
                    this.findViewById<ImageView>(R.id.imgOverlay).hide()
                }
            }
            recyclerView[4].apply {

                this.alpha = 0.5f
                this.isSelected = false
                this.findViewById<ImageView>(R.id.imgOverlay).show()

                if (isTablet(context) && !isLandTablet(context)) {
                    this.alpha = 1f
                    this.isSelected = true
                    this.findViewById<ImageView>(R.id.imgOverlay).hide()
                }
            }
            recyclerView[3].apply {
                this.alpha = 1f
                this.isSelected = true
                this.findViewById<ImageView>(R.id.imgOverlay).hide()

                if (isTablet(context) || isLandTablet(context)) {
                    this.alpha = 0.5f
                    this.isSelected = false
                    this.findViewById<ImageView>(R.id.imgOverlay).show()
                }

            }
            recyclerView[2].apply {
                this.alpha = 0.5f
                this.isSelected = false
                this.findViewById<ImageView>(R.id.imgOverlay).show()
            }
            recyclerView[1].apply {
                this.alpha = 0.5f
                this.isSelected = false
                this.findViewById<ImageView>(R.id.imgOverlay).show()
            }
            recyclerView[0].apply {
                this.alpha = 0.5f
                this.isSelected = false
                this.findViewById<ImageView>(R.id.imgOverlay).show()
            }

        }
    }

    private fun clickOnProviderSeeAllPage(position: Int, item: HomeResponse.Items, sectionPosition: Int) {
        if (position < item.filteredProvider.size) {
            val contentAnalyticsModel = item.getContentAnalyticsModel()
            mBannerClick.onSubItemClick(
                item.filteredProvider[position],
                position,
                sectionPosition,
                EventConstants.TYPE_APPS,
                null,
                item.title,
                item.sectionSource,
                contentAnalyticsModel = contentAnalyticsModel
            )
        }
    }

    private inner class StartFreeTrialViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: LayoutStartFreeTrialNudgeBinding? = DataBindingUtil.bind(itemView)
        fun bind(position: Int, contentAnalyticsModel: ContentAnalyticsModel) {
            binding?.trialBtnProceed?.setOnClickListener {
                mBannerClick.onSubItemClick(
                    ContentItem().apply {
                        currentDay = ""
                        availableDays = ""
                    },
                    position,
                    (position - ignorePositions.filter { it < position }.size + 1),
                    EventConstants.TYPE_START_FREE_TRIAL,
                    null,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
            freeTrialStartupNudgeData?.let {
                if (!it.title.isNullOrEmpty()) {
                    binding?.ftTitleTextView?.show()
                }
                binding?.verbiagesDetails = it
            }
        }
    }

    private inner class SelectPaidPackViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: LayoutSelectPaidPackNudgeBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            position: Int,
            progress: Double,
            currentDay: Int,
            availableDays: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.btnSelectSubscription?.setOnClickListener {
                mBannerClick.onSubItemClick(
                    ContentItem().apply {
                        this.currentDay = currentDay.toString()
                        this.availableDays = availableDays.toString()
                    },
                    position,
                    (position - ignorePositions.filter { it < position }.size + 1),
                    EventConstants.TYPE_SELECT_PAID_PACK,
                    null,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
            val paidPackSelectionNudgeData = partnerPacks?.getPaidPackSelectionNudge()
            binding?.verbiagesData = paidPackSelectionNudgeData
            binding?.progressBar?.progress = progress.toInt()
        }
    }

    private inner class UpgradeFreeTrialViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: LayoutSelectPaidPackNudgeBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            position: Int,
            progress: Double,
            currentDay: Int,
            availableDays: Int,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.btnSelectSubscription?.setOnClickListener {
                mBannerClick.onSubItemClick(
                    ContentItem().apply {
                        this.currentDay = currentDay.toString()
                        this.availableDays = availableDays.toString()
                    },
                    position,
                    (position - ignorePositions.filter { it < position }.size + 1),
                    EventConstants.TYPE_FREE_TRIAL_UPGRADE,
                    null,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
            val paidPackSelectionNudgeData = partnerPacks?.getPaidPackSelectionNudge()
            binding?.verbiagesData = paidPackSelectionNudgeData
            binding?.progressBar?.progress = progress.toInt()
        }
    }

    private inner class GameNudgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding: LayoutGameSubscribeWidgetBinding? = DataBindingUtil.bind(itemView)
        fun bind(position: Int, contentAnalyticsModel: ContentAnalyticsModel) {
            binding?.btnGameSubscribe?.setOnClickListener {
                mBannerClick.onSubItemClick(
                    ContentItem(),
                    position,
                    (position - ignorePositions.filter { it < position }.size + 1),
                    ItemViewType.GAME_NUDGE.name,
                    null,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }

    private inner class SelectLanguageWidgetViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: LayoutSelectLanguageWidgetBinding? = DataBindingUtil.bind(itemView)
        fun bind(position: Int, contentAnalyticsModel: ContentAnalyticsModel) {
            binding?.tvSelectLanguages?.setOnClickListener {
                mBannerClick.onSubItemClick(
                    ContentItem(),
                    position,
                    (position - ignorePositions.filter { it < position }.size + 1),
                    EventConstants.TYPE_SELECT_LANGUAGE_POP_UP,
                    null,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }
    }


    private inner class ShuffleRailViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val binding: LayoutShuffleRailBinding? = DataBindingUtil.bind(itemView)
        var viewPortPosition = -1
        fun bind(
            railsModel: RailsModel,
            item: HomeResponse.Items,
            providerLogos: ProviderLogo,
        ) {
            binding?.let {
                item.filteredShuffleList[item.shuffleIndex].provider = item.provider
                if (it.rvShuffleContents.adapter == null) {
                    it.railsModel = railsModel
                } else {
                        updateSbscriberList()
                    (it.rvShuffleContents.adapter as RailAdapter).updateListForApps(
                        item.filteredShuffleList[item.shuffleIndex],
                        mNonSubscribedPartnerList
                    )
                }


                val layoutManager = (binding.rvShuffleContents.layoutManager as GridLayoutManager)
                layoutManager.spanSizeLookup =
                    handleMixedRailSpanSize(item.filteredShuffleList[item.shuffleIndex].layoutType)
                if (!item.provider.isNullOrEmpty()) {
                    it.ivProvider.visibility = View.VISIBLE
                    it.tvRailTitle.visibility = View.GONE
                    val constraintLayout: ConstraintLayout = it.clProviderSpecificRail
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(
                        R.id.rv_shuffle_contents,
                        ConstraintSet.TOP,
                        R.id.iv_provider,
                        ConstraintSet.BOTTOM,
                        dpToPx(it.root.context, 8)
                    )
                    constraintSet.applyTo(constraintLayout)
                    updateProviderImage(
                        it.ivProvider,
                        item.provider ?: "",
                        providerLogos,
                        R.drawable.ic_rail_placeholder
                    )

                } else {
                    it.ivProvider.visibility = View.GONE
                    it.tvRailTitle.visibility = View.VISIBLE
                    it.tvRailTitle.text = item.title

                    val constraintLayout: ConstraintLayout = it.clProviderSpecificRail
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(
                        R.id.rv_shuffle_contents,
                        ConstraintSet.TOP,
                        R.id.tv_rail_title,
                        ConstraintSet.BOTTOM,
                        dpToPx(it.root.context, 8)
                    )
                    constraintSet.applyTo(constraintLayout)
                }

                if (item.filteredShuffleList.size > 1) {
                    it.tvShuffle.show()
                    var hashMapLandscape : HashMap<Int, PartnerData>
                            = HashMap()
                    var shuffleArray=ArrayList<ContentItem>()
                    var index=0
                    for(railItem in item.filteredShuffleList){
                        railItem?.let {
                            if(it.layoutType.equals(ItemLayoutType.LANDSCAPE.name,true)){
                                hashMapLandscape.put(index,it)
                            }else{
                                shuffleArray.addAll(it.filteredContentItems)
                            }
                        }

                        index++
                    }

                    it.tvShuffle.setOnClickListener { _ ->


                        homeAnalytics?.trackShuffleClick(
                            railType = "SHUFFLE",
                            pageName = pageName ?: "",
                            railTitle = item.title,
                            railPosition = railsModel.adapter.sectionPosition.toString() ?: ""
                        )
                        val adapter: RailAdapter =
                            binding.rvShuffleContents.adapter as RailAdapter
                        var index=0
                        var latestShuffledItem= item.filteredShuffleList[index]
                        if(item.shuffleIndex != item.filteredShuffleList.size-1){
                            index= item.shuffleIndex + 1
                        }
                        if(hashMapLandscape.containsKey(index)){
                           latestShuffledItem= hashMapLandscape.get(index)!!
                        }else{
                            latestShuffledItem=PartnerData()
                            latestShuffledItem.layoutType = ItemLayoutType.PORTRAIT.name
                            latestShuffledItem.contentList = shuffleArray.asSequence()
                                .shuffled()
                                .take(8)
                                .toList()
                        }

                        latestShuffledItem.provider = item.provider
                        if (it.rvShuffleContents.adapter is RailAdapter) {
                            layoutManager.spanSizeLookup =
                                handleMixedRailSpanSize(latestShuffledItem.layoutType)
                            item.shuffleIndex = index
                            updateSbscriberList()
                            adapter.updateListForApps(
                                latestShuffledItem,
                                mNonSubscribedPartnerList
                            )
                        }
                    }
                } else {
                    it.tvShuffle.hide()
                }
            }
        }
    }


    private inner class TittleRailWithBackgroundImageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val binding: LayoutSportsWithBackgroundBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            railsModel: RailsModel,
            item: HomeResponse.Items,
            providerLogos: ProviderLogo,
        ) {
            binding?.let {
                if (it.rvSportsRail.adapter != null) {
                    it.rvSportsRail.swapAdapter(railsModel.adapter, false)
                } else {
                    it.railsModel = railsModel
                }
                val bgImagePoint = getSportsBgImageDimension(it.root.context)
                val url = getCloudinaryUrl(
                    cloudinaryUrl,
                    bgImagePoint.x, bgImagePoint.y,
                    item.backgroundImage
                )
                imageLoad(it.ivBgSports, url)
                if(item.provider.isNullOrEmpty()){
                    it.ivProviderLogo.invisible()

                }
                else {
                    it.ivProviderLogo.show()
                    railsModel.adapter.layoutType = ItemLayoutType.SINGLE_PROVIDER_BANNER_RAIL.name


                    binding.clRoot.layoutParams =
                        ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )


                    updateProviderImage(
                        it.ivProviderLogo,
                        item.provider ?: PROVIDER_ZEE5,
                        providerLogos,
                        R.drawable.ic_rail_placeholder
                    )
                }
            }
        }


    }


    public override fun getNormalItemViewType(position: Int): Int {
//        e("getNormalItemViewType","${mDataList[position].sectionSource} - sectionSource")
        when {
            mDataList[position].sectionSource.equals(
                ItemViewType.DARSHAN_CHANNEL.name,
                ignoreCase = true
            ) -> {
                return RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.LIVE_EVENT_BANNER.name,
                ignoreCase = true
            ) -> {
                if (mDataList[position].filteredContentItems.isEmpty()) return 0
                return LIVE_BANNER
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.GAME_OF_THE_WEEK.name,
                ignoreCase = true
            ) -> {
                return GAME_WEEK_BANNER
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.NEWLY_ADDED_GAMES.name,
                true
            ) ->{
                return NEWLY_ADDED_GAMES
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.GENRE.name,
                true
            ) -> {
                return GENRE_RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.BINGE_CHANNEL.name,
                ignoreCase = true
            ) -> {
                return RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.GAMES.name,
                ignoreCase = true
            ) -> {
                return RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.GENRE_RAIL_FOR_GAMES.name,
                ignoreCase = true
            ) -> {
                return RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.CATEGORY.name,
                ignoreCase = true
            ) -> {
                return RAIL
            }

            mDataList[position].sectionSource.equals(
                ItemViewType.LANGUAGE_SECTION.name,
                ignoreCase = true
            ) || mDataList[position].sectionSource.equals(
                ItemViewType.LANGUAGE_NUDGE.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = SELECT_LANGUAGE_WIDGET
                return SELECT_LANGUAGE_WIDGET
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.GAME_NUDGE.name
            ) ->{
                mDataList[position].viewType = GAME_NUDGE
                return GAME_NUDGE
            }
            mDataList[position].sectionType.equals(
                ItemViewType.HERO_BANNER.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = HEROBANNER
                mHBPosition = position
                return HEROBANNER
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.MID_SCROLL_BANNER.name,
                ignoreCase = true
            ) ->{
                mDataList[position].viewType = MID_SCROLL_BANNER
                return MID_SCROLL_BANNER
            }

            mDataList[position].sectionSource.equals(
                ItemViewType.MID_BANNER_GAMES.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = MID_SCROLL_GAME
                return MID_SCROLL_GAME
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.MID_BANNER_RAIL.name,
                ignoreCase = true
            )|| mDataList[position].sectionSource.equals(
                ItemViewType.MID_BANNER_PROMO.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = MID_SCROLL
                return MID_SCROLL
            }
            mDataList[position].sectionType.equals(
                ItemViewType.ALL_CHANNELS.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = ALLCHANNELS
                return ALLCHANNELS
            }
            mDataList[position].sectionType.equals(
                ItemViewType.CONTINUE_WATCHING.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = RAIL
                return RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.BACKGROUND_BANNER_RAIL.name,
                ignoreCase = true
            ) -> {
                if (mDataList[position].filteredContentItems.isEmpty()) return 0
                mDataList[position].viewType = TITTLE_RAIL_WITH_BACKGROUND_IMAGE
                return TITTLE_RAIL_WITH_BACKGROUND_IMAGE
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.PROVIDER_BROWSE_APPS.name,
                true
            ) -> {
                mDataList[position].viewType = PROVIDER_WITH_CONTENTS
                return PROVIDER_WITH_CONTENTS
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.SHUFFLE_RAIL.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = SHUFFLE_RAIL
                return SHUFFLE_RAIL
            }
            mDataList[position].sectionType.equals(ItemViewType.RAIL.name, ignoreCase = true)
                    && !mDataList[position].sectionSource.equals(
                ItemViewType.PROVIDER.name,
                ignoreCase = true
            )
                    && !mDataList[position].sectionSource.equals(
                ItemViewType.FREE_TRIAL.name,
                ignoreCase = true
            )
                    && !mDataList[position].sectionSource.equals(
                ItemViewType.PAID_TRIAL.name,
                ignoreCase = true
            )
                    && !mDataList[position].sectionSource.equals(
                ItemViewType.FREE_TRIAL_UPGRADE.name,
                ignoreCase = true
            )&& !mDataList[position].sectionSource.equals(
                ItemViewType.MERGE_GAME_RAIL.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = RAIL
                return RAIL
            }
            mDataList[position].sectionType.equals(ItemViewType.WIDGET.name, ignoreCase = true) -> {
                mDataList[position].viewType = WIDGET
                return WIDGET
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.PROVIDER.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = PROVIDER
                return PROVIDER
            }
            //PROVIDER_UNSUBSCRIBED
            mDataList[position].sectionType.equals(
                ItemViewType.PROVIDER_UNSUBSCRIBED.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = PROVIDER_UNSUBSCRIBED
                return PROVIDER_UNSUBSCRIBED
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.FREE_TRIAL.name,
                ignoreCase = true
            ) -> {
                mDataList[position].viewType = STARTFREETRIALSTARTNUDGE
                positionOfNudges.add(position)
                return STARTFREETRIALSTARTNUDGE
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.PAID_TRIAL.name,
                ignoreCase = true
            ) -> {
                positionOfNudges.add(position)
                mDataList[position].viewType = SELECTPAIDPACKNUDGE
                return SELECTPAIDPACKNUDGE
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.MERGE_GAME_RAIL.name,
                ignoreCase = true
            ) -> {
                return MERGE_GAME_RAIL
            }
            mDataList[position].sectionSource.equals(
                ItemViewType.FREE_TRIAL_UPGRADE.name,
                ignoreCase = true
            ) -> {
                positionOfNudges.add(position)
                mDataList[position].viewType = FREETRIALUPGRADENUDGE
                return FREETRIALUPGRADENUDGE
            }

            else -> return 0
        }

    }


    fun notifyOrientationChange(customRecyclerView: CustomRecyclerView) {
        if(!customRecyclerView.isComputingLayout)
            notifyDataSetChanged()

        orientationChanged = true
    }

    fun notifyBannerChanges(){
        mBanner?.banner?.addSpaceValidation(true)
    }



    fun updateList(mItems: MutableList<HomeResponse.Items>) {
        updateSbscriberList()
        val diffResult = DiffUtil.calculateDiff(RailDiffCallback(this.mDataList, mItems), true)
        updateDataWithDiffCallback(
            mItems,
            diffResult,
            /* Passing empty model as items already have necessary data */
            emptyContentAnalyticsModel()
        )
        ignorePositions.clear()
        positionOfNudges.clear()
    }

    fun addToList(mItems: MutableList<HomeResponse.Items>){
        addTomDataList(
            mItems.filter { !mDataList.contains(it) },
            /* Passing empty model as items already have necessary data */
            emptyContentAnalyticsModel()
        )
       val uniquRailItems= mDataList.distinctBy { it.id } as MutableList
        mDataList=uniquRailItems
    }

    fun addLoading() {
        isAppending = true
    }

    fun removeLoading() {
        isAppending = false
    }

    fun replaceContentItems(
        item: HomeResponse.Items,
        listOfContentsToBeReplaceWith: ArrayList<ContentItem>
    ) {
        val indexOf = getIndexOf(item)
        if (indexOf != -1) {
            mDataList[indexOf].contentItem = listOfContentsToBeReplaceWith
            notifyItemChanged(indexOf)
        }
    }

    fun removeItem(
        item: HomeResponse.Items
    ) {
        val indexOf = getIndexOf(item)
        if (indexOf != -1) {
            removeItem(indexOf)
            ignorePositions.remove(indexOf)
        }
    }

    private fun getIndexOf(item: HomeResponse.Items): Int {
        return mDataList.indexOf(item)
    }

    fun getLastItemPosition() : Int{
        var lastItemPosition = 0
        if(manager != null) {
            lastItemPosition = manager!!.findLastCompletelyVisibleItemPosition()
        }
        return lastItemPosition
    }

    private fun handleMixedRailSpanSize(mixedLayoutType: String?):GridLayoutManager.SpanSizeLookup{
        return object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (ItemLayoutType.PORTRAIT.name.equals(mixedLayoutType,true)) {
                    return 1
                }
                return when (position) {
                    0 -> 2
                    else -> 1
                }
            }
        }
    }

    private fun handleTrailerExchanger(trailer: TrailerView?, addTrailer: Boolean, position: Int) {

        trailer?.mPlayUrl?.let {

            Log.d("Exchanger", "added " + position + " "+it)
            mTrailerMap[position] = trailer
        }

    }

    fun handleHomeTrailerScrolling(
        startIndex: Int,
        lastIndex: Int,
        scrollDown: Boolean
    ) {
        Log.d("Exchanger", "first " + startIndex)
        Log.d("Exchanger", "last " + lastIndex)
        if (mTrailerMap.isEmpty()) {
            return
        }
        mCurrentTrailerPair?.let { currentTrailerPair ->

            if (!scrollDown) {
                if (currentTrailerPair.first in startIndex..lastIndex) {
                    //play trailer
                    if (currentTrailerPair.first == startIndex)

                        for (trailerKey in mTrailerMap.keys) {
                            if (trailerKey != currentTrailerPair.first && trailerKey > currentTrailerPair.first) {
                                Log.d(
                                    "Exchanger",
                                    "viewport postion changes" + trailerKey + ":" + currentTrailerPair.first
                                )
                                handleHomeTrailerPlayBack(true)
                                mCurrentTrailerPair = Pair(trailerKey, mTrailerMap.get(trailerKey))
                                handleHomeTrailerPlayBack(false)
                                return
                            }
                        }
                    else {
                        Log.d("Exchanger", "current present in viewport : Played")
                        handleHomeTrailerPlayBack(false)
                    }

                } else {

                    findNewCurrentTrailer(startIndex, lastIndex, scrollDown)?.let {

                        handleHomeTrailerPlayBack(true)
                        mCurrentTrailerPair = it
                        Log.d("Exchanger", "new current found in viewport" + it.first)
                        handleHomeTrailerPlayBack(false)
                    }
                }
                return
            } else {
                if (currentTrailerPair.first in startIndex..lastIndex) {
                    //play trailer
                    if (currentTrailerPair.first == lastIndex)
                        findNewCurrentTrailer(startIndex, lastIndex, scrollDown)?.let {

                            handleHomeTrailerPlayBack(true)
                            mCurrentTrailerPair = it
                            Log.d("Exchanger", "viewport postion changes" + it.first)
                            handleHomeTrailerPlayBack(false)
                        }
//                        for (trailerKey in mTrailerMap.keys) {
//                            if (trailerKey != currentTrailerPair.first && trailerKey < currentTrailerPair.first) {
//                                Log.d(
//                                    "Exchanger",
//                                    "viewport postion changes" + trailerKey + ":" + currentTrailerPair.first
//                                )
//                                handleHomeTrailerPlayBack(true)
//                                mCurrentTrailerPair = Pair(trailerKey, mTrailerMap.get(trailerKey))
//                                handleHomeTrailerPlayBack(false)
//                                return
//                            }
//                        }
                    else {
                        Log.d("Exchanger", "current present in viewport : Played")
                        handleHomeTrailerPlayBack(false)
                    }

                } else {

                    findNewCurrentTrailer(startIndex, lastIndex, scrollDown)?.let {

                        handleHomeTrailerPlayBack(true)
                        mCurrentTrailerPair = it
                        Log.d("Exchanger", "new current found in viewport" + it.first)
                        handleHomeTrailerPlayBack(false)
                    }
                }
                return
            }

        }
        findNewCurrentTrailer(startIndex, lastIndex, !scrollDown)?.let {
            mCurrentTrailerPair = it
            handleHomeTrailerPlayBack(false)
        }


    }

    private fun
            findNewCurrentTrailer(
        startIndex: Int = -1,
        lastIndex: Int = -1,
        scrollDown: Boolean
    ): Pair<Int, TrailerView?>? {

        //To play the video which is on the top of list
        var indexToPlay: Int = -1
        if (!scrollDown)
            for (position in startIndex..lastIndex) {
                if (mTrailerMap.containsKey(position)) {
                    if (indexToPlay == -1)
                        indexToPlay = position

                    if (indexToPlay > position + 1)
                        indexToPlay = position

                }
            }
        else
            for (position in startIndex..lastIndex) {
                if (mTrailerMap.containsKey(position)) {
                    if (indexToPlay == -1)
                        indexToPlay = position

                    if (indexToPlay < position)
                        indexToPlay = position

                }
            }
        if (indexToPlay == -1)
            return null
        return Pair(indexToPlay, mTrailerMap.get(indexToPlay))

    }

    fun handleHomeTrailerPlayBack(pause: Boolean) {

        mCurrentTrailerPair?.let { currentTrailerPair ->
            if (pause) {
                Log.d("Exchanger", "Pause")
                currentTrailerPair.second?.pausePlayer()
            } else {
                Log.d("Exchanger", "Play")
                currentTrailerPair.second?.playUrl()
            }

        }
    }

    fun removeAllData(){
        val length = mDataList.size
        mDataList.clear()
        notifyItemRangeRemoved(0, length)
    }

    private var mNonSubscribedPartnerList = HashSet<String>()
    private var isPackAvailed = false
    fun updateSbscriberList() {
        isPackAvailed = sharedPrefs.getSubscribedPack() != null
        try {
            sharedPrefs.getSubscribedPack()?.nonSubscribedPartnerList?.let { partnerList ->
                mNonSubscribedPartnerList = HashSet<String>()
                e("RailAdapter","partnerList:$partnerList")
                for (partner in partnerList){
                    mNonSubscribedPartnerList.add((partner.partnerName ?: "").toLowerCase())
                }
            }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }


    private inner class ViewHolderLiveBanner(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: LayoutLiveBannerBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            position: Int,
            title: String,
            contentItem: List<ContentItem>,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.let { it ->
                contentItem.getOrNull(0)?.let { it1 ->
                    it.contentItem = it1
                    it.title = title
                    val imageUrl = if(it1.appImageBM.isNullOrEmpty())  it1.image else it1.appImageBM
                    val width = 906 // Optimum width for Live banner
                    val height = 512 // Optimum height for Live banner
                    val url = getCloudinaryUrl(
                        cloudinaryUrl,
                        width,
                        height,
                        imageUrl
                    )
                    binding.imgUrl = url
//                    imageLoad(
//                        it.liveBanerIv, url
//                    )
                    it.liveIndicatorVisible = it1.liveContent

                    it.root.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            it1,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?: "",
                                railTitle = title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = it1.getSubTitle(),
                                gamePartner = it1.provider,
                                gamePosition = "${1}",
                                gameRating = it1.gameRating,
                                releaseYear = "",
                                source = pageName ?: ""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }

                    updateProviderImage(
                        it.ivProviderLogo,
                        it1.provider,
                        providerLogos,
                        R.drawable.ic_gamezop_transparent
                    )
                }

            }
        }
    }

    private inner class ViewHolderGameWeekBanner(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val binding: HomeGamingWeekBannerBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            position: Int,
            title: String,
            contentItem: List<ContentItem>,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.let { it ->
                contentItem.getOrNull(0)?.let { it1 ->
                    binding.contentItem = it1
                    binding.title = title
                    val paint = binding.tvGameWeek.paint
                    paintPremiumGradient(binding.tvGameWeek, paint.measureText(title))
                    var gamesPageName = ""
                    if (pageName.equals(PROVIDER_GAMEZOP, true)) {
                        gamesPageName = SOURCE_GAMES
                    } else {
                        gamesPageName = pageName ?: ""
                    }
                    binding.btnPlayWeekGame.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            it1,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?: "",
                                railTitle = title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = it1.getSubTitle(),
                                gamePartner = it1.provider,
                                gamePosition = "${1}",
                                gameRating = it1.gameRating,
                                releaseYear = "",
                                source = gamesPageName ?: ""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
            }
        }
    }

    inner class ViewHolderGenreNew(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: GenreHomeRecyclerViewBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            model: RailsModel
        ) {
            binding?.let { it ->
                var wheelSpeed = 8 //Less value = More rotation speed
                if(it.homeRecyclerView.adapter != null && !orientationChanged){
                    binding.homeRecyclerView.swapAdapter(model.adapter,false)
                } else {
                    val params: ConstraintLayout.LayoutParams =
                        it.guide.layoutParams as ConstraintLayout.LayoutParams
                    it.railsModel = model
                    orientationChanged = false
                    when(getTabletType(it.root.context)){
                        TabletType.TABLET, TabletType.TABLET_7_INCH -> {
                            binding.homeRecyclerView.setPadding(
                                it.root.resources.getDimension(R.dimen.genre_items_wheel_padding)
                                    .toInt(),
                                0,
                                0,
                                0
                            )
                            params.guidePercent = .20f
                        }
                        TabletType.TABLET_LANDSCAPE -> {
                            binding.homeRecyclerView.setPadding(
                                it.root.resources.getDimension(R.dimen.genre_items_wheel_padding)
                                    .toInt(),
                                0,
                                0,
                                0
                            )
                            params.guidePercent = .14f
                        }
                        else -> {
                            wheelSpeed = 10
                            params.guidePercent = .30f
                        }
                    }
                }

                it.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        // Get the current scroll position of the RecyclerView
                        val scrollX = recyclerView.computeHorizontalScrollOffset()
                        // Update the rotation of the SVG based on the scroll position
                        it.ivGenreWheel.rotation = scrollX.toFloat()/wheelSpeed
                    }
                })
            }
        }
    }

    inner class ViewHolderGameNew(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: HomeNewGamesBinding? = DataBindingUtil.bind(itemView)
        fun bind(
            position: Int,
            title: String,
            contentList: List<ContentItem>,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.let {
                contentList.getOrNull(0)?.let { contentItem1 ->
                    binding.contentItem1 = contentItem1
                    binding.ivNewGame1.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            contentItem1,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?:"",
                                railTitle = title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = contentItem1.getSubTitle(),
                                gamePartner = contentItem1.provider,
                                gamePosition = "${1}",
                                gameRating = contentItem1.gameRating,
                                releaseYear = "",
                                source = pageName ?:""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
                if(contentList.getOrNull(1) == null){
                    if(isTablet(binding.ivNewGame2)){
                        binding.ivNewGame2.hide()
                        binding.tvNewGame2.hide()
                    }else{
                        binding.ivNewGame2.invisible()
                        binding.tvNewGame2.invisible()
                    }

                } else {
                    binding.ivNewGame2.show()
                    binding.tvNewGame2.show()
                }
                contentList.getOrNull(1)?.let { contentItem2 ->
                    binding.contentItem2 = contentItem2
                    binding.ivNewGame2.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            contentItem2,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?:"",
                                railTitle = title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = contentItem2.getSubTitle(),
                                gamePartner = contentItem2.provider,
                                gamePosition = "${2}",
                                gameRating = contentItem2.gameRating,
                                releaseYear = "",
                                source = pageName ?: ""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
                binding.title = title
            }
        }
    }

    inner class MergedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: MergeGamesRowBinding? = DataBindingUtil.bind(itemView)
        fun mergeBindRows(
            position: Int,
            item: HomeResponse.Items,
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
            binding?.let { it ->
                val contentList = item.contentItem
                contentList.getOrNull(0)?.let {it1->
                    binding.contentItem = it1
                    binding.title = item.title
                    val paint = binding.tvGameWeek.paint
                    paintPremiumGradient(binding.tvGameWeek, paint.measureText(item.title))
                    var gamesPageName = ""
                    if (pageName.equals(PROVIDER_GAMEZOP, true)) {
                        gamesPageName = SOURCE_GAMES
                    } else {
                        gamesPageName = pageName ?: ""
                    }
                    binding.btnPlayWeekGame.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            it1,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = item.title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?: "",
                                railTitle = item.title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = it1.getSubTitle(),
                                gamePartner = it1.provider,
                                gamePosition = "${1}",
                                gameRating = it1.gameRating,
                                releaseYear = "",
                                source = gamesPageName ?: ""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
                contentList.getOrNull(1)?.let { contentItem1 ->
                    binding.contentItem1 = contentItem1
                    binding.ivNewGame1.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            contentItem1,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = item.title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?: "",
                                railTitle = item.title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = contentItem1.getSubTitle(),
                                gamePartner = contentItem1.provider,
                                gamePosition = "${1}",
                                gameRating = contentItem1.gameRating,
                                releaseYear = "",
                                source = pageName ?: ""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
                if (contentList.getOrNull(2) == null) {
                    binding.ivNewGame2.hide()
                    binding.tvNewGame2.hide()
                } else {
                    binding.ivNewGame2.show()
                    binding.tvNewGame2.show()
                }
                contentList.getOrNull(2)?.let { contentItem2 ->
                    binding.contentItem2 = contentItem2
                    binding.ivNewGame2.setOnClickListener {
                        mBannerClick.onSubItemClick(
                            contentItem2,
                            position,
                            (position - ignorePositions.filter { it < position }.size + 1),
                            EventConstants.TYPE_GAMES,
                            null,
                            railTitle = item.title,
                            gamesMixpanelInfoModel = GamesMixpanelInfoModel(
                                pageName = pageName ?: "",
                                railTitle = item.title,
                                railPosition = "$position",
                                railType = "Editorial",
                                railCategory = ItemViewType.RAIL.name,
                                gameGenre = contentItem2.getSubTitle(),
                                gamePartner = contentItem2.provider,
                                gamePosition = "${2}",
                                gameRating = contentItem2.gameRating,
                                releaseYear = "",
                                source = pageName ?: ""
                            ),
                            contentAnalyticsModel = contentAnalyticsModel
                        )
                    }
                }
                binding.title = item.title
            }
        }
    }

}
