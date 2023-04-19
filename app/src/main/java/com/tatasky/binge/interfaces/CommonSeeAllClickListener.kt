package com.tatasky.binge.interfaces

import com.tatasky.binge.analytics.SOURCE_MIX
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.networking.models.response.HomeResponse

/**
 * Created by Srikant Karnani on 2/12/19.
 */
interface CommonSeeAllClickListener {
    fun onSeeAllClick(
        railIdName: Pair<Int, String>,
        sectionType: String,
        railPosition: Int?,
        placeHolder: String,
        configType: String?,
        provider: String? = SOURCE_MIX,
        isMixedRail: Boolean = false,
        isPrepand: Boolean = false,
        item: HomeResponse.Items? = null,
        backgroundImage: String?,
        layoutType: String?,
        refId : String,
        packName : String? = null,
        contentAnalyticsModel: ContentAnalyticsModel
    )
}

data class SeeAllTransition(
    val railIdName: Pair<Int, String>,
    val sectionSource: String,
    val placeHolder: String,
    val configType: String,
    val source: String,
    var isMixedRail: Boolean = false,
    var isPrepand: Boolean = false,
    var item: HomeResponse.Items? = null,
    val backgroundImage: String? = null,
    val layoutType: String? = null,
    val railPosition: Int?,
    val refId : String,
    val packName : String? = null,
    val contentAnalyticsModel: ContentAnalyticsModel
)