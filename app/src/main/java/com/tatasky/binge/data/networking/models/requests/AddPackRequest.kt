package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class AddPackRequest(
    @SerializedName("sid") var sid: String? = null,
    @SerializedName("packId") var packId: String? = null,
    @SerializedName("baId") var baId: String? = null,
    @SerializedName("amount") var amount: String? = null,
    @SerializedName("startDate") var startDate: String? = null,
    @SerializedName("endDate") var endDate: String? = null,
//    @SerializedName("paymentMethod") var paymentMethod: String? = null,
//    @SerializedName("paymentGateway") var paymentGateway: String? = null,
    @SerializedName("language") var language: String? = null,
    @SerializedName("deviceType") var deviceType: String? = null,
    @SerializedName("subscriptionType") var subscriptionType: String? = null,
    @SerializedName("cartId") var cartId: String? = null,
    @SerializedName("isTickTick") var userIsOnTickTick: Boolean? = null,
    @SerializedName("appflyerId") val appsflyerId: String
)