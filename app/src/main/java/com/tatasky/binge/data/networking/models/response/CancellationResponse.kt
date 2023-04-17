package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class CancellationResponse : BaseResponse() {
    @SerializedName("data")
    var data: CancellationResponse.Data? = null

    inner class Data(
        val amazonPrimeVideoExpiryDate: String?,
        val bingeSubscriptionExpiryDate: String?,
        val message: String,
        val typeOfSubscription: String?,
        @SerializedName("cancelMessage", alternate = ["revokeMessage"])
        val displayMessage: DisplayMessage? = null
    ) {
        inner class DisplayMessage {
            @SerializedName("title")
            var title: String? = null

            @SerializedName("message")
            var message: String? = null
                get() = (field?:"").plus(primeMessage?:"")

            @SerializedName("primeMessage")
            private var primeMessage : String ?= ""
        }
    }
}