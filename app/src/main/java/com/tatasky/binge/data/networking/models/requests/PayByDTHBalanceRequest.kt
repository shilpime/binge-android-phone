package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class PayByDTHBalanceRequest(
    @SerializedName("subscriberId") val sid: String? = null, // Subscriber ID
    @SerializedName("billingAmount") val billingAmount: String? = null,
    @SerializedName("productName") val productName: String? = null, // Pack name
    @SerializedName("productId") val productID: String? = null, //Pack ID
    @SerializedName("baId") val baId: String? = null,
    @SerializedName("paymentTransactionId") val paymentTransactionId: String? = null
)
