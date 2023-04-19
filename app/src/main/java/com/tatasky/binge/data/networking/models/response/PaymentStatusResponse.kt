package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class PaymentStatusResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("paymentStatus")
        var paymentStatus: String? = null
        @SerializedName("promoCode")
        var promoCode: String? = null

        @SerializedName("paymentMode")
        var paymentMode: String? = null /*Expected values: "OPEL_ONE_TIME"*/

        @SerializedName("paymentMethod")
        var paymentMethod: String? = null /*Expected values: "NB_BILLDESK", "TEST"*/
    }
}