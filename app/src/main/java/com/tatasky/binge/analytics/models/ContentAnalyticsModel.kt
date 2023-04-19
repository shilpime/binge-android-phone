package com.tatasky.binge.analytics.models

import android.os.Parcelable
import com.tatasky.binge.analytics.HERO
import com.tatasky.binge.analytics.BANNER
import com.tatasky.binge.analytics.EVENT_VALUE_RAIL_HB
import com.tatasky.binge.utils.EventConstants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContentAnalyticsModel(
    private val railSectionSource: String?,
    private val railSectionType: String?,
    private val railTitle: String?,
) : Parcelable {

    val railTitleForAnalytics = when {
        railSectionType?.contains(HERO, true) == true && railSectionType.contains(
            BANNER,
            true
        ) || railTitle?.contains(HERO, true) == true && railTitle.contains(
            BANNER,
            true
        ) || railSectionType.equals(EventConstants.TYPE_HERO, true) -> {
            EVENT_VALUE_RAIL_HB
        }
        else -> railTitle
    }
}
