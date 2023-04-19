package com.tatasky.binge.interfaces

import android.view.View
import androidx.navigation.fragment.FragmentNavigator
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.database.model.GamesMixpanelInfoModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.ui.features.home.model.RailItemsModel

interface CommonDTOClickListener {
    fun onSubItemClick(
        iListItem: ContentItem,
        iItemPosition: Int,
        iSectionPosition: Int,
        iSectionType: String,
        transitions:List<Pair<View, String>>?,
        railTitle:String="",
        origin : String? = null,
        gamesMixpanelInfoModel: GamesMixpanelInfoModel? = null,
        railItemsModel: RailItemsModel? = null,
        contentAnalyticsModel: ContentAnalyticsModel
    )
}

data class ContentItemTransitions(
    val contentItem: ContentItem,
    val extras: FragmentNavigator.Extras,
    val sectionSource: String,
    val gamesMixpanelInfoModel: GamesMixpanelInfoModel? = null,
    val railItemsModel: RailItemsModel? = null,
    val contentAnalyticsModel: ContentAnalyticsModel
)