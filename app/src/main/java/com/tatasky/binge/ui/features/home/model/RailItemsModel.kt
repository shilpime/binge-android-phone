package com.tatasky.binge.ui.features.home.model

import android.os.Parcelable
import com.tatasky.binge.data.networking.models.response.ContentItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RailItemsModel(
    val railItems: List<ContentItem>?,
    val railSectionSource: String,
    val railItemLayoutType: String,
    val railSectionType: String,
    val railTitle: String,
    val railConfigType: String,
): Parcelable
