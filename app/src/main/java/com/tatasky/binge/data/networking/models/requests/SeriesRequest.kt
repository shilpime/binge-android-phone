package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class SeriesRequest(
    @SerializedName("Id") val id: String,
    @SerializedName("max") val max: Int,
    @SerializedName("from") val from: Int,
    @SerializedName("profileId") val profileId: String?,
    @SerializedName("subscriberId") val subscriberId: String,
    @SerializedName("isLastWatch")val isLastWatch : Boolean,
    @SerializedName("isAutoScroll")val isAutoScroll : Boolean
)