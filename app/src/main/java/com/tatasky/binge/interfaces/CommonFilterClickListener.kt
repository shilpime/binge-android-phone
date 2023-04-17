package com.tatasky.binge.interfaces

import android.view.View
import androidx.navigation.fragment.FragmentNavigator
import com.tatasky.binge.data.networking.models.response.ContentItem

interface CommonFilterClickListener {
    fun onSubItemClick(
        iListItem: String,
        iItemPosition: Int,
        iSectionPosition: Int,
        iSectionType: String,
        transitions: List<Pair<View, String>>?
    )
}

data class FilterItemTransitions(val contentItem: String,
                                 val type: String,
                                 val extras: FragmentNavigator.Extras,
                                 val bgImage: String = "",
                                 val bgBottomImage: String = "",
                                 val railTitle:String ="",
                                 val categoryPageType: String)