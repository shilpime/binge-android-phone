package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

data class TaHeroBanner(
        @SerializedName("pageType") val pageType : String,
        @SerializedName("count") val count : Int,
        @SerializedName("position") val position : String,
        @SerializedName("placeHolder") val placeHolder : String)