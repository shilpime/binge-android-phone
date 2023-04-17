package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class RechargeResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("rechargeUrl")
        var rechargeUrl: String? = null
    }
}