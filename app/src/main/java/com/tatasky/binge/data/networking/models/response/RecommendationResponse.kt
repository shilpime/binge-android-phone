package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class RecommendationResponse: BaseResponse() {
    var isPrepand: Boolean = false

    @SerializedName("data")
    @Expose
    var data: HomeResponse.Items? = null
}