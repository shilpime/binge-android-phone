package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class PackValidationResponse : BaseResponse() {
    @SerializedName("data")
    var data: PackValidationResponse.Data? = null

    inner class Data {
        @SerializedName("accountId")
        var accountId: String? = null
        @SerializedName("term")
        var term: Term? = Term()
        @SerializedName("totalAmount")
        var totalAmount: String? = null
    }


    inner class Term {

        @SerializedName("startDate")
        var startDate: String? = null
        @SerializedName("endDate")
        var endDate: String? = null


    }


}