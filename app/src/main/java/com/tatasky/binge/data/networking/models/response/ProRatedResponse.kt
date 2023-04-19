package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class ProRatedResponse : BaseResponse() {

    @SerializedName("data")
    var data: ProRatedResponse.Data? = null

    inner class Data {
        @SerializedName("currentBalance")
        var currentBalance: String? = null

        @SerializedName("payableAmount")
        var payableAmount: String? = null

        @SerializedName("amount")
        var amount: String? = null

        @SerializedName("proRateFooterMessage")
        var proRateFooterMessage: String? = null

        @SerializedName("currentBalanceVerbiage")
        var currentBalanceVerbiage: String? = null

        @SerializedName("payableAmountVerbiage")
        var payableAmountVerbiage: String? = null

    }

}