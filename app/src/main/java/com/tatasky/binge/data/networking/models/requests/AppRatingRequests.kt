package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class GetAppRatingRequest(
    @SerializedName("deviceId") var deviceId: String? = null
)

data class SetAppRatingRequest(
    @SerializedName("deviceId") var deviceId: String? = null,
    @SerializedName("appRated") var appRated: Boolean? = null
)