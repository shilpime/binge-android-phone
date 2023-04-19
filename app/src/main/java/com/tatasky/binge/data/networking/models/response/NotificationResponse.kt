package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NotificationResponse: BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null
    class Data {

        @SerializedName("recent")
        @Expose
        var recent: MutableList<ContentItem>? = null

        @SerializedName("earlier")
        @Expose
        var earlier: MutableList<ContentItem>? = null
    }
}