package com.tatasky.binge.data.database.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GamesMixpanelInfoModel(
    var pageName : String,
    var railTitle : String,
    var railPosition : String,
    var railType : String,
    var railCategory : String,
    var gameGenre : String,
    var gamePartner : String,
    var gamePosition : String,
    var gameRating : Float?,
    var releaseYear: String,
    var source : String
) : Parcelable