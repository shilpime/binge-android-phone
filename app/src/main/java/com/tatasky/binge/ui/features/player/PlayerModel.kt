package com.tatasky.binge.ui.features.player

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.tatasky.binge.data.networking.models.response.ChaupalUrlResponse
import com.tatasky.binge.data.networking.models.response.Epid
import com.tatasky.binge.utils.PROVIDER_VOOTSELECT
import com.ttn.ttnplayer.player.SubtitleDTO
import java.util.ArrayList

@Keep
class PlayerModel constructor() : Parcelable {

    private var isLiveContent = false
    var enforceL1L3: Boolean = false
    private var cookies : String? = null
    private var taShowType: String? = null
    private var title: String? = null
    private var contentId: String? = null
    private var contentType: String? = null
    private var vodId: String? = null
    private var drmLicenseUrl: String? = null
    private var playbackUrl: String? = null
    private var drmType: String? = null
    private var keyRequestHeaders: List<String>? = null
    private var subtitleUrls: ArrayList<SubtitleDTO>? = null
    private var isEncrypted = false
    private var loading = false
    private var error = false
    private var isFavourite: Boolean = false
    private var contractName: String? = null
    private var entitlements: List<String>? = null
    private var isTrailer = false
    private var resumeTime: Long = 0
    private var totalDuration: Long = 0
    private var LA_URL: String? = null
    private var genre: List<String>? = null
    private var actors: List<String>? = null
    private var source: String? = null
    private var hd: Boolean = false
    private var provider: String? = null
    private var providerContentId: String? = null
    var audio: List<String>? = null
    private var image: String? = null
    private var smartUrl: String? = null
    private var parentTitle: String? = null
    private var epids : List<Epid>?= null

    private var partnerContentType : String? = null
    private var partnerSubType : String? = null

    private var kid : String? = null
    private var token : String? = null
    private var drmProxyUrl : String? = null

    fun isRecommendedContent(): Boolean {
        return isRecommendedContent
    }

    fun setRecommendedContent(recommendedContent: Boolean) {
        isRecommendedContent = recommendedContent
    }

    private var isRecommendedContent = false

    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        contentId = parcel.readString()
        contentType = parcel.readString()
        vodId = parcel.readString()
        drmLicenseUrl = parcel.readString()
        playbackUrl = parcel.readString()
        drmType = parcel.readString()
        keyRequestHeaders = parcel.createStringArrayList()
        isEncrypted = parcel.readByte() != 0.toByte()
        loading = parcel.readByte() != 0.toByte()
        error = parcel.readByte() != 0.toByte()
        isFavourite = parcel.readByte() != 0.toByte()
        contractName = parcel.readString()
        entitlements = parcel.createStringArrayList()
        isTrailer = parcel.readByte() != 0.toByte()
        resumeTime = parcel.readLong()
        LA_URL = parcel.readString()
        genre = parcel.createStringArrayList()
        actors = parcel.createStringArrayList()
        source = parcel.readString()
        hd = parcel.readByte() != 0.toByte()
        provider = parcel.readString()
        providerContentId = parcel.readString()
        audio = parcel.createStringArrayList()
        isRecommendedContent = parcel.readByte() != 0.toByte()
        image = parcel.readString()
        taShowType = parcel.readString()
        cookies = parcel.readString()
        totalDuration = parcel.readLong()
        smartUrl = parcel.readString()
        parentTitle = parcel.readString()
        isLiveContent = parcel.readByte() != 0.toByte()
    }

    constructor(
        title: String,
        playbackUrl: String?,
        LA_URL: String?,
        contractName: String,
        entitlements: List<String>?,
        contentType: String,
        isFavourite: Boolean,
        isTrailer: Boolean,
        resumeTime: Long,
        contentId: String,
        vodId: String?,
        keyRequestHeaders: List<String>,
        drmType: String?,
        genre: List<String>,
        source: String,
        actors: List<String>,
        hd: Boolean,
        isRecommendedContent: Boolean,
        provider : String?,
        providerContentId : String?,
        audio : List<String>?,
        image : String?,
        taShowType : String,
        cookies : String?,
        totalDuration: Long,
        smartUrl : String?,
        partnerContentType : String?,
        partnerTitle : String,
        partnerSubType : String?,
        isLiveContent: Boolean = false,
        subtitleUrls: ArrayList<SubtitleDTO>?
    ): this() {
        this.title = title
        this.playbackUrl = playbackUrl
        this.LA_URL = LA_URL
        this.keyRequestHeaders = keyRequestHeaders
        this.isFavourite = isFavourite
        this.contractName = contractName
        this.entitlements = entitlements
        this.resumeTime = resumeTime
        this.isTrailer = isTrailer
        this.contentId = contentId
        this.vodId = vodId
        this.contentType = contentType
        if (this.LA_URL != null) {
            this.isEncrypted = true
        }
        this.drmType = drmType ?: "widevine"
        if (isTrailer) {
            this.LA_URL = null
            this.isEncrypted = false
        }
        this.drmLicenseUrl = LA_URL
        this.genre = genre
        this.source = source
        this.actors = actors
        this.hd = hd
        this.isRecommendedContent = isRecommendedContent
        this.provider = provider
        this.providerContentId = providerContentId
        this.audio = audio
        this.image = image
        this.taShowType = taShowType
        this.cookies = cookies
        this.totalDuration = totalDuration
        this.smartUrl = smartUrl
        if (PROVIDER_VOOTSELECT.equals(provider, true))
            enforceL1L3 = true
        this.partnerContentType = partnerContentType
        this.parentTitle = partnerTitle
        this.partnerSubType = partnerSubType
        this.isLiveContent = isLiveContent
        this.subtitleUrls=subtitleUrls
    }

    fun getSmartUrl() : String{
        return smartUrl ?: ""
    }

    fun getCookies() : String?{
        return cookies
    }
    fun isTrailer(): Boolean {
        return isTrailer
    }

    fun getResumeTime(): Long {
        return resumeTime
    }
    fun getTotalDuration() : Long{
        return totalDuration
    }

    fun getLA_URL(): String? {
        return LA_URL
    }

    fun getContractName(): String? {
        return contractName
    }


    fun getImage() : String?{
        return image
    }

    fun getTitle() : String?{
        return title
    }

    fun getEntitlements(): List<String>? {
        return entitlements
    }

    fun getDrmLicenseUrl(): String? {
        return drmLicenseUrl
    }

    fun getPlaybackUrl(): String? {
        return playbackUrl
    }

    fun getDrmType(): String? {
        return drmType ?: "widevine"
    }

    fun getKeyRequestHeaders(): List<String>? {
        return keyRequestHeaders
    }

    fun isLiveContent(): Boolean {
        return isLiveContent
    }

    fun setLiveContent(value : Boolean){
        isLiveContent = value
    }

    fun isEncrypted(): Boolean {
        return isEncrypted
    }

    fun setDrmLicenseUrl(drmLicenseUrl: String) {
        this.drmLicenseUrl = drmLicenseUrl
    }

    fun getContentId(): String? {
        return contentId
    }

    fun getContentType(): String? {
        return contentType
    }

    fun getVodId(): String? {
        return vodId
    }

    fun setResumeTime(resumeTime: Long) {
        this.resumeTime = resumeTime
    }

    fun getGenre(): List<String>? {
        return genre
    }

    fun getSource(): String? {
        return source
    }

    fun getActors(): List<String>? {
        return actors
    }

    fun isHd(): Boolean {
        return hd
    }

    fun getAudioLanguages(): List<String>? {
        return audio
    }

    fun getProvider(): String? {
        return provider
    }

    fun getProviderContentId(): String? {
        return providerContentId
    }

    fun getParentTitle(): String {
        return parentTitle?:""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(contentId)
        parcel.writeString(contentType)
        parcel.writeString(vodId)
        parcel.writeString(drmLicenseUrl)
        parcel.writeString(playbackUrl)
        parcel.writeString(drmType)
        parcel.writeStringList(keyRequestHeaders)
        parcel.writeByte(if (isEncrypted) 1 else 0)
        parcel.writeByte(if (loading) 1 else 0)
        parcel.writeByte(if (error) 1 else 0)
        parcel.writeByte(if (isFavourite) 1 else 0)
        parcel.writeString(contractName)
        parcel.writeStringList(entitlements)
        parcel.writeByte(if (isTrailer) 1 else 0)
        parcel.writeLong(resumeTime)
        parcel.writeString(LA_URL)
        parcel.writeStringList(genre)
        parcel.writeStringList(actors)
        parcel.writeString(source)
        parcel.writeByte(if (hd) 1 else 0)
        parcel.writeString(provider)
        parcel.writeString(providerContentId)
        parcel.writeStringList(audio)
        parcel.writeByte(if (isRecommendedContent) 1 else 0)
        parcel.writeString(image)
        parcel.writeString(taShowType)
        parcel.writeString(cookies)
        parcel.writeLong(totalDuration)
        parcel.writeString(smartUrl)
        parcel.writeString(parentTitle)
        parcel.writeByte(if (isLiveContent) 1 else 0)
    }

    fun setPlaybackUrl(playbackUrl: String?) {
        this.playbackUrl = playbackUrl
    }

    fun setPlaybackSubtitleUrl(urls: ArrayList<SubtitleDTO>?) {
        this.subtitleUrls = urls
    }

    fun getSubtitleUrls() : ArrayList<SubtitleDTO>? {
        return subtitleUrls
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTAShowType(): String? {
        return taShowType
    }

    fun setLA_URL(s: String) {
        this.LA_URL = s
    }

    companion object CREATOR : Parcelable.Creator<PlayerModel> {
        override fun createFromParcel(parcel: Parcel): PlayerModel {
            return PlayerModel(parcel)
        }

        override fun newArray(size: Int): Array<PlayerModel?> {
            return arrayOfNulls(size)
        }
    }

    fun setProviderContentId(s: String) {
        this.providerContentId = s
    }

    fun setTrailer(b: Boolean) {
        isTrailer = b
    }

    fun getPartnerContentType(): String? {
        return partnerContentType
    }

    fun getPartnerSubType(): String? {
        return partnerSubType
    }

    fun setCookies(cookie: String?) {
        this.cookies = cookie
    }

    fun getKid(): String? {
        return kid
    }

    fun getToken(): String? {
        return token
    }

    fun getDrmProxyUrl(): String? {
        return drmProxyUrl
    }

    fun setDrmProxyUrl(drmProxyUrl : String){
        this.drmProxyUrl = drmProxyUrl
    }
    fun setToken(token : String){
        this.token = token
    }
    fun setKid(kid : String){
        this.kid = kid
    }
    fun setEpids(epids : List<Epid>?){
        this.epids = epids
    }
    fun getEpids() : List<Epid>?{
        return epids
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setContentId(s: String) {
        contentId = s
    }

    fun setProvider(s: String) {
        provider = s
    }

    fun setContentType(s: String) {
        contentType = s
    }
}
