package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannedString
import android.text.TextUtils
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.*
import com.ttn.ttnplayer.player.SubtitleDTO

class MetaDetails():Parcelable {

    //TextUtils.join(" | ", actor)
    fun getVodTitle(): String? {
        return if (contentType == TYPE_BRAND) brandTitle
        else if (contentType == TYPE_SERIES) seriesTitle
//        return if (contentTypeLocal == TYPE_BRAND) brandTitle// used for Episode PI Handling
//        else if (contentTypeLocal == TYPE_SERIES) seriesTitle
        else title
    }

    fun getParentTitle(): String? {
        return if (contentType == TYPE_BRAND) brandTitle
        else if (contentType == TYPE_SERIES) seriesTitle
//        return if (parentContentType == TYPE_BRAND) brandTitle// used for Episode PI Handling
//        else if (parentContentType == TYPE_SERIES) seriesTitle
        else title
    }

    fun getVodDescription(): String {
        return if (contentType == TYPE_BRAND) brandDescription ?: ""
        else if (contentType == TYPE_SERIES) seriesDescription ?: ""
//        return if (contentTypeLocal == TYPE_BRAND) brandDescription ?: ""// used for Episode PI Handling
//        else if (contentTypeLocal == TYPE_SERIES) seriesDescription ?: ""
        else description ?: ""
    }

    fun getExpire(): String {
        return if (expiryTime != null) getDateForHomeTile(expiryTime) else ""
    }

    fun getAudios(): SpannedString? {
        return audio?.filter { it.isNotBlank() }.takeIf { !it.isNullOrEmpty() }?.let {
            buildSpannedString(builderAction = {
                append(TextUtils.join("  \u2022  ", it))
            })
        }
    }

    fun getGenres(): SpannedString? {
        return genre?.filter { it.isNotBlank() }.takeIf { !it.isNullOrEmpty() }?.let {
            buildSpannedString(builderAction = {
                color(Color.parseColor("#BFFFFFFF"))  { append("Genre  \n") }
                append(TextUtils.join(" | ", it))
            })
        }
    }

    fun getSubtitles(): SpannedString? {
//        return if (genre != null)
//            TextUtils.join(" | ", genre)
//        else
//            ""

        return null
    }

    //producer
    fun getProducers(): SpannedString? {
        return producer?.filter { it.isNotBlank() }.takeIf { !it.isNullOrEmpty() }?.let {
            buildSpannedString(builderAction = {
                color(Color.parseColor("#BFFFFFFF")) { append("Producer  \n") }
                append(TextUtils.join(" | ", it).trim())
            })
        }
    }

    fun getStarring(): SpannedString? {
        return actor?.filter { it.isNotBlank() }.takeIf { !it.isNullOrEmpty() }?.let {
            buildSpannedString(builderAction = {
                color(Color.parseColor("#BFFFFFFF")) { append("Starring  \n") }
                append(TextUtils.join(" | ", it).trim())
            })
        }
    }

    fun getDirectors(): SpannedString? {
        return director?.filter { it.isNotBlank() }.takeIf { !it.isNullOrEmpty() }?.let {
            buildSpannedString(builderAction = {
                color(Color.parseColor("#BFFFFFFF")) { append(if (it.size > 1) "Directors  \n" else "Director  \n") }
                append(TextUtils.join(" | ", it))
            })
        }
    }

    fun getSubTitleStr1(): String {
        val stringBuilder = StringBuilder()
        var separator = "  "
        if (releaseYear != null && releaseYear.isNotEmpty()) {
            stringBuilder.append(releaseYear)
            separator = "  "
        }
        if (genre!!.isNotEmpty()) {
            stringBuilder.append(separator)
            stringBuilder.append(genre[0])
            separator = "  "
        }
        if (duration != 0 && (contentType.equals(TYPE_MOVIES, true) || contentType.equals(
                TYPE_TV_SHOWS, true))) {
            stringBuilder.append(separator)
            if(duration < 60)
                stringBuilder.append("$duration s")
            else
                stringBuilder.append(calculateDuration(duration))
        }
        return stringBuilder.toString()
    }
    fun getSubTitleStr2(): String {
        val stringBuilder = StringBuilder()

        if (contentType.equals(TYPE_BRAND, true) && seasonCount > 0) {
            if (seasonCount == 1)
                stringBuilder.append("$seasonCount Season")
            else
                stringBuilder.append("$seasonCount Seasons")
        }

        return stringBuilder.toString()
    }

    fun getImageItem(): String {
        if (!partnerBoxCoverImage.isNullOrEmpty())
            return partnerBoxCoverImage
        else return partnerPosterImage ?: ""
//        if (!boxCoverImage.isNullOrEmpty())// used for Episode PI Handling
//            return boxCoverImage
//        else return posterImage ?: ""
    }

//    var contentTypeLocal:String? = null // used for Episode PI Handling
//

    @SerializedName("episodeId")
    var episodeId : Int = 0
    @SerializedName("season")
    var season:Int = 0

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("vodId")
    @Expose
    val vodId: String? = null

    @SerializedName(value = "title", alternate = ["vodTitle"])
    @Expose
    var title: String? = null

    @SerializedName("seriesTitle")
    @Expose
    var seriesTitle: String? = null

    @SerializedName("brandTitle")
    @Expose
    var brandTitle: String? = null

    @SerializedName("boxCoverImage")
    @Expose
    val boxCoverImage: String? = null

    @SerializedName("posterImage")
    @Expose
    val posterImage: String? = null

    @SerializedName("genre")
    @Expose
    val genre: List<String>? = null

    @SerializedName("duration")
    @Expose
    var duration: Int = 0

    @SerializedName("releaseYear")
    @Expose
    val releaseYear: String? = null

    @SerializedName("description", alternate = ["vodDescription"])
    @Expose
    var description: String? = null

    @SerializedName("brandDescription")
    @Expose
    var brandDescription: String? = null

    @SerializedName("seriesDescription")
    @Expose
    var seriesDescription: String? = null

    @SerializedName("actor")
    @Expose
    val actor: List<String>? = null

    @SerializedName("director")
    @Expose
    val director: List<String>? = null

    @SerializedName("producer")
    @Expose
    val producer: List<String>? = null

    @SerializedName("writer")
    @Expose
    val writer: List<Any>? = null

    @SerializedName("rating")
    @Expose
    val rating: String? = null

    @SerializedName("masterRating")
    @Expose
    val masterRating: String? = null

    @SerializedName("audio")
    @Expose
    var audio: List<String>? = null

    @SerializedName("expiry")
    @Expose
    val expiry: String? = null

    @SerializedName("expiryTime")
    @Expose
    val expiryTime: Long? = null

    @SerializedName("contentType")
    @Expose
    var contentType: String? = null

    @SerializedName("vodAssetId")
    @Expose
    val vodAssetId: String? = null

    @SerializedName("provider")
    @Expose
    val provider: String? = null

    @SerializedName("providerContentId")
    @Expose
    var providerContentId: String? = null

    @SerializedName("downloadExpiry")
    @Expose
    val downloadExpiry: Int? = null

    @SerializedName("allowedForKids")
    @Expose
    val allowedForKids: Boolean = false

    @SerializedName("downloadable")
    @Expose
    val downloadable: Boolean = false

    @SerializedName("favourite")
    @Expose
    var favourite: Boolean? = false

    @SerializedName("hd")
    @Expose
    val hd: Boolean = false

    @SerializedName("startTime")
    @Expose
    val startTime: Long? = null

    @SerializedName("seriesId")
    @Expose
    val seriesId: String? = null

    @SerializedName("channelName")
    @Expose
    val channelName: String? = null

    @SerializedName("parentContentType")
    @Expose
    var parentContentType: String? = null

    @SerializedName("brandId")
    @Expose
    val brandId: String? = null

    @SerializedName("seasonCount")
    @Expose
    val seasonCount: Int = 0

    @SerializedName("partnerDeepLinkUrl")
    @Expose
    var partnerDeepLinkUrl: String = ""

    @SerializedName("partnerSubscriptionType")
    @Expose
    var partnerSubscriptionType : String? = null
    //firstEpisodeSubscriptionType
    @SerializedName("firstEpisodeSubscriptionType")
    @Expose
    val firstEpisodeSubscriptionType : String? = null

    @SerializedName("partnerWebUrl")
    @Expose
    var partnerWebUrl: String? = null

    //
    @SerializedName("partnerTrailerInfo")
    @Expose
    var partnerTrailerInfo: String? = null

    @SerializedName("vodContentType")
    @Expose
    var vodContentType: String? = null

    @SerializedName("taShowType")
    @Expose
    val taShowType: String? = null

    @SerializedName("partnerId")
    val partnerId: String? = null

    @SerializedName("partnerBoxCoverImage")
    @Expose
    val partnerBoxCoverImage: String? = null

    @SerializedName("partnerPosterImage")
    @Expose
    val partnerPosterImage: String? = null

    @SerializedName("hotstarAppDeeplink")
    @Expose
    var hotstarAppDeeplink : String? = null

    @SerializedName("isPlaybackStarted")
    @Expose
    var isPlaybackStarted : Boolean = true

    @SerializedName("showCase")
    @Expose
    val showCase : Boolean = true

    @SerializedName("purchaseExpiry")
    @Expose
    var purchaseExpiry: String? = null

    /*For Epicon and Docubay*/
    @SerializedName("partnerContentType")
    val partnerContentType: String? = null

    /*Subtitle Handling subtitlePlayUrl*/
    @SerializedName("subtitlePlayUrl")
    @Expose
    val subtitlePlayUrl: java.util.ArrayList<SubtitleDTO>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        title = parcel.readString()
        seriesTitle = parcel.readString()
        brandTitle = parcel.readString()
        duration = parcel.readInt()
        description = parcel.readString()
        brandDescription = parcel.readString()
        seriesDescription = parcel.readString()
        audio = parcel.createStringArrayList()
        contentType = parcel.readString()
        providerContentId = parcel.readString()
        favourite = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        parentContentType = parcel.readString()
        partnerDeepLinkUrl = parcel.readString().toString()
        partnerWebUrl = parcel.readString()
        partnerTrailerInfo = parcel.readString()
        vodContentType = parcel.readString()
        hotstarAppDeeplink = parcel.readString()
        isPlaybackStarted = parcel.readByte() != 0.toByte()
        purchaseExpiry = parcel.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeString(id)
        p0?.writeString(title)
        p0?.writeString(seriesTitle)
        p0?.writeString(brandTitle)
        p0?.writeInt(duration)
        p0?.writeString(description)
        p0?.writeString(brandDescription)
        p0?.writeString(seriesDescription)
        p0?.writeStringList(audio)
        p0?.writeString(contentType)
        p0?.writeString(providerContentId)
        p0?.writeValue(favourite)
        p0?.writeString(parentContentType)
        p0?.writeString(partnerDeepLinkUrl)
        p0?.writeString(partnerWebUrl)
        p0?.writeString(partnerTrailerInfo)
        p0?.writeString(vodContentType)
        p0?.writeString(hotstarAppDeeplink)
        p0?.writeByte(if (isPlaybackStarted) 1 else 0)
        p0?.writeString(purchaseExpiry)
    }

    companion object CREATOR : Parcelable.Creator<MetaDetails> {
        override fun createFromParcel(parcel: Parcel): MetaDetails {
            return MetaDetails(parcel)
        }

        override fun newArray(size: Int): Array<MetaDetails?> {
            return arrayOfNulls(size)
        }
    }
}