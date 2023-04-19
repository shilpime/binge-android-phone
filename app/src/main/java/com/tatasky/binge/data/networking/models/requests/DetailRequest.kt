package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class DetailRequest(
    @SerializedName("detailsType") val detailsType: String,
    @SerializedName("Id") val id: String,
    @SerializedName("language") val language: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("vodId") val vodId: String?,
    @SerializedName("max") val max: Int,
    @SerializedName("from") val from: Int,
    @SerializedName("profileId") val profileId: String?=null,
    @SerializedName("subscriberId") val subscriberId: String?=null
)