package com.tatasky.binge.analytics.util

import android.content.Context
import com.tatasky.binge.R
import com.tatasky.binge.analytics.EVENT_SEARCH_RESULT
import com.tatasky.binge.analytics.SEARCH_SUGGESTION
import com.tatasky.binge.analytics.SOURCE_DEEPLINK
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.networking.models.response.HomeResponse

/** Generate the content related analytics data
 * Pass this data while binding, as sometimes getting
 * data of another rail
 */
fun HomeResponse.Items.getContentAnalyticsModel() = ContentAnalyticsModel(
    sectionSource,
    sectionType,
    title
)

fun getDeeplinkContentAnalyticsModel() = ContentAnalyticsModel(
    railSectionSource = null,
    railSectionType = null,
    railTitle = SOURCE_DEEPLINK
)

fun emptyContentAnalyticsModel() = ContentAnalyticsModel(
    null,
    null,
    null
)

fun getSearchResultContentAnalyticsModel() = ContentAnalyticsModel(
    null,
    null,
    EVENT_SEARCH_RESULT
)

fun Context?.getSearchTrendingContentAnalyticsModel() = ContentAnalyticsModel(
    null,
    null,
    this?.getString(R.string.trending)
)

fun ContentAnalyticsModel.replaceRailTitleToSearchSuggestion() = copy(
    railTitle = SEARCH_SUGGESTION
)