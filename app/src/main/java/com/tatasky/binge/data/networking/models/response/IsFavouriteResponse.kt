package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.Exo.SubtitleGeneric
import com.ttn.ttnplayer.player.SubtitleDTO

class IsFavouriteResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    inner class Data {

        @SerializedName("episodeId")
        var episodeId : Int = 0
        @SerializedName("season")
        var season:Int = 0
        @SerializedName("subscriberId")
        var subscriberId: String? = null

        @SerializedName("partnerDeepLinkUrl")
        var partnerDeepLinkUrl: String = ""

        @SerializedName("providerContentId")
        var providerContentId: String="0"

        @SerializedName("contentId")
        var contentId: String? = null

        @SerializedName("contentType")
        var contentType: String? = null

        @SerializedName("favourite", alternate = ["status","isFavourite"])
        var favourite: Boolean = false

        @SerializedName("contentTitle")
        var contentTitle: String = ""
        @SerializedName("secondsWatched", alternate = ["watchedDuration"])
        var secondsWatched: Int = 0
        @SerializedName("durationInSeconds", alternate = ["totalDuration"])
        var durationInSeconds: Int = 0

        @SerializedName("vodId")
        var vodId: String? = null

        @SerializedName("dashWidewinePlayUrl")
        @Expose
        var dashWidewinePlayUrl: String? = null
        @SerializedName("dashWidewineLicenseUrl", alternate = ["licenseUrl"])
        @Expose
        var dashWidewineLicenseUrl: String? = null

        @SerializedName("cookies", alternate = ["authorizedCookies"])
        @Expose
        var cookies: String? = null

        @SerializedName("seriesId",alternate = ["seriedId"])
        @Expose
        val seriesId: String? = null

        @SerializedName("hotstarAppDeeplink")
        var hotstarAppDeeplink : String? = null

        @SerializedName("partnerWebUrl")
        var partnerWebUrl: String? = null

        /*This key is added for Epicon, HoiChoi and Docubay Playback as they are played on HLS*/
        @SerializedName("playUrl")
        @Expose
        var playUrl: String? = null

        @SerializedName("partnerSubscriptionType")
        @Expose
        var partnerSubscriptionType : String? = null

        /*Subtitle Handling subtitlePlayUrl*/
        @SerializedName("subtitlePlayUrl")
        @Expose
        val subtitlePlayUrl: java.util.ArrayList<SubtitleGeneric>? = null

        @SerializedName("offerId", alternate = ["offerIds"])
        @Expose
        val offerIds : OfferIds? = null

    }
}
