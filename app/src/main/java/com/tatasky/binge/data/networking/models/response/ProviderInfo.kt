package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProviderInfo {

    @SerializedName("SONYLIV", alternate = ["SonyLiv"])
    @Expose
    var SONYLIV: PartnerUniqueInfo? = null

    @SerializedName("ZEE5", alternate = ["Zee5"])
    @Expose
    var ZEE5: PartnerUniqueInfo? = null

    @SerializedName("PRIME", alternate = ["Prime"])
    @Expose
    var PRIME: PartnerUniqueInfo? = null

    @SerializedName("SHEMAROOME", alternate = ["Shemaroome"])
    @Expose
    var SHEMAROOME: PartnerUniqueInfo? = null

    @SerializedName("NETFLIX", alternate = ["Netflix"])
    @Expose
    var NETFLIX: PartnerUniqueInfo? = null

    @SerializedName("HUNGAMA", alternate = ["Hungama"])
    @Expose
    var HUNGAMA: PartnerUniqueInfo? = null

    @SerializedName("HOTSTAR", alternate = ["Hotstar"])
    @Expose
    var HOTSTAR: PartnerUniqueInfo? = null

    @SerializedName("SUNNXT", alternate = ["SunNxt"])
    @Expose
    var SUNNXT: PartnerUniqueInfo? = null

    @SerializedName("EROSNOW", alternate = ["ErosNow"])
    @Expose
    var EROSNOW: PartnerUniqueInfo? = null

    @SerializedName("TATASKY")
    @Expose
    var TATASKY: PartnerUniqueInfo? = null

    @SerializedName("VOOTSELECT", alternate = ["VootSelect"])
    @Expose
    var VOOTSELECT: PartnerUniqueInfo? = null

    @SerializedName("VOOTKIDS", alternate = ["VootKids"])
    @Expose
    var VOOTKIDS: PartnerUniqueInfo? = null

    @SerializedName("CURIOSITYSTREAM", alternate = ["CuriosityStream"])
    @Expose
    var CuriosityStream: PartnerUniqueInfo? = null

    @SerializedName("EPICON", alternate = ["EpicOn"])
    @Expose
    var Epicon: PartnerUniqueInfo? = null

    @SerializedName("LIONSGATE", alternate = ["LionsGate"])
    @Expose
    var Lionsgate: PartnerUniqueInfo? = null
}

class PartnerUniqueInfo {
    @SerializedName("partnerUniqueId")
    var partnerUniqueId: String? = null
    //
    @SerializedName("partnerId")
    var partnerUserId: String? = null
    @SerializedName("deviceId")
    var partnerDeviceId: String? = null
    @SerializedName("enabled")
    val enabled : Boolean = false
    @SerializedName("idSiteValue")
    val idSiteValue : String = "17"
}
