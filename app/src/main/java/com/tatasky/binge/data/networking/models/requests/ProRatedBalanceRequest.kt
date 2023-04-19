package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class ProRatedBalanceRequest(
    @SerializedName("accountId") var accountId: String? = null,
    @SerializedName("baId") var baId: String? = null,
    @SerializedName("currentPackId") var currentPackId: String? = null,
    @SerializedName("updatedPackId") var updatedPackId: String? = null,
    @SerializedName("subscriptionType") var subscriptionType: String? = null
)


