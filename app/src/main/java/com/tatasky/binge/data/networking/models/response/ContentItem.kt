package com.tatasky.binge.data.networking.models.response

import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableString
import android.text.TextUtils
import androidx.databinding.ObservableField
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.moengage.inbox.core.model.InboxMessage
import com.tatasky.binge.analytics.RAIL
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.SuggestionSuggestors
import com.tatasky.binge.utils.*
import com.ttn.ttnplayer.player.SubtitleDTO
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ContentItem() : Parcelable {

    var appleRedemptionStatus: String? = null
    var railSectionType: String = RAIL.uppercase()
    var railConfigType: String = EventConstants.TYPE_EDITORIAL.uppercase()

    fun getVodOrParentTitle(): String? {
        return when (contentType) {
            TYPE_BRAND -> title
            TYPE_SERIES -> seriesTitle
            else -> title
        }
    }

    fun containsCrown(): Boolean {
        /*if (!isGuestUser
            && !isPartnerSubscribed
            && (PROVIDER_PRIME.equals(provider, true)
                    || PREMIUM.equals(partnerSubscriptionType,
                true))
        )
            return true*/
        return isShowCrownOnContent(
            isPartnerSubscribed,
            isGuestUser,
            provider,
            partnerSubscriptionType,
            appleRedemptionStatus
        )
    }

    fun getRental() :String? {
        if(rentalExpiry == null || !RENTAL.equals(contractName, ignoreCase = true)) return null
        return "Expires in "+getExpiryTime(rentalExpiry!!.toLong(), TimeLevel.DAY, TimeLevel.MINUTE)
    }

    fun getGameSubTitle(): String{
        return "1.92M Playes"
    }

    fun getSubTitle(): String {

        if (!genres.isNullOrEmpty()) {
            return TextUtils.join(", ", genres)
        }
        else if(!subsTitle.isNullOrEmpty()){
            return TextUtils.join(", ", subsTitle)
        }
        return ""
    }

    fun getSubtitleSearchSuggestions(): String {

        if (!subText.isBlank())
            return subText
        val subtextSearch: ArrayList<String> = arrayListOf()
        if (!genres.isNullOrEmpty())
            subtextSearch.add(genres[0])
        if(!suggestionContentType.isNullOrEmpty())
            subtextSearch.add(suggestionContentType)
        if ((releaseYear != null) &&
            (releaseYear.trim() != "0")
            && (releaseYear.isNotEmpty())
        ) {
            subtextSearch.add(releaseYear)
        }
        if (!language.isNullOrEmpty())
            subtextSearch.add(language[0])
        return TextUtils.join(" | ", subtextSearch)

    }

    fun getAirDuration():String{
        if (!TextUtils.isEmpty(displayDate)) {
            return displayDate
        } else if (!TextUtils.isEmpty(duration)) {
            return duration
        } else if (!TextUtils.isEmpty(airedDate)) {
            val date = java.lang.Long.parseLong(airedDate)
            return getTimeCatchUp(date)
        } else {
            return ""
        }
    }

    fun getEpisodeDuration() : String{
        if(durationInSeconds!=0){
            if(durationInSeconds < 60)
                return "${durationInSeconds}s"
            if(durationInSeconds/60 > 59)
                return calculateDuration(durationInSeconds)//"${durationInSeconds/3600} Hrs"
            return "${durationInSeconds/60}m"
        }
        return ""
    }

    fun getDurationForSports(): String {
        return try {
            val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val reference: Date? = dateFormat.parse("00:00:00")
            val date: Date? = dateFormat.parse(duration)
            val seconds: Long =
                ((date?.time ?: 0L) - (reference?.time ?: 0L)) / 1000L //durationInSeconds
            if (seconds == 0L)
                throw Exception()
            val hours = seconds / 3600
            val minute = (seconds % 3600) / 60

            var returnString = ""
            if (hours > 0) {
                returnString += hours.toString() + "h "
            }
            if (minute > 0)
                returnString += minute.toString() + "m "
            returnString
        } catch (e: Exception) {
            ""
        }

    }

    var railPosition: String = ""

    var isTop10 = false
    var isPartnerSubscribed = false
    var isGuestUser = false

    @SerializedName("masterRating")
    var masterRating : String = ""


    @SerializedName("id")
    var id: String = "0"

    var suggestionPosition = 0

    @SerializedName("contentId")
    var contentId: String = ""
    @SerializedName("title")
    var title: String = ""
    @SerializedName("subText")
    var subText: String = ""


    var name : SpannableString = SpannableString("")

    @SerializedName("contentTitle")
    var contentTitle: String = ""
    @SerializedName("boxCoverImage")
    var boxCoverImage: String = ""
    @SerializedName("thumbnailImage")
    var thumbnailImage: String = ""
    @SerializedName("appImageBM",alternate = ["imageUrlApp"])
    val appImageBM : String? = null
    @SerializedName("image", alternate = ["imageUrl"])
    var image: String = ""

    @SerializedName("newImage")
    var newImage: String = ""

    @SerializedName("buttonText")
    var liveBannerButtonText: String = ""
    @SerializedName("bannerText")
    var bannerText: String = ""
    @SerializedName("contentType")
    var contentType: String = ""
    @SerializedName("suggestionContentType")
    var suggestionContentType: String = ""
    @SerializedName("genre", alternate = arrayOf("genres"))
    var genres: ArrayList<String> = arrayListOf()
    @SerializedName("linkUrl")
    var linkUrl: String = ""
    @SerializedName("selfCareScreen")
    var selfCareScreen: String = ""
    @SerializedName("subTitles")
    var subsTitle: ArrayList<String> = arrayListOf()
    @SerializedName("logo")
    var logo: String = ""
    @SerializedName("airedDate")
    var airedDate: String = ""
    @SerializedName("contractName")
    var contractName: String = ""
    @SerializedName("entitlements")
    var entitlements: ArrayList<String> = arrayListOf()
    @SerializedName("channelName")
    var channelName: String = ""
    @SerializedName("duration")
    var duration: String = ""
    @SerializedName("epgState")
    var epgState: String = ""
    @SerializedName("channelId")
    var channelId: String = ""
    @SerializedName("secondsWatched", alternate = ["watchedDuration"])
    var secondsWatched: Int = 0
    @SerializedName("durationInSeconds", alternate = ["totalDuration"])
    var durationInSeconds: Int = 0
    @SerializedName("vodId")
    var vodId: String = ""
    @SerializedName("isKidsViewable")
    var isKidsViewable: Boolean = false
    @SerializedName("language")
    var language: ArrayList<String> = arrayListOf()
//    @SerializedName("languages")
//    var languages:String = ""
    @SerializedName("lastWatched")
    var lastWatched: Long = 0
    @SerializedName("displayDate")
    var displayDate: String = ""
    @SerializedName("url")
    var url: String = ""
    @SerializedName("downloadProgress")
    var downloadProgress: Int = 0
    @SerializedName("sid")
    var sid: String = ""
    @SerializedName("mbr")
    var mbr: String = ""
    @SerializedName("campaignID")
    var campaignID: String = ""
    @SerializedName("layoutType")
    var layoutType: String = ""
    @SerializedName(value = "categoryType", alternate = ["contentCategory"])
    var categoryType: String = ""

    @SerializedName("provider")
    var provider: String = "Tatasky"
    @SerializedName("description", alternate = ["summary"])
    var description : String = ""
    @SerializedName("posterImage")
    var posterImage: String = ""
    @SerializedName("seriesId")
    var seriesId: String = "0"
    @SerializedName("pageType")
    var pageType: String = ""
    @SerializedName("playerDetail", alternate = ["playerDetailDTO","playerDetails", "detail"])
    var playerDetails : Detail?=null
    @SerializedName("partnerDeepLinkUrl")
    var partnerDeepLinkUrl: String = ""
    @SerializedName("partnerWebUrl")
    var partnerWebUrl: String? = null
    @SerializedName("providerContentId")
    var providerContentId: String="0"
    @SerializedName("hd")
    var hd: Boolean = false
    @SerializedName("episodeId")
    var episodeId : Int = 0
    @SerializedName("rentalPrice")
    var rentalPrice:String?=null
    @SerializedName("rentalExpiry")
    var rentalExpiry:String?=null
    @SerializedName("rentalStatus")
    var rentalStatus:String?=null
    @SerializedName("season")
    var season:String?=null

    @SerializedName("partnerId")
    var partnerId:String?=null

    @SerializedName("packName")
    var packName: String? = null

    @SerializedName("screenName")
    var screenName: String? = null

    @SerializedName("cookies", alternate = ["authorizedCookies"])
    @Expose
    var cookies: String? = null

    @SerializedName("releaseYear")
    val releaseYear: String? = null

    @SerializedName("actor")
    @Expose
    val actor: List<String>? = null

    @SerializedName("liveContent")
    @Expose
    val liveContent = false // Used mainly for Live tag and content identifier

    @SerializedName("suggestor")
    @Expose
    var suggestor : String = ""

    var suggestorForMixpanel: String = ""
        get() {
            return when(suggestor.lowercase()){
                SuggestionSuggestors.GenreSuggestor.name.lowercase() -> "GENRE"
                SuggestionSuggestors.LanguageSuggestor.name.lowercase() -> "LANGUAGE"
                SuggestionSuggestors.KeywordSuggestor.name.lowercase() -> "STRING"
                SuggestionSuggestors.TitleSuggestor.name.lowercase() -> "CONTENT"
                SuggestionSuggestors.ProviderSuggestor.name.lowercase() -> "PARTNER"
                else -> ""
            }
        }

    /*TA Related Shows Handling*/
    @SerializedName("seriesTitle")
    var seriesTitle:String?=null
    @SerializedName("seriescontentType")
    var seriescontentType:String?=null
    @SerializedName("seriesvrId")
    var seriesvrId:String?=null
    @SerializedName("seriesimage")
    var seriesimage:String?=null
    @SerializedName("hotstarAppDeeplink")
    var hotstarAppDeeplink : String? = null
    /*End of keys of ta*/


    /*For Epicon and Docubay*/
    @SerializedName("partnerContentType")
    val partnerContentType: String? = null

    @SerializedName("backgroundImage")
    val backgroundImage : String? = null

    @SerializedName("newBackgroundImage")
    val newBackgroundImage : String? =null

    @SerializedName("appContentList")
    var contentItem: ArrayList<ContentItem> = ArrayList()

    @SerializedName("heroBannerType")
    val heroBannerType : String? = null

    @SerializedName("railId")
    val railId : String? = null


    /*Subtitle Handling subtitlePlayUrl*/
    @SerializedName("subtitlePlayUrl")
    @Expose
    val subtitlePlayUrl: ArrayList<SubtitleDTO>? = null

    /*Ivod Handling*/
    @SerializedName("offerId", alternate = ["offerIds"])
    @Expose
    var offerIds : OfferIds? = null

    var isSubscribed : Boolean = false
    var isRead : Boolean = false
    @Transient
    var notificationInboxMessage : InboxMessage? = null
    var isHeader: Boolean = false
    //Fields Added for Mixpanel Handling
    var railName : String = ""
    var source : String = "Deeplink"
    var origin : String = EventConstants.TYPE_EDITORIAL
    var railCategory : String = ""
    var railType : String = ""
    var contentPosition : String = ""
    var contentConfigType : String = EventConstants.TYPE_EDITORIAL
    var currentDay: String = ""
    var availableDays: String = ""
    var isSelected = false
    var refId : String = ""
    var isCrown : Boolean = false

    var payload:MoEngageGenericModel? = null

    //New mixpanel fields added during game dev
    var gameRailName : String = ""

    //Need to send for mixpanel search suggestion
    var searchKeyword : String = ""

    @SerializedName("audio")
    @Expose
    var audio: List<String>? = null

    @SerializedName("partnerSubscriptionType")
    @Expose
    var partnerSubscriptionType : String? = null

    @SerializedName("trailerUrl")
    var trailerUrl : String? = null

    var isQuerySubmitted : Boolean = false
    @SerializedName("freeEpisodesAvailable")
    var freeEpisodesAvailable : Boolean = false

    var firstFreeEpisodeVerbiage : String = ""

    //Gamezop
    @SerializedName("gamePlayCount")
    var gamePlayCount: String = ""

    @SerializedName("gameRating")
    var gameRating: Float? = null

    @SerializedName("position")
    var position : Int = 0

    @SerializedName("freeGame")
    var freeGame: Boolean = false

    @SerializedName("playUrl")
    var playUrl: String = ""

    @SerializedName("adPlayUrl")
    var adPlayUrl: String = ""

    var mergeSectionType: String = ""
    var mergeSectionTitle: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()?:"0"
        contentId = parcel.readString()?:""
        title = parcel.readString()?:""
        boxCoverImage = parcel.readString()?:""
        thumbnailImage = parcel.readString()?:""
        image = parcel.readString()?:""
        contentType = parcel.readString()?:""
        linkUrl = parcel.readString()?:""
        selfCareScreen = parcel.readString()?:""
        logo = parcel.readString()?:""
        airedDate = parcel.readString()?:""
        contractName = parcel.readString()?:""
        channelName = parcel.readString()?:""
        duration = parcel.readString()?:""
        epgState = parcel.readString()?:""
        channelId = parcel.readString()?:""
        secondsWatched = parcel.readInt()
        durationInSeconds = parcel.readInt()
        vodId = parcel.readString()?:""
        isKidsViewable = parcel.readByte() != 0.toByte()
        lastWatched = parcel.readLong()
        displayDate = parcel.readString()?:""
        url = parcel.readString()?:""
        downloadProgress = parcel.readInt()
        sid = parcel.readString()?:""
        mbr = parcel.readString()?:""
        campaignID = parcel.readString()?:""
        layoutType = parcel.readString()?:""
        categoryType = parcel.readString()?:""
        provider = parcel.readString()?:""
        description = parcel.readString()?:""
        posterImage = parcel.readString()?:""
        seriesId = parcel.readString()?:""
        pageType = parcel.readString()?:""
        episodeId = parcel.readInt()
        season = parcel.readString()
        partnerDeepLinkUrl = parcel.readString()?:""
        providerContentId = parcel.readString()?:"0"
        rentalExpiry = parcel.readString()
        rentalPrice = parcel.readString()
        rentalStatus = parcel.readString()
        partnerId= parcel.readString()
        hd = parcel.readByte() != 0.toByte()
        isSubscribed = parcel.readByte() != 0.toByte()
        isRead = parcel.readByte() != 0.toByte()
        isHeader = parcel.readByte() != 0.toByte()
        railName = parcel.readString() ?: ""
        source = parcel.readString()?: ""
        origin = parcel.readString()?: ""
        railPosition = parcel.readString() ?: ""
        currentDay = parcel.readString()?: ""
        availableDays = parcel.readString()?: ""
        partnerSubscriptionType=parcel.readString()?:""
        trailerUrl = parcel.readString()?: ""
        isQuerySubmitted = parcel.readByte() != 0.toByte()
        playUrl = parcel.readString()?: ""
        adPlayUrl = parcel.readString()?: ""
        firstFreeEpisodeVerbiage = parcel.readString()?: ""
        refId = parcel.readString() ?: ""
        isCrown = parcel.readByte() != 0.toByte()
        position =parcel.readInt()
        suggestionPosition =parcel.readInt()
        suggestor = parcel.readString() ?: ""
        searchKeyword = parcel.readString() ?: ""
        mergeSectionType = parcel.readString() ?: ""
        mergeSectionTitle = parcel.readString() ?: ""
        railConfigType = parcel.readString() ?: EventConstants.TYPE_EDITORIAL.uppercase()
        railSectionType = parcel.readString() ?: RAIL.uppercase()
        appleRedemptionStatus = parcel.readString() ?: ""
    }

    fun getImageItem() : String{
        if(!image.isEmpty()) return  image
        else if(!boxCoverImage.isEmpty()) return  boxCoverImage
        else return ""
    }

    fun getLangGenreIcon(sectionSource: String): String {
        return if (sectionSource.equals(ItemViewType.GENRE.name, true)) newImage else image
    }

    fun getLangGenreBackDrop(sectionSource : String) : String? {
        return if (sectionSource.equals(
                ItemViewType.GENRE.name,
                true
            )
        ) newBackgroundImage else backgroundImage
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(contentId)
        parcel.writeString(title)
        parcel.writeString(boxCoverImage)
        parcel.writeString(thumbnailImage)
        parcel.writeString(image)
        parcel.writeString(contentType)
        parcel.writeString(linkUrl)
        parcel.writeString(selfCareScreen)
        parcel.writeString(logo)
        parcel.writeString(airedDate)
        parcel.writeString(contractName)
        parcel.writeString(channelName)
        parcel.writeString(duration)
        parcel.writeString(epgState)
        parcel.writeString(channelId)
        parcel.writeInt(secondsWatched)
        parcel.writeInt(durationInSeconds)
        parcel.writeString(vodId)
        parcel.writeByte(if (isKidsViewable) 1 else 0)
        parcel.writeLong(lastWatched)
        parcel.writeString(displayDate)
        parcel.writeString(url)
        parcel.writeInt(downloadProgress)
        parcel.writeString(sid)
        parcel.writeString(mbr)
        parcel.writeString(campaignID)
        parcel.writeString(layoutType)
        parcel.writeString(categoryType)
        parcel.writeString(provider)
        parcel.writeString(description)
        parcel.writeString(posterImage)
        parcel.writeString(seriesId)
        parcel.writeString(pageType)
        parcel.writeInt(episodeId)
        parcel.writeString(season)
        parcel.writeString(partnerDeepLinkUrl)
        parcel.writeString(providerContentId)
        parcel.writeString(rentalPrice)
        parcel.writeString(rentalExpiry)
        parcel.writeString(rentalStatus)
        parcel.writeString(partnerId)
        parcel.writeByte(if (hd) 1 else 0)
        parcel.writeByte(if (isSubscribed) 1 else 0)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeByte(if (isHeader) 1 else 0)
        parcel.writeString(railName)
        parcel.writeString(source)
        parcel.writeString(origin)
        parcel.writeString(railPosition)
        parcel.writeString(currentDay)
        parcel.writeString(availableDays)
        parcel.writeString(partnerSubscriptionType)
        parcel.writeString(trailerUrl)
        parcel.writeByte(if (isQuerySubmitted) 1 else 0)
        parcel.writeString(playUrl)
        parcel.writeString(adPlayUrl)
        parcel.writeString(firstFreeEpisodeVerbiage)
        parcel.writeString(refId)
        parcel.writeByte(if (isCrown) 1 else 0)
        parcel.writeInt(position)
        parcel.writeInt(suggestionPosition)
        parcel.writeString(suggestor)
        parcel.writeString(searchKeyword)
        parcel.writeString(mergeSectionType)
        parcel.writeString(mergeSectionTitle)
        parcel.writeString(railConfigType)
        parcel.writeString(railSectionType)
        parcel.writeString(appleRedemptionStatus)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContentItem> {
        override fun createFromParcel(parcel: Parcel): ContentItem {
            return ContentItem(parcel)
        }

        override fun newArray(size: Int): Array<ContentItem?> {
            return arrayOfNulls(size)
        }
    }

}
