package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class GenericDRMRequest(
    @SerializedName("partnerContentId") val partnerContentId: String,
    @SerializedName("provider") val provider: String,
    @SerializedName("deviceOs") val deviceOs: String = "ANDROID",
    val contentTypeId: String,
    val contentType: String
)