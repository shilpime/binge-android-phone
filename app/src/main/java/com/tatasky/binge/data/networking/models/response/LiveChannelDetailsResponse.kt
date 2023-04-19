package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("ParcelCreator")
class LiveChannelDetailsResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    @SerializedName("localizedMessage")
    var localizedMessage: String? = null

    data class Data(
        @SerializedName("meta") var meta: ArrayList<Meta>? = null,
        @SerializedName("detail") var detail: Detail? = null,
        @SerializedName("channelRedirection") var channelRedirection: ChannelRedirection? = null,
        @SerializedName("channelMeta") var channelMeta: ChannelMeta? = null,
        @SerializedName("epgRedirection") var epgRedirection: EpgRedirection? = null,
        @SerializedName("channelScheduleData") var channelScheduleData: ArrayList<ChannelScheduleData>? = null,
    )

    data class Meta(
        @SerializedName("id") var id: String? = null,
        @SerializedName("startTime") var startTime: Long? = null,
        @SerializedName("endTime") var endTime: Long? = null,
        @SerializedName("title") var title: String? = null,
        @SerializedName("producer") var producer: ArrayList<String> = arrayListOf(),
        @SerializedName("director") var director: ArrayList<String> = arrayListOf(),
        @SerializedName("writer") var writer: ArrayList<String> = arrayListOf(),
        @SerializedName("description") var description: String? = null,
        @SerializedName("rating") var rating: String? = null,
        @SerializedName("actor") var actor: ArrayList<String> = arrayListOf(),
        @SerializedName("audio") var audio: ArrayList<String> = arrayListOf(),
        @SerializedName("duration") var duration: Int? = null,
        @SerializedName("genre") var genre: ArrayList<String> = arrayListOf(),
        @SerializedName("boxCoverImage") var boxCoverImage: String? = null,
        @SerializedName("seriesId") var seriesId: String? = null,
        @SerializedName("epgState") var epgState: String? = null,
        @SerializedName("contentType") var contentType: String? = null,
        @SerializedName("groupId") var groupId: String? = null,
        @SerializedName("groupKey") var groupKey: String? = null,
        @SerializedName("groupType") var groupType: String? = null,
        @SerializedName("serviceId") var serviceId: Int? = null,
        @SerializedName("eventId") var eventId: Int? = null,
        @SerializedName("length") var length: Int? = null,
        @SerializedName("expiryTime") var expiryTime: Long? = null,
        @SerializedName("blackOut") var blackOut: Boolean? = null,
        @SerializedName("provider") var provider: String? = null,
        @SerializedName("matchId") var matchId: String? = null,
        @SerializedName("fifaWebUrl") var fifaWebUrl: String? = null,
        @SerializedName("taShowType") var taShowType: String? = null,
        @SerializedName("primaryGenre") var primaryGenre: String? = null,
        @SerializedName("cdvrEnabled") var cdvrEnabled: Boolean? = null,
        @SerializedName("samplingThresholdValue") var samplingThresholdValue: Int? = null,
        @SerializedName("samplingValue") var samplingValue: Int? = null,
        @SerializedName("allowedForKids") var allowedForKids: Boolean? = null,
        @SerializedName("downloadable") var downloadable: Boolean? = null,
        @SerializedName("displayFingerPrint") var displayFingerPrint: Boolean? = null,
        @SerializedName("recording") var recording: Boolean? = null,
        @SerializedName("catchup") var catchup: Boolean? = null,
        @SerializedName("samplingEnabled") var samplingEnabled: Boolean? = null,
    )

    data class ChannelScheduleData(
        @SerializedName("id") var id: String? = null,
        @SerializedName("startTime") var startTime: Long? = null,
        @SerializedName("endTime") var endTime: Long? = null,
        @SerializedName("title") var title: String? = null,
        @SerializedName("producer") var producer: ArrayList<String> = arrayListOf(),
        @SerializedName("director") var director: ArrayList<String> = arrayListOf(),
        @SerializedName("writer") var writer: ArrayList<String> = arrayListOf(),
        @SerializedName("description") var description: String? = null,
        @SerializedName("rating") var rating: String? = null,
        @SerializedName("actor") var actor: ArrayList<String> = arrayListOf(),
        @SerializedName("audio") var audio: ArrayList<String> = arrayListOf(),
        @SerializedName("duration") var duration: Int? = null,
        @SerializedName("genre") var genre: ArrayList<String> = arrayListOf(),
        @SerializedName("boxCoverImage") var boxCoverImage: String? = null,
        @SerializedName("seriesId") var seriesId: String? = null,
        @SerializedName("epgState") var epgState: String? = null,
        @SerializedName("channelLogo") var channelLogo: String? = null,
        @SerializedName("groupId") var groupId: String? = null,
        @SerializedName("groupKey") var groupKey: String? = null,
        @SerializedName("groupType") var groupType: String? = null,
        @SerializedName("serviceId") var serviceId: Int? = null,
        @SerializedName("eventId") var eventId: Int? = null,
        @SerializedName("length") var length: Int? = null,
        @SerializedName("channelName") var channelName: String? = null,
        @SerializedName("expiryTime") var expiryTime: Long? = null,
        @SerializedName("channelId") var channelId: Int? = null,
        @SerializedName("channelAssetId") var channelAssetId: String? = null,
        @SerializedName("blackOut") var blackOut: Boolean? = null,
        @SerializedName("provider") var provider: String? = null,
        @SerializedName("downloadExpiry") var downloadExpiry: Int? = null,
        @SerializedName("matchId") var matchId: String? = null,
        @SerializedName("fifaWebUrl") var fifaWebUrl: String? = null,
        @SerializedName("taShowType") var taShowType: String? = null,
        @SerializedName("displayFingerPrint") var displayFingerPrint: Boolean? = null,
        @SerializedName("preCatchupBuffer") var preCatchupBuffer: Int? = null,
        @SerializedName("primaryGenre") var primaryGenre: String? = null,
        @SerializedName("cdvrEnabled") var cdvrEnabled: Boolean? = null,
        @SerializedName("transparentImageUrl") var transparentImageUrl: String? = null,
        @SerializedName("channelNumber") var channelNumber: String? = null,
        @SerializedName("samplingThresholdValue") var samplingThresholdValue: Int? = null,
        @SerializedName("samplingValue") var samplingValue: Int? = null,
        @SerializedName("showTime") var showTime: Long? = null,
        @SerializedName("hd") var hd: Boolean? = null,
        @SerializedName("favourite") var favourite: Boolean? = null,
        @SerializedName("allowedForKids") var allowedForKids: Boolean? = null,
        @SerializedName("downloadable") var downloadable: Boolean? = null,
        @SerializedName("recording") var recording: Boolean? = null,
        @SerializedName("catchup") var catchup: Boolean? = null,
        @SerializedName("samplingEnabled") var samplingEnabled: Boolean? = null,
    )

    data class EpgRedirection(
        @SerializedName("hotstarEpisodeId") var hotstarEpisodeId: String? = null,
        @SerializedName("hotstarProgramId") var hotstarProgramId: String? = null,
    )

    data class ChannelMeta(
        @SerializedName("id") var id: String? = null,
        @SerializedName("name") var name: String? = null,
        @SerializedName("logo") var logo: String? = null,
        @SerializedName("contentType") var contentType: String? = null,
        @SerializedName("assetId") var assetId: String? = null,
        @SerializedName("channelNumber") var channelNumber: String? = null,
        @SerializedName("genre") var genre: ArrayList<String> = arrayListOf(),
        @SerializedName("ottChannel") var ottChannel: Boolean? = null,
        @SerializedName("restartTvAllowed") var restartTvAllowed: Boolean? = null,
        @SerializedName("cdvrEnabled") var cdvrEnabled: Boolean? = null,
        @SerializedName("primaryGenre") var primaryGenre: String? = null,
        @SerializedName("channelName") var channelName: String? = null,
        @SerializedName("transparentImageUrl") var transparentImageUrl: String? = null,
        @SerializedName("backgroundImageUrl") var backgroundImageUrl: String? = null,
        @SerializedName("thumbnailTrackUrl") var thumbnailTrackUrl: String? = null,
        @SerializedName("digitalFeed") var digitalFeed: Boolean? = null,
        @SerializedName("digitalPartner") var digitalPartner: String? = null,
        @SerializedName("hd") var hd: Boolean? = null,
        @SerializedName("favourite") var favourite: Boolean? = null,
    )

    data class ChannelRedirection(
        @SerializedName("redirectionType") var redirectionType: String? = null,
        @SerializedName("channelName") var channelName: String? = null,
        @SerializedName("channelId") var channelId: Int? = null,
    )

    data class Detail(
        @SerializedName("contractName") var contractName: String? = null,
        @SerializedName("entitlements") var entitlements: ArrayList<String> = arrayListOf(),
        @SerializedName("offerId") var offerId: OfferIds? = null,
        @SerializedName("enforceL3") var enforceL3: String? = null,
        @SerializedName("playUrl") var playUrl: String? = null,
        @SerializedName("trailerUrl") var trailerUrl: String? = null,
        @SerializedName("licenseUrl") var licenseUrl: String? = null,
        @SerializedName("dashWidewinePlayUrl") var dashWidewinePlayUrl: String? = null,
        @SerializedName("dashWidewineLicenseUrl") var dashWidewineLicenseUrl: String? = null,
        @SerializedName("dashWidewineTrailerUrl") var dashWidewineTrailerUrl: String? = null,
        @SerializedName("fairplayUrl") var fairplayUrl: String? = null,
        @SerializedName("fairplayTrailerUrl") var fairplayTrailerUrl: String? = null,
        @SerializedName("authorizedCookies") var authorizedCookies: String? = null,
    )
}