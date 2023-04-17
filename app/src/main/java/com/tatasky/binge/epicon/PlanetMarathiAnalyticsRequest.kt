package com.tatasky.binge.epicon

import com.google.gson.annotations.SerializedName

data class PlanetMarathiAnalyticsRequest(
    @SerializedName("eventID") val eventId: String? = null,
    @SerializedName("eventType") val eventType: String = "",
    @SerializedName("entityID") val entityID: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0,
    @SerializedName("signedUrl") val signedUrl: String? = null
    //signedUrl
)
