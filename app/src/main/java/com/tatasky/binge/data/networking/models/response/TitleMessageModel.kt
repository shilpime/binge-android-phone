package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class TitleMessageModel {
    @SerializedName("title")
    var title: String? = null

    @SerializedName("message")
    var message: String? = null
}