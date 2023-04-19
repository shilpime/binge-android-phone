package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CatchupResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {
        @SerializedName("meta")
        @Expose
        var metaDetails: List<MetaDetails>? = null

        @SerializedName("detail")
        @Expose
        var detail: Detail? = null
    }
}