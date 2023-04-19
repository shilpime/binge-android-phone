package com.tatasky.binge.interfaces

import android.os.Parcelable
import com.tatasky.binge.analytics.models.ContentAnalyticsModel
import com.tatasky.binge.data.networking.models.response.ContentItem

interface EpisodeClickListener : Parcelable {
    fun selectedEpisode(currentEpisode: ContentItem, contentAnalyticsModel: ContentAnalyticsModel)
}