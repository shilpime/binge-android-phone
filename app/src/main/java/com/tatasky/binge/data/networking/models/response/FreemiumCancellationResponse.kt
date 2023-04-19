package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class FreemiumCancellationResponse : BaseResponse() {
    @SerializedName("data")
    var data: FreemiumCancellationResponse.Data? = Data()

    inner class Data {

        @SerializedName("bingeSubscriptionExpiryDate")
        var bingeSubscriptionExpiryDate: String? = null

        @SerializedName("amazonPrimeVideoExpiryDate")
        var amazonPrimeVideoExpiryDate: String? = null

        @SerializedName("deactivateMessage")
        var deactivateMessage: DeactivateMessage? = DeactivateMessage()

    }


    inner class DeactivateMessage {

        @SerializedName("header")
        var header: String? = null

        @SerializedName("footer")
        var footer: String? = null

        @SerializedName("message")
        var message: String? = null

    }

}