package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class PartnerContentAnalyticsRequest(
    @SerializedName("subscriberID") val subscriberID: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("providerContentID") val providerContentID: String? = null,
    @SerializedName("duration") val duration: Long = 0,
    @SerializedName("partnerUserID") val partnerUserID: String? = null,
    @SerializedName("providerName") val providerName: String? = null,
    @SerializedName("partnerContentType") val partnerContentType: String? = null,
    @SerializedName("contentType") val contentType: String? = null
)
