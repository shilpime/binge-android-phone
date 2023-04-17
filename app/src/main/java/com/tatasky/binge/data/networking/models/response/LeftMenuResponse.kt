package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LeftMenuResponse : BaseResponse(){

    @SerializedName("data")
    @Expose
    var data: HomeMenuData? = null

    class HomeMenuData {

        @SerializedName("items")
        @Expose
        val items: List<LeftMenuItem>? = null

    }
}