package com.tatasky.binge.ui.features.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.analytics.util.getContentAnalyticsModel
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.ProviderLogo
import com.tatasky.binge.data.networking.models.response.RailPoint
import com.tatasky.binge.databinding.GenreHomeRecyclerViewBinding
import com.tatasky.binge.databinding.HomeRecyclerviewBinding
import com.tatasky.binge.databinding.HomeRecyclerviewGridBinding
import com.tatasky.binge.interfaces.*
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.ItemLayoutType
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.TabletType
import com.tatasky.binge.ui.features.home.adapter.RailAdapter
import com.tatasky.binge.ui.features.home.model.RailsModel
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.ui.features.search.model.TrendingModel
import com.tatasky.binge.utils.*

class SearchLandingAdapter(
    val viewModel: SearchViewModel,
    val list: MutableList<HomeResponse.Items>,
    val sectionPosition: Int,
    val cloudinaryUrl: String?,
    val context: Context,
    val hideTitle: Boolean,
    val loadMoreClickListener: CommonLoadMoreClickListener?,
    private val mSeeAllClickListener: CommonSeeAllClickListener,
    private val mBannerClick: CommonDTOClickListener,
    val providerLogos : ProviderLogo,
    val mRailScrollListener: SearchRailScrollListener
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var continuePaging : Boolean = false
    fun setContinuePaging(isPaging : Boolean){
        continuePaging = isPaging
    }
    private val RAIL = 3000
    private val GENRE_RAIL = 2000
    private val viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
    private val genreViewPool = RecyclerView.RecycledViewPool()
    private var orientationChanged = false
    private val railPoint = RailPoint()
    private fun calculateWidthAndHeight(context : Context) {
        railPoint.genreCharPoint = getCharcterGenrePoint(context)
        railPoint.mCircularWidth = railPoint.genreCharPoint!!.x
        railPoint.landscapeGenrePoint = getNormalThumbnailForGenreDimension(context)
        railPoint.mLandscapeGenreWidth = railPoint.landscapeGenrePoint?.x
        railPoint.mLandscapeGenreHeight = railPoint.landscapeGenrePoint?.y
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        calculateWidthAndHeight(recyclerView.context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            GENRE_RAIL -> {
                view = inflater.inflate(R.layout.genre_home_recycler_view, parent, false)
                viewHolder = ViewHolderGenreNew(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(
                    genreViewPool
                )
            }
            RAIL -> {
                view = inflater.inflate(R.layout.home_recyclerview, parent, false)
                viewHolder = ViewHolderRails(view)
                viewHolder.binding?.homeRecyclerView?.setRecycledViewPool(
                    viewPool
                )
            }
            else -> {
                viewHolder = TrendingViewHolder(
                    HomeRecyclerviewGridBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), context, cloudinaryUrl, viewModel,
                    loadMoreClickListener
                )
            }
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return list.size
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
            contentAnalyticsModel: ContentAnalyticsModel
        ) {
//            binding?.homeRecyclerViewTitle?.textSize = dpToPx()
            if (binding?.homeRecyclerView?.adapter != null) {
                binding.homeRecyclerViewTitle.text = railsModel.title
                binding.homeRecyclerView.swapAdapter(railsModel.adapter, false)
            } else {
                binding?.railsModel = railsModel
            }
            binding?.homeSeeAll?.hide()

            binding?.homeRecyclerView?.clearOnScrollListeners()
            binding?.homeRecyclerView?.addOnScrollListener(CustomScrollListener {
                mRailScrollListener.onSearchRailScrolled(
                    railName,
                    adapterPosition-1,
                    item.configType?.uppercase() ?: EDITORIAL,
                    item.sectionType,
                    provider = provider?:""
                )
            })

            binding?.homeSeeAll?.setOnClickListener {
                mSeeAllClickListener.onSeeAllClick(
                    Pair(
                        railId,
                        railName
                    ),
                    sectionSource,
                    adapterPosition,
                    placeHolder,
                    configType,
                    provider,
                    isMixedRail,
                    isPrepand,
                    item,
                    backgroundImage,
                    layoutType,
                    refId,
                    contentAnalyticsModel = contentAnalyticsModel
                )
            }
        }

    }




    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val railData = list[position]
        when (holder.itemViewType) {
            GENRE_RAIL -> {
                val genreNewViewHolder = holder as ViewHolderGenreNew
                genreNewViewHolder.bind(
                    RailsModel(
                        railData.title,
                        RailAdapter(
                            mBannerClick,
                            railData.contentItem,
                            railData.layoutType,
                            position,
                            cloudinaryUrl,
                            railData.sectionSource,
                            railData.continueWatching,
                            railData.title,
                            providerLogos,
                            railPoint,
                            sharedPrefs = viewModel.sharedPrefs,
                            refId = railData.refId
                        )
                    )
                )
            }

            RAIL -> {
                val railsViewHolder = holder as SearchLandingAdapter.ViewHolderRails
                var contentList = railData.filteredContentItems
                if (railData.sectionSource.equals(ItemViewType.LANGUAGE.name, true) ||
                    railData.sectionSource.equals(ItemLayoutType.POPULAR_CHARACTER.name, true)||
                    railData.sectionSource.equals(ItemLayoutType.CATEGORY.name, true)
                ) {
                    contentList = railData.contentItem
                }
                if(!(railData.sectionSource.equals(ItemViewType.LANGUAGE.name, true) ||
                    railData.sectionSource.equals(ItemViewType.CATEGORY.name, true) ||
                    railData.sectionSource.equals(ItemViewType.PROVIDER.name, true))){
                    railsViewHolder.binding?.clHomeRoot?.hide()
                }
                else if (contentList.isEmpty()) {
                    railsViewHolder.binding?.clHomeRoot?.hide()
//                    ignorePositions.add(position)
                } else {
//                    ignorePositions.remove(position)
                    railsViewHolder.binding?.clHomeRoot?.show()
                    if (railData.sectionSource.equals(ItemViewType.PRIME.name, true) &&
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
                    else if (railData.sectionSource.equals(ItemViewType.LANGUAGE.name, true) ||
                        railData.sectionSource.equals(ItemViewType.CATEGORY.name, true) ||
                        railData.sectionSource.equals(ItemLayoutType.POPULAR_CHARACTER.name, true) ||
                        railData.layoutType.equals(ItemLayoutType.TOP_PORTRAIT.name, true)
                    ) {
                        railsViewHolder.binding?.homeSeeAll?.hide()
                        railsViewHolder.binding?.providerLogo?.hide()
                    } else {
                        railsViewHolder.binding?.providerLogo?.hide()
                        railsViewHolder.binding?.homeSeeAll?.show()
                    }
                }
                val isPrepand = !"APPEND".equals(railData.recommendationPosition, true)
                val isMixedRail = !railData.recommendationPosition.isNullOrEmpty()
                val contentAnalyticsModel = railData.getContentAnalyticsModel()
                railsViewHolder.bind(
                    RailsModel(
                        railData.title,
                        RailAdapter(
                            mBannerClick,
                            contentList,
                            railData.layoutType,
                            position,
                            cloudinaryUrl,
                            railData.sectionSource,
                            railData.continueWatching,
                            railData.title,
                            providerLogos,
                            railPoint,
                            sharedPrefs = viewModel.sharedPrefs,
                            refId = railData.refId,
                            railSectionType = railData.sectionType,
                        )
                    ),
                    mSeeAllClickListener,
                    railData.id,
                    railData.title,
                    railData.sectionSource,
                    railData.lastPosition,
                    railData.placeHolder,
                    railData.configType,
                    railData.trendingProvider,
                    isMixedRail,
                    isPrepand,
                    railData,
                    railData.backgroundImage,
                    railData.layoutType,
                    railData.refId,
                    contentAnalyticsModel
                )
            }
        }
    }


    class TrendingViewHolder(
        val binding: HomeRecyclerviewGridBinding,
        val context: Context,
        val cloudinaryUrl: String?,
        val viewModel: SearchViewModel,
        val loadMoreClickListener: CommonLoadMoreClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: TrendingModel, hideTitle : Boolean,
                 continuePaging : Boolean) {
            if(hideTitle)
                binding.llTitle.hide()
            else
                binding.llTitle.show()
            binding.searchRecyclerView.show()
            val gridLayoutManager = RVGridLayoutManager(context, context.resources.getInteger(R.integer.grid_landscape))
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position ==  (binding.searchRecyclerView.adapter as TrendingAdapter).getListSize()) 2 else 1
                }
            }
            binding.searchRecyclerView.layoutManager = gridLayoutManager

            binding.searchRecyclerView.adapter = TrendingAdapter(
                viewModel.mBannerClick,
                model.list.toMutableList(), 0,
                cloudinaryUrl,
                loadMoreClickListener,
                viewModel.sharedPrefs.getProviderLogo(),
                continuePaging,
                viewModel.sharedPrefs
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        when {
            list[position].sectionSource.equals(
                ItemViewType.GENRE.name,
                true
            ) -> {
                list[position].viewType = GENRE_RAIL
                return GENRE_RAIL
            }
            list[position].sectionType.equals(
                ItemViewType.RAIL.name,
                ignoreCase = true
            ) -> {
                list[position].viewType = RAIL
                return RAIL
            }
            else -> return RAIL
        }
    }


    fun notifyOrientationChange() {
        notifyDataSetChanged()
        orientationChanged = true
    }
}
