package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class ChaupalUrlResponse :BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {
        @SerializedName("playUrl")
        val playUrl : String?= null

        @SerializedName("playUrls")
        val playUrls : List<PlayUrl>? = null

        @SerializedName("subtitles")
        val subtitles : List<SubtitleUrl>? = null
    }

    inner class SubtitleUrl{
        @SerializedName("url")
        @Expose
        var url: String? = null

        @SerializedName("language")
        @Expose
        var language: String? = null
    }

    inner class PlayUrl{
        @SerializedName("url")
        @Expose
        var url: String? = null

        @SerializedName("type")
        @Expose
        var type: String? = null

        @SerializedName("drmType")
        @Expose
        var drmType: String? = null

        @SerializedName("licenceUrl")
        @Expose
        var licenceUrl: String? = null
    }
}