package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ttn.ttnplayer.player.SubtitleDTO

class Detail {

    @SerializedName("contractName")
    @Expose
    var contractName: String? = null

    @SerializedName("entitlements")
    @Expose
    var entitlements: List<String>? = null

    @SerializedName("dashPlayreadyPlayUrl")
    @Expose
    var dashPlayreadyPlayUrl: String? = null

    @SerializedName("dashPlayreadyLicenseUrl")
    @Expose
    var dashPlayreadyLicenseUrl: String? = null

    @SerializedName("dashWidewinePlayUrl")
    @Expose
    var dashWidewinePlayUrl: String? = null

    @SerializedName("dashWidewineLicenseUrl", alternate = ["licenseUrl"])
    @Expose
    var dashWidewineLicenseUrl: String? = null

    @SerializedName("dashWidewineTrailerUrl")
    @Expose
    var dashWidewineTrailerUrl: String? = null

    @SerializedName("cookies", alternate = ["authorizedCookies"])
    @Expose
    var cookies: String? = null

    @SerializedName("partnerWebUrl")
    @Expose
    var partnerWebUrl: String? = null

    @SerializedName("offerId", alternate = ["offerIds"])
    @Expose
    val offerIds : OfferIds? = null

    /*This key is added for Epicon , HoiChoi and Docubay Playback as they are played on HLS*/
    @SerializedName("playUrl")
    @Expose
    var playUrl: String? = null
    @SerializedName("trailerUrl")
    @Expose
    val trailerUrl : String?= null

    /*Subtitle Handling subtitlePlayUrl*/
    @SerializedName("subtitlePlayUrl")
    @Expose
    val subtitlePlayUrl: ArrayList<SubtitleDTO>? = null
}