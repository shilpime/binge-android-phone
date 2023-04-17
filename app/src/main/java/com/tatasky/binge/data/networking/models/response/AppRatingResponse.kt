package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class AppRatingResponse: BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    data class Data(
        @SerializedName("allowRating") var allowRating: Boolean? = null
    )
}