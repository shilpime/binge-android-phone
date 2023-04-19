package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Zee5TagResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Tag? = null

    inner class Tag {
        @SerializedName("tag")
        @Expose
        var tag: String? = null
    }
}
