package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class AppleRedemptionRequest(
    @SerializedName("dsn") val dsn: String,
    @SerializedName("baId") val baId: String,
    @SerializedName("platform") val platform: String)
