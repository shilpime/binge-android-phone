package com.tatasky.binge.ui.features.watchlist

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tatasky.binge.R
import com.tatasky.binge.customviews.RVGridLayoutManager
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.data.networking.models.response.RecommendationResponse
import com.tatasky.binge.databinding.FragmentWatchlistBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.adapter.ItemGridAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragmentDirections
import com.tatasky.binge.utils.EndlessRecyclerOnScrollListener
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.showCustomToast
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class WatchlistFragment : BaseFragment<FragmentWatchlistBinding, FavouriteViewModel>() {

    private var mMenuSelectItem: MenuItem? = null
    private var lastItemPosition: Int = 0
    private var contentItem: List<ContentItem> = ArrayList()
    @Inject
    lateinit var watchAnalytics : WatchlistAnalytics

    private lateinit var endlessScrollListener: EndlessRecyclerOnScrollListener
    @Inject
    lateinit var homeAnalytics: HomeAnalytics

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            if(mMenuSelectItem?.title.toString().trim() != resources.getString(R.string.select)){
                viewModel.getWatchlistAdapter().setSelectionMode(false)
            }else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

    override fun toBeCalledOnce() {
        binding.vm = viewModel
        setSpan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            duration = 250
//        }
//        reenterTransition = backward
//
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            duration = 250
//        }
//        exitTransition = forward
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_select, menu)
        mMenuSelectItem = menu.findItem(R.id.menu_select)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select -> {
                when (item.title.toString().trim()) {
                    resources.getString(R.string.select) -> {
                        viewModel.getWatchlistAdapter().setSelectionMode(true)
                    }
                    resources.getString(R.string.bingelist_tool_tv_cancel) -> {
                        viewModel.getWatchlistAdapter().setSelectionMode(false)
                    }
                    else -> {
                        viewModel.removeSelectedWatchlist { success ->
                            if (success) {
                                viewModel.getSelectedItemSize().value?.peekContent()?.let {
                                    var toastItemString = "$it item"
                                    if (it > 1) {
                                        toastItemString += "s"
                                    }
                                    val view =
                                        DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                                            LayoutInflater.from(context),
                                            R.layout.layout_toast_success_failure,
                                            null,
                                            false
                                        )
                                    view.textLoginSuccessfulToast.text = String.format(
                                        Locale.US,
                                        getString(R.string.toast_msg_remove_binge_list),
                                        toastItemString)
                                    view.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                                    showCustomToast(context, view?.root, Gravity.FILL_HORIZONTAL)
                                }
                                refreshPage()
                            }
                            viewModel.getWatchlistAdapter().setSelectionMode(false)
                        }

                    }
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

    }

    override fun getViewModelClass() = FavouriteViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_watchlist

    override fun setObserver() {
        viewModel.getChangedCount().observe(viewLifecycleOwner, Observer {
            if (::endlessScrollListener.isInitialized) {
                it.getContentIfNotHandled()?.let { changedCount ->
                    endlessScrollListener.setTotalEntries(changedCount)
                }
            }
        })

        viewModel.getFavListResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (binding.swipeRefresh.isRefreshing)
                    binding.swipeRefresh.isRefreshing = false
                binding.progressBarBottom.startProgressAvd(false)
                setFavouriteList(it)
            }
        })

        viewModel.getErrorResponse().observe(viewLifecycleOwner, Observer {
            if (binding.swipeRefresh.isRefreshing)
                binding.swipeRefresh.isRefreshing = false
            binding.progressBarBottom.startProgressAvd(false)
        })
        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer
        {
            it.getContentIfNotHandled()?.let { contentItem ->
                if (contentItem.contentItem.id == "0") {
                    contentItem.contentItem.id = contentItem.contentItem.contentId
                }
                findNavController().navigateSafe(
                    SubFragmentDirections.actionToDetail(
                        contentItem.contentItem
                    ), contentItem.extras
                )
            }
        })

        viewModel.getSelectedItemSize().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { selectionSize ->
                handleMenuButtonText(selectionSize)
                mMenuSelectItem?.let { applyFontToMenuItem(it,R.font.volteplay_semibold) }
            }
        }

    }

    private fun handleMenuButtonText(selectionSize: Int) {
//                -1 status when user get exit from selection mode.
        when (selectionSize) {
            -1 -> {
                mMenuSelectItem?.title =
                    resources.getString(R.string.select)
            }
            0 -> {
                mMenuSelectItem?.title =
                    resources.getString(R.string.bingelist_tool_tv_cancel)
            }
            else -> {
                mMenuSelectItem?.title =
                    resources.getString(R.string.remove_with_number, selectionSize)
            }

        }

    }

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner, // LifecycleOwner
            onBackPressedCallback
        )
        watchAnalytics.trackFavoriteVisit()
        binding.swipeRefresh.setColorSchemeColors(Color.BLUE, Color.MAGENTA, Color.RED)
        binding.swipeRefresh.setOnRefreshListener {
            if (viewModel.isLoadingContent)
                binding.swipeRefresh.isRefreshing = false
            else {
                refreshPage()
            }
        }

        binding.btnDiscoverToAdd.setOnClickListener {
            startActivity(
                Intent(activity, LandingActivity::class.java)
                    .apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
        }

//        homeAnalytics.trackWatchlistInitiate()
    }

    private fun refreshPage() {
        viewModel.watchPageOffset = 0
        viewModel.fetchWatchList(false, true)
    }

    private fun setFavouriteList(response: RecommendationResponse) {
//
        if(viewModel.watchPageOffset == 0) {
            if (::endlessScrollListener.isInitialized) {
                binding.favRecyclerView.removeOnScrollListener(endlessScrollListener)
            }
            endlessScrollListener = object :
                EndlessRecyclerOnScrollListener(binding.favRecyclerView.layoutManager as GridLayoutManager) {
                override fun onLoadMore(current_page: Int) {
                    if(!binding.swipeRefresh.isRefreshing) {
                        viewModel.watchPageOffset++// = pageOffset/PAGELIMIT
                        viewModel.fetchWatchList(false, false)
                        binding.progressBarBottom.startProgressAvd(true)
                    }
                }
            }
            binding.favRecyclerView.addOnScrollListener(endlessScrollListener)
        }

        viewModel.updateList(response)
        binding.favRecyclerView.show()
        binding.llEmpty.hide()
        mMenuSelectItem?.isVisible = true
        if(viewModel.watchPageOffset == 0) {
            val isNewItemAdded = checkNewItem(response.data)
            if (isNewItemAdded)
                binding.favRecyclerView.scrollToPosition(0)
            contentItem = response.data?.filteredContentItems ?: ArrayList()
        }
        if (viewModel.watchPageOffset == 0 && viewModel.getWatchlistAdapter().itemCount == 0) {
            setNoResultView()
        }
    }

    private fun checkNewItem(data: HomeResponse.Items?): Boolean {
        if(contentItem.isNotEmpty()
            && data?.filteredContentItems?.size ?: 0 > 0
            && contentItem[0].contentId != data?.filteredContentItems!![0].contentId)
            return true
        return false
    }

    private fun setNoResultView() {
        binding.swipeRefresh.isRefreshing = false
        binding.favRecyclerView.hide()
        binding.llEmpty.show()
        mMenuSelectItem?.isVisible = false

    }

    private fun setSpan() {
        val gridLayoutManager = RVGridLayoutManager(requireContext())
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == (binding.favRecyclerView.adapter as ItemGridAdapter).getListSize()) 2 else 1
            }
        }
        binding.favRecyclerView.layoutManager = gridLayoutManager
    }

    override fun onResume() {
        super.onResume()
        viewModel.watchPageOffset = 0
//        if(viewModel.tvodResponse == null)
//            viewModel.fetchTVoD(contentItem.isEmpty())
//        else{
        viewModel.fetchWatchList(contentItem.isEmpty(), false)
//        }
    }

    override fun onPause() {
        super.onPause()
        val manager = binding.favRecyclerView.layoutManager as GridLayoutManager
        lastItemPosition = manager.findLastVisibleItemPosition()
        if (manager.findFirstVisibleItemPosition() == 0)
            lastItemPosition = 0
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        mMenuSelectItem?.let { applyFontToMenuItem(it,R.font.volteplay_semibold) }
        viewModel.getSelectedItemSize().value?.peekContent()?.let {
            handleMenuButtonText(it)
        }
        super.onPrepareOptionsMenu(menu)
    }
}
