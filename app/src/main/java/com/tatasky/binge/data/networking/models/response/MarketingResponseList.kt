package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MarketingResponseList {
    @SerializedName("autoScrollTime")
    @Expose
    val autoScrollTime: Int = 0
    @SerializedName("title")
    @Expose
    val title: String? = null
    @SerializedName("description")
    @Expose
    val description: String? = null
    @SerializedName("image")
    @Expose
    val image: String? = null
}