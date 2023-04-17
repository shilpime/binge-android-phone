package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


class SeriesListResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Items? = null

    class Items {
        @SerializedName(value = "contentList", alternate = ["contentResults", "items", "list"])
        var contentItem: ArrayList<ContentItem> = ArrayList()

        @SerializedName("total")
        @Expose
        var total: Int = 0

        @SerializedName("offset")
        @Expose
        var offset: Int = 0

        @SerializedName("limit")
        @Expose
        var limit: Int? = null

        @SerializedName("totalSearchCount")
        @Expose
        var totalSearchCount: Int? = null
    }
}