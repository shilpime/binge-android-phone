package com.tatasky.binge.interfaces

import android.os.Parcelable
import com.tatasky.binge.data.networking.models.response.ContentItem
import kotlinx.android.parcel.Parcelize

interface EpisodeClickListener:Parcelable {
    fun selectedEpisode(currentEpisode: ContentItem)
}