package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class PaymentStatusRequest(
    @SerializedName("baId") val baId: String? = null, // Subscriber ID
    @SerializedName("paymentTransaction") val paymentTransaction: String? = null,
)