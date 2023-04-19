package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class GenericPartnerDRMResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class PlayerDetails{
        @SerializedName("playUrl")
        val playUrl : String?= null
        @SerializedName("licenseUrl")
        val licenseUrl : String? = null
        @SerializedName("subtitles")
        val subtitles : List<SubtitleUrl>? = null
        @SerializedName("token")
        val token : String? = null
    }
    class Data {
        @SerializedName("playerDetail")
        val playerDetail : PlayerDetails? = null
    }

    inner class SubtitleUrl{
        @SerializedName("url")
        @Expose
        var url: String? = null

        @SerializedName("language")
        @Expose
        var language: String? = null
    }
}