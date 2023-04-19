package com.tatasky.binge.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessRecyclerOnScrollListener(
    private val mLinearLayoutManager : RecyclerView.LayoutManager) : RecyclerView.OnScrollListener() {
    private var mLoading =
        false // True if we are still waiting for the last set of data to load


    private var previousItemCount =
        0 // The total number of items in the dataset after the last load


    private var mTotalEntries // The total number of entries in the server
            = 0
    private var current_page = 0 // Always start at Page 1

    /*fun EndlessRecyclerOnScrollListener(linearLayoutManager: LinearLayoutManager?) {
        mLinearLayoutManager = linearLayoutManager
    }

    fun EndlessRecyclerOnScrollListener(linearLayoutManager: GridLayoutManager?) {
        mLinearLayoutManager = linearLayoutManager
    }

    fun EndlessRecyclerOnScrollListener(linearLayoutManager: StaggeredGridLayoutManager?) {
        mLinearLayoutManager = linearLayoutManager
    }*/

    // Concrete classes should implement the Loading of more data entries
    abstract fun onLoadMore(current_page: Int)

    private fun onMyScrollStateChanged(
        recyclerView: RecyclerView,
        newState: Int
    ) {
    }

    fun setTotalEntries(totalEntries: Int) {
        mTotalEntries = totalEntries
    }

    // when you're RecyclerView supports refreshing, also refresh the count
    fun refresh() {
        current_page = 0
        previousItemCount = 0
    }

    override fun onScrollStateChanged(
        recyclerView: RecyclerView,
        newState: Int
    ) {
        super.onScrollStateChanged(recyclerView, newState)
        onMyScrollStateChanged(recyclerView, newState)
    }

    override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int
    ) {
        super.onScrolled(recyclerView, dx, dy)
        if (mLinearLayoutManager is LinearLayoutManager) {
            val visibleItemCount = (mLinearLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = (mLinearLayoutManager as LinearLayoutManager).itemCount
            val firstVisibleItem =
                (mLinearLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            e("EndlessScroll","inside mLoading:$mLoading," +
                    " visibleItemCount : $visibleItemCount," +
                    " firstVisibleItem: $firstVisibleItem" +
                    " findLastVisibleItemPosition() ${mLinearLayoutManager.findLastVisibleItemPosition()}," +
                    " currentPage$current_page," +
                    " totalItemCount:$totalItemCount," +
                    " mTotalEntries:$mTotalEntries")

            if (mLoading) {
                val diffCurrentFromPrevious = totalItemCount - previousItemCount

                // check if current total is greater than previous (diff should be greater than 1, for considering placeholder)
                // and if current total is equal to the total in server
                if (diffCurrentFromPrevious >= 1 ||
                    totalItemCount >= mTotalEntries
                ) {
                    mLoading = false
                    previousItemCount = totalItemCount
                }
            }
            if(!mLoading){
                if (mLinearLayoutManager is GridLayoutManager) {
                    e("EndlessScroll","inside mLinearLayoutManager.findLastVisibleItemPosition() ${mLinearLayoutManager.findLastVisibleItemPosition()}," +
                            " currentPage$current_page," +
                            " totalItemCount:$totalItemCount," +
                            " mTotalEntries:$mTotalEntries")
                    if (mLinearLayoutManager.findLastVisibleItemPosition() >= totalItemCount - 1 && totalItemCount < mTotalEntries) {
                        previousItemCount = totalItemCount
                        e("EndlessScroll","inside GridLayoutManager onLoadMore $current_page")
                        onLoadMore(++current_page)
                        mLoading = true
                    }
                } else if (mLinearLayoutManager is LinearLayoutManager) {
                    if (totalItemCount >= mTotalEntries || totalItemCount == 1) {
                        // do nothing, we've reached the end of the appPack
                    } else {
                        // check if the we've reached the end of the appPack,
                        // and if the total items is less than the total items in the server
                        if (firstVisibleItem + visibleItemCount + 2 >= totalItemCount &&
                            totalItemCount < mTotalEntries
                        ) {
                            e("EndlessScroll","inside LinearLayoutManager onLoadMore $current_page")
                            onLoadMore(++current_page)
                            mLoading = true
                            previousItemCount = totalItemCount
                        }
                    }
                }
            }
        }
    }

}