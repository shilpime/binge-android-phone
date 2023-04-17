package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class GenreListResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data{
        @SerializedName("contentList")
        val list : List<String> = ArrayList()
    }
}