package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class DunningResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("response")
        var dunningMessage: String? = null
    }
}