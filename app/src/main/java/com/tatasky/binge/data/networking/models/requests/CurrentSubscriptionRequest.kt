package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class CurrentSubscriptionRequest (
    @SerializedName("baId") var baId: String,
    @SerializedName("accountId") var accountId: String,
    @SerializedName("dthStatus") var freemiumUserType: String,
    @SerializedName("isTickTick") val userIsOnTickTick: Boolean? = null
)