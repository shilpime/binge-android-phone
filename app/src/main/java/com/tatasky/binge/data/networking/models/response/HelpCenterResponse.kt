package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class HelpCenterResponse : BaseResponse() {
    @SerializedName("data")
    var data : Data ?= null

    class Data {
        @SerializedName("tinyUrl")
        @Expose
        var tinyUrl: String? = null

        @SerializedName("helpCenterToken")
        @Expose
        var helpCenterToken: String? = null

    }
}