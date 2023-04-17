package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class SubscriptionCreationRequest(
    @SerializedName("accountId") var sid: String,
    @SerializedName("baId") var baid: String,
    @SerializedName("packId") var packId: String
)