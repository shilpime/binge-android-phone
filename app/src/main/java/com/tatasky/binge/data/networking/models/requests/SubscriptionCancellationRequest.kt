package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class SubscriptionCancellationRequest(
    @SerializedName("baId") var baid: String,
    @SerializedName("packId") var packId: String?,
    @SerializedName("primeCancellation") var cancelPrime: Boolean,
    @SerializedName("bingeCancellation") var cancelBinge: Boolean
)