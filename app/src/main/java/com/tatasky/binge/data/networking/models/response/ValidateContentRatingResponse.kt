package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class ValidateContentRatingResponse : BaseResponse() {
    @SerializedName("data")
    var data: ValidateContentRatingResponseData? = null
}

class ValidateContentRatingResponseData {
    @SerializedName("contentAgeRating")
    var contentAgeRating: String? = null

    @SerializedName("subscriberAgeRating")
    var subscriberAgeRating: String? = null

    @SerializedName("pinRequired")
    val pinRequired: Boolean? = null
}