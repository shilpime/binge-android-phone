package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class FreemiumSubscriptionCancellationRequest(
    @SerializedName("accountId") var accountId: String?,
    @SerializedName("baId") var baId: String,
    @SerializedName("primeCancellation") var cancelPrime: Boolean,
    @SerializedName("bingeCancellation") var cancelBinge: Boolean
)