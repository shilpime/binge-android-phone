package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class AgeRatingsResponse : BaseResponse() {

    @SerializedName("data")
    var data: List<AgeRatings>? = null

    inner class AgeRatings {
        @SerializedName("ageRatingName")
        var ageRatingName: String? = null

        @SerializedName("ageRatingMasterMapping")
        var ageRatingMasterMapping: String? = null
    }
}