package com.tatasky.binge.ui.features.home

import android.util.SparseArray
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tatasky.binge.ui.features.home.sub.SubFragment

class ViewPagerFragmentStateAdapter(
    val pages: SparseArray<String>,
    val pageNames: SparseArray<String>,
    val searchPageNames: SparseArray<String>,
    fm: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fm, lifecycle) {
    override fun createFragment(position: Int) =
        SubFragment().apply {
            arguments = bundleOf(
                "pageType" to pages[position], "pageName" to pageNames[position],
                "searchPageName" to searchPageNames[position]
            )
        }

    override fun getItemCount(): Int = pages.size()

}